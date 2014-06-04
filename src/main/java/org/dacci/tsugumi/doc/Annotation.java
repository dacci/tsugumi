/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author dacci
 */
public class Annotation implements PageElement {

    private static final Logger log = LoggerFactory.getLogger(Annotation.class);

    private final PageElement text;

    private final PageElement type;

    /**
     * @param text
     * @param type
     */
    public Annotation(PageElement text, PageElement type) {
        this.text = text;
        this.type = type;
    }

    /**
     * @return the text
     */
    public PageElement getText() {
        return text;
    }

    /**
     * @return the type
     */
    public PageElement getType() {
        return type;
    }

    @Override
    public int length() {
        return text.length();
    }

    @Override
    public char charAt(int index) {
        return text.charAt(index);
    }

    @Override
    public PageElement subSequence(int start, int end) {
        if (start < 0 || end < 0 || end < start) {
            throw new IllegalArgumentException();
        }
        if (length() < end - start) {
            throw new IndexOutOfBoundsException();
        }

        if (end - start == length()) {
            return this;
        }

        return new Annotation(text.subSequence(start, end), type);
    }

    @Override
    public String toString() {
        return text.toString();
    }

    @Override
    public Element build(Document document) {
        Element element = document.createElement("span");
        element.appendChild(text.build(document));

        String type = new StringBuilder(this.type).toString();
        switch (type) {
        case "ゴシック体":
            element.setAttribute("class", "gfont");
            break;

        default:
            log.warn("Unknown annotation: {}", type);
        }

        return element;
    }
}
