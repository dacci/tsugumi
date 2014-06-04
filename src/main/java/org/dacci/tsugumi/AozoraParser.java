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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dacci.tsugumi.doc.Annotation;
import org.dacci.tsugumi.doc.Book;
import org.dacci.tsugumi.doc.Chapter;
import org.dacci.tsugumi.doc.ElementSequence;
import org.dacci.tsugumi.doc.Image;
import org.dacci.tsugumi.doc.PageElement;
import org.dacci.tsugumi.doc.Paragraph;
import org.dacci.tsugumi.doc.ParagraphContainer;
import org.dacci.tsugumi.doc.Resource;
import org.dacci.tsugumi.doc.Ruby;
import org.dacci.tsugumi.doc.SimpleParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dacci
 */
public class AozoraParser implements Closeable {

    private static final Logger log = LoggerFactory
            .getLogger(AozoraParser.class);

    private static final Charset CHARSET = Charset.forName("MS932");

    private static final Pattern RUBY_PATTERN = Pattern
            .compile("(?:｜(.+?))?《(.+?)》");

    private static final Pattern CHAR_REFERENCE_PATTERN = Pattern
            .compile("［＃.+?\\d+-\\d+-\\d+］");

    private static final Pattern ANNOTATION_PATTERN = Pattern
            .compile("(.+?)［＃「\\1」[には](.+?)］");

    private static final Pattern TAG_PATTERN = Pattern.compile("［＃(.+?)］");

    private static final Pattern IMAGE_TAG_PATTERN = Pattern
            .compile("(.*?)（(.+?)(?:、縦(\\d+)×横(\\d+))?）(?:入る)?");

    /**
     * @param string
     * @return
     */
    private static int parseInt(String string, int start, int end) {
        String digits = "０１２３４５６７８９";
        int parsed = 0;

        for (int i = start; i < end; ++i) {
            parsed *= 10;

            int digit = digits.indexOf(string.codePointAt(i));
            if (digit < 0) {
                throw new NumberFormatException();
            }

            parsed += digit;
        }

        return parsed;
    }

    private final Path basePath;

    private final BufferedReader reader;

    private Book book;

    private Chapter chapter;

    private int row;

    private final Deque<String> context = new LinkedList<>();

    private final Deque<ParagraphContainer> stack = new LinkedList<>();

