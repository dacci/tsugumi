/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

/**
 * @author dacci
 */
public class TextSegment implements Segment {

  private String text;

  /**
   * @param text
   */
  public TextSegment(String text) {
    this.text = text;
  }

  /**
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * @param text the text to set
   */
  public void setText(String text) {
    this.text = text;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return text.toString();
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasChildren() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public StringBuilder toString(StringBuilder builder) {
    return builder.append(text);
  }

  /** {@inheritDoc} */
  @Override
  public int length() {
    return text.length();
  }

  /** {@inheritDoc} */
  @Override
  public char charAt(int index) {
    return text.charAt(index);
  }

  /** {@inheritDoc} */
  @Override
  public Segment subSequence(int start, int end) {
    return new TextSegment(text.substring(start, end));
  }
}
