/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author dacci
 */
public class Text implements Section {

    private String text;

    private String style;

    /**
     * @param text
     */
    public Text(String text) {
        this.text = text;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText(String text) {
        this.text = text;
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
    public Node generate(Document document) {
        if (style == null || style.isEmpty()) {
            return document.createTextNode(text);
        } else {
            Element element = document.createElement("span");
            element.setAttribute("class", style);
            element.appendChild(document.createTextNode(text));
            return element;
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
