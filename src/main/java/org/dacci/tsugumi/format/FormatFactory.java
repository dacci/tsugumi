/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.format;

/**
 * @author dacci
 */
public interface FormatFactory {

    Format newInstance();

    default void setOption(String key, Object value) {
    }
}
