package com.spark;

import com.spark.models.Record;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Taras Zubrei
 */
public class Main {
    private static final RedissonClient redissonClient = Redisson.create();

    public static void main(String[] args) {
        JavaSparkContext sc = new JavaSparkContext(new SparkConf().setAppName("open-data").setMaster("local"));
        SQLContext sqlContext = new SQLContext(sc);

        Dataset<Record> ds = sqlContext.read()
                .format("com.databricks.spark.xml")
                .option("rootTag", "DATA")
                .option("rowTag", "RECORD")
                .option("charset", "windows-1251")
                .load("big-data/src/main/resources/records.xml")
                .select("EDRPOU", "NAME", "SHORT_NAME", "ADDRESS", "BOSS", "KVED", "STAN", "FOUNDERS")
                .map((MapFunction<Row, Record>) Main::parse, Encoders.bean(Record.class));

        ds.filter((FilterFunction<Record>) Main::isChanged)
                .foreach((ForeachFunction<Record>) t -> {
                    redissonClient.getBucket("record/" + t.getId() + "/value").set(t);
                    redissonClient.getBucket("record/" + t.getId() + "/hash").set(t.hashCode());
                    System.err.println("New data: " + t);
                });

        sc.close();
        redissonClient.shutdown();
    }

    private static boolean isChanged(Record record) {
        return !Objects.equals(redissonClient.getBucket("record/" + record.getId() + "/hash").get(), record.hashCode());
    }

    private static Record parse(Row row) {
        return new Record(
                row.getLong(0),
                row.getString(1),
                row.getString(2),
                row.getString(3),
                row.getString(4),
                row.getString(5),
                row.getString(6),
                row.get(7) != null ? row.<Row>getAs(7).getList(0) : new ArrayList<>()
        );
    }
}
