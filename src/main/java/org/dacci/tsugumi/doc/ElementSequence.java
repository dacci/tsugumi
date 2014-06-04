/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi.doc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/**
 * @author dacci
 */
public class ElementSequence implements PageElement {

    protected final NavigableMap<Integer, PageElement> slices = new TreeMap<>();

    /**
     * 
     */
    private ElementSequence() {
    }

    /**
     * @param string
     */
    public ElementSequence(String string) {
        this(new Text(string));
    }

    /**
     * @param element
     */
    public ElementSequence(PageElement element) {
        slices.put(0, element);
    }

    /**
     * @param start
     * @param end
     * @param slice
     */
    public void replace(Integer start, Integer end, PageElement slice) {
        splitAt(start);
        splitAt(end);
        slices.subMap(start, end).clear();

        int offset = end - start - slice.length();
        if (offset != 0) {
            Map<Integer, PageElement> moved = new HashMap<>();

            for (Iterator<Entry<Integer, PageElement>> i =
                    slices.tailMap(end).entrySet().iterator(); i.hasNext();) {
                Entry<Integer, PageElement> entry = i.next();
                moved.put(entry.getKey() - offset, entry.getValue());
                i.remove();
            }

            slices.putAll(moved);
        }

        slices.put(start, slice);
    }

    /**
     * @param start
     * @param end
     */
    public void erase(Integer start, Integer end) {
        splitAt(start);
        splitAt(end);
        slices.subMap(start, end).clear();

        int offset = end - start;
        if (offset != 0) {
            Map<Integer, PageElement> moved = new HashMap<>();

            for (Iterator<Entry<Integer, PageElement>> i =
                    slices.tailMap(end).entrySet().iterator(); i.hasNext();) {
                Entry<Integer, PageElement> entry = i.next();
                moved.put(entry.getKey() - offset, entry.getValue());
                i.remove();
            }

            slices.putAll(moved);
        }
    }

    /**
     * @param index
     */
    private void splitAt(Integer index) {
        Entry<Integer, PageElement> entry = slices.floorEntry(index);

        Integer floor = slices.floorKey(index);
        if (floor.equals(index)) {
            return;
        }

        PageElement sequence = entry.getValue();
        if (floor.intValue() + sequence.length() == index.intValue()) {
            return;
        }

        int offset = index - floor;
        PageElement lower = sequence.subSequence(0, offset);
        PageElement higher = sequence.subSequence(offset, sequence.length());

        slices.remove(floor);
        slices.put(floor, lower);
        slices.put(index, higher);
    }

    @Override
    public int length() {
        int length = 0;

        for (PageElement slice : slices.values()) {
            length += slice.length();
        }

        return length;
    }

    @Override
    public char charAt(int index) {
        Entry<Integer, PageElement> entry = slices.floorEntry(index);
        return entry.getValue().charAt(index - entry.getKey());
    }

    @Override
    public ElementSequence subSequence(int start, int end) {
        ElementSequence result = new ElementSequence();

        int index = start;
        int remaining = end - start;
        SortedMap<Integer, PageElement> tailMap =
                slices.tailMap(slices.floorKey(start));

        for (Entry<Integer, PageElement> entry : tailMap.entrySet()) {
            Integer key = entry.getKey();
            PageElement sequence = entry.getValue();

            int offset = index - key;
            int length = sequence.length() - offset;

            if (remaining < length) {
                length = remaining;
            }
            remaining -= length;

            PageElement subSequence =
                    sequence.subSequence(offset, offset + length);
            if (result.slices.isEmpty()) {
                result.slices.put(0, subSequence);
            } else {
                Entry<Integer, PageElement> lastEntry =
                        result.slices.lastEntry();
                result.slices.put(lastEntry.getKey() +
                        lastEntry.getValue().length(), subSequence);
            }

            if (remaining == 0) {
                break;
            }

            index = key + sequence.length();
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        slices.values().forEach(builder::append);
        return builder.toString();
    }

    @Override
    public Node build(Document document) {
        DocumentFragment fragment = document.createDocumentFragment();

        for (PageElement slice : slices.values()) {
            fragment.appendChild(slice.build(document));
        }

        return fragment;
    }
}
