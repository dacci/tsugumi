/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author dacci
 */
public interface Paragraph {

    void add(Paragraph paragraph);

    void add(PageElement element);

    Node build(Document document);
}
