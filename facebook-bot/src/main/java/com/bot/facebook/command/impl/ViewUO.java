package com.bot.facebook.command.impl;

import com.bot.facebook.command.Command;

/**
 * @author Taras Zubrei
 */
public class ViewUO implements Command {
    private final String id;

    public ViewUO(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
