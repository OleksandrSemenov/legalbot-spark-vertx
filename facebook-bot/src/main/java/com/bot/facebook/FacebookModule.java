package com.bot.facebook;

import com.bot.facebook.service.FacebookService;
import com.bot.facebook.service.impl.FacebookServiceImpl;
import com.bot.facebook.template.MessageTemplates;
import com.bot.facebook.verticle.FacebookVerticle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
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
    }
}
