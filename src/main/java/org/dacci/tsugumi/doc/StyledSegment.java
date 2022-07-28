/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author dacci
 */
public class StyledSegment implements Segment {

  private Segment segment;

  private final Set<Style> styles = new LinkedHashSet<>();

  /**
   * @param segment
   */
  public StyledSegment(Segment segment) {
    this.segment = segment;
  }

  /**
   * @param segment
   * @param styles
   */
  private StyledSegment(Segment segment, Set<Style> styles) {
    this.segment = segment;

    for (Style style : styles) {
      this.styles.add(style.copy());
    }
  }

  /**
   * @return the segment
   */
  public Segment getSegment() {
    return segment;
  }

  /**
   * @param segment the segment to set
   */
  public void setSegment(Segment segment) {
    this.segment = segment;
  }

  /**
   * @param style
   * @return
   */
  public boolean addStyle(Style style) {
    if (style == null) {
      throw new NullPointerException();
    }

    return styles.add(style);
  }

  /** */
  public void clearStyles() {
    styles.clear();
  }

  /**
   * @return
   */
  public Collection<Style> styles() {
    return Collections.unmodifiableCollection(styles);
  }

  /** {@InheritDoc} */
  @Override
  public int length() {
    return segment.length();
  }

  /** {@InheritDoc} */
  @Override
  public char charAt(int index) {
    return segment.charAt(index);
  }

  /** {@InheritDoc} */
  @Override
  public boolean hasChildren() {
    return segment.hasChildren();
  }

  /** {@InheritDoc} */
  @Override
  public StringBuilder toString(StringBuilder builder) {
    return segment.toString(builder);
  }

  /** {@InheritDoc} */
  @Override
  public Segment subSequence(int start, int end) {
    return new StyledSegment(segment.subSequence(start, end), styles);
  }
}
