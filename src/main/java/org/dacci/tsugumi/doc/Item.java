/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.nio.file.Path;

/**
 * @author dacci
 */
public abstract class Item {

    private String id;

    private Path path;

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
     * @return the path
     */
    public Path getPath() {
        return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(Path path) {
        this.path = path;
    }

    /**
     * @param from
     * @return
     */
    public String getHref(Path from) {
        return from.relativize(path).toString().replace('\\', '/');
    }

    /**
     * @return
     */
    public abstract String getMediaType();
}
