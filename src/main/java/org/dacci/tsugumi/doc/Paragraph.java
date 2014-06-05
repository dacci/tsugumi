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
public class Paragraph {

    private PageElement element;

    protected String style;

    protected int captionLevel = 0;

    public Paragraph(PageElement element) {
        this.element = element;
    }

    protected Paragraph() {
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

    /**
     * @return the captionLevel
     */
    public int getCaptionLevel() {
        return captionLevel;
    }

    /**
     * @param captionLevel
     *            the captionLevel to set
     */
    public void setCaptionLevel(int captionLevel) {
        this.captionLevel = captionLevel;
    }

    public Node build(Document document) {
        String tagName = "p";
        if (captionLevel > 0) {
            tagName = "h" + captionLevel;
        }

        Element paragraph = document.createElement(tagName);

        if (style != null && !style.isEmpty()) {
            paragraph.setAttribute("class", style);
        }

        if (element.length() == 0) {
            paragraph.appendChild(document.createElement("br"));
        } else {
            paragraph.appendChild(element.build(document));
        }

        return paragraph;
    }
}
