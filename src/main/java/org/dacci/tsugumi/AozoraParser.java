/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dacci.tsugumi.doc.Book;
import org.dacci.tsugumi.doc.Image;
import org.dacci.tsugumi.doc.Page;
import org.dacci.tsugumi.doc.Paragraph;
import org.dacci.tsugumi.doc.Ruby;
import org.dacci.tsugumi.doc.Section;
import org.dacci.tsugumi.doc.Text;

/**
 * @author dacci
 */
public class AozoraParser implements Closeable {

    private static final Pattern TAG_PATTERN = Pattern.compile("［＃(.+?)］");

    private static final Pattern ANNOTATION_PATTERN = Pattern
            .compile("(.+?)［＃「\\1」[には](.+?)］");

    private static final Pattern START_TAG_PATTERN = Pattern
            .compile("ここから(.+)");

    private static final Pattern END_TAG_PATTERN = Pattern
            .compile("ここで(.+)終わり");

    private static final Pattern IMAGE_TAG_PATTERN = Pattern
            .compile("(.*)（(.+)(、縦(\\d+)×横(\\d+))?）(入る)?");

    private static final Pattern RUBY_PATTERN = Pattern
            .compile("(?:｜(.+?))?《(.+?)》");

    private final Path source;

    private final BufferedReader reader;

    private int row = 0;

    private Deque<String> stack = new LinkedList<>();

    private Deque<Paragraph> context = new LinkedList<>();

    private Book book = new Book();

    private Page page;

