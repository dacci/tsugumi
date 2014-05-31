/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author dacci
 */
public class Book {

    private final Path basePath;

    private String title;

    private String originalTitle;

    private String subTitle;

    private String originalSubTitle;

    private String author;

    private String translator;

    private final List<Item> items = new ArrayList<>();

    private final Map<Path, ImageItem> images = new HashMap<>();

    private ImageItem coverImage;

    private CoverPage coverPage;

    private Page tocPage;

    /**
     * @param basePath
     */
    public Book(Path basePath) {
        this.basePath = basePath;
    }

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
     * @return the subTitle
     */
    public String getSubTitle() {
        return subTitle;
    }

    /**
     * @param subTitle
     *            the subTitle to set
     */
    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    /**
     * @return the originalSubTitle
     */
    public String getOriginalSubTitle() {
        return originalSubTitle;
    }

    /**
     * @param originalSubTitle
     *            the originalSubTitle to set
     */
    public void setOriginalSubTitle(String originalSubTitle) {
        this.originalSubTitle = originalSubTitle;
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
     * @return the items
     */
    public Collection<Item> getItems() {
        return items;
    }

    /**
     * @param path
     * @return
     * @throws ClassCastException
     */
    public ImageItem importImage(String path) {
        Path target = basePath.resolve(path);

        if (!images.containsKey(target)) {
            ImageItem item = new ImageItem(target);
            items.add(item);
            images.put(target, item);
        }

        return (ImageItem) images.get(target);
    }

    /**
     * @return
     */
    public Page addPage() {
        Page page = new Page(this);
        items.add(page);

        return page;
    }

    /**
     * @return
     */
    public boolean hasCoverImage() {
        return coverImage != null;
    }

    /**
     * @return the coverImage
     */
    public ImageItem getCoverImage() {
        return coverImage;
    }

    /**
     * @param coverImage
     *            the coverImage to set
     */
    public void setCoverImage(ImageItem coverImage) {
        this.coverImage = coverImage;

        if (coverPage == null) {
            coverPage = new CoverPage(this);
            items.add(0, coverPage);
        }
    }

    /**
     * @return the coverPage
     */
    public CoverPage getCoverPage() {
        return coverPage;
    }

    /**
     * @return the tocPage
     */
    public Page getTocPage() {
        return tocPage;
    }

    /**
     * @param tocPage
     *            the tocPage to set
     */
    public void setTocPage(Page tocPage) {
        this.tocPage = tocPage;
    }

    /**
     * @return
     */
    public String getUniqueId() {
        StringBuilder builder = new StringBuilder();

        if (title != null) {
            builder.append(title);
        }

        if (originalTitle != null) {
            builder.append(originalTitle);
        }

        if (subTitle != null) {
            builder.append(subTitle);
        }

        if (originalSubTitle != null) {
            builder.append(originalSubTitle);
        }

        if (author != null) {
            builder.append(author);
        }

        if (translator != null) {
            builder.append(translator);
        }

        UUID uuid =
                UUID.nameUUIDFromBytes(builder.toString().getBytes(
                        StandardCharsets.UTF_8));

        return "urn:uuid:" + uuid;
    }
}
