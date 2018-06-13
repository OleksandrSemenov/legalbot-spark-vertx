package com.bot.facebook.message;

import com.restfb.types.webhook.messaging.MessagingItem;

/**
 * @author Taras Zubrei
 */
public interface MessageStrategy {
    String id(MessagingItem message);

    boolean applies(MessagingItem message);

    String payload(MessagingItem message);
}