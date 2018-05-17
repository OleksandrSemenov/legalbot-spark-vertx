package com.spark.service.impl;

import com.core.models.FOP;
import com.core.models.UO;
import com.google.inject.Inject;
import com.spark.service.UFOPService;
import com.spark.util.RedisKeys;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * @author Taras Zubrei
 */
public class UFOPServiceImpl implements UFOPService {
    private final RedissonClient redisson;

    @Inject
    public UFOPServiceImpl(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public List<UO> findUO(String id) {
        return new ArrayList<>(redisson.<String, UO>getMap(String.format(RedisKeys.UO_TEMPLATE, id)).values());
    }

    @Override
    public Map<String, List<UO>> findUO(int page, int size) {
        return redisson.<Long>getScoredSortedSet(RedisKeys.UO).entryRange(page * size, (page + 1) * size)
                .stream()
                .map(ScoredEntry::getValue)
                .map(String::valueOf)
                .map(this::findUO)
                .collect(toMap(t -> String.valueOf(t.get(0).getId()), Function.identity()));
    }

    @Override
    public List<FOP> findFOP(int page, int size) {
        return redisson.<FOP>getList(RedisKeys.FOP).subList(page * size, (page + 1) * size);
    }
}
