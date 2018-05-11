package com.spark.util;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.spark.GuiceModule;
import com.spark.service.SparkService;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.AfterClass;
import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taras Zubrei
 */
public class SparkUtilTest {
    private static final Logger logger = LoggerFactory.getLogger(SparkUtilTest.class);
    private static final Injector injector = Guice.createInjector(new GuiceModule());

    @Test
    public void uo() {
        final SparkService sparkService = injector.getInstance(SparkService.class);
        sparkService.parseUOXml("src/main/resources/uo.xml");
        logger.info("--------------Updated data--------------");
        sparkService.parseUOXml("src/main/resources/uo_update.xml");
    }

    @AfterClass
    public static void shutdown() {
        injector.getInstance(SparkSession.class).close();
        injector.getInstance(JavaSparkContext.class).close();
        injector.getInstance(RedissonClient.class).shutdown();
    }
}
