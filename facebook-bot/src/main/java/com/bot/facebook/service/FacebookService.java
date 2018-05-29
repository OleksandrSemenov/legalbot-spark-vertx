package com.bot.facebook.service;

import com.core.models.User;
import com.restfb.types.send.Message;
import com.restfb.types.webhook.messaging.MessagingItem;

/**
 * @author Taras Zubrei
 */
public interface FacebookService {
    void sendMessage(String userId, Message message);

    void sendBasicMenu(User user);

    void unhandledMessage(User user, MessagingItem message);

    void viewUO(User user);

    void showUO(User user, String id);
}
