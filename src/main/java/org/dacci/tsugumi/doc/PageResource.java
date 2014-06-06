/*
 * Copyright (c) 2014 NTT DATA SMS Corporation.
 */

package org.dacci.tsugumi.doc;

import org.w3c.dom.Document;

/**
 * @author tsudasn
 */
public class PageResource extends Resource {

    private Document document;

    /**
     * @param source
     */
    PageResource() {
    }

    /**
     * @return the document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * @param document
     *            the document to set
     */
    public void setDocument(Document document) {
        this.document = document;
    }

    @Override
    public String getFileName() {
        return getId() + ".xhtml";
    }

    @Override
    public String getMediaType() {
        return "application/xhtml+xml";
    }
}
