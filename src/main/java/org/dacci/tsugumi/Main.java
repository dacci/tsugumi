/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dacci.tsugumi.doc.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dacci
 */
public final class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("d", "directory", false, "Save as directory.");
        options.addOption("h", "horizontal", false,
                "Output book witten horizontally.");

        try {
            CommandLine commandLine = new GnuParser().parse(options, args);
            main(commandLine);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param commandLine
     */
    @SuppressWarnings("unchecked")
    private static void main(CommandLine commandLine) {
        EPubBuilder builder;
        try {
            builder = new EPubBuilder();

            if (commandLine.hasOption("horizontal")) {
                builder.setOption(EPubOption.Direction, EPubOption.LeftToRight);
            } else {
                builder.setOption(EPubOption.Direction, EPubOption.RightToLeft);
            }
        } catch (BuilderException e) {
            log.error("Failed to setup builder", e);
            return;
        }

        for (String arg : (List<String>) commandLine.getArgList()) {
            Path path = Paths.get(arg);
            Book book = null;

            log.info("Start building {}", path);

            try (AozoraParser parser = new AozoraParser(path)) {
                book = parser.parse();
            } catch (IOException | ParserException e) {
                log.error("Failed to parse", e);
                return;
            }

            try {
                builder.build(book);

                String name = book.getAuthor() + " - " + book.getTitle();

                if (commandLine.hasOption("directory")) {
                    builder.saveToDirectory(path.resolveSibling(name));
                } else {
                    builder.saveToFile(path.resolveSibling(name + ".epub"));
                }
            } catch (BuilderException | IOException e) {
                log.error("Failed to build", e);
                return;
            }

            log.info("Building complete");
        }
    }

    private Main() {
    }
}
