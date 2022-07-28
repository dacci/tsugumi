/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author dacci
 */
public class EndMarginStyle implements Style, Cloneable {

  private int width;

  /**
   * @param width
   */
  public EndMarginStyle(int width) {
    this.width = width;
  }

  /**
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * @param width the width to set
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /** {@InheritDoc} */
  @Override
  public Style copy() {
    try {
      return (EndMarginStyle) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new Error(e);
    }
  }
}
