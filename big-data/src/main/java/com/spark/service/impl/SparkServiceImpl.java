package com.spark.service.impl;

import com.google.inject.Inject;
import com.spark.service.SparkService;
import com.spark.util.FileUtil;
import com.spark.util.HttpUtil;
import com.spark.util.SparkUtil;
import io.vertx.rxjava.core.eventbus.EventBus;
import org.apache.spark.sql.SparkSession;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;

/**
 * @author Taras Zubrei
 */
public class SparkServiceImpl implements SparkService, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(SparkServiceImpl.class);
    private static final String UFOP_PATH = "/tmp/legalbot/archives/ufop";

    private final SparkSession session;
    private final RedissonClient redisson;
    private final EventBus eventBus;

    @Inject
    public SparkServiceImpl(SparkSession session, RedissonClient redisson, EventBus eventBus) {
        this.session = session;
        this.redisson = redisson;
        this.eventBus = eventBus;
    }

    @Override
    public void parseLastUFOPData(boolean initial) {
        final HttpUtil.ArchiveUrl archiveUrl = HttpUtil.getUFOPDownloadUrl();
        final RBucket<String> lastUpdatedBucket = redisson.getBucket("ufop/date");
        final LocalDate lastUpdated = Optional.ofNullable(lastUpdatedBucket.get()).map(LocalDate::parse).orElse(LocalDate.ofEpochDay(0));
        if (archiveUrl.getDate().isAfter(lastUpdated)) {
            logger.info("Downloading new data for date: {}", archiveUrl.getDate());
            final String zipFile = FileUtil.downloadFile(archiveUrl.getUrl(), UFOP_PATH);
            final String extractFolder = FileUtil.unzip(zipFile);
            parseUOXml(Paths.get(extractFolder, "15.1-EX_XML_EDR_UO.xml").toString(), initial);
            parseFOPXml(Paths.get(extractFolder, "15.2-EX_XML_EDR_FOP.xml").toString(), initial);
            lastUpdatedBucket.set(archiveUrl.getDate().toString());
            logger.info("Successfully parsed new UFOP data for date: {}", archiveUrl.getDate());
        } else {
            logger.info("Data in db ({}) is the most recent. Archive date: {}", lastUpdated, archiveUrl.getDate());
        }
    }

    @Override
    public void parseFOPXml(String path) {
        parseFOPXml(path, false);
    }

    @Override
    public void parseUOXml(String path) {
        parseUOXml(path, false);
    }

    @Override
    public void parseFOPXml(String path, boolean initial) {
        SparkUtil.parseFOP(session, redisson, eventBus, path, initial);
        logger.info("Successfully parsed FOP data");
    }

    @Override
    public void parseUOXml(String path, boolean initial) {
        SparkUtil.parseUO(session, redisson, eventBus, path, initial);
        logger.info("Successfully parsed UO data");
    }
}
