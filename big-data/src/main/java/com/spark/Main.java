package com.spark;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.spark.handler.UOUpdateHandler;
import com.spark.job.UFOPJob;
import com.spark.models.Event;
import com.spark.models.UOUpdate;
import com.spark.service.SparkService;
import com.spark.util.CustomMessageCodec;
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
        final Injector injector = Guice.createInjector(new GuiceModule());
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

        Vertx vertx = injector.getInstance(Vertx.class);
        vertx.deployVerticle(injector.getInstance(RestVerticle.class));
        vertx.eventBus().<UOUpdate>consumer("parse/uo").handler(injector.getInstance(UOUpdateHandler.class));
        Reflections ref = new Reflections(Event.class.getPackage().getName());
        final CustomMessageCodec messageCodec = injector.getInstance(CustomMessageCodec.class);
        ref.getTypesAnnotatedWith(Event.class).forEach(clazz -> vertx.eventBus().registerDefaultCodec(clazz, messageCodec));
        scheduler.scheduleJob(job, trigger);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            injector.getInstance(Vertx.class).close();
            injector.getInstance(SparkSession.class).close();
            injector.getInstance(JavaSparkContext.class).close();
            injector.getInstance(RedissonClient.class).shutdown();
            try {
                injector.getInstance(Scheduler.class).shutdown();
            } catch (SchedulerException e) {
                throw new IllegalStateException("Failed to stop quartz scheduler", e);
            }
        }));
    }
}
