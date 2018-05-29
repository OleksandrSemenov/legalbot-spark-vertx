package com.bot.facebook.fsm;

import com.bot.facebook.command.Command;
import com.bot.facebook.command.Commands;
import com.core.models.User;
import com.restfb.types.webhook.messaging.MessagingItem;

/**
 * @author Taras Zubrei
 */
public interface FSMService {
    <T extends Command> void fire(User user, T command);

    void fire(User user, Commands command);

    void fire(User user, MessagingItem message);
}
