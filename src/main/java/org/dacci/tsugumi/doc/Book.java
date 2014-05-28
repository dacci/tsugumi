/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        Image image = new Image(path, images.size());
        images.add(image);

        return image;
    }
}
