/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dacci.tsugumi.doc.Book;
import org.dacci.tsugumi.doc.Chapter;
import org.dacci.tsugumi.doc.ElementSequence;
import org.dacci.tsugumi.doc.Image;
import org.dacci.tsugumi.doc.Link;
import org.dacci.tsugumi.doc.PageElement;
import org.dacci.tsugumi.doc.Paragraph;
import org.dacci.tsugumi.doc.ParagraphContainer;
import org.dacci.tsugumi.doc.Resource;
import org.dacci.tsugumi.doc.Ruby;
import org.dacci.tsugumi.doc.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dacci
 */
public class AozoraParser implements Closeable {

    private static final Logger log = LoggerFactory
            .getLogger(AozoraParser.class);

    private static final Charset CHARSET = Charset.forName("MS932");

    private static final CharsetDecoder DECODER = Charset
            .forName("x-SJIS_0213").newDecoder();

    private static final Pattern RUBY_PATTERN = Pattern
            .compile("(?:｜(.+?))?《(.+?)》");

    private static final Pattern CHAR_REFERENCE_PATTERN = Pattern
            .compile(".［＃.+?(?:(\\d+)-(\\d+)-(\\d+)|"
                    + "U\\+([0-9A-Fa-f]+)(?:、\\d+-\\d+)?)］");

    private static final Pattern ANNOTATION_PATTERN = Pattern
            .compile("(.+?)［＃「\\1」[には](.+?)］");

    private static final Pattern TAG_PATTERN = Pattern.compile("［＃(.+?)］");

    private static final Pattern IMAGE_TAG_PATTERN = Pattern
            .compile("(.*?)（(.+?)(?:、横(\\d+)×縦(\\d+))?(?:、.+)*）(?:入る)?");

    private static final Map<String, String> STYLES;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("ゴシック体", "gfont");
        map.put("傍点", "em-sesame");
        map.put("地付き", "align-end");
        map.put("横組み", "sideways");
        map.put("罫囲い", "k-solid");
        STYLES = Collections.unmodifiableMap(map);
    }

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

    /**
     * @param plane
     * @param row
     * @param cell
     * @throws CharacterCodingException
     */
    private static String decodeChar(int plane, int row, int cell)
            throws CharacterCodingException {
        if (plane < 1 || 2 < plane) {
            throw new IllegalArgumentException("illegal plane: " + plane);
        }
        if (row < 1 || 94 < row) {
            throw new IllegalArgumentException("illegal row: " + row);
        }
        if (cell < 1 || 94 < cell) {
            throw new IllegalArgumentException("illegal cell: " + cell);
        }

        int s1 = (row + 33) / 2, s2 = cell;

        if (1 <= row && row <= 62) {
            s1 += 112;
        } else if (63 <= row && row <= 94) {
            s1 += 176;
        }

        if (row % 2 == 0) {
            s2 += 158;
        } else {
            s2 += (cell + 32) / 96 + 63;
        }

        if (plane == 2) {
            if (1 <= row && row <= 5) {
                s1 += 0x6F;
            } else if (8 <= row && row <= 15) {
                s1 += 0x6C;
            } else if (78 <= row && row <= 94) {
                s1 += 0x0D;
            }
        }

        ByteBuffer buffer =
                ByteBuffer.wrap(new byte[] { (byte) s1, (byte) s2 });
        return DECODER.decode(buffer).toString();
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
        book.loadStyle("book-style");
        book.loadStyle("style-reset");
        book.loadStyle("style-standard");
        book.loadStyle("style-advance");

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
     * @throws ParserException
     */
    private ElementSequence preprocess(String line) throws ParserException {
        StringBuilder buffer = new StringBuilder(line);
        Matcher matcher = CHAR_REFERENCE_PATTERN.matcher(buffer);
        while (matcher.find(0)) {
            String decoded;
            if (matcher.start(1) != -1) {
                int plane = Integer.parseInt(matcher.group(1));
                int row = Integer.parseInt(matcher.group(2));
                int cell = Integer.parseInt(matcher.group(3));

                try {
                    decoded = decodeChar(plane, row, cell);
                } catch (CharacterCodingException e) {
                    throw new ParserException(this.row, e);
                }
            } else {
                int codePoint = Integer.parseInt(matcher.group(4), 16);
                decoded = String.valueOf(Character.toChars(codePoint));
            }

            buffer.replace(matcher.start(), matcher.end(), decoded);
        }

        ElementSequence sequence = new ElementSequence(buffer.toString());

        while (true) {
            boolean modified = false;

            matcher = ANNOTATION_PATTERN.matcher(sequence);
            while (matcher.find(0)) {
                PageElement text =
                        sequence.subSequence(matcher.start(1), matcher.end(1));
                String style = matcher.group(2);
                PageElement element = null;
                if (style.equals("リンク")) {
                    element = new Link(chapter, text);
                } else {
                    element = new Style(text);
                    setStyle(style, (Style) element);
                }

                sequence.replace(matcher.start(), matcher.end(), element);
                modified = true;
            }

            matcher = RUBY_PATTERN.matcher(sequence);
            while (true) {
                int lastMatch = -1;
                while (matcher.find()) {
                    lastMatch = matcher.start();
                }
                if (lastMatch < 0) {
                    break;
                }

                matcher.find(lastMatch);
                Ruby ruby = new Ruby(matcher.group(2));
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
                matcher.reset();
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
                Paragraph paragraph =
                        new Paragraph(sequence.subSequence(0, matcher.start()));

                if (lastTag != null) {
                    setStyle(lastTag, paragraph);
                }

                stack.peek().add(paragraph);
            }

            lastTag = matcher.group(1);
            sequence.erase(0, matcher.end());
            matcher.reset();
        }

        Paragraph paragraph = new Paragraph(sequence);

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
            Resource resource = book.loadImage(basePath.resolve(path));
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
                    stack.peek().add(new Paragraph(image));
                }
            }

            log.debug("{}/Load image: {}, alt={}, width={}, height={}", row,
                    path, alt, width, height);
            return;
        }

        log.warn("{}/Unknown tag: {}", row, tag);
    }

    /**
     * @param style
     * @param element
     */
    private void setStyle(String style, Style element) {
        String wordStyle = STYLES.get(style);
        if (wordStyle != null) {
            element.setStyle(wordStyle);
            return;
        }

        if (style.endsWith("段階大きい文字")) {
            int amount = parseInt(style, 0, style.length() - 7) + 10;
            element.setStyle("font-" + amount + "0per");
        } else if (style.endsWith("段階小さい文字")) {
            int amount = 10 - parseInt(style, 0, style.length() - 7);
            element.setStyle("font-0" + amount + "per");
        } else {
            log.warn("{}/Unknown style: {}", row, style);
        }
    }

    /**
     * @param style
     * @param paragraph
     */
    private void setStyle(String style, Paragraph paragraph) {
        String paragraphStyle = STYLES.get(style);
        if (paragraphStyle != null) {
            paragraph.setStyle(paragraphStyle);
            return;
        }

        if (style.endsWith("字下げ")) {
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

    /**
     * @param style
     */
    private void push(String style) {
        context.push(style);

        ParagraphContainer container = new ParagraphContainer();
        stack.push(container);
        setStyle(style, container);

        log.debug("{}/Start of block: {}", row, style);
    }

    /**
     * @param style
     * @throws ParserException
     */
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
