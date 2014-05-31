/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author dacci
 */
public class Page extends Item {

    private final Book book;

    private String title;

    private String style;

    private final Paragraph paragraph = new Paragraph();

    /**
     * @param book
     */
    Page(Book book) {
        this.book = book;

        style = "p-text";
    }

    @Override
    public String getMediaType() {
        return "application/xhtml+xml";
    }

    /**
     * @return the book
     */
    public Book getBook() {
        return book;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the style
     */
    public String getStyle() {
        return style;
    }

    /**
     * @param style
     *            the style to set
     */
    public void setStyle(String style) {
        if (style == null) {
            throw new NullPointerException();
        }

        this.style = style;
    }

    /**
     * @return the paragraph
     */
    public Paragraph getParagraph() {
        return paragraph;
    }

    /**
     * @param builder
     * @return
     */
    public Document generate(DocumentBuilder builder) {
        Document document = newDocument(builder);

        Element body = document.createElement("body");
        body.setAttribute("class", style);
        document.getDocumentElement().appendChild(body);

        Element div = (Element) paragraph.generate(this, document);
        div.setAttribute("class", "main");
        body.appendChild(div);

        return document;
    }

    /**
     * @param builder
     * @return
     */
    protected Document newDocument(DocumentBuilder builder) {
        Document document = builder.newDocument();

        Element html = document.createElement("html");
        html.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");
        html.setAttribute("xmlns:epub", "http://www.idpf.org/2007/ops");
        html.setAttribute("xml:lang", "ja");
        html.setAttribute("class", "vrtl");
        document.appendChild(html);

        Element head = document.createElement("head");
        html.appendChild(head);

        Element meta = document.createElement("meta");
        meta.setAttribute("charset", "UTF-8");
        head.appendChild(meta);

        Element title = document.createElement("title");
        title.appendChild(document.createTextNode(book.getTitle()));
        head.appendChild(title);

        Element link = document.createElement("link");
        link.setAttribute("rel", "stylesheet");
        link.setAttribute("type", "text/css");
        link.setAttribute("href", "../style/book-style.css");
        head.appendChild(link);

        return document;
    }
}
