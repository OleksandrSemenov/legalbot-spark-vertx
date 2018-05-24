package com.bot.facebook.service.impl;

import com.bot.facebook.command.Command;
import com.bot.facebook.command.CommandWrapper;
import com.bot.facebook.command.Commands;
import com.bot.facebook.command.impl.ChangeLanguage;
import com.bot.facebook.command.impl.Subscribe;
import com.bot.facebook.command.impl.Unsubscribe;
import com.bot.facebook.service.FacebookService;
import com.bot.facebook.template.MenuTemplate;
import com.bot.facebook.template.MessageTemplates;
import com.bot.facebook.util.ExceptionUtils;
import com.core.models.UO;
import com.core.models.User;
import com.core.service.UFOPService;
import com.core.service.UserService;
import com.core.util.RedisKeys;
import com.core.util.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.send.*;
import com.restfb.types.webhook.messaging.MessagingItem;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import static com.core.util.MessengerType.FACEBOOK;

/**
 * @author Taras Zubrei
 */
public class FacebookServiceImpl implements FacebookService {
    private static final Logger logger = LoggerFactory.getLogger(FacebookServiceImpl.class);

    private final FacebookClient facebookClient;
    private final UserService userService;
    private final RedissonClient redisson;
    private final UFOPService ufopService;
    private final MessageTemplates messageTemplates;
    private final ObjectMapper mapper;

    @Inject
    public FacebookServiceImpl(FacebookClient facebookClient, UserService userService, RedissonClient redisson, UFOPService ufopService, MessageTemplates messageTemplates, ObjectMapper mapper) {
        this.facebookClient = facebookClient;
        this.userService = userService;
        this.redisson = redisson;
        this.ufopService = ufopService;
        this.messageTemplates = messageTemplates;
        this.mapper = mapper;
    }

    @Override
    public void sendMessage(String userId, Message message) {
        facebookClient.publish("me/messages", SendResponse.class,
                Parameter.with("recipient", new IdMessageRecipient(userId)),
                Parameter.with("message", message)
        );
    }

    public void sendBasicMenu(User user) {
        final Locale language = Stream.of(Locale.US, new Locale("uk", "ua")).filter(l -> !Objects.equals(l, user.getLocale(FACEBOOK))).findAny().get();
        final MenuTemplate template = messageTemplates.getBasicMenuTemplate(user.getLocale(FACEBOOK), language);

        ButtonTemplatePayload payload = new ButtonTemplatePayload(template.getTitle());
        payload.addButton(new PostbackButton(template.getViewUOButton(), Commands.VIEW_UO));
        payload.addButton(new PostbackButton(template.getChangeLocaleButton(), write(new ChangeLanguage().setTo(language))));

        TemplateAttachment templateAttachment = new TemplateAttachment(payload);
        sendMessage(user.getMessengerId(FACEBOOK), new Message(templateAttachment));
    }

    @Override
    public void handleMessage(User user, MessagingItem message) {
        final String previous = redisson.<String>getBucket(String.format(RedisKeys.FACEBOOK_STATE, user.getId())).get();
        if (Objects.equals(previous, Commands.VIEW_UO)) {
            final String id = message.getMessage().getText();
            final List<UO> data = ufopService.findUO(id);
            final Message response = new Message(ExceptionUtils.wrapException(() -> new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data)));
            if (!data.isEmpty()) {
                if (userService.isSubscribed(user.getId(), Resource.UO, id))
                    response.addQuickReply(new QuickReply(messageTemplates.getUnsubscribeButton(user.getLocale(FACEBOOK)), write(new Unsubscribe(Resource.UO, id))));
                else
                    response.addQuickReply(new QuickReply(messageTemplates.getSubscribeButton(user.getLocale(FACEBOOK)), write(new Subscribe(Resource.UO, id))));
            }
            sendMessage(user.getMessengerId(FACEBOOK), response);
            redisson.<String>getBucket(String.format(RedisKeys.FACEBOOK_STATE, user.getId())).delete();
        } else unhandledMessage(user, message);
    }

    @Override
    public void viewUO(User user) {
        redisson.getBucket(String.format(RedisKeys.FACEBOOK_STATE, user.getId())).set(Commands.VIEW_UO);
        sendMessage(user.getMessengerId(FACEBOOK), new Message(messageTemplates.getUOId(user.getLocale(FACEBOOK))));
    }

    @Override
    public void unhandledMessage(User user, MessagingItem message) {
        logger.warn("Unhandled message from user: {}. Message: {}", user.getId(), message);
        sendMessage(user.getMessengerId(FACEBOOK), new Message(messageTemplates.getWrongCommandTemplate(user.getLocale(FACEBOOK))));
        sendBasicMenu(user);
    }

    private String write(Command command) {
        return ExceptionUtils.wrapException(() -> mapper.writeValueAsString(new CommandWrapper<>(command)));
    }
}
