/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.format.aozora;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.MutablePair;
import org.dacci.tsugumi.Util;
import org.dacci.tsugumi.doc.Block;
import org.dacci.tsugumi.doc.Book;
import org.dacci.tsugumi.doc.BookProperty;
import org.dacci.tsugumi.doc.Chapter;
import org.dacci.tsugumi.doc.EndMarginStyle;
import org.dacci.tsugumi.doc.FontSizeStyle;
import org.dacci.tsugumi.doc.Fragment;
import org.dacci.tsugumi.doc.ImageMarker;
import org.dacci.tsugumi.doc.Paragraph;
import org.dacci.tsugumi.doc.RubySegment;
import org.dacci.tsugumi.doc.Segment;
import org.dacci.tsugumi.doc.SimpleMarker;
import org.dacci.tsugumi.doc.SimpleStyle;
import org.dacci.tsugumi.doc.StartMarginStyle;
import org.dacci.tsugumi.doc.Style;
import org.dacci.tsugumi.doc.StyledSegment;
import org.dacci.tsugumi.doc.WidthStyle;
import org.dacci.tsugumi.format.BuildException;
import org.dacci.tsugumi.format.Format;
import org.dacci.tsugumi.format.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dacci
 */
public class AozoraFormat implements Format {

  private static final Logger LOG = LoggerFactory.getLogger(AozoraFormat.class);

  private static final String CHARSET_NAME = "x-SJIS_0213";

  private static final Charset CHARSET = Charset.forName(CHARSET_NAME);

  private static final CharsetDecoder DECODER = CHARSET.newDecoder();

  private static final Pattern LEGEND_MARK_PATTERN = Pattern.compile("-+");

  private static final Pattern BLOCK_TAG_PATTERN = Pattern.compile("??????((??????|???).+?|???????????????)???");

  private static final Pattern PROPERTY_TAG_PATTERN = Pattern.compile("??????(.+?)???(.*)???");

  private static final Pattern TAG_PATTERN = Pattern.compile("??????(.+?)???");

  /**
   * @param plane
   * @param row
   * @param cell
   * @throws CharacterCodingException
   */
  private static String decodeChar(int plane, int row, int cell) throws CharacterCodingException {
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

    ByteBuffer buffer = ByteBuffer.wrap(new byte[] {(byte) s1, (byte) s2});
    return DECODER.decode(buffer).toString();
  }

