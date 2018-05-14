package com.spark;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.spark.job.UFOPJob;
import com.spark.service.SparkService;
import com.spark.verticles.RestVerticle;
import io.vertx.rxjava.core.Vertx;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.quartz.*;
import org.redisson.api.RedissonClient;

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

        Vertx rxVertx = Vertx.vertx();
        io.vertx.core.Vertx vertx = (io.vertx.core.Vertx) rxVertx.getDelegate();
        vertx.deployVerticle(new RestVerticle(sparkService));
        scheduler.scheduleJob(job, trigger);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            vertx.close();
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
