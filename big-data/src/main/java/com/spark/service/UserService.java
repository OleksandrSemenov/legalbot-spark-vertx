package com.spark.service;

import com.spark.models.User;
import com.spark.util.Resource;

import java.util.List;
import java.util.UUID;

/**
 * @author Taras Zubrei
 */
public interface UserService {
    List<String> findAll();

    User find(UUID id);

    void subscribe(UUID userId, Resource to, String id);

     List<User> findSubscribedTo(Resource to, String id);

     boolean isSubscribed(UUID userId, Resource to, String id);

    void unsubscribe(UUID userId, Resource from, String id);

    User save(User user);

    User delete(UUID id);
}