  private static MutablePair<String, Style> mapToStyle(String tag) {
    boolean left = false;
    if (tag.startsWith("??????")) {
      tag = tag.substring(2);
      left = true;
    }

    MutablePair<String, Style> pair = MutablePair.of(tag, null);

    if (tag.endsWith("?????????")) {
      pair.left = "?????????";

      if (tag.indexOf('???') == -1) {
        int width = Util.parseInt(tag.substring(0, tag.length() - 3));
        pair.right = new StartMarginStyle(width);
      } else {
        pair.right = new StartMarginStyle(0);
        LOG.warn("Unsupported indentation: {}", tag);
      }
    } else if (tag.endsWith("?????????")) {
      pair.left = "?????????";

      int width = Util.parseInt(tag.substring(3, tag.length() - 3));
      pair.right = new EndMarginStyle(width);
    } else if (tag.endsWith("?????????")) {
      pair.left = "?????????";

      int width = Util.parseInt(tag.substring(0, tag.length() - 3));
      pair.right = new WidthStyle(width);
    } else if (tag.endsWith("?????????")) {
      int level = 1, type;
      if (tag.length() > 5) {
        pair.left = tag.substring(tag.length() - 5);
        level = Util.parseInt(tag.substring(0, tag.length() - 7));
        type = tag.codePointAt(3);
      } else {
        type = tag.codePointAt(0);
      }

      if (type == '???') {
        level *= -1;
      }

      pair.right = new FontSizeStyle(level);
    } else if (tag.endsWith("?????????")) {
      switch (tag.codePointAt(tag.length() - 4)) {
        case '???':
          pair.right = SimpleStyle.HeadingLarge;
          break;

        case '???':
          pair.right = SimpleStyle.HeadingMedium;
          break;

        case '???':
          pair.right = SimpleStyle.HeadingSmall;
          break;

        default:
          return null;
      }
    } else {
      switch (tag) {
        case "??????":
          pair.right = SimpleStyle.Bold;
          break;

        case "??????":
          pair.right = SimpleStyle.Italic;
          break;

        case "?????????":
          pair.right = SimpleStyle.AlignEnd;
          break;

        case "?????????":
        case "?????????":
          pair.right = SimpleStyle.Ruled;
          break;

        case "?????????":
          pair.right = SimpleStyle.Horizontal;
          break;

        case "??????????????????":
          pair.right = SimpleStyle.Caption;
          break;

        case "??????":
          pair.right = SimpleStyle.Sesame;
          break;

        case "???????????????":
          pair.right = SimpleStyle.OpenSesame;
          break;

        case "?????????":
          pair.right = SimpleStyle.Circle;
          break;

        case "????????????":
          pair.right = SimpleStyle.CircleOpen;
          break;

        case "???????????????":
          pair.right = SimpleStyle.Triangle;
          break;

        case "???????????????":
          pair.right = SimpleStyle.TriangleOpen;
          break;

        case "???????????????":
          pair.right = SimpleStyle.DoubleCircleOpen;
          break;

        case "???????????????":
          pair.right = SimpleStyle.DoubleCircle;
          break;

        case "????????????":
          pair.right = SimpleStyle.Saltire;
          break;

        case "??????":
          pair.right = SimpleStyle.Lined;
          break;

        case "????????????":
          pair.right = SimpleStyle.DoubleLined;
          break;

        case "??????":
          pair.right = SimpleStyle.Dotted;
          break;

        case "??????":
          pair.right = SimpleStyle.Dashed;
          break;

        case "??????":
          pair.right = SimpleStyle.WaveDashed;
          break;

        case "?????????":
          pair.right = SimpleStyle.Rotated;
          break;

        case "???????????????":
        case "??????????????????":
          pair.right = SimpleStyle.Superscript;
          break;

        case "???????????????":
        case "??????????????????":
          pair.right = SimpleStyle.Subscript;
          break;

        case "?????????":
          pair.right = SimpleStyle.Warichu;
          break;

        case "???????????????":
          pair.right = SimpleStyle.Gothic;
          break;

        default:
          return null;
      }
    }

    if (left) {
      pair.left = "??????" + pair.left;
    }

    return pair;
  }

  private Path sourcePath = null;

  private Book book = null;

  private Chapter chapter = null;

  private int row;

  private Deque<String> blockTypeStack = new LinkedList<>();

  private Deque<Block> blockStack = new LinkedList<>();

  /** {@inheritDoc} */
  @Override
  public boolean isParseSupported() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public Book parse(final Path path) throws ParseException {
    synchronized (this) {
      if (sourcePath == null) {
        sourcePath = path;
      } else {
        throw new ParseException(0, "already parsing");
      }
    }

    book = new Book();
    row = 0;

    try (BufferedReader reader = Files.newBufferedReader(path, CHARSET)) {
      String line = parseMetaData(reader);
      if (line == null) {
        throw new EOFException();
      }

      reader.mark(0);
      line = reader.readLine();
      if (line == null) {
        throw new EOFException();
      } else {
        reader.reset();
      }

      if (LEGEND_MARK_PATTERN.matcher(line).matches()) {
        reader.readLine();
        ++row;

        while ((line = reader.readLine()) != null) {
          ++row;

          if (LEGEND_MARK_PATTERN.matcher(line).matches()) {
            break;
          }
        }

        reader.readLine();
        ++row;
      }

      breakChapter();

      line = parseMainText(reader);
      if (line == null) {
        throw new EOFException();
      }

      parseAfterText(reader);
    } catch (EOFException e) {
      // end process
    } catch (IOException | RuntimeException e) {
      throw new ParseException(row, e);
    }

    synchronized (this) {
      Book parsedBook = book;

      book = null;
      chapter = null;
      blockTypeStack.clear();
      blockStack.clear();
      sourcePath = null;

      return parsedBook;
    }
  }

