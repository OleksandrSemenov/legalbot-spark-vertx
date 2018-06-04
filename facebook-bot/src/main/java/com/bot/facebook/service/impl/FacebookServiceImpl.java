package com.bot.facebook.service.impl;

import com.bot.facebook.command.Command;
import com.bot.facebook.command.CommandWrapper;
import com.bot.facebook.command.Commands;
import com.bot.facebook.command.impl.*;
import com.bot.facebook.service.FacebookService;
import com.bot.facebook.template.MenuTemplate;
import com.bot.facebook.template.MessageTemplates;
import com.bot.facebook.template.UOTemplate;
import com.bot.facebook.util.ExceptionUtils;
import com.core.models.UO;
import com.core.models.User;
import com.core.service.UFOPService;
import com.core.service.UserService;
import com.core.util.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.send.*;
import com.restfb.types.webhook.messaging.MessagingItem;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.core.util.MessengerType.FACEBOOK;

/**
 * @author Taras Zubrei
 */
public class FacebookServiceImpl implements FacebookService {
    private static final Logger logger = LoggerFactory.getLogger(FacebookServiceImpl.class);

    private final FacebookClient facebookClient;
    private final UserService userService;
    private final UFOPService ufopService;
    private final MessageTemplates messageTemplates;
    private final ObjectMapper mapper;

    @Inject
    public FacebookServiceImpl(FacebookClient facebookClient, UserService userService, UFOPService ufopService, MessageTemplates messageTemplates, ObjectMapper mapper) {
        this.facebookClient = facebookClient;
        this.userService = userService;
        this.ufopService = ufopService;
        this.messageTemplates = messageTemplates;
        this.mapper = mapper;
    }

    @Override
    public void sendMessage(String userId, Message message) {
        if (StringUtils.isNotBlank(message.getText()))
            logger.info("Sending message (size: {}): {}", message.getText().length(), StringUtils.normalizeSpace(message.getText()));
        facebookClient.publish("me/messages", SendResponse.class,
                Parameter.with("recipient", new IdMessageRecipient(userId)),
                Parameter.with("message", message)
        );
    }

    public void sendBasicMenu(User user) {
        final Locale language = Stream.of(Locale.US, new Locale("uk", "ua")).filter(l -> !Objects.equals(l, user.getLocale(FACEBOOK))).findAny().get();
        final MenuTemplate template = messageTemplates.getBasicMenuTemplate(user.getLocale(FACEBOOK), language);

        ButtonTemplatePayload payload = new ButtonTemplatePayload(template.getTitle());
        payload.addButton(new PostbackButton(template.getViewButton(), Commands.VIEW.toString()));
        payload.addButton(new PostbackButton(template.getShowSubscriptions(), Commands.SUBSCRIPTIONS.toString()));
        payload.addButton(new PostbackButton(template.getChangeLocaleButton(), write(new ChangeLanguage().setTo(language))));

        TemplateAttachment templateAttachment = new TemplateAttachment(payload);
        sendMessage(user.getMessengerId(FACEBOOK), new Message(templateAttachment));
    }

    @Override
    public void viewUO(User user) {
        sendMessage(user.getMessengerId(FACEBOOK), new Message(messageTemplates.getUOId(user.getLocale(FACEBOOK))));
    }

    @Override
    public void viewResources(User user) {
        Iterators.partition(Arrays.stream(Resource.values()).iterator(), 3)
                .forEachRemaining(resources -> {
                    ButtonTemplatePayload payload = new ButtonTemplatePayload(messageTemplates.getResource(user.getLocale(FACEBOOK)));
                    resources.forEach(resource -> payload.addButton(new PostbackButton(messageTemplates.getResourceName(resource, user.getLocale(FACEBOOK)), write(new ViewResource().setTo(resource)))));
                    TemplateAttachment templateAttachment = new TemplateAttachment(payload);
                    sendMessage(user.getMessengerId(FACEBOOK), new Message(templateAttachment));
                });
    }

