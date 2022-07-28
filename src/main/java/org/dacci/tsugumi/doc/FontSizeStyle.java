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
public class FontSizeStyle implements Style, Cloneable {

  private int level;

  /**
   * @param level
   */
  public FontSizeStyle(int level) {
    this.level = level;
  }

  /**
   * @return the level
   */
  public int getLevel() {
    return level;
  }

  /**
   * @param level the level to set
   */
  public void setLevel(int level) {
    this.level = level;
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
      return (FontSizeStyle) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new Error(e);
    }
  }
}
