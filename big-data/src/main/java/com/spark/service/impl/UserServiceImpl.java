package com.spark.service.impl;

import com.google.inject.Inject;
import com.spark.models.User;
import com.spark.service.UserService;
import com.spark.util.Resource;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.spark.util.RedisKeys.*;

/**
 * @author Taras Zubrei
 */
public class UserServiceImpl implements UserService {
    private final RedissonClient redisson;

    @Inject
    public UserServiceImpl(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public User find(UUID id) {
        return redisson.<User>getBucket(String.format(USER_TEMPlATE, id.toString())).get();
    }

    @Override
    public void subscribe(UUID userId, Resource to, String id) {
        redisson.getSet(String.format(USER_SUBSCRIPTION_TEMPlATE, userId.toString(), to.getName())).add(id);
    }

    @Override
    public List<User> findSubscribedTo(Resource to, String id) {
        return redisson.<String>getSet(USERS).stream()
                .filter(key -> redisson.getSet(String.format(USER_SUBSCRIPTION_TEMPlATE, key, to.getName())).contains(id))
                .map(key -> redisson.<User>getBucket(String.format(USER_TEMPlATE, key)).get())
                .collect(Collectors.toList());
    }

    @Override
    public boolean isSubscribed(UUID userId, Resource to, String id) {
        return redisson.getSet(String.format(USER_SUBSCRIPTION_TEMPlATE, userId.toString(), to.getName())).contains(id);
    }

    @Override
    public void unsubscribe(UUID userId, Resource from, String id) {
        redisson.getSet(String.format(USER_SUBSCRIPTION_TEMPlATE, userId.toString(), from.getName())).remove(id);
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) user.setId(UUID.randomUUID());
        redisson.<User>getBucket(String.format(USER_TEMPlATE, user.getId().toString())).set(user);
        redisson.getSet(USERS).add(user.getId());
        return user;
    }

    @Override
    public User delete(UUID id) {
        final RBucket<User> userBucket = redisson.getBucket(String.format(USER_TEMPlATE, id.toString()));
        final User user = userBucket.get();
        userBucket.delete();
        Arrays.stream(Resource.values())
                .forEach(resource -> redisson.getBucket(String.format(USER_SUBSCRIPTION_TEMPlATE, id.toString(), resource.getName())).delete());
        redisson.getSet(USERS).remove(user.getId());
        return user;
    }
}