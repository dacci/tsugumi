/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author dacci
 */
public class ParagraphContainer implements Paragraph {

    private static class ElementParagraph implements Paragraph {

        private final PageElement element;

        public ElementParagraph(PageElement element) {
            this.element = element;
        }

        @Override
        public String toString() {
            return "ElementParagraph[" + element.toString() + "]";
        }

        @Override
        public void add(Paragraph paragraph) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(PageElement element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Node build(Document document) {
            return element.build(document);
        }
    }

    private final Collection<Paragraph> paragraphs = new ArrayList<>();

    private String style;

    private int captionLevel = 0;

    private boolean block = false;

    /**
     * 
     */
    public ParagraphContainer() {
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

    /**
     * @return the paragraphs
     */
    public Collection<Paragraph> getParagraphs() {
        return paragraphs;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("ParagraphContainer[");
        paragraphs.forEach((Object paragraph) -> builder.append(paragraph));
        return builder.append("]").toString();
    }

    @Override
    public void add(Paragraph paragraph) {
        block = true;
        paragraphs.add(paragraph);
    }

    @Override
    public void add(PageElement element) {
        paragraphs.add(new ElementParagraph(element));
    }

    @Override
    public Node build(Document document) {
        String tagName = "p";
        if (block) {
            tagName = "div";
        } else if (captionLevel > 0) {
            tagName = "h" + captionLevel;
        }

        Element element = document.createElement(tagName);

        if (style != null && !style.isEmpty()) {
            element.setAttribute("class", style);
        }

        for (Paragraph paragraph : paragraphs) {
            if (paragraphs.size() == 1 ||
                    paragraph instanceof ParagraphContainer) {
                element.appendChild(paragraph.build(document));
            } else {
                element.appendChild(document.createElement("p")).appendChild(
                        paragraph.build(document));
            }
        }

        return element;
    }
}
