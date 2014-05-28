/*
 * Copyright (c) 2014 NTT DATA SMS Corporation.
 */

package org.dacci.tsugumi;

/**
 * @author tsudasn
 */
@SuppressWarnings("serial")
public class BuilderException extends Exception {

    /**
     * 
     */
    public BuilderException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public BuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public BuilderException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public BuilderException(Throwable cause) {
        super(cause);
    }
}
