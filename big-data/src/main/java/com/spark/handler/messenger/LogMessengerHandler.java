package com.spark.handler.messenger;

import com.spark.models.UOUpdate;
import com.spark.models.User;
import com.spark.util.MessengerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taras Zubrei
 */
public class LogMessengerHandler implements MessengerHandler {
    private static final Logger logger = LoggerFactory.getLogger(LogMessengerHandler.class);

    @Override
    public MessengerType type() {
        return MessengerType.LOG;
    }

    @Override
    public void onUOUpdate(User user, UOUpdate update) {
        logger.info("User {} received UO update: {}", user.getId(), update);
    }
}
