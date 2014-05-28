/*
 * Copyright (c) 2014 NTT DATA SMS Corporation.
 */

package org.dacci.tsugumi.doc;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author tsudasn
 */
public class TocPage extends Page {

    /**
     * @param book
     */
    TocPage(Book book) {
        super("p-toc", book);
    }

    @Override
    public Document generate(DocumentBuilder builder) {
        Document document = newDocument(builder);

        Element body = document.createElement("body");
        body.setAttribute("class", "p-toc");
        document.getDocumentElement().appendChild(body);

        Element div = document.createElement("div");
        div.setAttribute("class", "main");
        body.appendChild(div);

        Element h1 = document.createElement("h1");
        h1.setAttribute("class", "mokuji-midashi");
        h1.appendChild(document.createTextNode("　目次見出し"));
        div.appendChild(h1);

        for (Page page : getBook().getPages()) {
            Element p = document.createElement("p");
            div.appendChild(p);

            Element a = document.createElement("a");
            a.setAttribute("href", page.getId() + ".xhtml");
            a.appendChild(document.createTextNode(page.getId()));
            p.appendChild(a);
        }

        return document;
    }
}
