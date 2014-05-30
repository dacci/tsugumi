/*
 * Copyright (c) 2014 NTT DATA SMS Corporation.
 */

package org.dacci.tsugumi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
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
import org.dacci.tsugumi.doc.Image;
import org.dacci.tsugumi.doc.Page;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.io.ByteStreams;

/**
 * @author tsudasn
 */
public class EPubBuilder {

    private static final byte[] MIMETYPE = "application/epub+zip"
            .getBytes(StandardCharsets.UTF_8);

    private static final String DOCTYPE_HTML5 = "<!DOCTYPE html>";

    private static final byte[] LINE_SEPARATOR = System.getProperty(
            "line.separator").getBytes();

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

    private final Map<String, Path> images = new TreeMap<>();

    private final Map<String, Document> pages = new TreeMap<>();

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

        // images
        Consumer<Image> imageAdder = (Image image) -> {
            images.put(image.getId(), image.getPath());
        };

        if (book.hasCoverImage()) {
            imageAdder.accept(book.getCoverImage());
        }

        book.getImages().forEach(imageAdder);

        // pages
        Consumer<Page> pageAdder = (Page page) -> {
            Document document = page.generate(builder);
            pages.put(page.getId(), document);
        };

        if (book.hasCoverImage()) {
            Page page = book.getCoverPage();
            pageAdder.accept(page);
        }

        book.getPages().forEach(pageAdder);

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

        for (Map.Entry<String, Path> entry : images.entrySet()) {
            String id = entry.getKey();
            String name = entry.getValue().getFileName().toString();
            String extension = name.substring(name.lastIndexOf('.') + 1);

            Element item = document.createElement("item");
            item.setAttribute("media-type", Image.getContentType(extension));
            item.setAttribute("id", id);
            item.setAttribute("href", "image/" + id + "." + extension);

            if (id.equals("cover")) {
                item.setAttribute("properties", "cover-image");
            }

            manifest.appendChild(item);
        }

        for (String page : pages.keySet()) {
            Element item = document.createElement("item");
            item.setAttribute("media-type", "application/xhtml+xml");
            item.setAttribute("id", page);
            item.setAttribute("href", "xhtml/" + page + ".xhtml");
            manifest.appendChild(item);

            Element itemref = document.createElement("itemref");
            itemref.setAttribute("linear", "yes");
            itemref.setAttribute("idref", page);
            itemref.setAttribute("properties", "page-spread-left");
            spine.appendChild(itemref);
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

        if (book.hasCoverImage()) {
            Element a = document.createElement("a");
            a.setAttribute("href", "xhtml/p-cover.xhtml");
            a.appendChild(document.createTextNode("表紙"));
            ol.appendChild(document.createElement("li")).appendChild(a);
        }

        Element a = document.createElement("a");
        a.setAttribute("href", "xhtml/p-toc.xhtml");
        a.appendChild(document.createTextNode("目次"));
        ol.appendChild(document.createElement("li")).appendChild(a);

        return document;
    }

