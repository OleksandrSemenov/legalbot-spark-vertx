package com.spark.util;

import com.spark.models.FOP;
import com.spark.models.UO;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.redisson.api.RedissonClient;

import java.util.Objects;

/**
 * @author Taras Zubrei
 */
public class SparkUtil {
    private static RedissonClient redisson;

    public static void parseUO(SparkSession session, RedissonClient redissonClient, String path) {
        redisson = redissonClient;
        Dataset<UO> ds = session.read()
                .format("com.databricks.spark.xml")
                .option("rootTag", "DATA")
                .option("rowTag", "RECORD")
                .option("charset", "windows-1251")
                .load(path)
                .select("EDRPOU", "NAME", "SHORT_NAME", "ADDRESS", "BOSS", "KVED", "STAN", "FOUNDERS")
                .map((MapFunction<Row, UO>) UO::fromXml, Encoders.bean(UO.class));
        ds.filter((FilterFunction<UO>) SparkUtil::isChanged)
                .foreach((ForeachFunction<UO>) t -> {
                    redisson.getBucket("uo/" + t.getId() + "/value").set(t);
                    redisson.getBucket("uo/" + t.getId() + "/hash").set(t.hashCode());
                    System.err.println("New data: " + t);
                });
    }

    public static void parseFOP(SparkSession session, RedissonClient redissonClient, String path) {
        redisson = redissonClient;
        Dataset<FOP> ds = session.read()
                .format("com.databricks.spark.xml")
                .option("rootTag", "DATA")
                .option("rowTag", "RECORD")
                .option("charset", "windows-1251")
                .load(path)
                .select("FIO", "ADDRESS", "KVED", "STAN")
                .map((MapFunction<Row, FOP>) FOP::fromXml, Encoders.bean(FOP.class));
        ds.filter((FilterFunction<FOP>) SparkUtil::isChanged)
                .foreach((ForeachFunction<FOP>) t -> {
                    redisson.getBucket("fop/" + t.getId() + "/value").set(t);
                    redisson.getBucket("fop/" + t.getId() + "/hash").set(t.hashCode());
                    System.err.println("New data: " + t);
                });
    }

    private static boolean isChanged(UO record) {
        return !Objects.equals(redisson.getBucket("uo/" + record.getId() + "/hash").get(), record.hashCode());
    }

    private static boolean isChanged(FOP record) {
        return !Objects.equals(redisson.getBucket("fop/" + record.getId() + "/hash").get(), record.hashCode());
    }
}