    /**
     * @throws IOException
     */
    public AozoraParser(Path file) throws IOException {
        source = file;

        reader = Files.newBufferedReader(file, Charset.forName("MS932"));
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * @return
     * @throws IOException
     */
    public Book parse() throws ParserException {
        try {
            List<String> metaData = new ArrayList<>();
            String line = null;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }

                metaData.add(line);
            }

            book.setTitle(metaData.get(0));

            switch (metaData.size()) {
            case 2:
                book.setAuthor(metaData.get(1));
                break;

            case 3:
                book.setSubTitle(metaData.get(1));
                book.setAuthor(metaData.get(2));
                break;

            case 4:
                book.setOriginalTitle(metaData.get(1));
                book.setAuthor(metaData.get(2));
                book.setTranslator(metaData.get(3));
                break;

            case 6:
                book.setOriginalTitle(metaData.get(1));
                book.setSubTitle(metaData.get(2));
                book.setOriginalSubTitle(metaData.get(3));
                book.setAuthor(metaData.get(4));
                book.setTranslator(metaData.get(5));
                break;

            default:
                throw new ParserException("line: " + row
                        + ", illegal meta data count: " + metaData.size());
            }

            stack.clear();
            context.clear();
            context.push(page = book.addPage());

            while ((line = reader.readLine()) != null) {
                ++row;

                if (line.length() > 1 && line.charAt(0) == '　'
                        && line.charAt(1) != '　') {
                    line = line.substring(1);
                }

                Matcher matcher = TAG_PATTERN.matcher(line);
                if (matcher.lookingAt()) {
                    parseTag(line, matcher);
                } else {
                    context.push(context.peek().addParagraph());
                    parseLine(line);
                    context.pop();
                }
            }

            return book;
        } catch (IOException | RuntimeException e) {
            throw new ParserException(e.getMessage() + " at line " + row, e);
        }
    }

    /**
     * @param line
     * @param tagMatcher
     * @throws ParserException
     */
    private void parseTag(String line, Matcher tagMatcher)
            throws ParserException {
        String tag = tagMatcher.group(1);

        switch (tag) {
        case "改ページ":
            stack.clear();
            context.clear();
            context.push(page = book.addPage());
            return;

        case "小見出し":
            context.push(context.peek().addCaption());
            parseLine(line.substring(tagMatcher.end()));
            context.pop();
            return;

        case "地付き":
            context.push(context.peek().addParagraph());
            parseLine(line.substring(tagMatcher.end()));
            context.pop().setStyle("align-end");
            return;
        }

        Matcher matcher = null;

        matcher = START_TAG_PATTERN.matcher(tag);
        if (matcher.matches()) {
            String type = matcher.group(1);
            stack.push(type);

            Paragraph paragraph = context.peek().addParagraph();
            context.push(paragraph);

            if (type.equals("ゴシック体")) {
                paragraph.setStyle("gfont");
            } else if (type.endsWith("字下げ")) {
                int amount = parseInt(type, 0, type.length() - 3);
                paragraph.setStyle("start-" + amount + "em");
            } else {
                System.err.println("unsupported tag: " + type);
            }

            return;
        }

        matcher = END_TAG_PATTERN.matcher(tag);
        if (matcher.matches()) {
            String start = matcher.group(1);
            String end = stack.pop();
            if (!end.endsWith(start)) {
                throw new ParserException("line: " + row + ", unmatched tag: "
                        + end + " vs " + start);
            }

            context.pop();

            return;
        }

        matcher = IMAGE_TAG_PATTERN.matcher(tag);
        if (matcher.matches()) {
            Path path = source.resolveSibling(matcher.group(2));
            if (!Files.exists(path)) {
                throw new ParserException(new FileNotFoundException(
                        path.toString()));
            }

            Image image = book.createImage(path);

            if (matcher.start(1) > 0) {
                image.setCaption(matcher.group(1));
            }

            page.addParagraph().addElement(image);

            return;
        }

        System.err.println(line);

        if (!line.isEmpty()) {
            page.addParagraph().addElement(new Text(line));
        }
    }

    /**
     * @param line
     */
    private void parseLine(String line) {
        SortedMap<Integer, Section> elements = new TreeMap<>();
        NavigableMap<Integer, Integer> slices = new TreeMap<>();
        slices.put(0, line.length());

        Matcher matcher = RUBY_PATTERN.matcher(line);
        while (matcher.find()) {
            Ruby ruby = new Ruby();
            int start = matcher.start();

            if (matcher.start(1) == -1) {
                int end = matcher.start(2) - 1;
                start = end - 1;
                UnicodeBlock block = UnicodeBlock.of(line.codePointAt(start));
                for (; start >= 0; --start) {
                    if (start == 0) {
                        break;
                    }

                    if (UnicodeBlock.of(line.codePointAt(start - 1)) != block) {
                        break;
                    }
                }

                ruby.setText(line.substring(start, end));
            } else {
                ruby.setText(matcher.group(1));
            }

            ruby.setRuby(matcher.group(2));
            elements.put(start, ruby);

            Integer oldStart = slices.floorKey(start);
            Integer oldEnd = slices.put(oldStart, start);
            slices.put(matcher.end(), oldEnd);
        }

        matcher = ANNOTATION_PATTERN.matcher(line);
        while (matcher.find()) {
            Text text = new Text(matcher.group(1));
            String type = matcher.group(2);

            switch (type) {
            case "ゴシック体":
                text.setStyle("gfont");
                break;

            case "傍点":
                text.setStyle("em-sesame");
                break;

            default:
                System.err.println("unknown annotation: " + type);
            }

            Integer start = matcher.start();
            elements.put(start, text);

            Integer oldStart = slices.floorKey(start);
            Integer oldEnd = slices.put(oldStart, start);
            slices.put(matcher.end(), oldEnd);
        }

        for (Map.Entry<Integer, Integer> slice : slices.entrySet()) {
            if (!slice.getKey().equals(slice.getValue())) {
                String text = line.substring(slice.getKey(), slice.getValue());
                elements.put(slice.getKey(), new Text(text));
            }
        }

        for (Section element : elements.values()) {
            context.peek().addElement(element);
        }
    }

    /**
     * @param string
     * @param start
     * @param end
     * @return
     */
    private int parseInt(String string, int start, int end) {
        String digits = "０１２３４５６７８９";
        int result = 0;

        for (int i = start; i < end; ++i) {
            result *= 10;

            int digit = digits.indexOf(string.codePointAt(i));
            if (digit < 0) {
                throw new NumberFormatException();
            }

            result += digit;
        }

        return result;
    }
}
