/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author dacci
 */
public class Paragraph extends BookElement {

    private final Fragment fragment;

    private final Set<Style> styles = new LinkedHashSet<>();

    public Paragraph(String text) {
        fragment = new Fragment(text);
    }

    /**
     * @return the fragment
     */
    public Fragment getFragment() {
        return fragment;
    }

    /**
     * @param style
     * @return
     */
    public boolean addStyle(Style style) {
        if (style == null) {
            throw new NullPointerException();
        }

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
