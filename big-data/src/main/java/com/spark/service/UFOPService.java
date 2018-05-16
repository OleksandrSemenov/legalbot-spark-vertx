package com.spark.service;

import com.spark.models.FOP;
import com.spark.models.UO;

import java.util.List;
import java.util.Map;

/**
 * @author Taras Zubrei
 */
public interface UFOPService {
    List<UO> findUO(String id);

    Map<String, List<UO>> findUO(int page, int size);

    List<FOP> findFOP(int page, int size);
}
