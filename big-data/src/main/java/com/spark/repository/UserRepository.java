package com.spark.repository;

import com.core.models.User;
import com.core.util.MessengerType;
import com.google.inject.Inject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Taras Zubrei
 */
public class UserRepository {
    private final Datastore db;

    @Inject
    public UserRepository(Datastore datastore) {
        this.db = datastore;
    }

    public User findOne(String id) {
        return db.get(User.class, new ObjectId(id));
    }

    public List<User> find(List<String> ids) {
        return db.get(User.class, ids.stream().map(ObjectId::new).collect(Collectors.toList())).asList();
    }

    public User find(MessengerType type, String id) {
        return db.find(User.class).field("messengerIds." + type).equal(id).get();
    }

    public List<String> getAllIds() {
        return db.find(User.class)
                .asKeyList()
                .stream()
                .map(key -> (ObjectId) key.getId())
                .map(ObjectId::toString)
                .collect(Collectors.toList());
    }

    public void save(User user) {
        db.save(user);
    }

    public User delete(String id) {
        final User user = findOne(id);
        db.delete(User.class, id);
        return user;
    }
}
