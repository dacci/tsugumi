/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.nio.file.Path;

/**
 * @author dacci
 */
public abstract class Resource {

    private String id;

    private String properties;

    private Path destination;

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
     * @return the destination
     */
    public Path getDestination() {
        return destination;
    }

    /**
     * @param destination
     *            the destination to set
     */
    public void setDestination(Path destination) {
        this.destination = destination;
    }

    /**
     * @param from
     * @return
     */
    public String getHref(Path from) {
        return from.relativize(destination).toString().replace('\\', '/');
    }

    /**
     * @return
     */
    public abstract String getFileName();

    /**
     * @return
     */
    public abstract String getMediaType();
}
