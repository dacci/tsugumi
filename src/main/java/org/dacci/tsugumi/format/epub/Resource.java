/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.format.epub;

import java.nio.file.Path;

/**
 * @author dacci
 */
public class Resource {

  private String id;

  private Path path;

  private String mediaType = "application/octet-stream";

  private String fallback;

  private String properties;

  private String mediaOverlay;

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    if (id == null) {
      throw new NullPointerException();
    }
    if (id.isEmpty()) {
      throw new IllegalArgumentException();
    }

    this.id = id;
  }

  /**
   * @return the path
   */
  public Path getPath() {
    return path;
  }

  /**
   * @param path the path to set
   */
  public void setPath(Path path) {
    if (path == null) {
      throw new NullPointerException();
    }

    this.path = path;
  }

  /**
   * @return the mediaType
   */
  public String getMediaType() {
    return mediaType;
  }

  /**
   * @param mediaType the mediaType to set
   */
  public void setMediaType(String mediaType) {
    if (mediaType == null) {
      throw new NullPointerException();
    }
    if (mediaType.isEmpty()) {
      throw new IllegalArgumentException();
    }

    this.mediaType = mediaType;
  }

  /**
   * @return the fallback
   */
  public String getFallback() {
    return fallback;
  }

  /**
   * @param fallback the fallback to set
   */
  public void setFallback(String fallback) {
    if (fallback != null && fallback.isEmpty()) {
      this.fallback = null;
    } else {
      this.fallback = fallback;
    }
  }

  /**
   * @return the properties
   */
  public String getProperties() {
    return properties;
  }

  /**
   * @param properties the properties to set
   */
  public void setProperties(String properties) {
    if (properties != null && properties.isEmpty()) {
      this.properties = null;
    } else {
      this.properties = properties;
    }
  }

  /**
   * @return the mediaOverlay
   */
  public String getMediaOverlay() {
    return mediaOverlay;
  }

  /**
   * @param mediaOverlay the mediaOverlay to set
   */
  public void setMediaOverlay(String mediaOverlay) {
    if (mediaOverlay != null && mediaOverlay.isEmpty()) {
      this.mediaOverlay = null;
    } else {
      this.mediaOverlay = mediaOverlay;
    }
  }
}
