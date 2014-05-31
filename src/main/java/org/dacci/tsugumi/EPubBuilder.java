/*
 * Copyright (c) 2014 NTT DATA SMS Corporation.
 */

package org.dacci.tsugumi;

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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeSet;
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

import org.dacci.tsugumi.doc.Book;
import org.dacci.tsugumi.doc.ImageItem;
import org.dacci.tsugumi.doc.Item;
import org.dacci.tsugumi.doc.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author tsudasn
 */
public class EPubBuilder {

    private static final Logger log = LoggerFactory
            .getLogger(EPubBuilder.class);

    private static final byte[] MIMETYPE = "application/epub+zip"
            .getBytes(StandardCharsets.UTF_8);

    private static final String DOCTYPE_HTML5 = "<!DOCTYPE html>";

    private static final byte[] LINE_SEPARATOR = System.getProperty(
            "line.separator").getBytes();

    private static final Path ROOT_PATH = Paths.get(".");

    private static final Path ITEM_PATH = ROOT_PATH.resolve("item");

    @SuppressWarnings("unused")
    private static final Path STYLE_PATH = ITEM_PATH.resolve("style");

    private static final Path IMAGE_PATH = ITEM_PATH.resolve("image");

    private static final Path XHTML_PATH = ITEM_PATH.resolve("xhtml");

    private static final Collection<String> styles;

    static {
        Collection<String> list = new TreeSet<>();
        list.add("book-style");
        list.add("style-advance");
        list.add("style-check");
        list.add("style-reset");
        list.add("style-standard");

        styles = Collections.unmodifiableCollection(list);
    }

    private DocumentBuilder builder;

    private Transformer transformer;

    private final Collection<ImageItem> images = new LinkedHashSet<>();

    private final Map<String, Document> pages = new LinkedHashMap<>();

    private Document containerDocument;

    private Document packageDocument;

    private Document navigationDocument;

