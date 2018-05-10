package com.spark;

import com.spark.service.SparkService;
import com.spark.service.impl.SparkServiceImpl;
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
        JavaSparkContext sc = new JavaSparkContext(new SparkConf().setAppName("open-data").setMaster("local"));
        RedissonClient redissonClient = Redisson.create();
        final SparkSession session = new SparkSession(sc.sc());
        final SparkService sparkService = new SparkServiceImpl(session, redissonClient);

        sparkService.parseFOPXml("big-data/src/main/resources/fop.xml");
        sparkService.parseUOXml("big-data/src/main/resources/uo.xml");

        session.close();
        sc.close();
        redissonClient.shutdown();
    }
}
