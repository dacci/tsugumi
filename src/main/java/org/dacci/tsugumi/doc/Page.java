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
public class Page extends Paragraph {

    private final String id;

    private final Book book;

    /**
     * @param book
     */
    Page(String id, Book book) {
        this.id = id;
        this.book = book;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the book
     */
    public Book getBook() {
        return book;
    }

    /**
     * @param builder
     * @return
     */
    public Document generate(DocumentBuilder builder) {
        Document document = newDocument(builder);

        Element body = document.createElement("body");
        body.setAttribute("class", "p-text");
        document.getDocumentElement().appendChild(body);

        Element div = (Element) generate(document);
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
