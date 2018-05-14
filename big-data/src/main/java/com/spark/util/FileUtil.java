package com.spark.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * @author Taras Zubrei
 */
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static String downloadFile(String url, String path) {
        try {
            final File direcotory = new File(path);
            if (!direcotory.exists()) direcotory.mkdirs();
            final String filePath = Paths.get(path, FilenameUtils.getName(new URL(url).getPath())).toString();
            FileUtils.copyURLToFile(new URL(url), new File(filePath));
            return filePath;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to download file from: " + url + " to: " + path, e);
        }
    }

    public static String unzip(String path) {
        try {
            final ZipFile zipFile = new ZipFile(path);
            final String extractPath = Paths.get(Paths.get(path).getParent().toString(), Paths.get(path).getFileName().toString().substring(0, 8)).toString();
            zipFile.extractAll(extractPath);
            logger.info("Successfully extracted files from zip file: {} to directory: {}", path, extractPath);
            return extractPath;
        } catch (ZipException e) {
            throw new IllegalStateException("Failed to unzip file at: " + path, e);
        }
    }
}
