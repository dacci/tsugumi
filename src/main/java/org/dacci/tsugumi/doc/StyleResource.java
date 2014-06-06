/*
 * Copyright (c) 2014 NTT DATA SMS Corporation.
 */

package org.dacci.tsugumi.doc;

/**
 * @author tsudasn
 */
public class StyleResource extends Resource {

    /**
     * @param source
     */
    StyleResource(String id) {
        setId(id);
    }

    @Override
    public String getFileName() {
        return getId() + ".css";
    }

    @Override
    public String getMediaType() {
        return "text/css";
    }
}
