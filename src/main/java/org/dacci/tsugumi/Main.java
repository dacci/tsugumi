/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.dacci.tsugumi.doc.Book;

/**
 * @author dacci
 */
public final class Main {

    public static void main(String[] args) {
        for (String arg : args) {
            Path path = Paths.get(arg).toAbsolutePath();
            Book book = null;

            try (AozoraParser parser = new AozoraParser(path)) {
                book = parser.parse();
            } catch (IOException | ParserException e) {
                System.err.println("Failed to parse " + arg);
                e.printStackTrace();
                return;
            }

            try {
                EPubBuilder builder = new EPubBuilder();
                builder.build(book, path);
            } catch (BuilderException e) {
                System.err.println("Failed to build " + arg);
                e.printStackTrace();
                return;
            }
        }
    }

    private Main() {
    }
}
