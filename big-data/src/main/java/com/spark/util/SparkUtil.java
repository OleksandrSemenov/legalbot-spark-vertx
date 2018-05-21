package com.spark.util;

import com.core.models.FOP;
import com.core.models.UO;
import com.core.models.UOHistory;
import com.core.models.UOUpdate;
import com.spark.repository.FOPRepository;
import com.spark.repository.UORepository;
import io.vertx.core.eventbus.EventBus;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.storage.StorageLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * @author Taras Zubrei
 */
public class SparkUtil {
    private static UORepository uoRepository;
    private static FOPRepository fopRepository;
    private static EventBus eventBus;

    public static void parseUO(SparkSession session, UORepository repository, EventBus bus, String path, boolean initial) {
        uoRepository = repository;
        eventBus = bus;
        JavaRDD<UO> ds = session.read()
                .format("com.databricks.spark.xml")
                .option("rootTag", "DATA")
                .option("rowTag", "RECORD")
                .option("charset", "windows-1251")
                .load(path)
                .select("EDRPOU", "NAME", "SHORT_NAME", "ADDRESS", "BOSS", "KVED", "STAN", "FOUNDERS")
                .toJavaRDD()
                .map(SparkUtil::parseUO)
                .persist(StorageLevel.DISK_ONLY());
        if (initial) {
            ds.foreach(t -> uoRepository.save(t));
        } else {
            final List<String> updatedIds = ds
                    .filter(record -> uoRepository.isChanged(record))
                    .map(UO::getId)
                    .collect();
            if (!updatedIds.isEmpty()) {
                ds.filter(t -> updatedIds.contains(t.getId()))
                        .groupBy(UO::getId)
                        .foreach(tuple -> {
                            List<UO> previousData = Optional.ofNullable(uoRepository.findOne(tuple._1)).map(UOHistory::getData).orElse(new ArrayList<>());
                            final UOHistory history = new UOHistory(tuple._1, tuple._2);
                            uoRepository.save(history);
                            if (!previousData.isEmpty())
                                eventBus.publish(EventBusChannels.UO, new UOUpdate(tuple._1, previousData, history.getData()));
                        });
            }
        }
        ds.unpersist();
    }

    public static void parseFOP(SparkSession session, FOPRepository repository, EventBus bus, String path, boolean initial) {
        fopRepository = repository;
        eventBus = bus;
        fopRepository.deleteAll();
        session.read()
                .format("com.databricks.spark.xml")
                .option("rootTag", "DATA")
                .option("rowTag", "RECORD")
                .option("charset", "windows-1251")
                .load(path)
                .select("FIO", "ADDRESS", "KVED", "STAN")
                .toJavaRDD()
                .foreach(t -> fopRepository.save(parseFOP(t)));
        if (!initial) eventBus.publish(EventBusChannels.FOP, "success");
    }

    private static FOP parseFOP(Row row) {
        return new FOP(
                row.getString(0),
                row.getString(1),
                row.getString(2),
                row.getString(3)
        );
    }

    private static UO parseUO(Row row) {
        return new UO(
                String.valueOf(row.getLong(0)),
                row.getString(1),
                row.getString(2),
                row.getString(3),
                row.getString(4),
                row.getString(5),
                row.getString(6),
                row.get(7) != null ? row.<Row>getAs(7).<String>getList(0).stream().map(String::trim).collect(toList()) : new ArrayList<>()
        );
    }
}
