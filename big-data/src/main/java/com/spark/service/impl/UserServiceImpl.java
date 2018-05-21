package com.spark.service.impl;

import com.core.models.User;
import com.core.service.UserService;
import com.core.util.Resource;
import com.google.inject.Inject;
import com.spark.repository.UserRepository;
import org.redisson.api.RedissonClient;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.spark.util.RedisKeys.USER_SUBSCRIPTION_TEMPlATE;

/**
 * @author Taras Zubrei
 */
public class UserServiceImpl implements UserService {
    private final RedissonClient redisson;
    private final UserRepository userRepository;

    @Inject
    public UserServiceImpl(RedissonClient redisson, UserRepository userRepository) {
        this.redisson = redisson;
        this.userRepository = userRepository;
    }

    @Override
    public List<String> findAll() {
        return userRepository.getAllIds();
    }

    @Override
    public User find(String id) {
        return userRepository.findOne(id);
    }

    @Override
    public void subscribe(String userId, Resource to, String id) {
        redisson.getSet(String.format(USER_SUBSCRIPTION_TEMPlATE, userId, to.getName())).add(id);
    }

    @Override
    public List<User> findSubscribedTo(Resource to, String id) {
        final List<String> ids = userRepository.getAllIds().stream()
                .filter(key -> redisson.getSet(String.format(USER_SUBSCRIPTION_TEMPlATE, key, to.getName())).contains(id))
                .collect(Collectors.toList());
        return userRepository.find(ids);
    }

    @Override
    public boolean isSubscribed(String userId, Resource to, String id) {
        return redisson.getSet(String.format(USER_SUBSCRIPTION_TEMPlATE, userId, to.getName())).contains(id);
    }

    @Override
    public void unsubscribe(String userId, Resource from, String id) {
        redisson.getSet(String.format(USER_SUBSCRIPTION_TEMPlATE, userId, from.getName())).remove(id);
    }

    @Override
    public User save(User user) {
        userRepository.save(user);
        return user;
    }

    @Override
    public User delete(String id) {
        final User user = userRepository.delete(id);
        Arrays.stream(Resource.values())
                .forEach(resource -> redisson.getBucket(String.format(USER_SUBSCRIPTION_TEMPlATE, id, resource.getName())).delete());
        return user;
    }
}
