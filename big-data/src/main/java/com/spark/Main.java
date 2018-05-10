package com.spark;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.spark.service.SparkService;
import com.spark.service.impl.SparkServiceImpl;
import com.spark.verticles.RestVerticle;
import io.vertx.rxjava.core.Vertx;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.redisson.Redisson;
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

    private static class GuiceModule extends AbstractModule {
        @Override
        protected void configure() {
            final SparkConf sparkConf = new SparkConf().setAppName("open-data").setMaster("local");
            final JavaSparkContext sc = new JavaSparkContext(sparkConf);
            bind(SparkConf.class).toInstance(sparkConf);
            bind(JavaSparkContext.class).toInstance(sc);
            bind(RedissonClient.class).toInstance(Redisson.create());
            bind(SparkSession.class).toInstance(new SparkSession(sc.sc()));
            bind(SparkService.class).to(SparkServiceImpl.class);
        }
    }
}
