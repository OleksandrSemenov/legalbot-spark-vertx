package com.bot.facebook.command.impl;

import com.bot.facebook.command.Command;
import com.core.util.Resource;

/**
 * @author Taras Zubrei
 */
public class ShowSubscriptions implements Command {
    private Resource to;

    public Resource getTo() {
        return to;
    }

    public ShowSubscriptions setTo(Resource to) {
        this.to = to;
        return this;
    }
}