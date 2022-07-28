/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

/**
 * @author dacci
 */
public abstract class BookElement {

  private Block parent = null;

  /**
   * @return the parent
   */
  public Block getParent() {
    return parent;
  }

  /**
   * @param parent the parent to set
   */
  void setParent(Block parent) {
    this.parent = parent;
  }
}
