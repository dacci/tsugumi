/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author dacci
 */
public class Fragment implements Segment, Iterable<Segment> {

  private static final Fragment EMPTY_FRAGMENT = new Fragment();

  private final NavigableMap<Integer, Segment> children = new TreeMap<>();

  /**
   * @param text
   */
  Fragment(String text) {
    children.put(0, new TextSegment(text));
  }

  /** */
  private Fragment() {}

  /**
   * @param start
   * @param end
   * @param replacement
   */
  public void replace(Integer start, Integer end, Segment replacement) {
    splitAt(start);
    splitAt(end);

    List<Segment> segments = new ArrayList<>();
    if (replacement != null) {
      segments.add(replacement);
    }
    segments.addAll(children.tailMap(end).values());

    children.tailMap(start).clear();

    int index = start.intValue();
    for (Segment segment : segments) {
      children.put(index, segment);
      index += segment.length();
    }
  }

  private void splitAt(Integer index) {
    Entry<Integer, Segment> entry = children.floorEntry(index);
    if (entry == null) {
      throw new StringIndexOutOfBoundsException(index);
    }

    if (entry.getKey().equals(index)) {
      return;
    }

    int offset = entry.getKey();
    Segment segment = entry.getValue();
    Segment front = segment.subSequence(0, index - offset);
    Segment back = segment.subSequence(index - offset, segment.length());

    children.put(entry.getKey(), front);
    children.put(index, back);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return toString(new StringBuilder()).toString();
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasChildren() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public StringBuilder toString(StringBuilder builder) {
    for (Segment segment : children.values()) {
      segment.toString(builder);
    }

    return builder;
  }

  /** {@inheritDoc} */
  @Override
  public int length() {
    int length = 0;

    for (Segment segment : children.values()) {
      length += segment.length();
    }

    return length;
  }

  /** {@inheritDoc} */
  @Override
  public char charAt(int index) {
    Entry<Integer, Segment> entry = children.floorEntry(index);
    if (entry == null) {
      throw new StringIndexOutOfBoundsException(index);
    }

    return entry.getValue().charAt(index - entry.getKey().intValue());
  }

  /** {@inheritDoc} */
  @Override
  public Segment subSequence(int start, int end) {
    if (start < 0 || end < 0 || end < start) {
      throw new IllegalArgumentException();
    }

    if (start == end) {
      return EMPTY_FRAGMENT;
    }

    Integer startKey = children.floorKey(start);
    Integer endKey = children.ceilingKey(end);
    SortedMap<Integer, Segment> subMap;
    if (endKey != null) {
      subMap = children.subMap(startKey, endKey);
    } else {
      subMap = children.tailMap(startKey);
    }

    Iterator<Entry<Integer, Segment>> iterator = subMap.entrySet().iterator();
    Entry<Integer, Segment> entry = iterator.next();

    if (subMap.size() == 1) {
      return entry.getValue().subSequence(start - startKey, end - startKey);
    }

    Fragment fragment = new Fragment();

    Segment segment = entry.getValue();
    segment = segment.subSequence(start - entry.getKey(), segment.length());
    fragment.children.put(0, segment);
    int index = segment.length();

    for (int i = 1, l = subMap.size() - 1; i < l; ++i) {
      entry = iterator.next();
      segment = entry.getValue();
      fragment.children.put(index, segment);
      index += segment.length();
    }

    entry = iterator.next();
    segment = entry.getValue();
    segment = segment.subSequence(0, end - entry.getKey());
    fragment.children.put(index, segment);

    return fragment;
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<Segment> iterator() {
    return children.values().iterator();
  }
}
