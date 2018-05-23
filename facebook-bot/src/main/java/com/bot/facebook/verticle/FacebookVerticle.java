package com.bot.facebook.verticle;

import com.bot.facebook.command.Commands;
import com.bot.facebook.command.impl.ChangeLanguage;
import com.bot.facebook.command.impl.Subscribe;
import com.bot.facebook.command.impl.Unsubscribe;
import com.bot.facebook.service.FacebookService;
import com.bot.facebook.util.ExceptionUtils;
import com.core.models.User;
import com.core.service.UserService;
import com.core.util.MessengerType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.restfb.DefaultJsonMapper;
import com.restfb.types.webhook.WebhookObject;
import com.restfb.types.webhook.messaging.MessagingItem;
import com.restfb.types.webhook.messaging.PostbackItem;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Taras Zubrei
 */
public class FacebookVerticle extends AbstractVerticle {
    private static final int PORT = 7172;
    private final static Logger logger = LoggerFactory.getLogger(FacebookVerticle.class);

    private final UserService userService;
    private final FacebookService facebookService;
    private final ObjectMapper objectMapper;
    private final static DefaultJsonMapper jsonMapper = new DefaultJsonMapper();

    @Inject
    public FacebookVerticle(UserService userService, FacebookService facebookService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.facebookService = facebookService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void start(Future<Void> fut) throws Exception {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.post("/webhook").handler(this::webhookPost);
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
        final User user = userService.findOrCreate(MessengerType.FACEBOOK, message.getSender().getId());
        if (message.getItem() instanceof PostbackItem) {
            final String payload = message.getPostback().getPayload();
            if (Commands.VIEW_UO.equals(payload)) {
                facebookService.viewUO(user);
            } else {
                final Object command = ExceptionUtils.wrapException(() -> objectMapper.readValue(objectMapper.readTree(payload).get("value").toString(), Class.forName(objectMapper.readTree(payload).get("type").asText())));
                if (command instanceof ChangeLanguage) {
                    final ChangeLanguage changeLanguage = (ChangeLanguage) command;
                    user.getLocales().put(MessengerType.FACEBOOK, changeLanguage.getTo().toLanguageTag());
                    userService.save(user);
                    facebookService.sendBasicMenu(user);
                } else {
                    facebookService.unhandledMessage(user, message);
                }
            }
        } else if (message.getMessage() != null && message.getMessage().getQuickReply() != null) {
            final String payload = message.getMessage().getQuickReply().getPayload();
            final Object command = ExceptionUtils.wrapException(() -> objectMapper.readValue(objectMapper.readTree(payload).get("value").toString(), Class.forName(objectMapper.readTree(payload).get("type").asText())));
            if (command instanceof Subscribe) {
                final Subscribe subscribe = (Subscribe) command;
                userService.subscribe(user.getId(), subscribe.getResource(), subscribe.getId());
                facebookService.sendBasicMenu(user);
            } else if (command instanceof Unsubscribe) {
                final Unsubscribe unsubscribe = (Unsubscribe) command;
                userService.unsubscribe(user.getId(), unsubscribe.getResource(), unsubscribe.getId());
                facebookService.sendBasicMenu(user);
            } else facebookService.unhandledMessage(user, message);
        } else if (message.getMessage() != null) {
            final String payload = message.getMessage().getText();
            if (Commands.MENU.equals(payload)) {
                facebookService.sendBasicMenu(user);
            } else {
                facebookService.handleMessage(user, message);
            }
        }
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