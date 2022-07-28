/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

/**
 * @author dacci
 */
public interface Segment extends CharSequence {

  boolean hasChildren();

  StringBuilder toString(StringBuilder builder);

  /** {@inheritDoc} */
  @Override
  Segment subSequence(int start, int end);
}
