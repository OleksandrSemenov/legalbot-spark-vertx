package com.bot.facebook.message.impl;

import com.bot.facebook.message.MessageStrategy;
import com.restfb.types.webhook.messaging.MessagingItem;

/**
 * @author Taras Zubrei
 */
public class QuickReplyMessageStrategy implements MessageStrategy {
    @Override
    public String id(MessagingItem message) {
        return message.getMessage().getMid();
    }

    @Override
    public boolean applies(MessagingItem message) {
        return message.getMessage() != null && message.getMessage().getQuickReply() != null;
    }

    @Override
    public String payload(MessagingItem message) {
        return message.getMessage().getQuickReply().getPayload();
    }
}