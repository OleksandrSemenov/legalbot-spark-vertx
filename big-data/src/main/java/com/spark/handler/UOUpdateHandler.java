package com.spark.handler;

import com.google.inject.Inject;
import com.spark.handler.messenger.MessengerHandler;
import com.spark.models.UOUpdate;
import com.spark.service.UserService;
import com.spark.util.MessengerType;
import com.spark.util.Resource;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Taras Zubrei
 */
public class UOUpdateHandler implements Handler<Message<UOUpdate>> {
    private static final Logger logger = LoggerFactory.getLogger(UOUpdateHandler.class);
    private final Map<MessengerType, MessengerHandler> handlers;
    private final UserService userService;

    @Inject
    public UOUpdateHandler(Set<MessengerHandler> handlers, UserService userService) {
        this.handlers = handlers.stream().collect(Collectors.toMap(MessengerHandler::type, Function.identity()));
        this.userService = userService;
    }

    @Override
    public void handle(Message<UOUpdate> event) {
        final UOUpdate update = event.body();
        userService.findSubscribedTo(Resource.UO, update.getId().toString()).forEach(user -> user.getMessengerIds()
                .entrySet()
                .stream()
                .filter(entry -> handlers.containsKey(entry.getKey()))
                .map(entry -> handlers.get(entry.getKey()))
                .forEach(handler -> handler.onUOUpdate(user, update))
        );
        logger.info("Handled UO update: {}", update);
    }
}
