package com.bot.facebook;

import com.bot.facebook.service.FacebookService;
import com.bot.facebook.service.impl.FacebookServiceImpl;
import com.bot.facebook.verticle.FacebookVerticle;
import com.google.inject.AbstractModule;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;

/**
 * @author Taras Zubrei
 */
public class FacebookModule extends AbstractModule {
    private final static String PAGE_ACCESS_TOKEN = "EAAbwqJZAfCccBAM1oqFZAZBDRfxkBnh9Ppuf5w1Mn6jzyNLAdQXVH8ErZBgZCiBUvHuMg4R6532ZB2wqdqBCQNpr2HfalmGHs623dzFvwujeEOXH2pwesM5Q52FoqZCtnvwBYtZBTw8kL9fJBa46CSy7hZC5mHvsht7rLXk205nYOSgZDZD";

    @Override
    protected void configure() {
        FacebookClient pageClient = new DefaultFacebookClient(PAGE_ACCESS_TOKEN, Version.VERSION_2_6);
        bind(FacebookClient.class).toInstance(pageClient);
        bind(FacebookService.class).to(FacebookServiceImpl.class);
        bind(FacebookVerticle.class);
    }
}
