package com.bot.facebook.handler.messenger;

import com.bot.facebook.service.FacebookService;
import com.bot.facebook.template.MessageTemplates;
import com.core.handler.messenger.MessengerHandler;
import com.core.models.UOUpdate;
import com.core.models.User;
import com.core.util.MessengerType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.restfb.types.Message;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taras Zubrei
 */
public class FacebookMessageHandler implements MessengerHandler {
    private final Logger logger = LoggerFactory.getLogger(FacebookMessageHandler.class);
    private final FacebookService facebookService;
    private final MessageTemplates messageTemplates;
    private final ObjectWriter prettyPrinter = new ObjectMapper().writerWithDefaultPrettyPrinter();

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
            final Message message = new Message();
            final String template = messageTemplates.getUOUpdateMessage(user.getLocale(MessengerType.FACEBOOK));
            StrSubstitutor sub = new StrSubstitutor(ImmutableMap.of(
                    "id", update.getId(),
                    "previous", prettyPrinter.writeValueAsString(update.getPrevious()),
                    "actual", prettyPrinter.writeValueAsString(update.getActual())
            ));
            message.setMessage(sub.replace(template));
            facebookService.sendMessage(user.getMessengerIds().get(MessengerType.FACEBOOK), message);
        } catch (Exception ex) {
            logger.error("Failed to send UO update (" + update.getId() + ") to user: " + user.getId(), ex);
        }
    }
}
