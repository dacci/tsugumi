/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

/**
 * @author dacci
 */
public interface Marker extends Segment {

    /**
     * {@inheritDoc}
     */
    @Override
    default int length() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default char charAt(int index) {
        if (index != 0) {
            throw new StringIndexOutOfBoundsException(index);
        }

        return '\0';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean hasChildren() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default StringBuilder toString(StringBuilder builder) {
        return builder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default Segment subSequence(int start, int end) {
        throw new UnsupportedOperationException();
    }
}