    /**
     * @param path
     * @throws IOException
     */
    public void saveToFile(Path path) throws IOException {
        try (ZipOutputStream out =
                new ZipOutputStream(new BufferedOutputStream(
                        Files.newOutputStream(path)))) {
            CRC32 crc32 = new CRC32();
            crc32.update(MIMETYPE);

            ZipEntry mimetype = new ZipEntry("mimetype");
            mimetype.setSize(MIMETYPE.length);
            mimetype.setCrc(crc32.getValue());

            out.setMethod(ZipOutputStream.STORED);
            out.putNextEntry(mimetype);
            out.write(MIMETYPE);
            out.closeEntry();

            out.setMethod(ZipOutputStream.DEFLATED);
            out.setLevel(9);

            out.putNextEntry(new ZipEntry("META-INF/container.xml"));
            writeDocument(containerDocument, out);
            out.closeEntry();

            out.putNextEntry(new ZipEntry("item/standard.opf"));
            writeDocument(packageDocument, out);
            out.closeEntry();

            out.putNextEntry(new ZipEntry("item/navigation-documents.xhtml"));
            writePage(navigationDocument, out);
            out.closeEntry();

            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            for (String style : styles) {
                ZipEntry zipEntry =
                        new ZipEntry("item/style/" + style + ".css");
                out.putNextEntry(zipEntry);

                try (InputStream in =
                        loader.getResourceAsStream(style + ".css")) {
                    ByteStreams.copy(in, out);
                }

                out.closeEntry();
            }

            for (Map.Entry<String, Path> entry : images.entrySet()) {
                String name = entry.getValue().getFileName().toString();
                String extension = name.substring(name.lastIndexOf('.'));

                ZipEntry zipEntry =
                        new ZipEntry("item/image/" + entry.getKey() + extension);
                out.putNextEntry(zipEntry);

                try (BufferedInputStream in =
                        new BufferedInputStream(Files.newInputStream(entry
                                .getValue()))) {
                    ByteStreams.copy(in, out);
                }

                out.closeEntry();
            }

            for (Map.Entry<String, Document> entry : pages.entrySet()) {
                ZipEntry zipEntry =
                        new ZipEntry("item/xhtml/" + entry.getKey() + ".xhtml");
                out.putNextEntry(zipEntry);

                writePage(entry.getValue(), out);

                out.closeEntry();
            }
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    /**
     * @param path
     * @throws IOException
     */
    public void saveToDirectory(Path path) throws IOException {
        Path basePath = Files.createDirectories(path);

        try (BufferedOutputStream out =
                new BufferedOutputStream(Files.newOutputStream(basePath
                        .resolve("mimetype")))) {
            out.write(MIMETYPE);
        }

        Path metaInfPath =
                Files.createDirectories(basePath.resolve("META-INF"));

        try (BufferedOutputStream out =
                new BufferedOutputStream(Files.newOutputStream(metaInfPath
                        .resolve("container.xml")))) {
            writeDocument(containerDocument, out);
        } catch (TransformerException e) {
            throw new IOException(e);
        }

        Path itemPath = Files.createDirectories(basePath.resolve("item"));

        try (BufferedOutputStream out =
                new BufferedOutputStream(Files.newOutputStream(itemPath
                        .resolve("standard.opf")))) {
            writeDocument(packageDocument, out);
        } catch (TransformerException e) {
            throw new IOException(e);
        }

        try (BufferedOutputStream out =
                new BufferedOutputStream(Files.newOutputStream(itemPath
                        .resolve("navigation-documents.xhtml")))) {
            writePage(navigationDocument, out);
        } catch (TransformerException e) {
            throw new IOException(e);
        }

        Path stylePath = Files.createDirectories(itemPath.resolve("style"));
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (String style : styles) {
            try (InputStream in = loader.getResourceAsStream(style + ".css");
                    BufferedOutputStream out =
                            new BufferedOutputStream(
                                    Files.newOutputStream(stylePath
                                            .resolve(style + ".css")))) {
                ByteStreams.copy(in, out);
            }
        }

        Path imagePath = Files.createDirectories(itemPath.resolve("image"));

        for (Map.Entry<String, Path> entry : images.entrySet()) {
            String name = entry.getValue().getFileName().toString();
            String extension = name.substring(name.lastIndexOf('.'));

            try (BufferedInputStream in =
                    new BufferedInputStream(Files.newInputStream(entry
                            .getValue()));
                    BufferedOutputStream out =
                            new BufferedOutputStream(
                                    Files.newOutputStream(imagePath
                                            .resolve(entry.getKey() + extension)))) {
                ByteStreams.copy(in, out);
            }
        }

        Path xhtmlPath = Files.createDirectories(itemPath.resolve("xhtml"));

        for (Map.Entry<String, Document> entry : pages.entrySet()) {
            try (BufferedOutputStream out =
                    new BufferedOutputStream(Files.newOutputStream(xhtmlPath
                            .resolve(entry.getKey() + ".xhtml")))) {
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
