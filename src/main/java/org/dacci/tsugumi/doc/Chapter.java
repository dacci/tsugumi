/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author dacci
 */
public class Chapter extends ParagraphContainer {

    private static final String TITLE = "タイトル";

    private final Book book;

    private final Map<String, String> properties = new HashMap<>();

    /**
     * 
     */
    Chapter(Book book) {
        this.book = book;

        setStyle("main");
    }

    /**
     * @param name
     * @return
     */
    public String getProperty(String name) {
        return properties.get(name);
    }

    /**
     * @param name
     * @param value
     * @return
     */
    public String setProperty(String name, String value) {
        return properties.put(name, value);
    }

    public Document build(DocumentBuilder builder) {
        Document document = builder.newDocument();

        Element html = document.createElement("html");
        document.appendChild(html);

        Element head = document.createElement("head");
        html.appendChild(head);

        Element title = document.createElement("title");
        head.appendChild(title);

        title.appendChild(document.createTextNode(book.getTitle()));
        if (properties.containsKey(TITLE)) {
            title.appendChild(document.createTextNode(" - "));
            title.appendChild(document.createTextNode(properties.get(TITLE)));
        }

        Element body = document.createElement("body");
        html.appendChild(body);

        body.appendChild(build(document));

        return document;
    }
}
