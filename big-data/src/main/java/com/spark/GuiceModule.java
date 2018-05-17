package com.spark;

/**
 * @author Taras Zubrei
 */

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.spark.handler.UOUpdateHandler;
import com.spark.handler.messenger.LogMessengerHandler;
import com.spark.handler.messenger.MessengerHandler;
import com.spark.service.SparkService;
import com.spark.service.UFOPService;
import com.spark.service.UserService;
import com.spark.service.impl.SparkServiceImpl;
import com.spark.service.impl.UFOPServiceImpl;
import com.spark.service.impl.UserServiceImpl;
import com.spark.util.CustomMessageCodec;
import com.spark.verticles.RestVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        try {
            final SparkConf sparkConf = new SparkConf().setAppName("open-data").setMaster("local");
            final JavaSparkContext sc = new JavaSparkContext(sparkConf);
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            Vertx vertx = Vertx.vertx(new VertxOptions().setMaxWorkerExecuteTime(Long.MAX_VALUE));
            final Config config = new Config();
            config.useSingleServer().setAddress("redis:6379");

            bind(Vertx.class).toInstance(vertx);
            bind(EventBus.class).toInstance(vertx.eventBus());
            bind(SchedulerFactory.class).toInstance(schedulerFactory);
            bind(Scheduler.class).toInstance(scheduler);
            bind(SparkConf.class).toInstance(sparkConf);
            bind(JavaSparkContext.class).toInstance(sc);
            bind(RedissonClient.class).toInstance(Redisson.create(config));
            bind(SparkSession.class).toInstance(new SparkSession(sc.sc()));
            bind(SparkService.class).to(SparkServiceImpl.class);
            bind(UserService.class).to(UserServiceImpl.class);
            bind(UFOPService.class).to(UFOPServiceImpl.class);
            bind(RestVerticle.class);
            bind(UOUpdateHandler.class);
            bind(CustomMessageCodec.class).in(Scopes.NO_SCOPE);
            final Multibinder<MessengerHandler> messengerHandlers = Multibinder.newSetBinder(binder(), MessengerHandler.class);
            messengerHandlers.addBinding().to(LogMessengerHandler.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to start up application", ex);
        }
    }
}