/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi;

import java.io.BufferedReader;
import java.io.Closeable;
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
import org.dacci.tsugumi.doc.Caption;
import org.dacci.tsugumi.doc.Image;
import org.dacci.tsugumi.doc.ImageItem;
import org.dacci.tsugumi.doc.Page;
import org.dacci.tsugumi.doc.Paragraph;
import org.dacci.tsugumi.doc.Ruby;
import org.dacci.tsugumi.doc.Section;
import org.dacci.tsugumi.doc.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dacci
 */
public class AozoraParser implements Closeable {

    private static final Logger log = LoggerFactory
            .getLogger(AozoraParser.class);

    private static final Pattern TAG_PATTERN = Pattern.compile("［＃(.+?)］");

    private static final Pattern ANNOTATION_PATTERN = Pattern
            .compile("(.+?)［＃「\\1」[には](.+?)］");

    private static final Pattern START_TAG_PATTERN = Pattern
            .compile("ここから(.+)");

    private static final Pattern END_TAG_PATTERN = Pattern
            .compile("ここで(.+)終わり");

    private static final Pattern IMAGE_TAG_PATTERN = Pattern
            .compile("(.*)（(.+)(?:、縦(\\d+)×横(\\d+))?）(入る)?");

    private static final Pattern META_DATA_PATTERN = Pattern
            .compile("(.+?)＝(.+)");

    private static final Pattern RUBY_PATTERN = Pattern
            .compile("(?:｜(.+?))?《(.+?)》");

    private final Book book;

    private final BufferedReader reader;

    private int row = 0;

    private Deque<String> stack = new LinkedList<>();

    private Deque<Paragraph> context = new LinkedList<>();

    private Page page;

    /**
     * @throws IOException
     */
    public AozoraParser(Path file) throws IOException {
        book = new Book(file.resolveSibling(""));
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
                throw new ParserException("line: " + row +
                        ", illegal meta data count: " + metaData.size());
            }

            stack.clear();
            context.clear();
            page = book.addPage();
            context.push(page.getParagraph());

            while ((line = reader.readLine()) != null) {
                ++row;

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
            page = book.addPage();
            context.push(page.getParagraph());
            return;

        case "大見出し":
        case "中見出し":
        case "小見出し":
            Caption caption = context.peek().addCaption();
            switch (tag.charAt(0)) {
            case '大':
                caption.setLevel(1);
                break;

            case '中':
                caption.setLevel(2);
                break;

            case '小':
                caption.setLevel(3);
                break;
            }

            context.push(caption);
            parseLine(line.substring(tagMatcher.end()));
            context.pop();
            return;

        case "地付き":
            context.push(context.peek().addParagraph());
            context.peek().setStyle("align-end");
            parseLine(line.substring(tagMatcher.end()));
            context.pop();
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
                int amount = parseInt(type, 0, type.length() - 3) / 2;
                paragraph.setStyle("start-" + amount + "em");
            } else {
                log.warn("Unsupported block: {}", type);
            }

            return;
        }

        matcher = END_TAG_PATTERN.matcher(tag);
        if (matcher.matches()) {
            String start = matcher.group(1);
            String end = stack.pop();
            if (!end.endsWith(start)) {
                throw new ParserException("line: " + row + ", unmatched tag: " +
                        end + " vs " + start);
            }

            context.pop();

            return;
        }

        matcher = IMAGE_TAG_PATTERN.matcher(tag);
        if (matcher.matches()) {
            String caption = matcher.group(1);
            ImageItem imageItem = book.importImage(matcher.group(2));

            if (caption != null) {
                switch (caption) {
                case "表紙":
                    book.setCoverImage(imageItem);
                    return;
                }
            }

            Image image = new Image(imageItem);
            image.setCaption(caption);

            if (matcher.start(3) > 0) {
                image.setWidth(Integer.parseInt(matcher.group(3)));
            }

            if (matcher.start(4) > 0) {
                image.setHeight(Integer.parseInt(matcher.group(4)));
            }

            context.peek().addParagraph().addElement(image);

            return;
        }

        matcher = META_DATA_PATTERN.matcher(tag);
        if (matcher.matches()) {
            String key = matcher.group(1);
            String value = matcher.group(2);

            switch (key) {
            case "タイトル":
                page.setTitle(value);

                if (value.equals("目次")) {
                    page.setId("p-toc");
                    book.setTocPage(page);
                }
                break;

            default:
                log.warn("Unknown key: {}", key);
            }

            return;
        }

        log.warn("Unsupported tag: {}", line);

        if (!line.isEmpty()) {
            context.peek().addParagraph().addElement(new Text(line));
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

            case "リンク":
                text.setLink(text.getText());
                break;

            default:
                if (type.endsWith("段階大きい文字")) {
                    int amount =
                            parseInt(line, matcher.start(2), matcher.end(2) - 7) * 10 + 100;
                    text.setStyle(String.format("font-%03dper", amount));
                } else if (type.endsWith("段階小さい文字")) {
                    int amount =
                            100 - parseInt(line, matcher.start(2),
                                    matcher.end(2) - 7) * 10;
                    text.setStyle(String.format("font-%03dper", amount));
                } else {
                    log.warn("Unknown annotation: {}", type);
                }
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
        if (start >= end) {
            throw new IllegalArgumentException();
        }

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
