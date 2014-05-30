/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.dacci.tsugumi.doc.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dacci
 */
public final class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        for (String arg : args) {
            Path path = Paths.get(arg).toAbsolutePath();
            Book book = null;

            log.info("Start building {}", path);

            try (AozoraParser parser = new AozoraParser(path)) {
                book = parser.parse();
            } catch (IOException | ParserException e) {
                log.error("Failed to parse", e);
                return;
            }

            try {
                EPubBuilder builder = new EPubBuilder();
                builder.build(book, path);
            } catch (BuilderException e) {
                log.error("Failed to build", e);
                return;
            }

            log.info("Building complete");
        }
    }

    private Main() {
    }
}
