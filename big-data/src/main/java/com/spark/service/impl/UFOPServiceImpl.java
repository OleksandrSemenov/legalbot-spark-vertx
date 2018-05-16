package com.spark.service.impl;

import com.google.inject.Inject;
import com.spark.models.FOP;
import com.spark.models.UO;
import com.spark.service.UFOPService;
import com.spark.util.RedisKeys;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    public Map<String, List<UO>> findUO(Integer page, Integer size) {
        return redisson.<String>getList(RedisKeys.UO).subList(page != null ? page * size : 0, (Optional.ofNullable(page).orElse(0) + 1) * size).stream()
                .map(this::findUO)
                .collect(toMap(t -> String.valueOf(t.get(0).getId()), Function.identity()));
    }

    @Override
    public List<FOP> findFOP(Integer page, Integer size) {
        return redisson.<FOP>getList(RedisKeys.FOP).subList(page != null ? page * size : 0, (Optional.ofNullable(page).orElse(0) + 1) * size);
    }
}
