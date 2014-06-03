/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.nio.file.Path;

/**
 * @author dacci
 */
public class Resource {

    private final Path source;

    private String id;

    private String properties;

    private Path destination;

    /**
     * 
     */
    Resource(Path source) {
        this.source = source;
    }

    /**
     * @return the source
     */
    public Path getSource() {
        return source;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the properties
     */
    public String getProperties() {
        return properties;
    }

    /**
     * @param properties
     *            the properties to set
     */
    public void setProperties(String properties) {
        this.properties = properties;
    }

    /**
     * @param from
     * @return
     */
    public String getHref(Path from) {
        return from.relativize(destination).toString();
    }

    /**
     * @return
     */
    public String getMediaType() {
        String name = source.getFileName().toString().toLowerCase();

        switch (name.substring(name.lastIndexOf('.') + 1)) {
        case "gif":
            return "image/gif";

        case "jpg":
        case "jpeg":
            return "image/jpeg";

        case "png":
            return "image/png";
        }

        return "application/octet-stream";
    }
}
