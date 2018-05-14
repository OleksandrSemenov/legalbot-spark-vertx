package com.spark.service;

/**
 * @author Taras Zubrei
 */
public interface SparkService {
    void parseLastUFOPData(boolean initial);

    void parseFOPXml(String path);

    void parseUOXml(String path);

    void parseFOPXml(String path, boolean initial);

    void parseUOXml(String path, boolean initial);
}
