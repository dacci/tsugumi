/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.format.epub;

import org.w3c.dom.Document;

/**
 * @author dacci
 */
public class DocumentResource extends Resource {

    private Document document;

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
}
