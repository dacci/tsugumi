/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.format.epub;

import org.dacci.tsugumi.format.Format;
import org.dacci.tsugumi.format.FormatFactory;

/**
 * @author dacci
 */
public class EPubFormatFactory implements FormatFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public Format newInstance() {
        return new EPubFormat();
    }
}