  private void breakChapter() {
    blockTypeStack.clear();
    blockStack.clear();

    chapter = book.addChapter(new Chapter());
    blockStack.add(chapter.getRoot());
  }

  private void enterBlock(String tag) throws ParseException {
    MutablePair<String, Style> pair = mapToStyle(tag);
    if (pair == null) {
      pair = MutablePair.of(tag, null);
      LOG.warn("Unknown block tag: {}", tag);
      // throw new ParseException(row, "Unknown block tag: " + tag);
    }

    if (pair.left.equals("?????????")
        && !blockTypeStack.isEmpty()
        && blockTypeStack.peek().equals("?????????")) {
      leaveBlock("?????????");
    }

    Block block = new Block();
    block.addStyle(pair.right);

    blockTypeStack.push(pair.left);
    blockStack.peek().addElement(block);
    blockStack.push(block);
  }

  private void leaveBlock(String tag) throws ParseException {
    if (!blockTypeStack.peek().equals(tag)) {
      throw new ParseException(row, "Unmatched block: " + tag);
    }

    if (tag.equals("?????????")) {
      Iterator<String> blockTypes = blockTypeStack.iterator();
      Iterator<Block> blocks = blockStack.iterator();

      while (blockTypes.hasNext() && blocks.hasNext()) {
        String blockType = blockTypes.next();
        blocks.next();

        if (!blockType.equals("?????????")) {
          break;
        }

        blockTypes.remove();
        blocks.remove();
      }
    } else {
      blockTypeStack.pop();
      blockStack.pop();
    }
  }

  private static final Pattern ACCENT_PATTERN = Pattern.compile("???(.+?)???");

  /**
   * @param input
   * @return
   */
  private StringBuilder processAccent(StringBuilder input) {
    List<MatchResult> results = Util.findAll(ACCENT_PATTERN, input, true);

    for (MatchResult result : results) {
      String original = result.group(1);
      String replaced = AozoraAccentMap.replaceAll(original);
      if (!replaced.equals(original)) {
        input.replace(result.start(), result.end(), replaced);
      }
    }

    return input;
  }

  private static final Pattern CHAR_REFERENCE_PATTERN =
      Pattern.compile("?????????(.+?)???(?:.*?(\\d+)-(\\d+)-(\\d+)|(U\\+([0-9A-Fa-f]{4,6}))?.*)???");

  /**
   * @param input
   * @return
   * @throws ParseException
   */
  private StringBuilder processCharReference(StringBuilder input) throws ParseException {
    List<MatchResult> results = Util.findAll(CHAR_REFERENCE_PATTERN, input, true);

    for (MatchResult result : results) {
      try {
        String replacement;

        if (result.group(2) != null) {
          int plane = Integer.parseInt(result.group(2));
          int row = Integer.parseInt(result.group(3));
          int cell = Integer.parseInt(result.group(4));

          replacement = decodeChar(plane, row, cell);
        } else if (result.group(5) != null) {
          int codePoint = Integer.parseInt(result.group(6), 16);
          replacement = String.valueOf(Character.toChars(codePoint));
        } else {
          replacement = "???" + result.group(1);
        }

        input.replace(result.start(), result.end(), replacement);
      } catch (CharacterCodingException e) {
        throw new ParseException(row, e);
      }
    }

    return input;
  }