    /**
     * @param source
     * @throws IOException
     */
    public AozoraParser(Path source) throws IOException {
        basePath = source.toAbsolutePath().getParent();

        reader = Files.newBufferedReader(source, CHARSET);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * @return
     */
    public Book parse() throws ParserException {
        book = new Book();
        chapter = book.addChapter();

        row = 1;
        context.clear();
        stack.clear();
        stack.push(chapter);

        List<String> metaData = new ArrayList<>();

        try {
            for (String line; (line = reader.readLine()) != null; ++row) {
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
                book.setSubtitle(metaData.get(1));
                book.setAuthor(metaData.get(2));
                break;

            case 4:
                book.setOriginalTitle(metaData.get(1));
                book.setAuthor(metaData.get(2));
                book.setTranslator(metaData.get(3));
                break;

            case 6:
                book.setOriginalTitle(metaData.get(1));
                book.setSubtitle(metaData.get(2));
                book.setOriginalSubtitle(metaData.get(3));
                book.setAuthor(metaData.get(4));
                book.setTranslator(metaData.get(5));
                break;

            default:
                throw new ParserException(row, "illegal meta data count: " +
                        metaData.size());
            }

            ++row;

            for (String line; (line = reader.readLine()) != null; ++row) {
                ElementSequence sequence = preprocess(line);
                parse(sequence);
            }
        } catch (RuntimeException | IOException e) {
            throw new ParserException(row, e);
        }

        return book;
    }

    /**
     * @param line
     * @return
     */
    private ElementSequence preprocess(String line) {
        line = CHAR_REFERENCE_PATTERN.matcher(line).replaceAll("");

        ElementSequence sequence = new ElementSequence(line);

        while (true) {
            boolean modified = false;
            Matcher matcher;

            matcher = ANNOTATION_PATTERN.matcher(sequence);
            while (matcher.find(0)) {
                PageElement text =
                        sequence.subSequence(matcher.start(1), matcher.end(1));
                PageElement command =
                        sequence.subSequence(matcher.start(2), matcher.end(2));
                Annotation annotation = new Annotation(text, command);

                sequence.replace(matcher.start(), matcher.end(), annotation);
                modified = true;
            }

            matcher = RUBY_PATTERN.matcher(sequence);
            while (matcher.find(0)) {
                Ruby ruby =
                        new Ruby(sequence.subSequence(matcher.start(2),
                                matcher.end(2)));

                int start = matcher.start();

                if (matcher.start(1) == -1) {
                    int textEnd = matcher.start(2) - 1;
                    start = textEnd - 1;
                    UnicodeBlock block =
                            UnicodeBlock.of(sequence.charAt(start));

                    for (; start >= 0; --start) {
                        if (start == 0) {
                            break;
                        }

                        if (UnicodeBlock.of(sequence.charAt(start - 1)) != block) {
                            break;
                        }
                    }

                    ruby.setText(sequence.subSequence(start, textEnd));
                } else {
                    ruby.setText(sequence.subSequence(matcher.start(1),
                            matcher.end(1)));
                }

                sequence.replace(start, matcher.end(), ruby);
                modified = true;
            }

            if (!modified) {
                break;
            }
        }

        return sequence;
    }

    /**
     * @param sequence
     * @throws ParserException
     */
    private void parse(ElementSequence sequence) throws ParserException {
        Matcher matcher = TAG_PATTERN.matcher(sequence);
        if (matcher.matches()) {
            processTag(matcher.group(1));
            return;
        }

        matcher.reset();

        String lastTag = null;
        while (matcher.find()) {
            if (matcher.start() > 0) {
                ElementSequence subSequence =
                        sequence.subSequence(0, matcher.start());

                SimpleParagraph paragraph = new SimpleParagraph(subSequence);

                if (lastTag != null) {
                    setStyle(lastTag, paragraph);
                }

                stack.peek().add(paragraph);
            }

            lastTag = matcher.group(1);
            sequence.erase(0, matcher.end());
            matcher.reset();
        }

        SimpleParagraph paragraph = new SimpleParagraph(sequence);

        if (lastTag != null) {
            setStyle(lastTag, paragraph);
        }

        stack.peek().add(paragraph);
    }

    /**
     * @param tag
     * @throws ParserException
     */
    private void processTag(String tag) throws ParserException {
        switch (tag) {
        case "改ページ":
            chapter = book.addChapter();
            context.clear();
            stack.clear();
            stack.push(chapter);

            log.debug("{}/Page break", row);
            return;
        }

        if (tag.startsWith("ここから")) {
            push(tag.substring(4));
            return;
        } else if (tag.startsWith("ここで") && tag.endsWith("終わり")) {
            pop(tag.substring(3, tag.length() - 3));
            return;
        }

        int index = tag.indexOf('＝');
        if (index > 0) {
            String name = tag.substring(0, index);
            String value = tag.substring(index + 1);
            chapter.setProperty(name, value);

            log.debug("{}/Set property: {}, {}", row, name, value);
            return;
        }

        Matcher matcher = IMAGE_TAG_PATTERN.matcher(tag);
        if (matcher.matches()) {
            String path = matcher.group(2);
            Resource resource = book.loadResource(basePath.resolve(path));
            Image image = new Image(resource, chapter.getResource());

            String width = matcher.group(3);
            if (width != null) {
                image.setWidth(Integer.parseInt(width));
            }

            String height = matcher.group(4);
            if (height != null) {
                image.setHeight(Integer.parseInt(height));
            }

            String alt = matcher.group(1);
            if (alt != null) {
                image.setTitle(alt);

                if (alt.equals("表紙")) {
                    resource.setId("cover");
                    resource.setProperties("cover-image");
                    book.setCoverImage(image);
                } else {
                    stack.peek().add(new SimpleParagraph(image));
                }
            }

            log.debug("{}/Load image: {}, alt={}, width={}, height={}", row,
                    path, alt, width, height);
            return;
        }

        log.warn("{}/Unknown tag: {}", row, tag);
    }

    private void setStyle(String style, SimpleParagraph paragraph) {
        if (style.equals("地付き")) {
            paragraph.setStyle("align-end");
        } else if (style.endsWith("字下げ")) {
            int amount = parseInt(style, 0, style.length() - 3) / 2;
            paragraph.setStyle("start-" + amount + "em");
        } else if (style.endsWith("見出し")) {
            switch (style.charAt(0)) {
            case '大':
                paragraph.setCaptionLevel(1);
                break;

            case '中':
                paragraph.setCaptionLevel(2);
                break;

            case '小':
                paragraph.setCaptionLevel(3);
                break;
            }
        } else {
            log.warn("{}/Unknown style: {}", row, style);
        }
    }

    private void push(String style) {
        context.push(style);

        ParagraphContainer container = new ParagraphContainer();
        stack.push(container);
        setStyle(style, container);

        log.debug("{}/Start of block: {}", row, style);
    }

    private void pop(String style) throws ParserException {
        String current = context.pop();
        if (!current.endsWith(style)) {
            throw new ParserException(row, "Unmatched tag: " + current +
                    " vs " + style);
        }

        log.debug("{}/End of block: {}", row, style);

        Paragraph container = stack.pop();
        stack.peek().add(container);
    }
}
