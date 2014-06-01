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
public class Image implements Section {

    private ImageItem item;

    private String caption;

    private int width;

    private int height;

    private String style = "fit";

    public Image(ImageItem item) {
        this.item = item;
    }

    /**
     * @return the item
     */
    public ImageItem getItem() {
        return item;
    }

    /**
     * @param item
     *            the item to set
     */
    public void setItem(ImageItem item) {
        this.item = item;
    }

    /**
     * @return the caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * @param caption
     *            the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
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

    /**
     * @return the style
     */
    public String getStyle() {
        return style;
    }

    /**
     * @param style
     *            the style to set
     */
    public void setStyle(String style) {
        this.style = style;
    }

    @Override
    public Node generate(Page page, Document document) {
        Element element = document.createElement("img");
        element.setAttribute("src", item.getHref(page.getPath().getParent())
                .toString());

        if (caption != null && !caption.isEmpty()) {
            element.setAttribute("alt", caption);
            element.setAttribute("title", caption);
        }

        if (width > 0) {
            element.setAttribute("width", Integer.toString(width));
        }

        if (height > 0) {
            element.setAttribute("height", Integer.toString(height));
        }

        if (style != null) {
            element.setAttribute("class", style);
        }

        return element;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("［＃");

        if (caption != null) {
            builder.append(caption);
        }

        builder.append("（").append(item);

        if (width > 0 || height > 0) {
            builder.append("、縦").append(width).append("×横").append(width);
        }

        return builder.append("）入る］").toString();
    }
}
