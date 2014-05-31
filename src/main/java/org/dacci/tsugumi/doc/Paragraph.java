/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author dacci
 */
public class Paragraph implements Section {

    private final List<Section> elements = new ArrayList<>();

    private String style;

    private boolean block = false;

    /**
     * 
     */
    Paragraph() {
    }

    /**
     * @return the elements
     */
    public List<Section> getElements() {
        return elements;
    }

    /**
     * @param element
     * @return
     */
    public boolean addElement(Section element) {
        return elements.add(element);
    }

    /**
     * @return
     */
    public Caption addCaption() {
        block = true;

        Caption caption = new Caption();
        elements.add(caption);

        return caption;
    }

    /**
     * @return
     */
    public Paragraph addParagraph() {
        block = true;

        Paragraph paragraph = new Paragraph();
        elements.add(paragraph);

        return paragraph;
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
    public Node generate(Page page, Document document) {
        Element paragraph = document.createElement(block ? "div" : "p");

        if (style != null && !style.isEmpty()) {
            paragraph.setAttribute("class", style);
        }

        if (elements.isEmpty()) {
            paragraph.appendChild(document.createElement("br"));
        } else {
            for (Section element : elements) {
                paragraph.appendChild(element.generate(page, document));
            }
        }

        return paragraph;
    }
}
