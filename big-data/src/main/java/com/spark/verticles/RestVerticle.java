package com.spark.verticles;

import com.google.inject.Inject;
import com.spark.service.SparkService;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author Taras Zubrei
 */
public class RestVerticle extends AbstractVerticle {
    private static final int PORT = 7171;
    private final static Logger logger = LoggerFactory.getLogger(RestVerticle.class);
    private final SparkService sparkService;

    @Inject
    public RestVerticle(SparkService sparkService) {
        this.sparkService = sparkService;
    }

    @Override
    public void start(Future<Void> fut) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/parse/ufop").handler(ctx -> vertx.executeBlocking(future -> {
            sparkService.parseLastUFOPData(Optional.ofNullable(ctx.request().getParam("initial")).map(Boolean::valueOf).orElse(false));
            future.complete();
        }, res -> ctx.response().setStatusCode(200).end()));
        router.post("/parse/uo").handler(ctx -> vertx.executeBlocking(future -> {
            sparkService.parseUOXml(ctx.getBodyAsString(), Optional.ofNullable(ctx.request().getParam("initial")).map(Boolean::valueOf).orElse(false));
            future.complete();
        }, res -> ctx.response().setStatusCode(200).end()));
        router.post("/parse/fop").handler(ctx -> vertx.executeBlocking(future -> {
            sparkService.parseFOPXml(ctx.getBodyAsString(), Optional.ofNullable(ctx.request().getParam("initial")).map(Boolean::valueOf).orElse(false));
            future.complete();
        }, res -> ctx.response().setStatusCode(200).end()));
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/html").end("<h1>Legal bot main page</h1>");
        });
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