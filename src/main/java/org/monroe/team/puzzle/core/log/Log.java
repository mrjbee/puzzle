package org.monroe.team.puzzle.core.log;

import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

public class Log {

    private org.slf4j.Logger implementation;

    public Log(String pieceTag) {
        this(LoggerFactory.getLogger("piece."+pieceTag));
    }

    Log(final org.slf4j.Logger logger) {
        implementation = logger;
    }

    public void warn(final String msgTemplate, final Object ...  args) {
        implementation.warn(msgTemplate, args);
    }

    public void warn(final Throwable ex, final String msgTemplate, final Object ...  args) {
        try {
            implementation.warn(MessageFormatter.format(msgTemplate, args).getMessage(), ex);
        } catch (RuntimeException e){
            implementation.warn(msgTemplate, ex);
            implementation.warn("Log fallback", e);
        }
    }

    public void info(final String message, final Object... args) {
        implementation.info(message, args);
    }
}
