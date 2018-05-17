package com.spark.util;

import com.spark.models.FOP;
import com.spark.models.UO;
import com.spark.models.UOUpdate;
import io.vertx.core.eventbus.EventBus;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.storage.StorageLevel;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;

import static com.spark.util.RedisKeys.UO_TEMPLATE;

/**
 * @author Taras Zubrei
 */
public class SparkUtil {
    private static RedissonClient redisson;
    private static EventBus eventBus;

    public static void parseUO(SparkSession session, RedissonClient redissonClient, EventBus bus, String path, boolean initial) {
        redisson = redissonClient;
        eventBus = bus;
        JavaRDD<UO> ds = session.read()
                .format("com.databricks.spark.xml")
                .option("rootTag", "DATA")
                .option("rowTag", "RECORD")
                .option("charset", "windows-1251")
                .load(path)
                .select("EDRPOU", "NAME", "SHORT_NAME", "ADDRESS", "BOSS", "KVED", "STAN", "FOUNDERS")
                .toJavaRDD()
                .map(UO::fromXml)
                .persist(StorageLevel.DISK_ONLY());
        if (initial) {
            ds.foreach(t -> {
                redisson.getScoredSortedSet(RedisKeys.UO).add(t.getId(), t.getId());
                redisson.getMap(String.format(UO_TEMPLATE, t.getId())).fastPut(t.hashCode(), t);
            });
        } else {
            final List<Long> updatedIds = ds
                    .filter(SparkUtil::isChanged)
                    .map(UO::getId)
                    .collect();
            if (!updatedIds.isEmpty()) {
                ds.filter(t -> updatedIds.contains(t.getId()))
                        .groupBy(UO::getId)
                        .foreach(tuple -> {
                            final RMap<Integer, UO> map = redisson.getMap(String.format(UO_TEMPLATE, tuple._1));
                            final ArrayList<UO> previousData = new ArrayList<>(map.values());
                            map.delete();
                            redisson.getScoredSortedSet(RedisKeys.UO).add(tuple._1, tuple._1);
                            tuple._2.forEach(t -> map.fastPut(t.hashCode(), t));
                            if (!previousData.isEmpty())
                                eventBus.publish(EventBusChannels.UO, new UOUpdate(tuple._1, previousData, new ArrayList<>(map.values())));
                        });
            }
        }
        ds.unpersist();
    }

    public static void parseFOP(SparkSession session, RedissonClient redissonClient, EventBus bus, String path, boolean initial) {
        redisson = redissonClient;
        eventBus = bus;
        redisson.getList(RedisKeys.FOP).delete();
        session.read()
                .format("com.databricks.spark.xml")
                .option("rootTag", "DATA")
                .option("rowTag", "RECORD")
                .option("charset", "windows-1251")
                .load(path)
                .select("FIO", "ADDRESS", "KVED", "STAN")
                .toJavaRDD()
                .foreach(t -> redisson.getList(RedisKeys.FOP).add(FOP.fromXml(t)));
        if (!initial) eventBus.publish(EventBusChannels.FOP, redisson.getList(RedisKeys.FOP).size());
    }

    private static boolean isChanged(UO record) {
        return !redisson.getMap(String.format(UO_TEMPLATE, record.getId())).containsKey(record.hashCode());
    }
}
