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

    private String link;

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

    /**
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * @param link
     *            the link to set
     */
    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public Node generate(Page page, Document document) {
        Element element = null;

        if (link != null) {
            Item target = null;
            for (Item item : page.getBook().getItems()) {
                if (item instanceof Page &&
                        link.equals(((Page) item).getTitle())) {
                    target = item;
                    break;
                }
            }

            if (target != null) {
                element = document.createElement("a");
                element.setAttribute("href",
                        target.getHref(page.getPath().getParent()).toString());
            }
        }

        if (style != null) {
            if (element == null) {
                element = document.createElement("span");
            }

            element.setAttribute("class", style);
        }

        Node textNode = document.createTextNode(text);
        if (element == null) {
            return textNode;
        } else {
            element.appendChild(textNode);
            return element;
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
