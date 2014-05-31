/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.nio.file.Path;

/**
 * @author dacci
 */
public class ImageItem extends Item {

    private final Path source;

    private final String extension;

    /**
     * @param source
     */
    public ImageItem(Path source) {
        this.source = source;

        String fileName = source.getFileName().toString();
        extension = fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * @return the source
     */
    public Path getSource() {
        return source;
    }

    /**
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    @Override
    public String getMediaType() {
        switch (extension.toLowerCase()) {
        case "png":
            return "image/png";

        case "jpg":
        case "jpeg":
            return "image/jpeg";

        case "git":
            return "image/gif";
        }

        return "application/octet-stream";
    }
}
