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
public class CoverPage extends Page {

    /**
     * @param id
     * @param book
     */
    CoverPage(Book book) {
        super("p-cover", book);
    }

    @Override
    public Document generate(DocumentBuilder builder) {
        Document document = newDocument(builder);

        Element body = document.createElement("body");
        body.setAttribute("class", "p-cover");
        document.getDocumentElement().appendChild(body);

        Element div = document.createElement("div");
        div.setAttribute("class", "main");
        body.appendChild(div);

        Element p = document.createElement("p");
        div.appendChild(p);

        Image image = getBook().getCoverImage();
        String src =
                String.format("../image/%s.%s", image.getId(),
                        image.getExtension());

        Element img = document.createElement("img");
        img.setAttribute("class", "fit");
        img.setAttribute("src", src);
        p.appendChild(img);

        return document;
    }
}
