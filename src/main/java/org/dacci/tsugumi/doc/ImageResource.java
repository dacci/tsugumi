/*
 * Copyright (c) 2014 NTT DATA SMS Corporation.
 */

package org.dacci.tsugumi.doc;

import java.nio.file.Path;

/**
 * @author tsudasn
 */
public class ImageResource extends Resource {

    private final Path source;

    private final String extension;

    /**
     * @param source
     */
    ImageResource(Path source) {
        this.source = source;

        String fileName = source.getFileName().toString().toLowerCase();
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            extension = "";
        } else {
            extension = fileName.substring(index);
        }
    }

    /**
     * @return the source
     */
    public Path getSource() {
        return source;
    }

    @Override
    public String getFileName() {
        if (extension.isEmpty()) {
            return getId();
        } else {
            return getId() + extension;
        }
    }

    @Override
    public String getMediaType() {
        switch (extension) {
        case ".gif":
            return "image/gif";

        case ".jpg":
        case ".jpeg":
            return "image/jpeg";

        case ".png":
            return "image/png";
        }

        return "application/octet-stream";
    }
}
