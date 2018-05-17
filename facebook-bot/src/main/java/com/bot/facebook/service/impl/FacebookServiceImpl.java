package com.bot.facebook.service.impl;

import com.bot.facebook.service.FacebookService;
import com.core.service.UserService;
import com.google.inject.Inject;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.Message;
import com.restfb.types.send.IdMessageRecipient;
import com.restfb.types.send.SendResponse;

/**
 * @author Taras Zubrei
 */
public class FacebookServiceImpl implements FacebookService {
    private final FacebookClient facebookClient;
    private final UserService userService;

    @Inject
    public FacebookServiceImpl(FacebookClient facebookClient, UserService userService) {
        this.facebookClient = facebookClient;
        this.userService = userService;
    }

    @Override
    public void sendMessage(String userId, Message message) {
        facebookClient.publish("me/messages", SendResponse.class,
                Parameter.with("recipient", new IdMessageRecipient(userId)),
                Parameter.with("message", message)
        );
    }
}
