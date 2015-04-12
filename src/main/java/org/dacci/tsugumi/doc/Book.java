/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author dacci
 */
public class Book {

    private final EnumMap<BookProperty, String> properties = new EnumMap<>(
            BookProperty.class);

    private final List<Chapter> chapters = new ArrayList<>();

    private final Set<Path> resources = new LinkedHashSet<>();

    /**
     * @param chapter
     * @return
     */
    public Chapter addChapter(Chapter chapter) {
        if (chapter == null) {
            throw new NullPointerException();
        }
        if (chapter.getBook() != null) {
            throw new IllegalArgumentException();
        }

        if (!chapters.add(chapter)) {
            return null;
        }

        chapter.setBook(this);

        return chapter;
    }

    /**
     * @return
     */
    public Collection<Chapter> chapters() {
        return Collections.unmodifiableCollection(chapters);
    }

    /**
     * @param path
     * @return
     */
    public boolean loadResource(Path path) {
        return resources.add(path.toAbsolutePath());
    }

    /**
     * @return
     */
    public Collection<Path> resources() {
        return Collections.unmodifiableCollection(resources);
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
}
