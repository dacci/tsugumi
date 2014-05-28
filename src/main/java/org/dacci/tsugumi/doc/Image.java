/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.nio.file.Path;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author dacci
 */
public class Image implements Section {

    public static String getContentType(Image image) {
        switch (image.getExtension()) {
        case "png":
            return "image/png";

        case "jpg":
        case "jpeg":
            return "image/jpeg";

        default:
            return "application/octet-stream";
        }
    }

    private final Path path;

    private final String id;

    private final String extension;

    private String caption;

    private int width;

    private int height;

    /**
     * @param path
     */
    Image(Path path, int index) {
        this.path = path;

        id = String.format("img-%03d", index);

        String name = path.getFileName().toString().toLowerCase();
        extension = name.substring(name.lastIndexOf('.') + 1);
    }

    /**
     * @return the path
     */
    public Path getPath() {
        return path;
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
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    @Override
    public Node generate(Document document) {
        Element element = document.createElement("img");
        element.setAttribute("src",
                String.format("../image/%s.%s", id, extension));

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

        return element;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("［＃");

        if (caption != null) {
            builder.append(caption);
        }

        builder.append("（").append(path);

        if (width > 0 || height > 0) {
            builder.append("、縦").append(width).append("×横").append(width);
        }

        return builder.append("）入る］").toString();
    }
}
