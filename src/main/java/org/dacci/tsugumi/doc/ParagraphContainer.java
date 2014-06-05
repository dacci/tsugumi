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
public class ParagraphContainer extends Paragraph {

    private final Collection<Paragraph> paragraphs = new ArrayList<>();

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

    public void add(Paragraph paragraph) {
        paragraphs.add(paragraph);
    }

    @Override
    public Node build(Document document) {
        Element element = document.createElement("div");

        if (style != null && !style.isEmpty()) {
            element.setAttribute("class", style);
        }

        for (Paragraph paragraph : paragraphs) {
            element.appendChild(paragraph.build(document));
        }

        return element;
    }
}
