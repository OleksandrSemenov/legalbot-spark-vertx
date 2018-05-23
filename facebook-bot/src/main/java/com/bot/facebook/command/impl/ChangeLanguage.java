package com.bot.facebook.command.impl;

import com.bot.facebook.command.Command;

import java.util.Locale;

/**
 * @author Taras Zubrei
 */
public class ChangeLanguage implements Command {
    private Locale to;

    public Locale getTo() {
        return to;
    }

    public ChangeLanguage setTo(Locale to) {
        this.to = to;
        return this;
    }
}