    /**
     * @throws BuilderException
     */
    public EPubBuilder() throws BuilderException {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "2");
        } catch (ParserConfigurationException | TransformerException e) {
            throw new BuilderException(e);
        }
    }

    /**
     * @param book
     * @throws BuilderException
     */
    public void build(Book book) throws BuilderException {
        images.clear();
        pages.clear();

        if (book.hasCoverImage()) {
            book.getCoverImage().setId("cover");
        }

        int imageIndex = 0;
        for (Item item : book.getItems()) {
            if (!(item instanceof ImageItem)) {
                continue;
            }

            ImageItem image = (ImageItem) item;

            if (image.getId() == null) {
                image.setId(String.format("img-%03d", ++imageIndex));
            }

            item.setPath(IMAGE_PATH.resolve(image.getId() + "." +
                    image.getExtension()));

            images.add(image);
        }

        int pageIndex = 0;
        for (Item item : book.getItems()) {
            if (!(item instanceof Page)) {
                continue;
            }

            Page page = (Page) item;

            if (page.getId() == null) {
                page.setId(String.format("p-%03d", ++pageIndex));
            }

            page.setPath(XHTML_PATH.resolve(page.getId() + ".xhtml"));
        }

        for (Item item : book.getItems()) {
            if (!(item instanceof Page)) {
                continue;
            }

            Page page = (Page) item;

            log.debug("Building {}", page.getHref(ROOT_PATH));

            Document document = page.generate(builder);
            pages.put(page.getHref(ROOT_PATH), document);
        }

        containerDocument = buildContainer();
        packageDocument = buildPackage(book);
        navigationDocument = buildNavigation(book);
    }

    /**
     * @return
     */
    private Document buildContainer() {
        Document document = builder.newDocument();

        Element container = document.createElement("container");
        container.setAttribute("xmlns",
                "urn:oasis:names:tc:opendocument:xmlns:container");
        container.setAttribute("version", "1.0");
        document.appendChild(container);

        Element rootfiles = document.createElement("rootfiles");
        container.appendChild(rootfiles);

        Element rootfile = document.createElement("rootfile");
        rootfile.setAttribute("full-path", "item/standard.opf");
        rootfile.setAttribute("media-type", "application/oebps-package+xml");
        rootfiles.appendChild(rootfile);

        return document;
    }

    /**
     * @param book
     * @return
     */
    private Document buildPackage(Book book) {
        Document document = builder.newDocument();

        Element root = document.createElement("package");
        root.setAttribute("xmlns", "http://www.idpf.org/2007/opf");
        root.setAttribute("xml:lang", "ja");
        root.setAttribute("version", "3.0");
        root.setAttribute("unique-identifier", "unique-id");
        root.setAttribute("prefix", "ebpaj: http://www.ebpaj.jp/");
        document.appendChild(root);

        Element metadata = document.createElement("metadata");
        metadata.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
        root.appendChild(metadata);

        Element element = document.createElement("dc:title");
        element.appendChild(document.createTextNode(book.getTitle()));
        metadata.appendChild(element);

        element = document.createElement("dc:creator");
        element.appendChild(document.createTextNode(book.getAuthor()));
        metadata.appendChild(element);

        element = document.createElement("dc:language");
        element.appendChild(document.createTextNode("ja"));
        metadata.appendChild(element);

        element = document.createElement("dc:identifier");
        element.setAttribute("id", "unique-id");
        element.appendChild(document.createTextNode(book.getUniqueId()));
        metadata.appendChild(element);

        element = document.createElement("meta");
        element.setAttribute("property", "dcterms:modified");
        element.appendChild(document.createTextNode(ZonedDateTime
                .now(ZoneOffset.UTC).withNano(0)
                .format(DateTimeFormatter.ISO_INSTANT)));

        metadata.appendChild(element);

        element = document.createElement("meta");
        element.setAttribute("property", "ebpaj:guide-version");
        element.appendChild(document.createTextNode("1.1.1"));
        metadata.appendChild(element);

        Element manifest = document.createElement("manifest");
        root.appendChild(manifest);

        element = document.createElement("item");
        element.setAttribute("media-type", "application/xhtml+xml");
        element.setAttribute("id", "toc");
        element.setAttribute("href", "navigation-documents.xhtml");
        element.setAttribute("properties", "nav");
        manifest.appendChild(element);

        Element spine = document.createElement("spine");
        spine.setAttribute("page-progression-direction", "rtl");
        root.appendChild(spine);

        for (String style : styles) {
            Element item = document.createElement("item");
            item.setAttribute("media-type", "text/css");
            item.setAttribute("id", style);
            item.setAttribute("href", "style/" + style + ".css");
            manifest.appendChild(item);
        }

        for (Item item : book.getItems()) {
            element = document.createElement("item");
            element.setAttribute("id", item.getId());
            element.setAttribute("media-type", item.getMediaType());
            element.setAttribute("href", item.getHref(ITEM_PATH));

            if (item instanceof ImageItem && item.getId().equals("cover")) {
                element.setAttribute("properties", "cover-image");
            }

            manifest.appendChild(element);

            if (item instanceof Page) {
                element = document.createElement("itemref");
                element.setAttribute("linear", "yes");
                element.setAttribute("idref", item.getId());
                element.setAttribute("properties", "page-spread-left");
                spine.appendChild(element);
            }
        }

        return document;
    }

    /**
     * @param book
     * @return
     */
    private Document buildNavigation(Book book) {
        Document document = builder.newDocument();

        Element html = document.createElement("html");
        html.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");
        html.setAttribute("xmlns:epub", "http://www.idpf.org/2007/ops");
        html.setAttribute("xml:lang", "ja");
        document.appendChild(html);

        Element head = document.createElement("head");
        html.appendChild(head);

        Element body = document.createElement("body");
        html.appendChild(body);

        Element nav = document.createElement("nav");
        nav.setAttribute("epub:type", "toc");
        nav.setAttribute("id", "toc");
        body.appendChild(nav);

        Element element = document.createElement("h1");
        element.appendChild(document.createTextNode("Navigation"));
        nav.appendChild(element);

        Element ol = document.createElement("ol");
        nav.appendChild(ol);

        for (Item item : book.getItems()) {
            if (!(item instanceof Page)) {
                continue;
            }

            Page page = (Page) item;
            if (page.getTitle() == null) {
                continue;
            }

            Element a = document.createElement("a");
            a.setAttribute("href", page.getHref(ITEM_PATH));
            a.appendChild(document.createTextNode(page.getTitle()));
            ol.appendChild(document.createElement("li")).appendChild(a);
        }

        return document;
    }

    /**
     * @param path
     * @throws IOException
     */
    public void saveToFile(Path path) throws IOException {
        try (ZipOutputStream out =
                new ZipOutputStream(Files.newOutputStream(path))) {
            CRC32 crc32 = new CRC32();
            crc32.update(MIMETYPE);

            ZipEntry mimetype = new ZipEntry("mimetype");
            mimetype.setMethod(ZipOutputStream.STORED);
            mimetype.setSize(MIMETYPE.length);
            mimetype.setCrc(crc32.getValue());

            out.putNextEntry(mimetype);
            out.write(MIMETYPE);
        }

        URI uri = URI.create("jar:" + path.toUri());
        try (FileSystem fileSystem =
                FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            saveTo(fileSystem.getPath("/"));
        }
    }

    /**
     * @param path
     * @throws IOException
     */
    public void saveToDirectory(Path path) throws IOException {
        Path basePath = Files.createDirectories(path);

        try (OutputStream out =
                Files.newOutputStream(basePath.resolve("mimetype"))) {
            out.write(MIMETYPE);
        }

        saveTo(basePath);
    }

    /**
     * @param path
     * @throws IOException
     */
    private void saveTo(Path basePath) throws IOException {
        Path metaInfPath =
                Files.createDirectories(basePath.resolve("META-INF"));

        try (OutputStream out =
                Files.newOutputStream(metaInfPath.resolve("container.xml"))) {
            writeDocument(containerDocument, out);
        } catch (TransformerException e) {
            throw new IOException(e);
        }

        Path itemPath = Files.createDirectories(basePath.resolve("item"));

        try (OutputStream out =
                Files.newOutputStream(itemPath.resolve("standard.opf"))) {
            writeDocument(packageDocument, out);
        } catch (TransformerException e) {
            throw new IOException(e);
        }

        try (OutputStream out =
                Files.newOutputStream(itemPath
                        .resolve("navigation-documents.xhtml"))) {
            writePage(navigationDocument, out);
        } catch (TransformerException e) {
            throw new IOException(e);
        }

        Path stylePath = Files.createDirectories(itemPath.resolve("style"));
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (String style : styles) {
            try (InputStream in = loader.getResourceAsStream(style + ".css")) {
                Files.copy(in, stylePath.resolve(style + ".css"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }

        Files.createDirectories(itemPath.resolve("image"));

        for (ImageItem image : images) {
            try (InputStream in = Files.newInputStream(image.getSource())) {
                Files.copy(image.getSource(),
                        basePath.resolve(image.getPath().toString()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }

        Files.createDirectories(itemPath.resolve("xhtml"));

        for (Map.Entry<String, Document> entry : pages.entrySet()) {
            try (OutputStream out =
                    Files.newOutputStream(basePath.resolve(entry.getKey()))) {
                writePage(entry.getValue(), out);
            } catch (TransformerException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * @param document
     * @param stream
     * @throws IOException
     * @throws TransformerException
     */
    private void writePage(Document document, OutputStream stream)
            throws IOException, TransformerException {
        stream.write(DOCTYPE_HTML5.getBytes());
        stream.write(LINE_SEPARATOR);

        writeDocument(document, stream);
    }

    /**
     * @param document
     * @param stream
     * @throws TransformerException
     */
    private void writeDocument(Document document, OutputStream stream)
            throws TransformerException {
        transformer
                .transform(new DOMSource(document), new StreamResult(stream));
    }
}
