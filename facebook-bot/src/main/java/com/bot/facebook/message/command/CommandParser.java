package com.bot.facebook.message.command;

/**
 * @author Taras Zubrei
 */
public interface CommandParser<T> {
    boolean applies(String payload);

    T handleMessage(String payload);
}