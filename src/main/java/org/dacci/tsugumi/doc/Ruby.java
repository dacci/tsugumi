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
public class Ruby implements Section {

    private String text;

    private String ruby;

    /**
     * 
     */
    public Ruby() {
    }

    /**
     * @param text
     * @param ruby
     */
    public Ruby(String text, String ruby) {
        this.text = text;
        this.ruby = ruby;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text
     *            the text to set
     */
    public void setText(String text) {
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
    public Node generate(Page page, Document document) {
        Element element = document.createElement("ruby");
        element.appendChild(document.createTextNode(text));

        element.appendChild(document.createElement("rp")).appendChild(
                document.createTextNode("("));
        element.appendChild(document.createElement("rt")).appendChild(
                document.createTextNode(ruby));
        element.appendChild(document.createElement("rp")).appendChild(
                document.createTextNode(")"));

        return element;
    }

    @Override
    public String toString() {
        return String.format("｜%s《%s》", text, ruby);
    }
}
