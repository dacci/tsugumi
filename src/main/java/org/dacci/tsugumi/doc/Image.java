/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author dacci
 */
public class Image implements PageElement {

    private Resource image;

    private Resource chapter;

    private String title;

    private int width = -1;

    private int height = -1;

    /**
     * @param image
     */
    public Image(Resource image, Resource chapter) {
        this.image = image;
        this.chapter = chapter;
    }

    /**
     * @return the image
     */
    public Resource getImage() {
        return image;
    }

    /**
     * @param image
     *            the image to set
     */
    public void setImage(Resource image) {
        this.image = image;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the alt to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width
     *            the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height
     *            the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public char charAt(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PageElement subSequence(int start, int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "Image[]";
    }

    @Override
    public Node build(Document document) {
        Element element = document.createElement("img");

        element.setAttribute("src",
                image.getHref(chapter.getDestination().getParent()));
        element.setAttribute("class", "fit");

        if (title != null && !title.isEmpty()) {
            element.setAttribute("alt", title);
            element.setAttribute("title", title);
        }

        if (width >= 0) {
            element.setAttribute("width", Integer.toString(width));
        }

        if (height >= 0) {
            element.setAttribute("height", Integer.toString(height));
        }

        return element;
    }
}
