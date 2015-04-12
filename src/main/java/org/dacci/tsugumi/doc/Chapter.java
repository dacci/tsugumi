/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.util.EnumMap;

/**
 * @author dacci
 */
public class Chapter {

    private Book book = null;

    private final EnumMap<BookProperty, String> properties = new EnumMap<>(
            BookProperty.class);

    private final Block root = new Block();

    /**
     * 
     */
    public Chapter() {
        root.setChapter(this);
    }

    /**
     * @return the book
     */
    public Book getBook() {
        return book;
    }

    /**
     * @param book
     *            the book to set
     */
    void setBook(Book book) {
        this.book = book;
    }

    /**
     * @param key
     * @return
     */
    public boolean hasProperty(BookProperty key) {
        return properties.containsKey(key);
    }

    /**
     * @param key
     * @return
     */
    public String getProperty(BookProperty key) {
        return properties.get(key);
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public String setProperty(BookProperty key, String value) {
        return properties.put(key, value);
    }

    /**
     * @return the root
     */
    public Block getRoot() {
        return root;
    }
}
