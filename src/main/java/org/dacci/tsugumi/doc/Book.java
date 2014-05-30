/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author dacci
 */
public class Book {

    private String title;

    private String originalTitle;

    private String subTitle;

    private String originalSubTitle;

    private String author;

    private String translator;

    private final List<Page> pages = new ArrayList<>();

    private final List<Image> images = new ArrayList<>();

    private Image coverImage;

    private CoverPage coverPage;

    /**
     * 
     */
    public Book() {
        pages.add(new TocPage(this));
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
     * @return the pages
     */
    public List<Page> getPages() {
        return pages;
    }

    /**
     * @return
     */
    public Page addPage() {
        String id = String.format("p-%03d", pages.size());
        Page page = new Page(id, this);
        pages.add(page);

        return page;
    }

    /**
     * @return the images
     */
    public List<Image> getImages() {
        return images;
    }

    /**
     * @param path
     * @return
     */
    public Image createImage(Path path) {
        String id = String.format("img-%03d", images.size());
        Image image = new Image(path, id);
        images.add(image);

        return image;
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
    public Image getCoverImage() {
        return coverImage;
    }

    /**
     * @param coverImage
     *            the coverImage to set
     */
    public void setCoverImage(Image coverImage) {
        this.coverImage = coverImage;
    }

    /**
     * @param path
     */
    public void setCoverImage(Path path) {
        coverImage = new Image(path, "cover");

        if (coverPage == null) {
            coverPage = new CoverPage(this);
        }
    }

    /**
     * @return the coverPage
     */
    public CoverPage getCoverPage() {
        return coverPage;
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
