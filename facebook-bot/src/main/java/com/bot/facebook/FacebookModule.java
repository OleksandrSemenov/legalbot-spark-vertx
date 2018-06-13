package com.bot.facebook;

import com.bot.facebook.fsm.FSMService;
import com.bot.facebook.fsm.impl.FSMServiceImpl;
import com.bot.facebook.message.MessageStrategy;
import com.bot.facebook.message.command.CommandParser;
import com.bot.facebook.message.command.impl.CommandWrapperParser;
import com.bot.facebook.message.command.impl.SimpleCommandParser;
import com.bot.facebook.message.impl.PostbackMessageStrategy;
import com.bot.facebook.message.impl.QuickReplyMessageStrategy;
import com.bot.facebook.message.impl.TextMessageStrategy;
import com.bot.facebook.service.FacebookService;
import com.bot.facebook.service.impl.FacebookServiceImpl;
import com.bot.facebook.template.MessageTemplates;
import com.bot.facebook.verticle.FacebookVerticle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;

/**
 * @author Taras Zubrei
 */
public class FacebookModule extends AbstractModule {
    private final static String PAGE_ACCESS_TOKEN = "EAACzPQ0a8wYBALV9Gqn2xWat8nZCHuI0jDsEiHPcoZBsroj2wVmNhyRMjPxJ22RQZC4fOjaLdQHhbkaZBStNevmvPvi8lHp9wNE9yutMrMWgYEu2Q4W8JtOaLjfJFD1CbdfsVXQz5ZA2hE5alNHSQ3B5olZCQMupJ2uvpENAt4vwZDZD";

    @Override
    protected void configure() {
        FacebookClient pageClient = new DefaultFacebookClient(PAGE_ACCESS_TOKEN, Version.VERSION_2_6);
        bind(FacebookClient.class).toInstance(pageClient);
        bind(FacebookService.class).to(FacebookServiceImpl.class);
        bind(FacebookVerticle.class);
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
        bindConstant().annotatedWith(Names.named("i18n")).to("i18n");
        bind(MessageTemplates.class);
        bind(FSMService.class).to(FSMServiceImpl.class);
        final Multibinder<CommandParser> commandParsers = Multibinder.newSetBinder(binder(), CommandParser.class);
        commandParsers.addBinding().to(CommandWrapperParser.class);
        commandParsers.addBinding().to(SimpleCommandParser.class);
        final Multibinder<MessageStrategy> messengerHandlers = Multibinder.newSetBinder(binder(), MessageStrategy.class);
        messengerHandlers.addBinding().to(PostbackMessageStrategy.class);
        messengerHandlers.addBinding().to(QuickReplyMessageStrategy.class);
        messengerHandlers.addBinding().to(TextMessageStrategy.class);
    }
}
