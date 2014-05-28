/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author dacci
 */
public interface Section {

    /**
     * @param document
     * @return
     */
    Node generate(Document document);
}
