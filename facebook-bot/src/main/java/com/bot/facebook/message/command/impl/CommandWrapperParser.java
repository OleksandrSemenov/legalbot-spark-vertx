package com.bot.facebook.message.command.impl;

import com.bot.facebook.command.Command;
import com.bot.facebook.message.command.CommandParser;
import com.bot.facebook.util.ExceptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Taras Zubrei
 */
public class CommandWrapperParser implements CommandParser<Command> {
    private final ObjectMapper mapper;

    @Inject
    public CommandWrapperParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean applies(String payload) {
        return StringUtils.isNotBlank(payload) && payload.startsWith("{");
    }

    @Override
    public Command handleMessage(String payload) {
        return (Command) ExceptionUtils.wrapException(() -> mapper.readValue(
                mapper.readTree(payload).get("value").toString(),
                Class.forName(mapper.readTree(payload).get("type").asText())));
    }
}