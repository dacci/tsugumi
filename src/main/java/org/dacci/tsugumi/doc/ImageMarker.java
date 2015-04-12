/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.nio.file.Path;

/**
 * @author dacci
 */
public class ImageMarker implements Marker {

    private String caption;

    private Path file;

    /**
     * @param file
     */
    public ImageMarker(Path file) {
        this.file = file;
    }

    private int width = -1;

    private int height = -1;

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
     * @return the file
     */
    public Path getFile() {
        return file;
    }

    /**
     * @param file
     *            the file to set
     */
    public void setFile(Path file) {
        if (file == null) {
            throw new NullPointerException();
        }

        this.file = file;
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
        if (width < 0) {
            this.width = -1;
        } else {
            this.width = width;
        }
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
        if (height < 0) {
            this.height = -1;
        } else {
            this.height = height;
        }
    }
}
