/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author dacci
 */
public class Style implements PageElement {

    private final PageElement text;

    private String style;

    /**
     * @param text
     */
    public Style(PageElement text) {
        this(text, null);
    }

    /**
     * @param text
     * @param style
     */
    public Style(PageElement text, String style) {
        this.text = text;
        this.style = style;
    }

    /**
     * @return the text
     */
    public PageElement getText() {
        return text;
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
        this.style = style;
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

        return new Style(text.subSequence(start, end), style);
    }

    @Override
    public String toString() {
        return text.toString();
    }

    @Override
    public Element build(Document document) {
        Element element = document.createElement("span");

        if (style != null && !style.isEmpty()) {
            element.setAttribute("class", style);
        }

        element.appendChild(text.build(document));

        return element;
    }
}
