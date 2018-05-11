package com.spark;

/**
 * @author Taras Zubrei
 */

import com.google.inject.AbstractModule;
import com.spark.service.SparkService;
import com.spark.service.impl.SparkServiceImpl;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

public class GuiceModule extends AbstractModule {
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