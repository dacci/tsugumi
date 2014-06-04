/*
 * Copyright (c) 2014 NTT DATA SMS Corporation.
 */

package org.dacci.tsugumi.doc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ElementParagraph implements Paragraph {

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
        Element container = document.createElement("p");
        container.appendChild(element.build(document));
        return container;
    }
}
