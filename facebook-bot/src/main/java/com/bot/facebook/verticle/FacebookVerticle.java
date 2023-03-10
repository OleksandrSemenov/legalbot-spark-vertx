package com.bot.facebook.verticle;

import com.bot.facebook.command.Command;
import com.bot.facebook.command.Commands;
import com.bot.facebook.fsm.FSMService;
import com.bot.facebook.message.MessageStrategy;
import com.bot.facebook.message.command.CommandParser;
import com.bot.facebook.util.ExceptionUtils;
import com.core.models.User;
import com.core.service.UserService;
import com.core.util.MessengerType;
import com.core.util.RedisKeys;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.restfb.DefaultJsonMapper;
import com.restfb.types.webhook.WebhookObject;
import com.restfb.types.webhook.messaging.MessagingItem;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.commons.io.IOUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Taras Zubrei
 */
public class FacebookVerticle extends AbstractVerticle {
    private static final int PORT = 7172;
    private final static Logger logger = LoggerFactory.getLogger(FacebookVerticle.class);

    private final UserService userService;
    private final FSMService fsmService;
    private final ObjectMapper objectMapper;
    private final RedissonClient redisson;
    private final Set<CommandParser> commandParsers;
    private final Set<MessageStrategy> messageHandlers;
    private final static DefaultJsonMapper jsonMapper = new DefaultJsonMapper();

    @Inject
    public FacebookVerticle(UserService userService, FSMService fsmService, ObjectMapper objectMapper, RedissonClient redisson, Set<CommandParser> commandParsers, Set<MessageStrategy> messageHandlers) {
        this.userService = userService;
        this.fsmService = fsmService;
        this.objectMapper = objectMapper;
        this.redisson = redisson;
        this.commandParsers = commandParsers;
        this.messageHandlers = messageHandlers;
    }

    @Override
    public void start(Future<Void> fut) throws Exception {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.post("/webhook").blockingHandler(this::webhookPost);
        router.get("/webhook").handler(this::webhookGet);
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/html").end("<h1>Facebook bot</h1>");
        });

        router.route("/public/*").handler(StaticHandler.create("public"));

        vertx.createHttpServer(getHttpServerOptions()).requestHandler(router::accept).listen(
                result -> {
                    if (result.succeeded()) {
                        logger.info("Facebook bot started at port:[{}]", PORT);
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }

    /**
     * This method used for subscribing
     *
     * @param routingContext
     */
    private void webhookGet(RoutingContext routingContext) {
        final String bodyJson = routingContext.getBodyAsString();
        logger.info("GET BODY Received [{}]", bodyJson);

        MultiMap params = routingContext.request().params();
        for (String param : params.names()) {
            logger.info("Param key={}; value={}", param, params.get(param));
        }

        routingContext.response().setStatusCode(200).end(params.get("hub.challenge"));
    }

    /**
     * This is for handling events
     *
     * @param routingContext
     */
    private void webhookPost(RoutingContext routingContext) {
        final String bodyJson = routingContext.getBodyAsString();
        logger.info("Recieve FB POST Event : {}", bodyJson);
        routingContext.response().setStatusCode(200).end("{\n" +
                "  \"success\": true\n" +
                "}");

        WebhookObject webhookObject = jsonMapper.toJavaObject(bodyJson, WebhookObject.class);
        MessagingItem message = lookupMessageEvent(webhookObject).get(0);
        if (message.getDelivery() != null) return;
        final User user = userService.findOrCreate(MessengerType.FACEBOOK, message.getSender().getId());
        final RBucket<String> previousMessage = redisson.getBucket(String.format(RedisKeys.FACEBOOK_PREVIOUS_MESSAGE_TEMPLATE, user.getId()));
        final MessageStrategy handler = getMessageHandler(message);
        if (Objects.equals(previousMessage.get(), handler.id(message)))
            return;
        else
            previousMessage.set(handler.id(message));
        handleMessage(user, message, handler.payload(message));
    }

    private MessageStrategy getMessageHandler(MessagingItem message) {
        return messageHandlers.stream()
                .filter(parser -> parser.applies(message))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Wrong message format. Can't find appropriate strategy"));
    }

    private void handleMessage(User user, MessagingItem message, String text) {
        if (text.startsWith("[")) {
            ExceptionUtils.wrapException(() -> Lists.newArrayList(objectMapper.readTree(text)))
                    .stream()
                    .map(node -> parsePayload(node.isTextual() ? node.asText() : node.toString()))
                    .filter(Objects::nonNull)
                    .forEach(command -> fireCommand(user, message, command));
        } else {
            fireCommand(user, message, parsePayload(text));
        }
    }

    private Object parsePayload(String payload) {
        return commandParsers.stream()
                .filter(parser -> parser.applies(payload))
                .findAny()
                .map(parser -> parser.handleMessage(payload))
                .orElse(null);
    }

    private void fireCommand(User user, MessagingItem message, Object command) {
        if (command instanceof Command) {
            fsmService.fire(user, (Command) command);
        } else if (command instanceof Commands) {
            fsmService.fire(user, (Commands) command);
        } else fsmService.fire(user, message);
    }

    private List<MessagingItem> lookupMessageEvent(final WebhookObject webhookObject) {
        return webhookObject.getEntryList().stream()
                .flatMap(e -> e.getMessaging().stream())
                .collect(Collectors.toList());

    }

    private static HttpServerOptions getHttpServerOptions() throws IOException {
        byte[] jksBytes = IOUtils.toByteArray(Resources.getResource("keys.jks").openStream());
        Buffer store = Buffer.buffer(jksBytes);
        JksOptions jksOptions = new JksOptions();
        jksOptions.setPassword("password");
        jksOptions.setValue(store);
        HttpServerOptions options = new HttpServerOptions();
        options.setSsl(true);
        options.setKeyStoreOptions(jksOptions);
        options.setPort(PORT);
        return options;
    }
}