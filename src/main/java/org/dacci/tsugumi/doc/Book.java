/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        String id = String.format("p-%03d", ++nextChapter);
        Path path = Paths.get(".", "item", "xhtml", id + ".xhtml");

        PageResource resource = new PageResource(path);
        resource.setId(id);
        resources.add(resource);

        Chapter chapter = new Chapter(this, resource);
        chapters.add(chapter);
        return chapter;
    }

    /**
     * @param path
     * @return
     */
    public Resource loadResource(Path path) {
        for (Resource resource : resources) {
            if (path.equals(resource.getSource())) {
                return resource;
            }
        }

        Resource resource = new Resource(path);
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
     * @return the coverImage
     */
    public Image getCoverImage() {
        return coverImage;
    }

    /**
     * @return
     */
    public boolean hasCoverImage() {
        return coverImage != null;
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

        Path path = Paths.get(".", "item", "xhtml", "p-cover.xhtml");
        PageResource resource = new PageResource(path);
        resource.setId("p-cover");
        resources.add(0, resource);

        Chapter chapter = new Chapter(this, resource);
        chapter.setStyle("p-cover");
        chapter.setType("cover");
        chapter.add(new Paragraph(coverImage));
        chapters.add(0, chapter);
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
