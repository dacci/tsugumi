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

    private PageElement ruby;

    /**
     * @param string
     */
    public Ruby(PageElement ruby) {
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
    public PageElement getRuby() {
        return ruby;
    }

    /**
     * @param ruby
     *            the ruby to set
     */
    public void setRuby(PageElement ruby) {
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
        return "Ruby[" + text + '=' + ruby + ']';
    }

    @Override
    public Node build(Document document) {
        Element element = document.createElement("ruby");
        element.appendChild(text.build(document));

        element.appendChild(document.createElement("rp")).appendChild(
                document.createTextNode("("));
        element.appendChild(document.createElement("rt")).appendChild(
                ruby.build(document));
        element.appendChild(document.createElement("rp")).appendChild(
                document.createTextNode(")"));

        return element;
    }
}
