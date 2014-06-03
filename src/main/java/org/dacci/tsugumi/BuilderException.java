/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi;

/**
 * @author dacci
 */
@SuppressWarnings("serial")
public class BuilderException extends Exception {

    /**
     * 
     */
    public BuilderException() {
    }

    /**
     * @param message
     */
    public BuilderException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public BuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public BuilderException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    protected BuilderException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
