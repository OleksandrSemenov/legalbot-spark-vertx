package com.bot.facebook.command.impl;

import com.bot.facebook.command.Command;

import java.util.List;

/**
 * @author Taras Zubrei
 */
public class ViewUO implements Command {
    private String id;
    private List<String> next;

    public ViewUO() {
    }

    public ViewUO(String id) {
        this.id = id;
    }

    public ViewUO(List<String> ids) {
        this.id = ids.get(0);
        if (ids.size() > 1)
            this.next = ids.subList(1, ids.size());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasNext() {
        return next != null && !next.isEmpty();
    }

    public List<String> getNext() {
        return next;
    }

    public void setNext(List<String> next) {
        this.next = next;
    }
}