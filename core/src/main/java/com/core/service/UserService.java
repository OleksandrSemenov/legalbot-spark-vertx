package com.core.service;

import com.core.models.User;
import com.core.util.Resource;

import java.util.List;

/**
 * @author Taras Zubrei
 */
public interface UserService {
    List<String> findAll();

    User find(String id);

    void subscribe(String userId, Resource to, String id);

    List<User> findSubscribedTo(Resource to, String id);

    boolean isSubscribed(String userId, Resource to, String id);

    void unsubscribe(String userId, Resource from, String id);

    User save(User user);

    User delete(String id);
}
