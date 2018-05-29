package com.bot.facebook.fsm.impl;

import com.bot.facebook.command.Command;
import com.bot.facebook.command.Commands;
import com.bot.facebook.command.impl.ChangeLanguage;
import com.bot.facebook.command.impl.Subscribe;
import com.bot.facebook.command.impl.Unsubscribe;
import com.bot.facebook.command.impl.ViewUO;
import com.bot.facebook.fsm.FSMService;
import com.bot.facebook.fsm.State;
import com.bot.facebook.service.FacebookService;
import com.core.models.User;
import com.core.service.UserService;
import com.core.util.MessengerType;
import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.github.oxo42.stateless4j.delegates.Func2;
import com.github.oxo42.stateless4j.delegates.Func3;
import com.github.oxo42.stateless4j.triggers.TriggerWithParameters1;
import com.github.oxo42.stateless4j.triggers.TriggerWithParameters2;
import com.google.inject.Inject;
import com.restfb.types.webhook.messaging.MessagingItem;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.Optional;
import java.util.function.Consumer;

import static com.core.util.RedisKeys.FACEBOOK_STATE;

/**
 * @author Taras Zubrei
 */
public class FSMServiceImpl implements FSMService {
    private final FacebookService facebookService;
    private final UserService userService;
    private final RedissonClient redisson;
    private final StateMachineConfig<State, String> config;

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
                .permitDynamic(inCaseOf(Commands.VIEW_UO), transitionTo(State.GET_UO_ID), facebookService::viewUO)
                .permitDynamic(inCaseOf(Commands.MENU), transitionTo(State.DEFAULT), facebookService::sendBasicMenu);
        config.configure(State.GET_UO_ID)
                .permitDynamic(inCaseOf(ViewUO.class), moveTo(State.VIEW_UO), (viewUO, user) -> facebookService.showUO(user, viewUO.getId()))
                .permitDynamic(inCaseOf(Commands.MENU), transitionTo(State.DEFAULT), facebookService::sendBasicMenu);
        config.configure(State.VIEW_UO)
                .permitDynamic(inCaseOf(Subscribe.class), moveTo(State.DEFAULT), (subscribe, user) -> {
                    userService.subscribe(user.getId(), subscribe.getResource(), subscribe.getId());
                    facebookService.sendBasicMenu(user);
                })
                .permitDynamic(inCaseOf(Unsubscribe.class), moveTo(State.DEFAULT), (unsubscribe, user) -> {
                    userService.unsubscribe(user.getId(), unsubscribe.getResource(), unsubscribe.getId());
                    facebookService.sendBasicMenu(user);
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
            if (machine.getState() == State.GET_UO_ID && StringUtils.isNotBlank(message.getMessage().getText()) && message.getMessage().getText().matches("\\d+")) {
                machine.fire(new TriggerWithParameters2<>(ViewUO.class.getName(), ViewUO.class, User.class), new ViewUO(message.getMessage().getText()), user);
            } else facebookService.unhandledMessage(user, message);
        });
    }

    private void fireCommand(User user, Consumer<StateMachine<State, String>> fire) {
        final RBucket<String> stateBucket = redisson.getBucket(String.format(FACEBOOK_STATE, user.getId()));
        final StateMachine<State, String> machine = new StateMachine<>(Optional.ofNullable(stateBucket.get()).map(State::valueOf).orElse(State.DEFAULT), config);
        fire.accept(machine);
        stateBucket.set(machine.getState().name());
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
