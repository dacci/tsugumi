/*
 * Copyright (c) 2014 NTT DATA SMS Corporation.
 */

package org.dacci.tsugumi;

/**
 * @author tsudasn
 */
@SuppressWarnings("serial")
public class ParserException extends Exception {

    /**
     * 
     */
    public ParserException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public ParserException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ParserException(Throwable cause) {
        super(cause);
    }
}
