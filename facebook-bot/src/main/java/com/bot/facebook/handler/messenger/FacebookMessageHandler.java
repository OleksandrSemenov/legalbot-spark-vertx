package com.bot.facebook.handler.messenger;

import com.bot.facebook.service.FacebookService;
import com.core.handler.messenger.MessengerHandler;
import com.core.models.UOUpdate;
import com.core.models.User;
import com.core.util.MessengerType;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taras Zubrei
 */
public class FacebookMessageHandler implements MessengerHandler {
    private final Logger logger = LoggerFactory.getLogger(FacebookMessageHandler.class);
    private final FacebookService facebookService;

    @Inject
    public FacebookMessageHandler(FacebookService facebookService) {
        this.facebookService = facebookService;
    }

    @Override
    public MessengerType type() {
        return MessengerType.FACEBOOK;
    }

    @Override
    public void onUOUpdate(User user, UOUpdate update) {
        logger.info("Sending facebook message ({}) to user: {}", update.getId(), user);
    }
}
