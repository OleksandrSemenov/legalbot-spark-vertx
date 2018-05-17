package com.spark;

import com.bot.facebook.FacebookModule;
import com.bot.facebook.verticle.FacebookVerticle;
import com.core.models.Event;
import com.core.models.UOUpdate;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.spark.handler.UOUpdateHandler;
import com.spark.job.UFOPJob;
import com.spark.service.SparkService;
import com.spark.util.CustomMessageCodec;
import com.spark.util.EventBusChannels;
import com.spark.verticles.RestVerticle;
import io.vertx.core.Vertx;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.quartz.*;
import org.redisson.api.RedissonClient;
import org.reflections.Reflections;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Taras Zubrei
 */
public class Main {
    private static final int SCHEDULER_HOUR = 23;

    public static void main(String[] args) throws Exception {
        final Injector injector = Guice.createInjector(new GuiceModule(), new FacebookModule());
        configureQuartz(injector);
        configureVertx(injector);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(injector)));
    }

    public static void configureQuartz(Injector injector) throws Exception {
        final SparkService sparkService = injector.getInstance(SparkService.class);
        final Scheduler scheduler = injector.getInstance(Scheduler.class);

        scheduler.getContext().put("SparkService", sparkService);
        scheduler.start();
        JobDetail job = JobBuilder.newJob(UFOPJob.class)
                .withIdentity("parse", "UFOP")
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("parse")
                .startAt(Date.from(LocalDateTime.of(LocalDate.now(), LocalTime.of(SCHEDULER_HOUR, 0)).atZone(ZoneId.systemDefault()).toInstant()))
                .withSchedule(CalendarIntervalScheduleBuilder.calendarIntervalSchedule().withIntervalInDays(1))
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    public static void configureVertx(Injector injector) {
        Vertx vertx = injector.getInstance(Vertx.class);
        vertx.deployVerticle(injector.getInstance(RestVerticle.class));
        vertx.deployVerticle(injector.getInstance(FacebookVerticle.class));
        vertx.eventBus().<UOUpdate>consumer(EventBusChannels.UO).handler(injector.getInstance(UOUpdateHandler.class));
        Reflections ref = new Reflections(Event.class.getPackage().getName());
        final CustomMessageCodec messageCodec = injector.getInstance(CustomMessageCodec.class);
        ref.getTypesAnnotatedWith(Event.class).forEach(clazz -> vertx.eventBus().registerDefaultCodec(clazz, messageCodec));
    }

    public static void shutdown(Injector injector) {
        if (injector.getBindings().containsKey(Key.get(Vertx.class)))
            injector.getInstance(Vertx.class).close();
        if (injector.getBindings().containsKey(Key.get(SparkSession.class)))
            injector.getInstance(SparkSession.class).close();
        if (injector.getBindings().containsKey(Key.get(JavaSparkContext.class)))
            injector.getInstance(JavaSparkContext.class).close();
        if (injector.getBindings().containsKey(Key.get(RedissonClient.class)))
            injector.getInstance(RedissonClient.class).shutdown();
        try {
            if (injector.getBindings().containsKey(Key.get(Scheduler.class)))
                injector.getInstance(Scheduler.class).shutdown();
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to stop quartz scheduler", e);
        }
    }
}
