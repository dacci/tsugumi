/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi;

/**
 * @author dacci
 */
@SuppressWarnings("serial")
public class ParserException extends Exception {

    private final int line;

    /**
     * @param line
     */
    public ParserException(int line) {
        super();
        this.line = line;
    }

    /**
     * @param line
     * @param message
     */
    public ParserException(int line, String message) {
        super(message);
        this.line = line;
    }

    /**
     * @param line
     * @param message
     * @param cause
     */
    public ParserException(int line, String message, Throwable cause) {
        super(message, cause);
        this.line = line;
    }

    /**
     * @param line
     * @param cause
     */
    public ParserException(int line, Throwable cause) {
        super(cause);
        this.line = line;
    }

    /**
     * @param line
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    protected ParserException(int line, String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.line = line;
    }

    /**
     * @return the line
     */
    public int getLine() {
        return line;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " at line " + line;
    }
}
