/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author dacci
 */
public class Text implements PageElement {

    private final String string;

    private final int start;

    private final int length;

    /**
     * @param string
     */
    Text(String string) {
        this(string, 0);
    }

    /**
     * @param string
     * @param start
     */
    Text(String string, int start) {
        this(string, 0, string.length());
    }

    /**
     * @param string
     * @param start
     * @param end
     */
    Text(String string, int start, int length) {
        this.string = string;
        this.start = start;
        this.length = length;
    }

    /**
     * @return the start
     */
    public int getStart() {
        return start;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        if (index >= length) {
            throw new IndexOutOfBoundsException();
        }

        return string.charAt(start + index);
    }

    @Override
    public PageElement subSequence(int start, int end) {
        if (start < 0 || end < 0 || end <= start) {
            throw new IllegalArgumentException();
        }
        if (length < end - start) {
            throw new IndexOutOfBoundsException();
        }

        return new Text(string, this.start + start, end - start);
    }

    @Override
    public String toString() {
        return new StringBuilder("Text[").append(string, start, start + length)
                .append(']').toString();
    }

    @Override
    public Node build(Document document) {
        return document.createTextNode(string.substring(start, start + length));
    }
}