  /**
   * @param fragment
   * @return true if the fragment is marker-only.
   */
  private boolean processMarker(Fragment fragment) {
    List<MatchResult> results = Util.findAll(TAG_PATTERN, fragment, true);

    for (MatchResult result : results) {
      switch (result.group(1)) {
        case "??????":
          fragment.replace(result.start(), result.end(), SimpleMarker.LineBreak);
          break;

        case "????????????????????????":
          chapter.getRoot().addStyle(SimpleStyle.PageCenter);
          return true;
      }
    }

    return false;
  }

  private static final Pattern KUNTEN_PATTERN = Pattern.compile("??????(.????)???");

  /**
   * @param fragment
   */
  private void processKunten(Fragment fragment) {
    List<MatchResult> results = Util.findAll(KUNTEN_PATTERN, fragment, true);

    for (MatchResult result : results) {
      Segment segment = fragment.subSequence(result.start(1), result.end(1));
      StyledSegment styledSegment = new StyledSegment(segment);
      styledSegment.addStyle(SimpleStyle.Kunten);

      fragment.replace(result.start(), result.end(), styledSegment);
    }
  }

  private static final Pattern OKURIGANA_PATTERN = Pattern.compile("?????????(.+?)??????");

  /**
   * @param fragment
   */
  private void processOkurigana(Fragment fragment) {
    List<MatchResult> results = Util.findAll(OKURIGANA_PATTERN, fragment, true);

    for (MatchResult result : results) {
      String text = result.group(1);
      if (text.indexOf('.') != -1) {
        continue;
      }

      Segment segment = fragment.subSequence(result.start(1), result.end(1));
      StyledSegment styledSegment = new StyledSegment(segment);
      styledSegment.addStyle(SimpleStyle.Okurigana);

      fragment.replace(result.start(), result.end(), styledSegment);
    }
  }

  private static final Pattern IMAGE_PATTERN =
      Pattern.compile("??????(.*?)???(.+?)(??????(\\d+)?????(\\d+))?(???.+?)*???(??????)????");

  /**
   * @param fragment
   */
  private void processImage(Fragment fragment) {
    List<MatchResult> results = Util.findAll(IMAGE_PATTERN, fragment, true);

    for (MatchResult result : results) {
      String fileName = result.group(2);
      Path path = sourcePath.resolveSibling(fileName).toAbsolutePath();
      book.loadResource(path);

      ImageMarker image = new ImageMarker(path);

      String caption = result.group(1);
      if (caption != null && !caption.isEmpty()) {
        if (caption.codePointAt(0) == '???') {
          int index = caption.indexOf('???', 1);
          if (index != -1) {
            caption = caption.substring(1, index);
          }
        }

        image.setCaption(caption);
      }

      String width = result.group(4);
      String height = result.group(5);
      if (width != null && !width.isEmpty() && height != null && !height.isEmpty()) {
        image.setWidth(Integer.parseInt(width));
        image.setHeight(Integer.parseInt(height));
      }

      fragment.replace(result.start(), result.end(), image);
    }
  }

  private static final Pattern RUBY_PATTERN = Pattern.compile("(???(.+?))????(.+?)???");

  private static final Pattern RUBY_AREA_PATTERN =
      Pattern.compile("??????((?:??????)?)((?:??????|??????)??????)???(.*?)??????\\1???(.+?)??????\\2????????????");

  /**
   * @param fragment
   */
  private void processRuby(Fragment fragment) {
    final String special = "??????";

    List<MatchResult> results = Util.findAll(RUBY_PATTERN, fragment, true);
    for (MatchResult result : results) {
      int rangeStart = result.start();
      int textStart = result.start(2), textEnd = result.end(2);

      if (textStart == -1) {
        textEnd = result.start(3) - 1;
        textStart = textEnd - 1;
        UnicodeBlock endType = null;

        for (; textStart >= 0; --textStart) {
          char c = fragment.charAt(textStart);
          while (textStart > 0) {
            if (special.indexOf(c) == -1) {
              break;
            }

            c = fragment.charAt(--textStart);
          }

          UnicodeBlock type = UnicodeBlock.of(c);

          if (endType == null) {
            endType = type;
          } else if (!type.equals(endType)) {
            ++textStart;
            break;
          }
        }
        if (textStart < 0) {
          textStart = 0;
        }

        rangeStart = textStart;
      }

      Segment text = fragment.subSequence(textStart, textEnd);
      RubySegment segment = new RubySegment(text, result.group(3));
      fragment.replace(rangeStart, result.end(), segment);
    }

    results = Util.findAll(RUBY_AREA_PATTERN, fragment, true);
    for (MatchResult result : results) {
      Segment text = fragment.subSequence(result.start(3), result.end(3));
      RubySegment segment = new RubySegment(text, result.group(4));
      fragment.replace(result.start(), result.end(), segment);
    }
  }

