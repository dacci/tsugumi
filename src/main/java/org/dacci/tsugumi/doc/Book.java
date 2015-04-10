/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author dacci
 */
public class Book {

    private final List<Chapter> chapters = new ArrayList<>();

    int nextChapter = 0;

    private final List<Resource> resources = new ArrayList<>();

    private String title;

    private String originalTitle;

    private String subtitle;

    private String originalSubtitle;

    private String author;

    private String translator;

    private String series;

    private String position;

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
     * @return the series
     */
    public String getSeries() {
        return series;
    }

    /**
     * @param series
     *            the series to set
     */
    public void setSeries(String collection) {
        this.series = collection;
    }

    /**
     * @return the position
     */
    public String getPosition() {
        return position;
    }

    /**
     * @param position
     *            the position to set
     */
    public void setPosition(String position) {
        this.position = position;
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
        PageResource resource = new PageResource();
        resources.add(resource);

        Chapter chapter = new Chapter(this, resource);
        chapters.add(chapter);
        return chapter;
    }

    /**
     * @param path
     * @return
     */
    public ImageResource loadImage(Path path) {
        for (Resource resource : resources) {
            if (!(resource instanceof ImageResource)) {
                continue;
            }

            ImageResource imageResource = (ImageResource) resource;
            if (path.equals(imageResource.getSource())) {
                return imageResource;
            }
        }

        ImageResource resource = new ImageResource(path);
        resources.add(resource);

        return resource;
    }

    /**
     * @param id
     * @return
     */
    public StyleResource loadStyle(String id) {
        for (Resource resource : resources) {
            if (id.equals(resource.getId())) {
                return (StyleResource) resource;
            }
        }

        StyleResource resource = new StyleResource(id);
        resources.add(resource);

        return resource;
    }

    /**
     * @return
     */
    public Collection<Resource> getResources() {
        return resources;
    }

    /**
     * @return
     */
    public String getUniqueId() {
        StringBuilder builder = new StringBuilder();

        if (title != null && !title.isEmpty()) {
            builder.append(title);
        }

        if (originalTitle != null && !originalTitle.isEmpty()) {
            builder.append(originalTitle);
        }

        if (subtitle != null && !subtitle.isEmpty()) {
            builder.append(subtitle);
        }

        if (originalSubtitle != null && !originalSubtitle.isEmpty()) {
            builder.append(originalSubtitle);
        }

        if (author != null && !author.isEmpty()) {
            builder.append(author);
        }

        if (translator != null && !translator.isEmpty()) {
            builder.append(translator);
        }

        return "urn:uuid:" +
                UUID.nameUUIDFromBytes(
                        builder.toString().getBytes(StandardCharsets.UTF_8))
                        .toString();
    }
}
