package com.bot.facebook.message.impl;

import com.bot.facebook.message.MessageStrategy;
import com.restfb.types.webhook.messaging.MessagingItem;
import com.restfb.types.webhook.messaging.PostbackItem;

/**
 * @author Taras Zubrei
 */
public class PostbackMessageStrategy implements MessageStrategy {
    @Override
    public String id(MessagingItem message) {
        return String.valueOf(message.getTimestamp().getTime());
    }

    @Override
    public boolean applies(MessagingItem message) {
        return message.getItem() instanceof PostbackItem;
    }

    @Override
    public String payload(MessagingItem message) {
        return message.getPostback().getPayload();
    }
}