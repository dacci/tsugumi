/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.format;

/**
 * @author dacci
 */
@SuppressWarnings("serial")
public class BuildException extends Exception {

  /** */
  public BuildException() {}

  /**
   * @param message
   */
  public BuildException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public BuildException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public BuildException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public BuildException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
