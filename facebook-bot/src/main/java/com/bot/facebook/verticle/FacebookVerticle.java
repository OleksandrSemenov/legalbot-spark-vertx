package com.bot.facebook.verticle;

import com.google.common.io.Resources;
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

    private final static DefaultJsonMapper jsonMapper = new DefaultJsonMapper();

    @Override
    public void start(Future<Void> fut) throws Exception {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.post("/get/scissors/webhook").handler(this::webhookPost);
        router.get("/get/scissors/webhook").handler(this::webhookGet);
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
        List<MessagingItem> messages = lookupMessageEvent(webhookObject);
        logger.info("Messages: {}", messages);
    }

    private List<MessagingItem> lookupMessageEvent(final WebhookObject webhookObject) {
        return webhookObject.getEntryList().stream()
                .flatMap(e -> e.getMessaging().stream())
                .filter(e -> e.getMessage() != null)
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