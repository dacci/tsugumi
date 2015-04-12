/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.format;

import java.nio.file.Path;

import org.dacci.tsugumi.doc.Book;

/**
 * @author dacci
 */
public interface Format {

    String OUTPUT_PATH = "OutputPath";

    default void setProperty(String key, Object value) {
    }

    boolean isParseSupported();

    Book parse(Path path) throws ParseException;

    boolean isBuildSupported();

    Path build(Book book) throws BuildException;
}
