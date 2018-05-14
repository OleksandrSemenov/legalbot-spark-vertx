package com.spark.util;

import com.spark.models.FOP;
import com.spark.models.UO;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.storage.StorageLevel;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taras Zubrei
 */
public class SparkUtil {
    private static RedissonClient redisson;

    public static void parseUO(SparkSession session, RedissonClient redissonClient, String path, boolean initial) {
        redisson = redissonClient;
        JavaRDD<UO> ds = session.read()
                .format("com.databricks.spark.xml")
                .option("rootTag", "DATA")
                .option("rowTag", "RECORD")
                .option("charset", "windows-1251")
                .load(path)
                .select("EDRPOU", "NAME", "SHORT_NAME", "ADDRESS", "BOSS", "KVED", "STAN", "FOUNDERS")
                .toJavaRDD()
                .map(UO::fromXml)
                .persist(StorageLevel.MEMORY_AND_DISK());
        if (initial) {
            ds.foreach(t -> redisson.getMap("uo/" + t.getId()).fastPut(t.hashCode(), t));
        } else {
            final List<Long> updatedIds = ds
                    .filter(SparkUtil::isChanged)
                    .map(UO::getId)
                    .collect();
            ds.filter(t -> updatedIds.contains(t.getId()))
                    .groupBy(UO::getId)
                    .foreach(tuple -> {
                        final RMap<Integer, UO> map = redisson.getMap("uo/" + tuple._1);
                        final ArrayList<UO> previousData = new ArrayList<>(map.values());
                        map.delete();
                        tuple._2.forEach(t -> map.fastPut(t.hashCode(), t));
                        if (!previousData.isEmpty())
                            System.err.println("Id: " + tuple._1 + ". New data: " + map.values() + ". Old data: " + previousData);
                    });
        }
        ds.unpersist();
    }

    public static void parseFOP(SparkSession session, RedissonClient redissonClient, String path, boolean initial) {
        redisson = redissonClient;
        redisson.getList("fop").delete();
        session.read()
                .format("com.databricks.spark.xml")
                .option("rootTag", "DATA")
                .option("rowTag", "RECORD")
                .option("charset", "windows-1251")
                .load(path)
                .select("FIO", "ADDRESS", "KVED", "STAN")
                .toJavaRDD()
                .map(FOP::fromXml)
                .foreach(t -> redisson.getList("fop").add(t));
        if (!initial) System.err.println("New data size: " + redisson.getList("fop").size());
    }

    private static boolean isChanged(UO record) {
        return !redisson.getMap("uo/" + record.getId()).containsKey(record.hashCode());
    }
}