  private static final Pattern ANNOTATION_PATTERN =
      Pattern.compile("(.+)?????????\\1???(?:(?:??????)?[??????])(???(.+?)??????(??????|??????)|.+?)???");

  /**
   * @param fragment
   * @throws ParseException
   */
  private void processAnnotation(Fragment fragment) throws ParseException {
    Matcher matcher = ANNOTATION_PATTERN.matcher(fragment);
    while (matcher.find(0)) {
      Segment text = fragment.subSequence(matcher.start(1), matcher.end(1));

      Segment segment = null;
      if (matcher.start(3) != -1) {
        segment = new RubySegment(text, matcher.group(3));
      } else {
        String annotation = matcher.group(2);
        MutablePair<String, Style> pair = mapToStyle(annotation);
        if (pair != null) {
          segment = new StyledSegment(text);
          ((StyledSegment) segment).addStyle(pair.right);
        } else {
          LOG.warn("discarding annotation {} on line {}", annotation, row);
        }
      }

      fragment.replace(matcher.start(), matcher.end(), segment);
    }
  }

  /**
   * @param fragment
   */
  private Collection<Style> processInlineTags(Fragment fragment) {
    Deque<MutablePair<String, Style>> styleStack = new LinkedList<>();
    Deque<MatchResult> matchStack = new LinkedList<>();

    Matcher matcher = TAG_PATTERN.matcher(fragment);
    if (!matcher.find()) {
      return Collections.emptySet();
    }

    while (true) {
      String tag = matcher.group(1);
      if (tag.endsWith("?????????")) {
        tag = tag.substring(0, tag.length() - 3);

        while (!styleStack.isEmpty()) {
          MutablePair<String, Style> pair = styleStack.peek();
          if (pair.left.equals(tag)) {
            break;
          }

          styleStack.pop();
          matchStack.pop();
        }

        MutablePair<String, Style> stylePair = styleStack.pop();
        if (stylePair.right == null) {
          throw new NullPointerException();
        }

        MatchResult match = matchStack.pop();

        Segment segment = fragment.subSequence(match.end(), matcher.start());
        StyledSegment styledSegment = new StyledSegment(segment);
        styledSegment.addStyle(stylePair.right);
        fragment.replace(match.start(), matcher.end(), styledSegment);

        if (!matcher.find(match.start())) {
          break;
        }
      } else {
        MutablePair<String, Style> stylePair = mapToStyle(tag);
        if (stylePair == null) {
          stylePair = MutablePair.of(tag, null);
        }

        styleStack.push(stylePair);
        matchStack.push(matcher.toMatchResult());

        if (!matcher.find()) {
          break;
        }
      }
    }

    if (styleStack.isEmpty()) {
      return Collections.emptySet();
    }

    Collection<Style> styles = new LinkedHashSet<>();
    while (!styleStack.isEmpty()) {
      MutablePair<String, Style> pair = styleStack.removeLast();
      MatchResult match = matchStack.removeLast();

      if (pair.right == null) {
        LOG.warn("discarding tag {} on line {}", match.group(), row);
      } else {
        styles.add(pair.right);
      }

      fragment.replace(match.start(), match.end(), null);
    }

    return styles;
  }

