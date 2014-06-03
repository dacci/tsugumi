/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dacci
 */
public class Book {

    private final Collection<Chapter> chapters = new ArrayList<>();

    private final Map<Path, Resource> resources = new LinkedHashMap<>();

    private String title;

    private String originalTitle;

    private String subtitle;

    private String originalSubtitle;

    private String author;

    private String translator;

    private Image coverImage;

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the originalTitle
     */
    public String getOriginalTitle() {
        return originalTitle;
    }

    /**
     * @param originalTitle
     *            the originalTitle to set
     */
    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    /**
     * @return the subtitle
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * @param subtitle
     *            the subtitle to set
     */
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * @return the originalSubtitle
     */
    public String getOriginalSubtitle() {
        return originalSubtitle;
    }

    /**
     * @param originalSubtitle
     *            the originalSubtitle to set
     */
    public void setOriginalSubtitle(String originalSubtitle) {
        this.originalSubtitle = originalSubtitle;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author
     *            the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the translator
     */
    public String getTranslator() {
        return translator;
    }

    /**
     * @param translator
     *            the translator to set
     */
    public void setTranslator(String translator) {
        this.translator = translator;
    }

    /**
     * @return the chapters
     */
    public Collection<Chapter> getChapters() {
        return chapters;
    }

    /**
     * @return
     */
    public Chapter addChapter() {
        Chapter chapter = new Chapter(this);
        chapters.add(chapter);
        return chapter;
    }

    /**
     * @param path
     * @return
     */
    public Resource loadResource(Path path) {
        if (!resources.containsKey(path)) {
            resources.put(path, new Resource(path));
        }

        return resources.get(path);
    }

    /**
     * @return
     */
    public Collection<Resource> getResources() {
        return resources.values();
    }

    /**
     * @return the coverImage
     */
    public Image getCoverImage() {
        return coverImage;
    }

    /**
     * @param coverImage
     *            the coverImage to set
     */
    public void setCoverImage(Image coverImage) {
        if (this.coverImage != null) {
            throw new IllegalStateException("cover image is already set");
        }

        this.coverImage = coverImage;
    }
}
