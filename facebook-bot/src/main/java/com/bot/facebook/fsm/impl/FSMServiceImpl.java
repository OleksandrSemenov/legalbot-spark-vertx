package com.bot.facebook.fsm.impl;

import com.bot.facebook.command.Command;
import com.bot.facebook.command.Commands;
import com.bot.facebook.command.impl.*;
import com.bot.facebook.fsm.FSMService;
import com.bot.facebook.fsm.State;
import com.bot.facebook.service.FacebookService;
import com.core.models.User;
import com.core.service.UserService;
import com.core.util.MessengerType;
import com.core.util.Resource;
import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.github.oxo42.stateless4j.delegates.Func2;
import com.github.oxo42.stateless4j.delegates.Func3;
import com.github.oxo42.stateless4j.triggers.TriggerWithParameters1;
import com.github.oxo42.stateless4j.triggers.TriggerWithParameters2;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.restfb.types.webhook.messaging.MessagingItem;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.core.util.RedisKeys.FACEBOOK_STATE_TEMPLATE;

/**
 * @author Taras Zubrei
 */
public class FSMServiceImpl implements FSMService {
    private final Logger logger = LoggerFactory.getLogger(FSMServiceImpl.class);
    private final FacebookService facebookService;
    private final UserService userService;
    private final RedissonClient redisson;
    private final StateMachineConfig<State, String> config;
    private static final Map<Resource, State> VIEW_RESOURCE_STATE_MAP = ImmutableMap.of(
            Resource.UO, State.VIEW_UO
    );
    private static final Map<Resource, State> GET_RESOURCE_ID_STATE_MAP = ImmutableMap.of(
            Resource.UO, State.GET_UO_ID
    );

    @Inject
    public FSMServiceImpl(FacebookService facebookService, UserService userService, RedissonClient redisson) {
        this.facebookService = facebookService;
        this.userService = userService;
        this.config = new StateMachineConfig<>();
        this.redisson = redisson;
        configure();
    }

    private void configure() {
        config.configure(State.DEFAULT)
                .permitDynamic(inCaseOf(ChangeLanguage.class), moveTo(State.DEFAULT), (changeLanguage, user) -> {
                    user.getLocales().put(MessengerType.FACEBOOK, changeLanguage.getTo().toLanguageTag());
                    userService.save(user);
                    facebookService.sendBasicMenu(user);
                })
                .permitDynamic(inCaseOf(Commands.SUBSCRIPTIONS), transitionTo(State.SUBSCRIPTIONS), facebookService::viewSubscriptions)
                .permitDynamic(inCaseOf(Commands.VIEW), transitionTo(State.GET_RESOURCE), facebookService::viewResources)
                .permitDynamic(inCaseOf(Commands.MENU), transitionTo(State.DEFAULT), facebookService::sendBasicMenu);
        config.configure(State.GET_RESOURCE)
                .permitDynamic(inCaseOf(ViewResource.class), (command, user) -> GET_RESOURCE_ID_STATE_MAP.get(command.getTo()), (command, user) -> {
                    if (command.getTo() == Resource.UO)
                        facebookService.viewUO(user);
                })
                .permitDynamic(inCaseOf(Commands.MENU), transitionTo(State.DEFAULT), facebookService::sendBasicMenu);
        config.configure(State.GET_UO_ID)
                .permitDynamic(inCaseOf(ViewUO.class), moveTo(State.VIEW_UO), facebookService::showUO)
                .permitDynamic(inCaseOf(Commands.MENU), transitionTo(State.DEFAULT), facebookService::sendBasicMenu);
        config.configure(State.VIEW_UO)
                .permitDynamic(inCaseOf(Subscribe.class), moveTo(State.VIEW_UO), (subscribe, user) -> {
                    userService.subscribe(user.getId(), subscribe.getResource(), subscribe.getId());
                })
                .permitDynamic(inCaseOf(Unsubscribe.class), moveTo(State.VIEW_UO), (unsubscribe, user) -> {
                    userService.unsubscribe(user.getId(), unsubscribe.getResource(), unsubscribe.getId());
                })
                .permitDynamic(inCaseOf(ViewUO.class), moveTo(State.VIEW_UO), facebookService::showUO)
                .permitDynamic(inCaseOf(Commands.MENU), transitionTo(State.DEFAULT), facebookService::sendBasicMenu);
        config.configure(State.SUBSCRIPTIONS)
                .permitDynamic(inCaseOf(ShowSubscriptions.class), (command, user) -> VIEW_RESOURCE_STATE_MAP.get(command.getTo()), (command, user) -> {
                    final List<String> subscriptions = userService.findSubscriptions(user.getId()).get(command.getTo());
                    facebookService.showSubscriptions(user, command.getTo(), subscriptions);
                })
                .permitDynamic(inCaseOf(Commands.MENU), transitionTo(State.DEFAULT), facebookService::sendBasicMenu);
    }

    @Override
    public <T extends Command> void fire(User user, T command) {
        fireCommand(user, machine -> machine.fire(new TriggerWithParameters2<>(command.getClass().getName(), (Class<T>) command.getClass(), User.class), command, user));
    }

    @Override
    public void fire(User user, Commands command) {
        fireCommand(user, machine -> machine.fire(new TriggerWithParameters1<>(command.toString(), User.class), user));
    }

    @Override
    public void fire(User user, MessagingItem message) {
        fireCommand(user, machine -> {
            final String text = message.getMessage().getText();
            if (machine.getState() == State.GET_UO_ID && StringUtils.isNotBlank(text) && text.matches("[\\d, ]+")) {
                final List<String> ids = Arrays.stream(text.split(",")).map(String::trim).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                machine.fire(new TriggerWithParameters2<>(ViewUO.class.getName(), ViewUO.class, User.class), new ViewUO(ids), user);
            } else {
                facebookService.unhandledMessage(user, message);
                throw new IllegalStateException("Unhandled message");
            }
        });
    }

    private void fireCommand(User user, Consumer<StateMachine<State, String>> fire) {
        final RBucket<String> stateBucket = redisson.getBucket(String.format(FACEBOOK_STATE_TEMPLATE, user.getId()));
        final StateMachine<State, String> machine = new StateMachine<>(Optional.ofNullable(stateBucket.get()).map(State::valueOf).orElse(State.DEFAULT), config);
        try {
            fire.accept(machine);
        } catch (Throwable t) {
            machine.fire(new TriggerWithParameters1<>(Commands.MENU.toString(), User.class), user);
        }
        stateBucket.set(machine.getState().name());
        logger.info("Change state to: {}", machine.getState());
    }

    private static <T extends Command> TriggerWithParameters2<T, User, String> inCaseOf(Class<T> command) {
        return new TriggerWithParameters2<>(command.getName(), command, User.class);
    }

    private static TriggerWithParameters1<User, String> inCaseOf(Commands command) {
        return new TriggerWithParameters1<>(command.toString(), User.class);
    }

    private static <TArg0, TArg1> Func3<TArg0, TArg1, State> moveTo(State state) {
        return (__, ___) -> state;
    }

    private static <TArg0> Func2<TArg0, State> transitionTo(State state) {
        return __ -> state;
    }
}