  private String parseMetaData(BufferedReader reader) throws IOException, ParseException {
    String line;
    List<String> metadata = new ArrayList<>(6);

    while ((line = reader.readLine()) != null) {
      ++row;

      if (line.isEmpty()) {
        break;
      }

      metadata.add(line);
    }

    book.setProperty(BookProperty.Title, metadata.get(0));

    switch (metadata.size()) {
      case 2:
        book.setProperty(BookProperty.Author, metadata.get(1));
        break;

      case 3:
        book.setProperty(BookProperty.Subtitle, metadata.get(1));
        book.setProperty(BookProperty.Author, metadata.get(2));
        break;

      case 4:
        book.setProperty(BookProperty.OriginalTitle, metadata.get(1));
        book.setProperty(BookProperty.Author, metadata.get(2));
        book.setProperty(BookProperty.Translator, metadata.get(3));
        break;

      case 6:
        book.setProperty(BookProperty.OriginalTitle, metadata.get(1));
        book.setProperty(BookProperty.Subtitle, metadata.get(2));
        book.setProperty(BookProperty.OriginalSubtitle, metadata.get(3));
        book.setProperty(BookProperty.Author, metadata.get(4));
        book.setProperty(BookProperty.Translator, metadata.get(5));
        break;

      default:
        throw new ParseException(row, "Invalid number of book info lines");
    }

    return line;
  }

  private String parseMainText(BufferedReader reader) throws IOException, ParseException {
    String line;

    while ((line = reader.readLine()) != null) {
      ++row;

      Matcher matcher = BLOCK_TAG_PATTERN.matcher(line);
      if (matcher.matches()) {
        String tag = matcher.group(1);

        if (tag.startsWith("????????????")) {
          enterBlock(tag.substring(4));
        } else if (tag.startsWith("?????????")) {
          leaveBlock(tag.substring(3, tag.length() - 3));
        } else if (tag.startsWith("???")) {
          breakChapter();
        } else if (tag.equals("???????????????")) {
          break;
        } else {
          throw new ParseException(row, "Unexpected block tag: " + tag);
        }

        continue;
      }

      matcher = PROPERTY_TAG_PATTERN.matcher(line);
      if (matcher.matches()) {
        String key = matcher.group(1);
        String value = matcher.group(2);

        switch (key) {
          case "????????????":
            chapter.setProperty(BookProperty.Title, value);
            break;

          default:
            LOG.warn("Unknown chapter property: {}", key);
        }

        continue;
      }

      Paragraph paragraph = parseLine(line);
      if (paragraph != null) {
        blockStack.peek().addElement(paragraph);
      }
    }

    return line;
  }

  private Paragraph parseLine(String line) throws ParseException {
    StringBuilder builder = new StringBuilder(line);
    processAccent(builder);
    processCharReference(builder);

    Paragraph paragraph = new Paragraph(builder.toString());

    Fragment fragment = paragraph.getFragment();

    if (processMarker(fragment)) {
      return null;
    }

    processKunten(fragment);
    processOkurigana(fragment);
    processImage(fragment);

    processRuby(fragment);
    processAnnotation(fragment);

    Collection<Style> styles = processInlineTags(fragment);
    for (Style style : styles) {
      paragraph.addStyle(style);
    }

    return paragraph;
  }

  private void parseAfterText(BufferedReader reader) throws IOException {
    String line;

    while ((line = reader.readLine()) != null) {
      ++row;

      Matcher matcher = PROPERTY_TAG_PATTERN.matcher(line);
      if (matcher.matches()) {
        String key = matcher.group(1);
        String value = matcher.group(2);

        switch (key) {
          case "????????????":
            book.setProperty(BookProperty.Series, value);
            break;

          case "??????":
            book.setProperty(BookProperty.SeriesPosition, value);
            break;

          default:
            LOG.warn("Unknown book property: {}", key);
        }
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isBuildSupported() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public Path build(Book book) throws BuildException {
    throw new UnsupportedOperationException();
  }
}
