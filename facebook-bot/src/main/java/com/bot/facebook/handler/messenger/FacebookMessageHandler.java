package com.bot.facebook.handler.messenger;

import com.bot.facebook.service.FacebookService;
import com.bot.facebook.template.MessageTemplates;
import com.bot.facebook.template.UOTemplate;
import com.core.handler.messenger.MessengerHandler;
import com.core.models.UOUpdate;
import com.core.models.User;
import com.core.util.MessengerType;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.restfb.types.send.Message;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.core.util.MessengerType.FACEBOOK;

/**
 * @author Taras Zubrei
 */
public class FacebookMessageHandler implements MessengerHandler {
    private final Logger logger = LoggerFactory.getLogger(FacebookMessageHandler.class);
    private final FacebookService facebookService;
    private final MessageTemplates messageTemplates;

    @Inject
    public FacebookMessageHandler(FacebookService facebookService, MessageTemplates messageTemplates) {
        this.facebookService = facebookService;
        this.messageTemplates = messageTemplates;
    }

    @Override
    public MessengerType type() {
        return MessengerType.FACEBOOK;
    }

    @Override
    public void onUOUpdate(User user, UOUpdate update) {
        try {
            logger.info("Sending facebook message ({}) to user: {}", update.getId(), user.getId());
            final UOTemplate template = messageTemplates.getUOTemplate(user.getLocale(FACEBOOK));
            facebookService.sendMessage(user.getMessengerId(FACEBOOK), new Message(new StrSubstitutor(ImmutableMap.of(
                    "id", update.getId()
            )).replace(messageTemplates.getUOUpdateMessage(user.getLocale(FACEBOOK)))));
            update.getPrevious().stream().map(template::replace).map(Message::new)
                    .forEach(response -> facebookService.sendMessage(user.getMessengerId(FACEBOOK), response));
            facebookService.sendMessage(user.getMessengerId(FACEBOOK),
                    new Message(messageTemplates.getUOUpdateActualMessage(user.getLocale(FACEBOOK))));
            update.getActual().stream().map(template::replace).map(Message::new)
                    .forEach(response -> facebookService.sendMessage(user.getMessengerId(FACEBOOK), response));
        } catch (Exception ex) {
            logger.error("Failed to send UO update (" + update.getId() + ") to user: " + user.getId(), ex);
        }
    }
}
