/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

/**
 * @author dacci
 */
public class RubySegment implements Segment {

  private Segment text;

  private String ruby;

  /**
   * @param text
   * @param ruby
   */
  public RubySegment(Segment text, String ruby) {
    this.text = text;
    this.ruby = ruby;
  }

  /**
   * @return the text
   */
  public Segment getText() {
    return text;
  }

  /**
   * @param text the text to set
   */
  public void setText(Segment text) {
    this.text = text;
  }

  /**
   * @return the ruby
   */
  public String getRuby() {
    return ruby;
  }

  /**
   * @param ruby the ruby to set
   */
  public void setRuby(String ruby) {
    this.ruby = ruby;
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasChildren() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public StringBuilder toString(StringBuilder builder) {
    return text.toString(builder);
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
    if (start != 0 || end != text.length()) {
      throw new IllegalArgumentException();
    }

    return this;
  }
}
