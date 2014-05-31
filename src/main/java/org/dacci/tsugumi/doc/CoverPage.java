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
        super(book);

        setId("p-cover");
        setStyle("p-cover");
        setTitle("表紙");

        Image image = new Image(book.getCoverImage());
        getParagraph().addParagraph().addElement(image);
    }

    @Override
    public Document generate(DocumentBuilder builder) {
        Document document = super.generate(builder);

        Element img = (Element) document.getElementsByTagName("img").item(0);
        img.setAttribute("class", "fit");

        return document;
    }
}
