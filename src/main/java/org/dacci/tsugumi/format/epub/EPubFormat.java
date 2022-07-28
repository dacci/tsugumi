/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.format.epub;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.dacci.tsugumi.Util;
import org.dacci.tsugumi.doc.Block;
import org.dacci.tsugumi.doc.Book;
import org.dacci.tsugumi.doc.BookElement;
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
import org.dacci.tsugumi.doc.TextSegment;
import org.dacci.tsugumi.doc.WidthStyle;
import org.dacci.tsugumi.format.BuildException;
import org.dacci.tsugumi.format.Format;
import org.dacci.tsugumi.format.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author dacci
 */
public class EPubFormat implements Format {

  private static final Logger LOG = LoggerFactory.getLogger(EPubFormat.class);

  private static final Path ITEM_PATH = Paths.get("item");

  private static final Path STYLE_PATH = ITEM_PATH.resolve("style");

  private static final Path IMAGE_PATH = ITEM_PATH.resolve("image");

  private static final Path XHTML_PATH = ITEM_PATH.resolve("xhtml");

  private static final byte[] MIMETYPE = "application/epub+zip".getBytes(StandardCharsets.UTF_8);

  private static String mapToClassName(Style style) {
    if (style == null) {
      return null;
    }

    if (style instanceof SimpleStyle) {
      switch ((SimpleStyle) style) {
        case Bold:
          return "bold";

        case Italic:
          return "italic";

        case Ruled:
          return "k-solid";

        case Horizontal:
          return "sideways";

        case AlignEnd:
          return "align-end";

        case Caption:
          break;

        case HeadingLarge:
          return "font-150per";

        case HeadingMedium:
          return "font-130per";

        case HeadingSmall:
          return "font-110per";

        case Sesame:
          return "em-sesame";

        case OpenSesame:
          return "em-sesame-open";

        case Circle:
        case Saltire:
          return "em-circle";

        case CircleOpen:
          return "em-circle-open";

        case Triangle:
          return "em-triangle";

        case TriangleOpen:
          return "em-triangle-open";

        case DoubleCircle:
          return "em-double-circle";

        case DoubleCircleOpen:
          return "em-double-circle-open";

        case Lined:
          return "em-line";

        case DoubleLined:
          return "k-double-right";

        case Dotted:
          return "k-dotted-right";

        case Dashed:
          return "k-dashed-right";

        case WaveDashed:
          return "k-wave-dashed-right";

        case Rotated:
          return "tcy";

        case Superscript:
          return "super";

        case Subscript:
          return "sub";

        case Kunten:
          return "kunten";

        case Okurigana:
          return "kunten-okuri";

        case Warichu:
          break;

        case PageCenter:
          return "block-align-center";

        case Gothic:
          return "gfont";
      }
    } else if (style instanceof FontSizeStyle) {
      int level = 100 + ((FontSizeStyle) style).getLevel() * 10;
      return String.format("font-%dper", level);
    } else if (style instanceof StartMarginStyle) {
      int width = ((StartMarginStyle) style).getWidth();
      return String.format("start-%dem", width);
    } else if (style instanceof EndMarginStyle) {
      int width = ((EndMarginStyle) style).getWidth();
      return String.format("end-%dem", width);
    } else if (style instanceof WidthStyle) {
      int width = ((WidthStyle) style).getWidth();
      return String.format("width-%dem", width);
    }

    LOG.warn("Unsupported style: {}", style);

    return "";
  }

  private Path outputPath = Paths.get(".");

  private DocumentBuilder builder = null;

  private Document document = null;

  private Transformer transformer;

  private Document containerDocument = null;

  private Book book = null;

  private int pages;

  private Map<Path, Resource> resources = new LinkedHashMap<>();

  private List<Pair<String, Resource>> contents = new ArrayList<>();

  private Document packageDocument;

