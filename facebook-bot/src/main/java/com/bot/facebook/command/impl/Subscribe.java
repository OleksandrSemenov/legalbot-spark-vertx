package com.bot.facebook.command.impl;

import com.bot.facebook.command.Command;
import com.core.util.Resource;

/**
 * @author Taras Zubrei
 */
public class Subscribe implements Command {
    private Resource resource;
    private String id;

    public Subscribe() {
    }

    public Subscribe(Resource resource, String id) {
        this.resource = resource;
        this.id = id;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
