/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.dacci.tsugumi.doc.Book;
import org.dacci.tsugumi.format.BuildException;
import org.dacci.tsugumi.format.Format;
import org.dacci.tsugumi.format.FormatFactory;
import org.dacci.tsugumi.format.ParseException;
import org.dacci.tsugumi.format.aozora.AozoraFormatFactory;
import org.dacci.tsugumi.format.epub.EPubFormatFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * @author dacci
 */
public final class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final String OPTION_HELP = "h";

    private static final String OPTION_DIRECTORY = "d";

    private static final String OPTION_VERBOSE = "v";

    private static FormatFactory parserFactory = new AozoraFormatFactory();

    private static FormatFactory builderFactory = new EPubFormatFactory();

    private static CommandLine commandLine;

    /**
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        Options options = new Options();

        options.addOption(OPTION_HELP, "help", false, "Show help message.");
        options.addOption(OPTION_DIRECTORY, "directory", false,
                "Output as a directory.");
        options.addOption(OPTION_VERBOSE, "verbose", false,
                "Increase verbosity.");

        try {
            commandLine = new GnuParser().parse(options, args);

            if (commandLine.getArgList().size() == 0 ||
                    commandLine.hasOption(OPTION_HELP)) {
                new HelpFormatter().printHelp(
                        "tsugumi [OPTIONS] FILE [FILE...]", options);
                return;
            }

            if (commandLine.hasOption(OPTION_VERBOSE)) {
                ch.qos.logback.classic.Logger rootLogger =
                        (ch.qos.logback.classic.Logger) LoggerFactory
                                .getLogger(Logger.ROOT_LOGGER_NAME);
                rootLogger.setLevel(Level.DEBUG);
            }

            for (String arg : (List<String>) commandLine.getArgList()) {
                processFile(Paths.get(arg));
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param path
     */
    private static void processFile(Path path) {
        try {
            LOG.info("Begin parsing {} . . .", path);
            Book book = parserFactory.newInstance().parse(path);

            LOG.info("Building book . . .");
            Format builder = builderFactory.newInstance();
            builder.setProperty(Format.OUTPUT_PATH, path.getParent());

            builder.build(book);
            LOG.info("Done!");
        } catch (ParseException e) {
            LOG.error("Parse error", e);
        } catch (BuildException e) {
            LOG.error("Build error", e);
        }
    }

    private Main() {
    }
}
