package com.spark.repository;

import com.core.models.FOP;
import com.google.inject.Inject;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.FindOptions;

import java.util.List;

/**
 * @author Taras Zubrei
 */
public class FOPRepository {
    private final static int PAGE_SIZE = 10;
    private final Datastore db;

    @Inject
    public FOPRepository(Datastore datastore) {
        this.db = datastore;
    }

    public List<FOP> findPaged(Integer page) {
        return db.find(FOP.class).asList(new FindOptions().skip(page * PAGE_SIZE).limit(PAGE_SIZE));
    }

    public void deleteAll() {
        db.getCollection(FOP.class).drop();
    }

    public void save(FOP fop) {
        db.save(fop);
    }
}