    @Override
    public void showUO(ViewUO viewUO, User user) {
        final List<UO> data = ufopService.findUO(viewUO.getId());
        Message response;
        if (data.isEmpty()) {
            response = new Message(messageTemplates.getEmptyResource(Resource.UO, user.getLocale(FACEBOOK)));
        } else {
            final UOTemplate template = messageTemplates.getUOTemplate(user.getLocale(FACEBOOK));
            data.subList(0, data.size() - 1).stream().map(template::replace).map(Message::new)
                    .forEach(message -> sendMessage(user.getMessengerId(FACEBOOK), message));
            response = new Message(template.replace(Iterables.getLast(data)));
            final Object nextCommand = viewUO.hasNext() ? new ViewUO(viewUO.getNext()) : Commands.MENU;
            if (userService.isSubscribed(user.getId(), Resource.UO, viewUO.getId()))
                response.addQuickReply(new QuickReply(messageTemplates.getUnsubscribeButton(user.getLocale(FACEBOOK)), write(new Unsubscribe(Resource.UO, viewUO.getId()), nextCommand)));
            else
                response.addQuickReply(new QuickReply(messageTemplates.getSubscribeButton(user.getLocale(FACEBOOK)), write(new Subscribe(Resource.UO, viewUO.getId()), nextCommand)));
            if (viewUO.hasNext())
                response.addQuickReply(new QuickReply(messageTemplates.getNextButton(user.getLocale(FACEBOOK)), write(new ViewUO(viewUO.getNext()))));
        }
        response.addQuickReply(new QuickReply(messageTemplates.getBasicMenuTemplate(user.getLocale(FACEBOOK), Locale.US).getTitle(), Commands.MENU.toString()));
        sendMessage(user.getMessengerId(FACEBOOK), response);
    }

    @Override
    public void viewSubscriptions(User user) {
        Iterators.partition(Arrays.stream(Resource.values()).iterator(), 3)
                .forEachRemaining(resources -> {
                    ButtonTemplatePayload payload = new ButtonTemplatePayload(messageTemplates.getResource(user.getLocale(FACEBOOK)));
                    resources.forEach(resource -> payload.addButton(new PostbackButton(messageTemplates.getResourceName(resource, user.getLocale(FACEBOOK)), write(new ShowSubscriptions().setTo(resource)))));
                    TemplateAttachment templateAttachment = new TemplateAttachment(payload);
                    sendMessage(user.getMessengerId(FACEBOOK), new Message(templateAttachment));
                });
    }

    @Override
    public void showSubscriptions(User user, Resource to, List<String> subscriptions) {
        if (subscriptions.isEmpty()) {
            final Message response = new Message(messageTemplates.getEmptySubscriptions(to, user.getLocale(FACEBOOK)));
            response.addQuickReply(new QuickReply(messageTemplates.getBasicMenuTemplate(user.getLocale(FACEBOOK), Locale.US).getTitle(), Commands.MENU.toString()));
            sendMessage(user.getMessengerId(FACEBOOK), response);
            return;
        }
        sendMessage(user.getMessengerId(FACEBOOK), new Message(messageTemplates.getSubscriptions(to, user.getLocale(FACEBOOK))));
        if (to == Resource.UO) {
            showUO(new ViewUO(subscriptions), user);
        }
    }

    @Override
    public void unhandledMessage(User user, MessagingItem message) {
        logger.warn("Unhandled message from user: {}. Message: {}", user.getId(), message);
        sendMessage(user.getMessengerId(FACEBOOK), new Message(messageTemplates.getWrongCommandTemplate(user.getLocale(FACEBOOK))));
        sendBasicMenu(user);
    }

    private String write(Object... commands) {
        final List<Object> payload = Arrays.stream(commands).map(command -> {
            if (command instanceof Command)
                return new CommandWrapper<>((Command) command);
            return command;
        }).collect(Collectors.toList());
        return ExceptionUtils.wrapException(() -> mapper.writeValueAsString(payload));
    }
}
