/*
 * Copyright (c) 2014 NTT DATA SMS Corporation.
 */

package org.dacci.tsugumi.doc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author tsudasn
 */
public class Caption extends Paragraph {

    /**
     * 
     */
    Caption() {
    }

    @Override
    public Node generate(Page page, Document document) {
        Element caption = document.createElement("h3");

        for (Section element : getElements()) {
            caption.appendChild(element.generate(page, document));
        }

        return caption;
    }
}
