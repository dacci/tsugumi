/*
 * Copyright (c) 2014 NTT DATA SMS Corporation.
 */

package org.dacci.tsugumi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
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

    private static final String DOCTYPE_HTML5 = "<!DOCTYPE html>";

    private static final byte[] LINE_SEPARATOR = System.getProperty(
            "line.separator").getBytes();

    private static final Collection<String> styles;

    static {
        List<String> list = new ArrayList<>();
        list.add("book-style");
        list.add("style-advance");
        list.add("style-check");
        list.add("style-reset");
        list.add("style-standard");

        styles = Collections.unmodifiableCollection(list);
    }

    private DocumentBuilder builder;

    private Transformer transformer;

    /**
     * @throws BuilderException
     * 
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
    public void build(Book book, Path output) throws BuilderException {
        String name = book.getAuthor() + " - " + book.getTitle();
        Path path = output.resolveSibling(name + ".epub");
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new BuilderException(e);
            }
        }

        try (ZipOutputStream zipStream =
                new ZipOutputStream(new BufferedOutputStream(
                        Files.newOutputStream(path, StandardOpenOption.CREATE)))) {
            byte[] bytes =
                    "application/epub+zip".getBytes(StandardCharsets.UTF_8);

            CRC32 crc32 = new CRC32();
            crc32.update(bytes);

            ZipEntry entry = new ZipEntry("mimetype");
            entry.setSize(bytes.length);
            entry.setCrc(crc32.getValue());

            zipStream.setMethod(ZipOutputStream.STORED);
            zipStream.putNextEntry(entry);
            zipStream.write(bytes);
            zipStream.closeEntry();

            zipStream.setMethod(ZipOutputStream.DEFLATED);
            zipStream.setLevel(9);

            buildContainer(zipStream);
            buildPackage(book, zipStream);

            buildNavigation(book, zipStream);
            buildPages(book, zipStream);
            buildResources(book, zipStream);
        } catch (IOException | TransformerException e) {
            throw new BuilderException(e);
        }
    }

    /**
     * @param zipStream
     * @throws IOException
     * @throws TransformerException
     */
    private void buildContainer(ZipOutputStream zipStream) throws IOException,
            TransformerException {
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

        zipStream.putNextEntry(new ZipEntry("META-INF/container.xml"));
        transformer.transform(new DOMSource(container), new StreamResult(
                zipStream));
        zipStream.closeEntry();
    }

    /**
     * @param zipStream
     * @throws IOException
     * @throws TransformerException
     */
    private void buildPackage(Book book, ZipOutputStream zipStream)
            throws IOException, TransformerException {
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
        element.appendChild(document.createTextNode(UUID.randomUUID()
                .toString()));
        metadata.appendChild(element);

        element = document.createElement("meta");
        element.setAttribute("property", "dcterms:modified");
        element.appendChild(document.createTextNode(new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'").format(Calendar.getInstance(
                TimeZone.getTimeZone("UTC")).getTime())));
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

        Consumer<Page> pageAdder = (Page page) -> {
            Element item = document.createElement("item");
            item.setAttribute("media-type", "application/xhtml+xml");
            item.setAttribute("id", page.getId());
            item.setAttribute("href", "xhtml/" + page.getId() + ".xhtml");
            manifest.appendChild(item);

            Element itemref = document.createElement("itemref");
            itemref.setAttribute("linear", "yes");
            itemref.setAttribute("idref", page.getId());
            itemref.setAttribute("properties", "page-spread-left");
            spine.appendChild(itemref);
        };

        if (book.hasCoverImage()) {
            pageAdder.accept(book.getCoverPage());
        }

        book.getPages().forEach(pageAdder);

        for (String style : styles) {
            Element item = document.createElement("item");
            item.setAttribute("media-type", "text/css");
            item.setAttribute("id", style);
            item.setAttribute("href", "style/" + style + ".css");
            manifest.appendChild(item);
        }

        if (book.hasCoverImage()) {
            Image image = book.getCoverImage();
            String href = "image/" + image.getId() + "." + image.getExtension();

            Element item = document.createElement("item");
            item.setAttribute("media-type", Image.getContentType(image));
            item.setAttribute("id", image.getId());
            item.setAttribute("href", href);
            item.setAttribute("properties", "cover-image");
            manifest.appendChild(item);
        }

        for (Image image : book.getImages()) {
            String href = "image/" + image.getId() + "." + image.getExtension();

            Element item = document.createElement("item");
            item.setAttribute("media-type", Image.getContentType(image));
            item.setAttribute("id", image.getId());
            item.setAttribute("href", href);
            manifest.appendChild(item);
        }

        zipStream.putNextEntry(new ZipEntry("item/standard.opf"));
        transformer.transform(new DOMSource(root), new StreamResult(zipStream));
        zipStream.closeEntry();
    }

    /**
     * @param book
     * @param zipStream
     * @throws IOException
     * @throws TransformerException
     */
    private void buildNavigation(Book book, ZipOutputStream zipStream)
            throws IOException, TransformerException {
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

        Element li = document.createElement("li");
        ol.appendChild(li);

        Element a = document.createElement("a");
        a.setAttribute("href", "xhtml/p-toc.xhtml");
        a.appendChild(document.createTextNode("目次"));
        li.appendChild(a);

        zipStream.putNextEntry(new ZipEntry("item/navigation-documents.xhtml"));
        zipStream.write(DOCTYPE_HTML5.getBytes());
        zipStream.write(LINE_SEPARATOR);

        transformer.transform(new DOMSource(document), new StreamResult(
                zipStream));
        zipStream.closeEntry();
    }

    /**
     * @param book
     * @param zipStream
     * @throws IOException
     * @throws TransformerException
     */
    private void buildPages(Book book, ZipOutputStream zipStream)
            throws IOException, TransformerException {
        if (book.hasCoverImage()) {
            writePage(book.getCoverPage(), zipStream);
        }

        for (Page page : book.getPages()) {
            writePage(page, zipStream);
        }
    }

    /**
     * @param book
     * @param zipStream
     * @throws IOException
     */
    private void buildResources(Book book, ZipOutputStream zipStream)
            throws IOException {
        ClassLoader loader = getClass().getClassLoader();

        for (String style : styles) {
            String name = style + ".css";
            zipStream.putNextEntry(new ZipEntry("item/style/" + name));

            try (InputStream in = loader.getResourceAsStream(name)) {
                ByteStreams.copy(in, zipStream);
            }

            zipStream.closeEntry();
        }

        if (book.hasCoverImage()) {
            writeImage(book.getCoverImage(), zipStream);
        }

        for (Image image : book.getImages()) {
            writeImage(image, zipStream);
        }
    }

    /**
     * @param page
     * @param zipStream
     * @throws IOException
     * @throws TransformerException
     */
    private void writePage(Page page, ZipOutputStream zipStream)
            throws IOException, TransformerException {
        Document document = page.generate(builder);

        zipStream.putNextEntry(new ZipEntry("item/xhtml/" + page.getId() +
                ".xhtml"));

        zipStream.write(DOCTYPE_HTML5.getBytes());
        zipStream.write(LINE_SEPARATOR);

        transformer.transform(new DOMSource(document), new StreamResult(
                zipStream));

        zipStream.closeEntry();
    }

    /**
     * @param image
     * @param zipStream
     * @throws IOException
     */
    private void writeImage(Image image, ZipOutputStream zipStream)
            throws IOException {
        String name = image.getId() + "." + image.getExtension();
        zipStream.putNextEntry(new ZipEntry("item/image/" + name));

        try (BufferedInputStream in =
                new BufferedInputStream(Files.newInputStream(image.getPath()))) {
            ByteStreams.copy(in, zipStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        zipStream.closeEntry();
    }
}
