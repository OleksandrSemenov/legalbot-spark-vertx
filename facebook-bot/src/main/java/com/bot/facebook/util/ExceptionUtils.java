package com.bot.facebook.util;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Taras Zubrei
 */
public class ExceptionUtils {

    public static <T> T wrapException(Command<T> command) {
        try {
            return command.get();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @FunctionalInterface
    public interface Command<T> {
        T get() throws IOException, ClassNotFoundException;
    }
}
