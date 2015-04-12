/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author dacci
 */
public class Block extends BookElement {

    private Chapter chapter = null;

    private final List<BookElement> elements = new ArrayList<>();

    private final Set<Style> styles = new LinkedHashSet<>();

    public BookElement addElement(BookElement element) {
        if (element == null) {
            throw new NullPointerException();
        }
        if (element.getParent() != null) {
            throw new IllegalArgumentException();
        }

        if (!elements.add(element)) {
            return null;
        }

        element.setParent(this);

        return element;
    }

    public Iterable<BookElement> elements() {
        return Collections.unmodifiableCollection(elements);
    }

    /**
     * @return the chapter
     */
    public Chapter getChapter() {
        return chapter;
    }

    /**
     * @param chapter
     *            the chapter to set
     */
    void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    /**
     * @param style
     * @return
     */
    public boolean addStyle(Style style) {
        return styles.add(style);
    }

    /**
     * 
     */
    public void clearStyles() {
        styles.clear();
    }

    /**
     * @return
     */
    public Iterable<Style> styles() {
        return Collections.unmodifiableCollection(styles);
    }
}
