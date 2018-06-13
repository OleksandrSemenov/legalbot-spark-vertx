package com.bot.facebook.message.command.impl;

import com.bot.facebook.command.Commands;
import com.bot.facebook.message.command.CommandParser;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author Taras Zubrei
 */
public class SimpleCommandParser implements CommandParser<Commands> {
    @Override
    public boolean applies(String payload) {
        return StringUtils.isNotBlank(payload)
                && Arrays.stream(Commands.values()).map(Enum::name).anyMatch(payload.toUpperCase()::equals);
    }

    @Override
    public Commands handleMessage(String payload) {
        return Commands.valueOf(payload.toUpperCase());
    }
}