package com.spark.verticles;

import com.core.models.User;
import com.core.service.UserService;
import com.core.util.Resource;
import com.google.inject.Inject;
import com.spark.service.SparkService;
import com.spark.service.UFOPService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Taras Zubrei
 */
public class RestVerticle extends AbstractVerticle {
    private static final int PORT = 7171;
    private final static Logger logger = LoggerFactory.getLogger(RestVerticle.class);
    private final SparkService sparkService;
    private final UserService userService;
    private final UFOPService ufopService;

    @Inject
    public RestVerticle(SparkService sparkService, UserService userService, UFOPService ufopService) {
        this.sparkService = sparkService;
        this.userService = userService;
        this.ufopService = ufopService;
    }

    @Override
    public void start(Future<Void> fut) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/parse/ufop").blockingHandler(ctx -> {
            sparkService.parseLastUFOPData(Optional.ofNullable(ctx.request().getParam("initial")).map(Boolean::valueOf).orElse(false));
            ctx.response().end("OK");
        });
        router.post("/parse/uo").blockingHandler(ctx -> {
            sparkService.parseUOXml(ctx.getBodyAsString(), Optional.ofNullable(ctx.request().getParam("initial")).map(Boolean::valueOf).orElse(false));
            ctx.response().end("OK");
        });
        router.post("/parse/fop").blockingHandler(ctx -> {
            sparkService.parseFOPXml(ctx.getBodyAsString(), Optional.ofNullable(ctx.request().getParam("initial")).map(Boolean::valueOf).orElse(false));
            ctx.response().end("OK");
        });

        router.get("/user").handler(ctx -> ctx.response().end(Json.encodePrettily(userService.findAll())));
        router.get("/user/:id").handler(ctx -> {
            final String id = ctx.request().getParam("id");
            if (StringUtils.isBlank(id))
                ctx.response().setStatusCode(400).end();
            else
                ctx.response().end(Json.encodePrettily(userService.find(id)));
        });
        router.post("/user").handler(ctx -> ctx.response().setStatusCode(201).end(Json.encodePrettily(userService.save(Json.decodeValue(ctx.getBodyAsString(), User.class)))));
        router.post("/user/:id/subscriptions/:resource/:resourceId").handler(ctx -> {
            final String id = ctx.request().getParam("id");
            final String resource = ctx.request().getParam("resource");
            final String resourceId = ctx.request().getParam("resourceId");
            if (StringUtils.isBlank(id)
                    || StringUtils.isBlank(resource)
                    || Arrays.stream(Resource.values()).map(Resource::getName).noneMatch(resource::equals)
                    || StringUtils.isBlank(resourceId))
                ctx.response().setStatusCode(400).end();
            else {
                userService.subscribe(id, Resource.fromName(resource), resourceId);
                ctx.response().setStatusCode(201).end();
            }
        });
        router.get("/user/:id/subscriptions/:resource/:resourceId").handler(ctx -> {
            final String id = ctx.request().getParam("id");
            final String resource = ctx.request().getParam("resource");
            final String resourceId = ctx.request().getParam("resourceId");
            if (StringUtils.isBlank(id)
                    || StringUtils.isBlank(resource)
                    || Arrays.stream(Resource.values()).map(Resource::getName).noneMatch(resource::equals)
                    || StringUtils.isBlank(resourceId))
                ctx.response().setStatusCode(400).end();
            else {
                if (userService.isSubscribed(id, Resource.fromName(resource), resourceId))
                    ctx.response().setStatusCode(200).end();
                else
                    ctx.response().setStatusCode(404).end();
            }
        });
        router.delete("/user/:id/subscriptions/:resource/:resourceId").handler(ctx -> {
            final String id = ctx.request().getParam("id");
            final String resource = ctx.request().getParam("resource");
            final String resourceId = ctx.request().getParam("resourceId");
            if (StringUtils.isBlank(id)
                    || StringUtils.isBlank(resource)
                    || Arrays.stream(Resource.values()).map(Resource::getName).noneMatch(resource::equals)
                    || StringUtils.isBlank(resourceId))
                ctx.response().setStatusCode(400).end();
            else {
                userService.unsubscribe(id, Resource.fromName(resource), resourceId);
                ctx.response().setStatusCode(201).end();
            }
        });
        router.delete("/user/:id").handler(ctx -> {
            final String id = ctx.request().getParam("id");
            if (StringUtils.isBlank(id))
                ctx.response().setStatusCode(400).end();
            else
                ctx.response().end(Json.encodePrettily(userService.delete(id)));
        });

        router.get("/data/uo").handler(ctx -> {
            final Integer page = Optional.ofNullable(ctx.request().getParam("page")).map(Integer::valueOf).orElse(0);
            ctx.response().end(Json.encodePrettily(ufopService.findPagedUO(page)));
        });
        router.get("/data/uo/:id").handler(ctx -> {
            final String id = ctx.request().getParam("id");
            if (StringUtils.isBlank(id) || !id.matches("\\d+"))
                ctx.response().setStatusCode(400).end();
            else {
                ctx.response().end(Json.encodePrettily(ufopService.findUO(id)));
            }
        });
        router.get("/data/fop").handler(ctx -> {
            final Integer page = Optional.ofNullable(ctx.request().getParam("page")).map(Integer::valueOf).orElse(0);
            ctx.response().end(Json.encodePrettily(ufopService.findPagedFOP(page)));
        });

        router.route("/").handler(ctx -> ctx.response().putHeader("content-type", "text/html").end("<h1>Legal bot main page</h1>"));
        router.route("/public/*").handler(StaticHandler.create("public"));

        vertx.createHttpServer(getHttpServerOptions()).requestHandler(router::accept).listen(
                result -> {
                    if (result.succeeded()) {
                        logger.info("Legal bot started at: [{}]", PORT);
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }

    private static HttpServerOptions getHttpServerOptions() {
        HttpServerOptions options = new HttpServerOptions();
        options.setPort(PORT);
        return options;
    }
}