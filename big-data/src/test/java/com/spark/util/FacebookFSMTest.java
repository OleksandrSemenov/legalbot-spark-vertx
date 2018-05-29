package com.spark.util;

import com.bot.facebook.FacebookModule;
import com.bot.facebook.command.Commands;
import com.bot.facebook.command.impl.*;
import com.bot.facebook.fsm.FSMService;
import com.core.models.User;
import com.core.service.UserService;
import com.core.util.MessengerType;
import com.core.util.Resource;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.restfb.types.webhook.messaging.MessageItem;
import com.restfb.types.webhook.messaging.MessagingItem;
import com.spark.GuiceModule;
import com.spark.Main;
import com.spark.service.SparkService;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author Taras Zubrei
 */
public class FacebookFSMTest {
    private static final Injector injector = Guice.createInjector(new GuiceModule(), new FacebookModule());

    @Test
    public void simulate() {
        Main.configureVertx(injector);
        Main.configureMorphia(injector);
        final UserService userService = injector.getInstance(UserService.class);
        final FSMService fsmService = injector.getInstance(FSMService.class);
        final SparkService sparkService = injector.getInstance(SparkService.class);
        sparkService.parseUOXml("src/main/resources/uo.xml", true);

        User user = new User();
        user.addMessenger(MessengerType.FACEBOOK, "2257721630906361", Locale.US);
        user = userService.save(user);
        fsmService.fire(user, Commands.MENU);
        fsmService.fire(user, Commands.VIEW);
        fsmService.fire(user, new ViewResource().setTo(Resource.UO));
        fsmService.fire(user, createMessage("9"));
        fsmService.fire(user, new Subscribe(Resource.UO, "9"));
        fsmService.fire(user, Commands.MENU);
        fsmService.fire(user, Commands.VIEW);
        fsmService.fire(user, new ViewResource().setTo(Resource.UO));
        fsmService.fire(user, createMessage("23"));
        fsmService.fire(user, new Subscribe(Resource.UO, "23"));
        fsmService.fire(user, Commands.MENU);
        fsmService.fire(user, Commands.VIEW);
        fsmService.fire(user, new ViewResource().setTo(Resource.UO));
        fsmService.fire(user, createMessage("15776"));
        fsmService.fire(user, new Subscribe(Resource.UO, "15776"));

        sparkService.parseUOXml("src/main/resources/uo_update.xml");
        Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS); //sleep for async event bus message handling

        fsmService.fire(user, Commands.MENU);
        fsmService.fire(user, Commands.VIEW);
        fsmService.fire(user, Commands.MENU);

        fsmService.fire(user, Commands.MENU);
        fsmService.fire(user, Commands.VIEW);
        fsmService.fire(user, new ViewResource().setTo(Resource.UO));
        fsmService.fire(user, Commands.MENU);

        fsmService.fire(user, Commands.SUBSCRIPTIONS);
        fsmService.fire(user, new ShowSubscriptions().setTo(Resource.UO));
        fsmService.fire(user, new ViewUO(Arrays.asList("23", "15776")));
        fsmService.fire(user, new ViewUO(Arrays.asList("15776")));
        fsmService.fire(user, Commands.MENU);

        fsmService.fire(user, Commands.SUBSCRIPTIONS);
        fsmService.fire(user, Commands.MENU);

        fsmService.fire(user, Commands.SUBSCRIPTIONS);
        fsmService.fire(user, new ShowSubscriptions().setTo(Resource.UO));
        fsmService.fire(user, new ViewUO(Arrays.asList("23", "15776")));
        fsmService.fire(user, new Unsubscribe(Resource.UO, "23"));
        fsmService.fire(user, new ViewUO(Arrays.asList("15776")));
        fsmService.fire(user, new Unsubscribe(Resource.UO, "15776"));
        fsmService.fire(user, Commands.MENU);

        fsmService.fire(user, Commands.VIEW);
        fsmService.fire(user, new ViewResource().setTo(Resource.UO));
        fsmService.fire(user, createMessage("9"));
        fsmService.fire(user, new Unsubscribe(Resource.UO, "9"));

        fsmService.fire(user, Commands.MENU);
        fsmService.fire(user, Commands.VIEW);
        fsmService.fire(user, new ViewResource().setTo(Resource.UO));
        fsmService.fire(user, createMessage("89798798456"));
        fsmService.fire(user, Commands.MENU);

        fsmService.fire(user, new ChangeLanguage().setTo(new Locale("uk", "ua")));

        fsmService.fire(user, Commands.SUBSCRIPTIONS);
        fsmService.fire(user, new ShowSubscriptions().setTo(Resource.UO));
        fsmService.fire(user, Commands.MENU);

        userService.delete(user.getId());
    }

    private MessagingItem createMessage(String text) {
        final MessagingItem messagingItem = new MessagingItem();
        final MessageItem messageItem = new MessageItem();
        messageItem.setText(text);
        messagingItem.setMessage(messageItem);
        return messagingItem;
    }

    @AfterClass
    public static void shutdown() {
        Main.shutdown(injector);
    }
}
