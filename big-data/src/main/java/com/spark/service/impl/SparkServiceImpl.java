package com.spark.service.impl;

import com.google.inject.Inject;
import com.spark.service.SparkService;
import com.spark.util.SparkUtil;
import org.apache.spark.sql.SparkSession;
import org.redisson.api.RedissonClient;

import java.io.Serializable;

/**
 * @author Taras Zubrei
 */
public class SparkServiceImpl implements SparkService, Serializable {
    private final SparkSession session;
    private final RedissonClient redisson;

    @Inject
    public SparkServiceImpl(SparkSession session, RedissonClient redisson) {
        this.session = session;
        this.redisson = redisson;
    }

    @Override
    public void parseFOPXml(String path) {
        SparkUtil.parseFOP(session, redisson, path);
    }

    @Override
    public void parseUOXml(String path) {
        SparkUtil.parseUO(session, redisson, path);
    }
}
