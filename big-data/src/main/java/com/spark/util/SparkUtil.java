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
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.StreamSupport;

/**
 * @author Taras Zubrei
 */
public class SparkUtil {
    private static RedissonClient redisson;

    public static void parseUO(SparkSession session, RedissonClient redissonClient, String path, boolean initial) {
        redisson = redissonClient;
        Dataset<Row> ds = session.read()
                .format("com.databricks.spark.xml")
                .option("rootTag", "DATA")
                .option("rowTag", "RECORD")
                .option("charset", "windows-1251")
                .load(path)
                .select("EDRPOU", "NAME", "SHORT_NAME", "ADDRESS", "BOSS", "KVED", "STAN", "FOUNDERS");
        ds.toJavaRDD()
                .map(UO::fromXml)
                .groupBy(UO::getId)
                .filter(tuple -> StreamSupport.stream(tuple._2.spliterator(), true).anyMatch(SparkUtil::isChanged))
                .foreach(tuple -> {
                    final RMap<Integer, UO> map = redisson.getMap("uo/" + tuple._1);
                    final ArrayList<UO> previousData = new ArrayList<>(map.values());
                    map.delete();
                    tuple._2.forEach(t -> map.fastPut(t.hashCode(), t));
                    if (!initial && !previousData.isEmpty())
                        System.err.println("Id: " + tuple._1 + ". New data: " + map.values() + ". Old data: " + previousData);
                });
    }

    public static void parseFOP(SparkSession session, RedissonClient redissonClient, String path, boolean initial) {
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
        return !redisson.getMap("uo/" + record.getId()).containsKey(record.hashCode());
    }

    private static boolean isChanged(FOP record) {
        return !Objects.equals(redisson.getBucket("fop/" + record.getId() + "/hash").get(), record.hashCode());
    }
}
