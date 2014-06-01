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

    private int level = 2;

    /**
     * 
     */
    Caption() {
    }

    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param level
     *            the level to set
     */
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public Node generate(Page page, Document document) {
        Element caption = document.createElement("h" + level);

        for (Section element : getElements()) {
            caption.appendChild(element.generate(page, document));
        }

        return caption;
    }
}
