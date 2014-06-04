/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.dacci.tsugumi.doc.Chapter;
import org.dacci.tsugumi.doc.PageResource;
import org.dacci.tsugumi.doc.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author dacci
 */
public class EPubBuilder {

    private static final Logger log = LoggerFactory
            .getLogger(EPubBuilder.class);

    private static final byte[] MIMETYPE = "application/epub+zip".getBytes();

    private static final byte[] DOCTYPE_HTML5 = "<!DOCTYPE html>".getBytes();

    private static final byte[] LINE_SEPARATOR = System.getProperty(
            "line.separator").getBytes();

    private static final Path ROOT_PATH = Paths.get(".");

    private static final Path ITEM_PATH = ROOT_PATH.resolve("item");

    private static final Path IMAGE_PATH = ITEM_PATH.resolve("image");

    private static final Collection<String> styles;

    static {
        Set<String> set = new HashSet<>();
        set.add("book-style");
        set.add("style-advance");
        set.add("style-check");
        set.add("style-reset");
        set.add("style-standard");
        styles = Collections.unmodifiableCollection(set);
    }

    private final DocumentBuilder builder;

    private final Transformer transformer;

    private final List<Resource> resources = new ArrayList<>();

    private boolean used = false;

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
        if (used) {
            throw new IllegalStateException("this instance is already used.");
        }

        used = true;
        resources.clear();
        resources.addAll(book.getResources());

        log.debug("Resolving resources");

        int index = 0;
        for (Resource resource : book.getResources()) {
            if (resource instanceof PageResource) {
                continue;
            }

            if (resource.getId() == null) {
                String id = String.format("img-%03d", ++index);
                resource.setId(id);
            }

            if (resource.getDestination() == null) {
                String extension = "";
                switch (resource.getMediaType()) {
                case "image/gif":
                    extension = ".gif";
                    break;

                case "image/jpeg":
                    extension = ".jpg";
                    break;

                case "image/png":
                    extension = ".png";
                    break;
                }

                resource.setDestination(IMAGE_PATH.resolve(resource.getId() +
                        extension));
            }
        }

        log.debug("Building pages");

        for (Chapter chapter : book.getChapters()) {
            chapter.build(builder);
        }

