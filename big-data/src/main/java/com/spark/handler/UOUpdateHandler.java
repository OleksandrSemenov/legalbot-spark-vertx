package com.spark.handler;

import com.google.inject.Inject;
import com.spark.handler.messenger.MessengerHandler;
import com.spark.models.UOUpdate;
import com.spark.util.MessengerType;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Taras Zubrei
 */
public class UOUpdateHandler implements Handler<Message<UOUpdate>> {
    private static final Logger logger = LoggerFactory.getLogger(UOUpdateHandler.class);
    private final Map<MessengerType, MessengerHandler> handlers;
    private final RedissonClient redisson;

    @Inject
    public UOUpdateHandler(Set<MessengerHandler> handlers, RedissonClient redisson) {
        this.handlers = handlers.stream().collect(Collectors.toMap(MessengerHandler::type, Function.identity()));
        this.redisson = redisson;
    }

    @Override
    public void handle(Message<UOUpdate> event) {
        final UOUpdate update = event.body();
        handlers.values().forEach(handler -> handler.onUOUpdate(update));
        logger.info("Handled UO update: {}", update);
    }
}
