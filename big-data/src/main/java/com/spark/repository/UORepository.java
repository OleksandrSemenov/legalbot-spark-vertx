package com.spark.repository;

import com.core.models.UO;
import com.core.models.UOHistory;
import com.google.inject.Inject;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.FindOptions;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.stream.Collectors;

import static com.spark.util.RedisKeys.UO_TEMPLATE;

/**
 * @author Taras Zubrei
 */
public class UORepository {
    private final static int PAGE_SIZE = 10;
    private final Datastore db;
    private final RedissonClient redisson;

    @Inject
    public UORepository(Datastore datastore, RedissonClient redisson) {
        this.db = datastore;
        this.redisson = redisson;
    }

    public UOHistory findOne(String id) {
        return db.get(UOHistory.class, id);
    }

    public List<UOHistory> findPaged(Integer page) {
        return db.find(UOHistory.class).asList(new FindOptions().skip(page * PAGE_SIZE).limit(PAGE_SIZE));
    }

    public boolean isChanged(UO record) {
        return !redisson.getSet(String.format(UO_TEMPLATE, record.getId())).contains(record.hashCode());
    }

    public void save(UO record) {
        redisson.getSet(String.format(UO_TEMPLATE, record.getId())).add(record.hashCode());
        final UOHistory history = findOne(record.getId());
        if (history != null) {
            history.addUO(record);
            db.save(history);
        } else {
            db.save(new UOHistory(record));
        }
    }

    public void save(UOHistory history) {
        final RSet<Integer> set = redisson.getSet(String.format(UO_TEMPLATE, history.getId()));
        set.delete();
        set.addAll(history.getData().stream().map(Object::hashCode).collect(Collectors.toList()));
        db.save(history);
    }

    public void delete(String id) {
        redisson.getSet(String.format(UO_TEMPLATE, id)).delete();
        db.delete(UOHistory.class, id);
    }
}