        buildContainer();
        buildPackage(book);
        buildNavigation(book);
    }

    /**
     * 
     */
    private void buildContainer() {
        log.debug("Building container");

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

        containerDocument = document;
    }

    /**
     * @param book
     */
    private void buildPackage(Book book) {
        log.debug("Building package");

        Document document = builder.newDocument();

        Element root = document.createElement("package");
        root.setAttribute("xmlns", "http://www.idpf.org/2007/opf");
        root.setAttribute("version", "3.0");
        root.setAttribute("xml:lang", "ja");
        root.setAttribute("unique-identifier", "unique-id");
        root.setAttribute("prefix", "ebpaj: http://www.ebpaj.jp/");
        document.appendChild(root);

        Element metadata = document.createElement("metadata");
        metadata.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
        root.appendChild(metadata);

        Element element = document.createElement("dc:title");
        element.setAttribute("id", "title");
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

        Element spine = document.createElement("spine");
        spine.setAttribute("page-progression-direction", "rtl");
        root.appendChild(spine);

        element = document.createElement("item");
        element.setAttribute("id", "toc");
        element.setAttribute("media-type", "application/xhtml+xml");
        element.setAttribute("href", "navigation-documents.xhtml");
        element.setAttribute("properties", "nav");
        manifest.appendChild(element);

        for (String style : styles) {
            element = document.createElement("item");
            element.setAttribute("id", style);
            element.setAttribute("media-type", "text/css");
            element.setAttribute("href", "style/" + style + ".css");
            manifest.appendChild(element);
        }

        for (Resource resource : resources) {
            element = document.createElement("item");
            element.setAttribute("id", resource.getId());
            element.setAttribute("media-type", resource.getMediaType());
            element.setAttribute("href", resource.getHref(ITEM_PATH));
            manifest.appendChild(element);

            if (resource instanceof PageResource) {
                element = document.createElement("itemref");
                element.setAttribute("idref", resource.getId());
                spine.appendChild(element);
            }
        }

        packageDocument = document;
    }

    /**
     * @param book
     */
    private void buildNavigation(Book book) {
        log.debug("Building navigation");

        Document document = builder.newDocument();

        Element html = document.createElement("html");
        html.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");
        html.setAttribute("xmlns:epub", "http://www.idpf.org/2007/ops");
        html.setAttribute("xml:lang", "ja");
        document.appendChild(html);

        Element head = document.createElement("head");
        html.appendChild(head);

        ((Element) head.appendChild(document.createElement("meta")))
                .setAttribute("charset", "UTF-8");

        head.appendChild(document.createElement("title")).appendChild(
                document.createTextNode("Navigation"));

        Element body = document.createElement("body");
        html.appendChild(body);

        Element nav = document.createElement("nav");
        nav.setAttribute("id", "toc");
        nav.setAttribute("epub:type", "toc");
        body.appendChild(nav);

        nav.appendChild(document.createElement("h1")).appendChild(
                document.createTextNode("Navigation"));

        Element ol = (Element) nav.appendChild(document.createElement("ol"));

        for (Chapter chapter : book.getChapters()) {
            String title = chapter.getProperty(Chapter.TITLE);
            if (title == null) {
                continue;
            }

            Element a = document.createElement("a");
            a.setAttribute("href", chapter.getResource().getHref(ITEM_PATH));
            a.appendChild(document.createTextNode(title));

            ol.appendChild(document.createElement("li")).appendChild(a);
        }

        navigationDocument = document;
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

            ZipEntry entry = new ZipEntry("mimetype");
            entry.setMethod(ZipOutputStream.STORED);
            entry.setSize(MIMETYPE.length);
            entry.setCrc(crc32.getValue());

            out.putNextEntry(entry);
            out.write(MIMETYPE);
        }

        try (FileSystem fileSystem =
                FileSystems.newFileSystem(URI.create("jar:" + path.toUri()),
                        Collections.emptyMap())) {
            saveTo(fileSystem.getPath("/"));
        }
    }

    /**
     * @param path
     * @throws IOException
     */
    public void saveToDirectory(Path path) throws IOException {
        Files.createDirectories(path);

        try (OutputStream out = Files.newOutputStream(path.resolve("mimetype"))) {
            out.write(MIMETYPE);
        }

        saveTo(path);
    }

    /**
     * @param path
     * @throws IOException
     */
    private void saveTo(Path path) throws IOException {
        Path metaInfPath = Files.createDirectories(path.resolve("META-INF"));
        Path itemPath = Files.createDirectories(path.resolve("item"));
        Path stylePath = Files.createDirectories(itemPath.resolve("style"));
        Files.createDirectories(itemPath.resolve("image"));
        Files.createDirectories(itemPath.resolve("xhtml"));

        try (OutputStream out =
                Files.newOutputStream(metaInfPath.resolve("container.xml"))) {
            transformer.transform(new DOMSource(containerDocument),
                    new StreamResult(out));
        } catch (TransformerException e) {
            throw new IOException(e);
        }

        try (OutputStream out =
                Files.newOutputStream(itemPath.resolve("standard.opf"))) {
            transformer.transform(new DOMSource(packageDocument),
                    new StreamResult(out));
        } catch (TransformerException e) {
            throw new IOException(e);
        }

        try (OutputStream out =
                Files.newOutputStream(itemPath
                        .resolve("navigation-documents.xhtml"))) {
            transformer.transform(new DOMSource(navigationDocument),
                    new StreamResult(out));
        } catch (TransformerException e) {
            throw new IOException(e);
        }

        ClassLoader loader = getClass().getClassLoader();
        for (String style : styles) {
            String name = style + ".css";
            try (InputStream in = loader.getResourceAsStream(name)) {
                Files.copy(in, stylePath.resolve(name),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }

        for (Resource resource : resources) {
            if (resource instanceof PageResource) {
                writePage(path, (PageResource) resource);
            } else {
                writeResource(path, resource);
            }
        }
    }

    /**
     * @param path
     * @param resource
     * @throws IOException
     */
    private void writeResource(Path path, Resource resource) throws IOException {
        Path destination = path.resolve(resource.getDestination().toString());
        Files.copy(resource.getSource(), destination,
                StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * @param path
     * @param resource
     * @throws IOException
     */
    private void writePage(Path path, PageResource resource) throws IOException {
        Path destination = path.resolve(resource.getDestination().toString());

        try (OutputStream out = Files.newOutputStream(destination)) {
            out.write(DOCTYPE_HTML5);
            out.write(LINE_SEPARATOR);

            transformer.transform(new DOMSource(resource.getDocument()),
                    new StreamResult(out));
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }
}
