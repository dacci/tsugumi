/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.format.aozora;

import org.dacci.tsugumi.format.Format;
import org.dacci.tsugumi.format.FormatFactory;

/**
 * @author dacci
 */
public class AozoraFormatFactory implements FormatFactory {

  /** {@inheritDoc} */
  @Override
  public Format newInstance() {
    return new AozoraFormat();
  }
}
