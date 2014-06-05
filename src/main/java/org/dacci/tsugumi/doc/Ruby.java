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
public class Ruby implements PageElement {

    private PageElement text;

    private String ruby;

    /**
     * @param string
     */
    public Ruby(String ruby) {
        this.ruby = ruby;
    }

    /**
     * @return the text
     */
    public PageElement getText() {
        return text;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText(PageElement text) {
        this.text = text;
    }

    /**
     * @return the ruby
     */
    public String getRuby() {
        return ruby;
    }

    /**
     * @param ruby
     *            the ruby to set
     */
    public void setRuby(String ruby) {
        this.ruby = ruby;
    }

    @Override
    public int length() {
        return text.length();
    }

    @Override
    public char charAt(int index) {
        return text.charAt(index);
    }

    @Override
    public PageElement subSequence(int start, int end) {
        if (start != 0 || end - start != length()) {
            throw new UnsupportedOperationException();
        }

        return this;
    }

    @Override
    public String toString() {
        return text.toString();
    }

    @Override
    public Node build(Document document) {
        Element element = document.createElement("ruby");
        element.appendChild(text.build(document));

        element.appendChild(document.createElement("rp")).appendChild(
                document.createTextNode("("));
        element.appendChild(document.createElement("rt")).appendChild(
                document.createTextNode(ruby));
        element.appendChild(document.createElement("rp")).appendChild(
                document.createTextNode(")"));

        return element;
    }
}