  /** {@inheritDoc} */
  @Override
  public void setProperty(String key, Object value) {
    switch (key) {
      case Format.OUTPUT_PATH:
        if (value == null) {
          outputPath = Paths.get(".");
        } else {
          outputPath = (Path) value;
        }
        break;
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isParseSupported() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public Book parse(Path path) throws ParseException {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isBuildSupported() {
    return true;
  }

  private Element newHTMLDocument() {
    Document document = builder.newDocument();

    Element html = document.createElementNS("http://www.w3.org/1999/xhtml", "html");
    html.setAttribute("xmlns:epub", "http://www.idpf.org/2007/ops");
    html.setAttribute("xml:lang", "ja");
    document.appendChild(html);

    Element head = document.createElement("head");
    html.appendChild(head);

    Element element;

    element = document.createElement("meta");
    element.setAttribute("charset", "UTF-8");
    head.appendChild(element);

    element = document.createElement("title");
    element.setTextContent(book.getProperty(BookProperty.Title));
    head.appendChild(element);

    element = document.createElement("link");
    element.setAttribute("rel", "stylesheet");
    element.setAttribute("type", "text/css");
    element.setAttribute("href", "../style/book-style.css");
    head.appendChild(element);

    Element body = document.createElement("body");
    html.appendChild(body);

    return body;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized Path build(Book book) throws BuildException {
    if (this.book == null) {
      this.book = book;
    } else {
      throw new IllegalStateException();
    }

    if (builder == null) {
      try {
        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      } catch (ParserConfigurationException e) {
        throw new BuildException(e);
      }
    }

    if (transformer == null) {
      try {
        transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2");
      } catch (TransformerException e) {
        throw new BuildException(e);
      }
    }

    if (containerDocument == null) {
      containerDocument = builder.newDocument();
      Element container =
          containerDocument.createElementNS(
              "urn:oasis:names:tc:opendocument:xmlns:container", "container");
      container.setAttribute("version", "1.0");
      containerDocument.appendChild(container);

      Element rootfile = containerDocument.createElement("rootfile");
      rootfile.setAttribute("media-type", "application/oebps-package+xml");
      rootfile.setAttribute("full-path", "item/standard.opf");
      container.appendChild(containerDocument.createElement("rootfiles")).appendChild(rootfile);
    }

    try {
      pages = 0;

      setupResources();

      for (Chapter chapter : book.chapters()) {
        build(chapter);
      }

      buildNavigation();
      buildPackage();

      Path path = saveToFile();

      resources.clear();
      contents.clear();
      packageDocument = null;
      this.book = null;

      return path;
    } catch (RuntimeException e) {
      throw new BuildException(e);
    }
  }

  /** */
  private void setupResources() {
    resources.clear();

    Resource resource;

    resource = new Resource();
    resource.setId("book-style");
    resource.setPath(STYLE_PATH.resolve(resource.getId() + ".css"));
    resource.setMediaType("text/css");
    resources.put(resource.getPath(), resource);

    resource = new Resource();
    resource.setId("style-reset");
    resource.setPath(STYLE_PATH.resolve(resource.getId() + ".css"));
    resource.setMediaType("text/css");
    resources.put(resource.getPath(), resource);

    resource = new Resource();
    resource.setId("style-standard");
    resource.setPath(STYLE_PATH.resolve(resource.getId() + ".css"));
    resource.setMediaType("text/css");
    resources.put(resource.getPath(), resource);

    resource = new Resource();
    resource.setId("style-advance");
    resource.setPath(STYLE_PATH.resolve(resource.getId() + ".css"));
    resource.setMediaType("text/css");
    resources.put(resource.getPath(), resource);

    int index = 0;
    for (Path path : book.resources()) {
      String extension = path.getFileName().toString();
      int position = extension.lastIndexOf('.');
      if (position == -1) {
        LOG.warn("resource without extension: {}", path);
        continue;
      }

      extension = extension.substring(position);
      String id = String.format("item-%03d%s", ++index, extension);

      resource = new Resource();
      resource.setId(id);
      resource.setPath(IMAGE_PATH.resolve(id));

      switch (extension) {
        case ".gif":
          resource.setMediaType("image/gif");
          break;

        case ".jpg":
        case ".jpeg":
          resource.setMediaType("image/jpeg");
          break;

        case ".png":
          resource.setMediaType("image/png");
          break;
      }

      resources.put(path, resource);
    }
  }

  /**
   * @param chapter
   * @throws BuildException
   */
  private void build(Chapter chapter) throws BuildException {
    Element body = newHTMLDocument();
    document = body.getOwnerDocument();
    ((Element) document.getFirstChild()).setAttribute("class", "vrtl");

    DocumentResource resource = new DocumentResource();
    resource.setDocument(document);
    resource.setMediaType("application/xhtml+xml");

    String title = chapter.getProperty(BookProperty.Title);
    if (title != null) {
      switch (title) {
        case "表紙":
          body.setAttribute("class", "p-cover");
          body.setAttribute("epub:type", "cover");
          resource.setId("p-cover");
          break;

        case "目次":
          body.setAttribute("class", "p-toc");
          resource.setId("p-toc");
          break;

        case "奥付":
          body.setAttribute("class", "p-colophon");
          resource.setId("p-colophon");
          break;

        default:
          // XXX(dacci): duplicated code
          body.setAttribute("class", "p-text");
          resource.setId(String.format("p-%03d", ++pages));
      }
    } else {
      // XXX(dacci): duplicated code
      body.setAttribute("class", "p-text");
      resource.setId(String.format("p-%03d", ++pages));
    }
    resource.setPath(XHTML_PATH.resolve(resource.getId() + ".xhtml"));

    Element main = build(chapter.getRoot());

    String className = main.getAttribute("class");
    if (className.isEmpty()) {
      className = "main";
    } else {
      className = "main " + className;
    }
    main.setAttribute("class", className);

    body.appendChild(main);
    contents.add(Pair.of(title, resource));

    document = null;
  }

  /**
   * @param paragraph
   * @throws BuildException
   */
  private Element build(BookElement element) throws BuildException {
    Element result = null;

    if (element instanceof Block) {
      result = document.createElement("div");

      StringJoiner joiner = new StringJoiner(" ");
      for (Style style : ((Block) element).styles()) {
        String className = mapToClassName(style);
        if (className != null) {
          joiner.add(className);
        }
      }
      if (joiner.length() > 0) {
        result.setAttribute("class", joiner.toString());
      }

      for (BookElement child : ((Block) element).elements()) {
        result.appendChild(build(child));
      }
    } else if (element instanceof Paragraph) {
      StringJoiner joiner = new StringJoiner(" ");
      for (Style style : ((Paragraph) element).styles()) {
        String className = mapToClassName(style);
        if (className != null) {
          joiner.add(className);
        }
      }

      if (result == null) {
        result = document.createElement("p");
      }

      if (joiner.length() > 0) {
        result.setAttribute("class", joiner.toString());
      }

      for (Segment segment : ((Paragraph) element).getFragment()) {
        result.appendChild(build(segment));
      }
    } else {
      throw new BuildException("Unsupported element: " + element.getClass());
    }

    return result;
  }

  private Node build(Segment segment) throws BuildException {
    if (segment instanceof Fragment) {
      DocumentFragment fragment = document.createDocumentFragment();

      for (Segment child : (Fragment) segment) {
        fragment.appendChild(build(child));
      }

      return fragment;
    } else if (segment instanceof TextSegment) {
      return document.createTextNode(((TextSegment) segment).getText());
    } else if (segment instanceof RubySegment) {
      RubySegment rubySegment = (RubySegment) segment;
      Element element = document.createElement("ruby");

      Node textNode = build(rubySegment.getText());
      element.appendChild(textNode);

      Element rubyElement = document.createElement("rt");
      rubyElement.setTextContent(rubySegment.getRuby());
      element.appendChild(rubyElement);

      return element;
    } else if (segment instanceof StyledSegment) {
      StyledSegment styledSegment = (StyledSegment) segment;
      StringJoiner joiner = new StringJoiner(" ");
      Element element = null;

      for (Style style : styledSegment.styles()) {
        String className = mapToClassName(style);
        if (className != null) {
          joiner.add(className);
        }
      }

      if (element == null) {
        element = document.createElement("span");
      }

      if (joiner.length() > 0) {
        element.setAttribute("class", joiner.toString());
      }

      element.appendChild(build(styledSegment.getSegment()));

      return element;
    } else if (segment instanceof ImageMarker) {
      ImageMarker imageSegment = (ImageMarker) segment;

      Element element = document.createElement("img");

      Resource resource = resources.get(imageSegment.getFile());
      if (resource == null) {
        throw new BuildException("resource not found: " + imageSegment.getFile());
      }

      Path path = XHTML_PATH.relativize(resource.getPath());
      element.setAttribute("src", path.toString().replace('\\', '/'));

      String caption = imageSegment.getCaption();
      if (caption != null) {
        element.setAttribute("alt", caption);
        element.setAttribute("title", caption);

        if (caption.equals("表紙")) {
          resource.setProperties("cover-image");
        }
      }

      if (imageSegment.getWidth() >= 0) {
        element.setAttribute("width", String.valueOf(imageSegment.getWidth()));
      }

      if (imageSegment.getHeight() >= 0) {
        element.setAttribute("height", String.valueOf(imageSegment.getHeight()));
      }

      return element;
    } else if (segment instanceof SimpleMarker) {
      switch ((SimpleMarker) segment) {
        case LineBreak:
          return document.createElement("br");

        default:
          throw new UnsupportedOperationException();
      }
    } else {
      LOG.warn("Unsupported segment: {}", segment.getClass());
      return document.createTextNode(segment.toString());
    }
  }

  /** */
  private void buildNavigation() {
    Element body = newHTMLDocument();
    Document document = body.getOwnerDocument();

    document.getElementsByTagName("title").item(0).setTextContent("Navigation");

    NodeList list = document.getElementsByTagName("link");
    for (int i = 0, l = list.getLength(); i < l; ++i) {
      Node item = list.item(i);
      item.getParentNode().removeChild(item);
    }

    Element nav = document.createElement("nav");
    nav.setAttribute("epub:type", "toc");
    nav.setAttribute("id", "toc");
    body.appendChild(nav);

    nav.appendChild(document.createElement("h1")).setTextContent("Navigation");

    Element ol = document.createElement("ol");
    for (Pair<String, Resource> pair : contents) {
      String title = pair.getKey();
      if (title == null || title.isEmpty()) {
        continue;
      }

      String path = ITEM_PATH.relativize(pair.getValue().getPath()).toString().replace('\\', '/');
      Element anchor = document.createElement("a");
      anchor.setAttribute("href", path);
      anchor.setTextContent(title);

      ol.appendChild(document.createElement("li")).appendChild(anchor);
    }

    nav.appendChild(ol);

    DocumentResource resource = new DocumentResource();
    resource.setId("toc");
    resource.setPath(ITEM_PATH.resolve("navigation-documents.xhtml"));
    resource.setMediaType("application/xhtml+xml");
    resource.setProperties("nav");
    resource.setDocument(document);
    resources.put(resource.getPath(), resource);
  }

  /** */
  private void buildPackage() {
    for (Pair<String, Resource> pair : contents) {
      Resource resource = pair.getValue();
      resources.put(resource.getPath(), resource);
    }

    Document document = builder.newDocument();
    Element root = document.createElementNS("http://www.idpf.org/2007/opf", "package");
    root.setAttribute("xml:lang", "ja");
    root.setAttribute("version", "3.0");
    root.setAttribute("unique-identifier", "unique-id");
    root.setAttribute("prefix", "ebpaj: http://www.ebpaj.jp/");
    document.appendChild(root);

    Element metadata = document.createElement("metadata");
    metadata.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");

    StringJoiner joiner = new StringJoiner("\0");

    Element element;

    element = document.createElement("dc:title");
    element.setAttribute("id", "title");
    element.setTextContent(book.getProperty(BookProperty.Title));
    metadata.appendChild(element);
    joiner.add(book.getProperty(BookProperty.Title));

    element = document.createElement("meta");
    element.setAttribute("refines", "#title");
    element.setAttribute("property", "title-type");
    element.setTextContent("main");
    metadata.appendChild(element);

    if (book.hasProperty(BookProperty.Subtitle)) {
      element = document.createElement("dc:title");
      element.setAttribute("id", "subtitle");
      element.setTextContent(book.getProperty(BookProperty.Subtitle));
      metadata.appendChild(element);
      joiner.add(book.getProperty(BookProperty.Subtitle));

      element = document.createElement("meta");
      element.setAttribute("refines", "#subtitle");
      element.setAttribute("property", "title-type");
      element.setTextContent("subtitle");
      metadata.appendChild(element);
    }

    element = document.createElement("dc:creator");
    element.setAttribute("id", "author");
    element.setTextContent(book.getProperty(BookProperty.Author));
    metadata.appendChild(element);
    joiner.add(book.getProperty(BookProperty.Author));

    element = document.createElement("meta");
    element.setAttribute("refines", "#author");
    element.setAttribute("property", "role");
    element.setAttribute("scheme", "marc:relators");
    element.appendChild(document.createTextNode("aut"));
    metadata.appendChild(element);

    if (book.hasProperty(BookProperty.Translator)) {
      element = document.createElement("dc:creator");
      element.setAttribute("id", "translator");
      element.setTextContent(book.getProperty(BookProperty.Translator));
      metadata.appendChild(element);
      joiner.add(book.getProperty(BookProperty.Translator));

      element = document.createElement("meta");
      element.setAttribute("refines", "#translator");
      element.setAttribute("property", "role");
      element.setAttribute("scheme", "marc:relators");
      element.appendChild(document.createTextNode("trl"));
      metadata.appendChild(element);
    }

    if (book.hasProperty(BookProperty.Series)) {
      String series = book.getProperty(BookProperty.Series);

      element = document.createElement("meta");
      element.setAttribute("id", "series");
      element.setAttribute("property", "belongs-to-collection");
      element.setTextContent(series);
      metadata.appendChild(element);
      joiner.add(series);

      element = document.createElement("meta");
      element.setAttribute("refines", "#series");
      element.setAttribute("property", "collection-type");
      element.setTextContent("series");
      metadata.appendChild(element);

      // Calibre compatible
      element = document.createElement("meta");
      element.setAttribute("name", "calibre:series");
      element.setAttribute("content", series);
      metadata.appendChild(element);

      if (book.hasProperty(BookProperty.SeriesPosition)) {
        String position = book.getProperty(BookProperty.SeriesPosition);

        element = document.createElement("meta");
        element.setAttribute("refines", "#series");
        element.setAttribute("property", "group-position");
        element.setTextContent(position);
        metadata.appendChild(element);
        joiner.add(position);

        // Calibre compatible
        element = document.createElement("meta");
        element.setAttribute("name", "calibre:series_index");
        element.setAttribute("content", position);
        metadata.appendChild(element);
      }
    }

    if (book.hasProperty(BookProperty.Set)) {
      element = document.createElement("meta");
      element.setAttribute("id", "set");
      element.setAttribute("property", "belongs-to-collection");
      element.setTextContent(book.getProperty(BookProperty.Set));
      metadata.appendChild(element);
      joiner.add(book.getProperty(BookProperty.Set));

      element = document.createElement("meta");
      element.setAttribute("refines", "#set");
      element.setAttribute("property", "collection-type");
      element.setTextContent("set");
      metadata.appendChild(element);

      if (book.hasProperty(BookProperty.SetPosition)) {
        element = document.createElement("meta");
        element.setAttribute("refines", "#set");
        element.setAttribute("property", "group-position");
        element.setTextContent(book.getProperty(BookProperty.SetPosition));
        metadata.appendChild(element);
        joiner.add(book.getProperty(BookProperty.SetPosition));
      }
    }

    metadata.appendChild(document.createElement("dc:language")).setTextContent("ja");

    UUID uuid = UUID.nameUUIDFromBytes(joiner.toString().getBytes(StandardCharsets.UTF_8));
    element = document.createElement("dc:identifier");
    element.setAttribute("id", "unique-id");
    element.setTextContent("urn:uuid:" + uuid.toString());
    metadata.appendChild(element);

    element = document.createElement("meta");
    element.setAttribute("property", "dcterms:modified");
    element.setTextContent(DateTimeFormatter.ISO_INSTANT.format(OffsetDateTime.now().withNano(0)));
    metadata.appendChild(element);

    element = document.createElement("meta");
    element.setAttribute("property", "ebpaj:guide-version");
    element.setTextContent("1.1.3");
    metadata.appendChild(element);

    root.appendChild(metadata);

    Element manifest = document.createElement("manifest");

    for (Resource resource : resources.values()) {
      String href = ITEM_PATH.relativize(resource.getPath()).toString().replace('\\', '/');

      Element item = document.createElement("item");
      item.setAttribute("id", resource.getId());
      item.setAttribute("href", href);
      item.setAttribute("media-type", resource.getMediaType());

      if (resource.getFallback() != null) {
        item.setAttribute("fallback", resource.getFallback());
      }

      if (resource.getProperties() != null) {
        item.setAttribute("properties", resource.getProperties());
      }

      if (resource.getMediaOverlay() != null) {
        item.setAttribute("media-overlay", resource.getMediaOverlay());
      }

      manifest.appendChild(item);
    }

    Element spine = document.createElement("spine");
    spine.setAttribute("page-progression-direction", "rtl");

    for (Pair<String, Resource> pair : contents) {
      Resource resource = pair.getValue();
      Element itemref = document.createElement("itemref");
      itemref.setAttribute("linear", "yes");
      itemref.setAttribute("idref", resource.getId());
      itemref.setAttribute("properties", "page-spread-left");
      spine.appendChild(itemref);
    }

    root.appendChild(manifest);
    root.appendChild(spine);

    packageDocument = document;
  }

  /**
   * @return
   * @throws BuildException
   */
  private Path saveToFile() throws BuildException {
    String fileName =
        String.format(
            "%s - %s.epub",
            Util.safeFileName(book.getProperty(BookProperty.Author)),
            Util.safeFileName(book.getProperty(BookProperty.Title)));
    Path path = outputPath.resolve(fileName);

    try (ZipOutputStream stream = new ZipOutputStream(Files.newOutputStream(path))) {
      CRC32 crc32 = new CRC32();
      crc32.update(MIMETYPE);

      ZipEntry entry = new ZipEntry("mimetype");
      entry.setMethod(ZipOutputStream.STORED);
      entry.setSize(MIMETYPE.length);
      entry.setCrc(crc32.getValue());

      stream.putNextEntry(entry);
      stream.write(MIMETYPE);
    } catch (IOException e) {
      throw new BuildException(e);
    }

    try (FileSystem fileSystem =
        FileSystems.newFileSystem(URI.create("jar:" + path.toUri()), Collections.emptyMap())) {
      save(fileSystem.getPath("/"));
    } catch (IOException e) {
      throw new BuildException(e);
    }

    return path;
  }

  /**
   * @param fileSystem
   */
  private void save(Path rootPath) throws BuildException {
    FileSystem fileSystem = rootPath.getFileSystem();
    Path metaInfPath = fileSystem.getPath("META-INF");
    Path itemPath = fileSystem.getPath("item");
    Path stylePath = itemPath.resolve("style");
    Path imagePath = itemPath.resolve("image");
    Path xhtmlPath = itemPath.resolve("xhtml");

    try {
      Files.createDirectories(metaInfPath);
      Files.createDirectories(stylePath);
      Files.createDirectories(imagePath);
      Files.createDirectories(xhtmlPath);
    } catch (IOException e) {
      throw new BuildException(e);
    }

    try (OutputStream stream = Files.newOutputStream(metaInfPath.resolve("container.xml"))) {
      transformer.transform(new DOMSource(containerDocument), new StreamResult(stream));
    } catch (IOException | TransformerException e) {
      throw new BuildException(e);
    }

    try (OutputStream stream = Files.newOutputStream(itemPath.resolve("standard.opf"))) {
      transformer.transform(new DOMSource(packageDocument), new StreamResult(stream));
    } catch (IOException | TransformerException e) {
      throw new BuildException(e);
    }

    ClassLoader classLoader = getClass().getClassLoader();

    for (Map.Entry<Path, Resource> entry : resources.entrySet()) {
      Resource resource = entry.getValue();

      Path path = rootPath;
      for (Path name : resource.getPath()) {
        path = path.resolve(name.getFileName().toString());
      }

      try {
        if (resource instanceof DocumentResource) {
          try (OutputStream out = Files.newOutputStream(path)) {
            transformer.transform(
                new DOMSource(((DocumentResource) resource).getDocument()), new StreamResult(out));
          }
        } else if (resource.getMediaType().equals("text/css")) {
          try (InputStream in = classLoader.getResourceAsStream(path.getFileName().toString())) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
          }
        } else {
          Files.copy(entry.getKey(), path, StandardCopyOption.REPLACE_EXISTING);
        }
      } catch (IOException | TransformerException e) {
        throw new BuildException(e);
      }
    }
  }
}
