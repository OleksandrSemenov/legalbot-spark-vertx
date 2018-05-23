package com.bot.facebook.command;

/**
 * @author Taras Zubrei
 */
public class CommandWrapper<T extends Command> {
    private Class<T> type;
    private T value;

    public CommandWrapper() {
    }

    public CommandWrapper(T value) {
        this.type = (Class<T>) value.getClass();
        this.value = value;
    }

    public Class<T> getType() {
        return type;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
