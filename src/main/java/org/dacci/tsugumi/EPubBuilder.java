/*
 * Copyright (c) 2014 dacci.org
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
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
import org.dacci.tsugumi.doc.ImageResource;
import org.dacci.tsugumi.doc.PageResource;
import org.dacci.tsugumi.doc.Resource;
import org.dacci.tsugumi.doc.StyleResource;
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

    private static final byte[] MIMETYPE = "application/epub+zip"
            .getBytes(StandardCharsets.UTF_8);

    private static final byte[] DOCTYPE_HTML5 = "<!DOCTYPE html>"
            .getBytes(StandardCharsets.UTF_8);

    private static final byte[] LINE_SEPARATOR = System.getProperty(
            "line.separator").getBytes(StandardCharsets.UTF_8);

    private static final Path ROOT_PATH = Paths.get(".");

    private static final Path ITEM_PATH = ROOT_PATH.resolve("item");

    private static final Path IMAGE_PATH = ITEM_PATH.resolve("image");

    private static final Path XHTML_PATH = ITEM_PATH.resolve("xhtml");

    private static final Path STYLE_PATH = ITEM_PATH.resolve("style");

    private final DocumentBuilder builder;

    private final Transformer transformer;

    private final EnumMap<EPubOption, Object> options = new EnumMap<>(
            EPubOption.class);

    private final List<Resource> resources = new ArrayList<>();

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
     * @param option
     * @return
     */
    public Object getOption(EPubOption option) {
        return options.get(option);
    }

    /**
     * @param option
     * @param value
     */
    public void setOption(EPubOption option, Object value) {
        options.put(option, value);
    }

    /**
     * @param book
     * @throws BuilderException
     */
    public void build(Book book) throws BuilderException {
        resources.clear();
        resources.addAll(book.getResources());
        containerDocument = null;
        packageDocument = null;
        navigationDocument = null;

        log.debug("Resolving resources");

        int index = 0;
        for (Chapter chapter : book.getChapters()) {
            String id = null;
            String title = chapter.getProperty(Chapter.TITLE);
            if (title != null) {
                switch (title) {
                case "表紙":
                    id = "p-cover";
                    chapter.setStyle("p-cover");
                    chapter.setType("cover");
                    break;

                case "目次":
                    id = "p-toc";
                    break;
                }
            }

            if (id == null) {
                id = String.format("p-%03d", ++index);
            }

            chapter.getResource().setId(id);
        }

        index = 0;
        for (Resource resource : book.getResources()) {
            Path basePath = null;

            if (resource instanceof PageResource) {
                basePath = XHTML_PATH;
            } else if (resource instanceof ImageResource) {
                basePath = IMAGE_PATH;

                if (resource.getId() == null) {
                    resource.setId(String.format("img-%03d", ++index));
                }
            } else if (resource instanceof StyleResource) {
                basePath = STYLE_PATH;
            }

            resource.setDestination(basePath.resolve(resource.getFileName()));
        }

        log.debug("Building pages");

        EPubOption direction = (EPubOption) options.get(EPubOption.Direction);

        for (Chapter chapter : book.getChapters()) {
            chapter.build(builder);

            if (direction != null) {
                Document document =
                        ((PageResource) chapter.getResource()).getDocument();
                Element html = (Element) document.getFirstChild();

                switch (direction) {
                case LeftToRight:
                    html.setAttribute("class", "hltr");
                    break;

                case RightToLeft:
                    html.setAttribute("class", "vrtl");
                    break;

                default:
                    log.warn("Invalid value for direction: {}", direction);
                }
            }
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

        element = document.createElement("meta");
        element.setAttribute("refines", "#title");
        element.setAttribute("property", "title-type");
        element.appendChild(document.createTextNode("main"));
        metadata.appendChild(element);

        String subtitle = book.getSubtitle();
        if (subtitle != null && !subtitle.isEmpty()) {
            element = document.createElement("dc:title");
            element.setAttribute("id", "subtitle");
            element.appendChild(document.createTextNode(book.getTitle()));
            metadata.appendChild(element);

            element = document.createElement("meta");
            element.setAttribute("refines", "#subtitle");
            element.setAttribute("property", "title-type");
            element.appendChild(document.createTextNode("subtitle"));
            metadata.appendChild(element);
        }

        element = document.createElement("dc:creator");
        element.setAttribute("id", "author");
        element.appendChild(document.createTextNode(book.getAuthor()));
        metadata.appendChild(element);

        element = document.createElement("meta");
        element.setAttribute("refines", "#author");
        element.setAttribute("property", "role");
        element.setAttribute("scheme", "marc:relators");
        element.appendChild(document.createTextNode("aut"));
        metadata.appendChild(element);

        String translator = book.getTranslator();
        if (translator != null && !translator.isEmpty()) {
            element = document.createElement("dc:creator");
            element.setAttribute("id", "translator");
            element.appendChild(document.createTextNode(translator));
            metadata.appendChild(element);

            element = document.createElement("meta");
            element.setAttribute("refines", "#translator");
            element.setAttribute("property", "role");
            element.setAttribute("scheme", "marc:relators");
            element.appendChild(document.createTextNode("trl"));
            metadata.appendChild(element);
        }

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

        if (options.containsKey(EPubOption.Direction)) {
            EPubOption direction =
                    (EPubOption) options.get(EPubOption.Direction);
            switch (direction) {
            case LeftToRight:
                spine.setAttribute("page-progression-direction", "ltr");
                break;

            case RightToLeft:
                spine.setAttribute("page-progression-direction", "rtl");
                break;

            default:
                log.warn("Invalid value for direction: {}", direction);
            }
        }

        root.appendChild(spine);

        element = document.createElement("item");
        element.setAttribute("id", "toc");
        element.setAttribute("media-type", "application/xhtml+xml");
        element.setAttribute("href", "navigation-documents.xhtml");
        element.setAttribute("properties", "nav");
        manifest.appendChild(element);

        for (Resource resource : resources) {
            element = document.createElement("item");
            element.setAttribute("id", resource.getId());
            element.setAttribute("media-type", resource.getMediaType());
            element.setAttribute("href", resource.getHref(ITEM_PATH));

            String properties = resource.getProperties();
            if (properties != null) {
                element.setAttribute("properties", properties);
            }

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
        Files.createDirectories(itemPath.resolve("style"));
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
            out.write(DOCTYPE_HTML5);
            out.write(LINE_SEPARATOR);

            transformer.transform(new DOMSource(navigationDocument),
                    new StreamResult(out));
        } catch (TransformerException e) {
            throw new IOException(e);
        }

        for (Resource resource : resources) {
            if (resource instanceof PageResource) {
                writePage(path, (PageResource) resource);
            } else if (resource instanceof ImageResource) {
                writeResource(path, (ImageResource) resource);
            } else if (resource instanceof StyleResource) {
                writeResource(path, (StyleResource) resource);
            }
        }
    }

    /**
     * @param path
     * @param resource
     */
    private void writeResource(Path path, StyleResource resource)
            throws IOException {
        Path destination = path.resolve(resource.getDestination().toString());
        try (InputStream in =
                getClass().getClassLoader().getResourceAsStream(
                        resource.getFileName())) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * @param path
     * @param resource
     * @throws IOException
     */
    private void writeResource(Path path, ImageResource resource)
            throws IOException {
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
