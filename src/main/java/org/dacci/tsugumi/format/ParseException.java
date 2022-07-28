/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.format;

/**
 * @author dacci
 */
@SuppressWarnings("serial")
public class ParseException extends Exception {

  private final int line;

  /**
   * @param message
   */
  public ParseException(int line, String message) {
    super(message);
    this.line = line;
  }

  /**
   * @param cause
   */
  public ParseException(int line, Throwable cause) {
    super(cause);
    this.line = line;
  }

  /**
   * @param message
   * @param cause
   */
  public ParseException(int line, String message, Throwable cause) {
    super(message, cause);
    this.line = line;
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public ParseException(
      int line,
      String message,
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.line = line;
  }

  /**
   * @return the line
   */
  public int getLine() {
    return line;
  }

  /** {@inheritDoc} */
  @Override
  public String getMessage() {
    return super.getMessage() + " on line " + line;
  }
}
