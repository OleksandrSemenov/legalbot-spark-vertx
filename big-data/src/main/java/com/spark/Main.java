package com.spark;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.spark.service.SparkService;
import com.spark.verticles.RestVerticle;
import io.vertx.rxjava.core.Vertx;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.redisson.api.RedissonClient;

/**
 * @author Taras Zubrei
 */
public class Main {
    public static void main(String[] args) {
        final Injector injector = Guice.createInjector(new GuiceModule());
        final SparkService sparkService = injector.getInstance(SparkService.class);

        Vertx rxVertx = Vertx.vertx();
        io.vertx.core.Vertx vertx = (io.vertx.core.Vertx) rxVertx.getDelegate();
        vertx.deployVerticle(new RestVerticle(sparkService));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            vertx.close();
            injector.getInstance(SparkSession.class).close();
            injector.getInstance(JavaSparkContext.class).close();
            injector.getInstance(RedissonClient.class).shutdown();
        }));
    }
}
