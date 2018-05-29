package com.bot.facebook.service;

import com.bot.facebook.command.impl.ViewUO;
import com.core.models.User;
import com.core.util.Resource;
import com.restfb.types.send.Message;
import com.restfb.types.webhook.messaging.MessagingItem;

import java.util.List;

/**
 * @author Taras Zubrei
 */
public interface FacebookService {
    void sendMessage(String userId, Message message);

    void sendBasicMenu(User user);

    void unhandledMessage(User user, MessagingItem message);

    void viewUO(User user);

    void viewResources(User user);

    void showUO(ViewUO viewUO, User user);

    void viewSubscriptions(User user);

    void showSubscriptions(User user, Resource to, List<String> subscriptions);
}
