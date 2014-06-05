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

    public static final String TITLE = "タイトル";

    private final Book book;

    private final PageResource resource;

    private String type;

    private final Map<String, String> properties = new HashMap<>();

    /**
     * 
     */
    Chapter(Book book, PageResource resource) {
        this.book = book;
        this.resource = resource;

        setStyle("p-text");
    }

    /**
     * @return the resource
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
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

    public void build(DocumentBuilder builder) {
        Document document = builder.newDocument();

        Element html = document.createElement("html");
        html.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");
        html.setAttribute("xmlns:epub", "http://www.idpf.org/2007/ops");
        html.setAttribute("xml:lang", "ja");
        document.appendChild(html);

        Element head = document.createElement("head");
        html.appendChild(head);

        Element element = document.createElement("meta");
        element.setAttribute("charset", "UTF-8");
        head.appendChild(element);

        element = document.createElement("title");
        head.appendChild(element);

        element.appendChild(document.createTextNode(book.getTitle()));
        if (properties.containsKey(TITLE)) {
            element.appendChild(document.createTextNode(" - "));
            element.appendChild(document.createTextNode(properties.get(TITLE)));
        }

        element = document.createElement("link");
        element.setAttribute("rel", "stylesheet");
        element.setAttribute("type", "text/css");
        element.setAttribute("href", "../style/book-style.css");
        head.appendChild(element);

        Element body = document.createElement("body");
        html.appendChild(body);

        if (type != null && !type.isEmpty()) {
            body.setAttribute("epub:type", type);
        }

        Element content = (Element) build(document);
        body.setAttribute("class", content.getAttribute("class"));
        content.setAttribute("class", "main");
        body.appendChild(content);

        resource.setDocument(document);
    }
}
