package com.spark.util;

import com.bot.facebook.FacebookModule;
import com.core.models.User;
import com.core.service.UserService;
import com.core.util.MessengerType;
import com.core.util.Resource;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.spark.GuiceModule;
import com.spark.Main;
import com.spark.repository.UORepository;
import com.spark.service.SparkService;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author Taras Zubrei
 */
public class SparkUtilTest {
    private static final Logger logger = LoggerFactory.getLogger(SparkUtilTest.class);
    private static final Injector injector = Guice.createInjector(new GuiceModule(), new FacebookModule());

    @Test
    public void uo() {
        Main.configureVertx(injector);
        Main.configureMorphia(injector);
        final UserService userService = injector.getInstance(UserService.class);
        final UORepository uoRepository = injector.getInstance(UORepository.class);
        final SparkService sparkService = injector.getInstance(SparkService.class);

        Stream.of("0", "9").forEach(uoRepository::delete);
        User user = new User();
        user.addMessenger(MessengerType.FACEBOOK, "social id", Locale.US);
        user = userService.save(user);
        userService.subscribe(user.getId(), Resource.UO, "0");
        userService.subscribe(user.getId(), Resource.UO, "9");

        sparkService.parseUOXml("src/main/resources/uo.xml", true);
        logger.info("--------------Updated data--------------");
        sparkService.parseUOXml("src/main/resources/uo_update.xml");

        Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS); //sleep for async event bus message handling
        userService.delete(user.getId());
    }

    @AfterClass
    public static void shutdown() {
        Main.shutdown(injector);
    }
}
