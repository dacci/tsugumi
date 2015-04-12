/*
 * Copyright (c) 2015 dacci.org
 */

package org.dacci.tsugumi.doc;

/**
 * @author dacci
 */
public enum SimpleStyle implements Style {
    Bold,
    Italic,
    Ruled,
    Horizontal,
    AlignEnd,
    Caption,
    HeadingLarge,
    HeadingMedium,
    HeadingSmall,
    Sesame,
    OpenSesame,
    Circle,
    CircleOpen,
    Triangle,
    TriangleOpen,
    DoubleCircle,
    DoubleCircleOpen,
    Saltire,
    Lined,
    DoubleLined,
    Dotted,
    Dashed,
    WaveDashed,
    Rotated,
    Superscript,
    Subscript,
    Kunten,
    Okurigana,
    Warichu,
    PageCenter,
    Gothic;

    /**
     * {@InheritDoc}
     */
    @Override
    public Style copy() {
        return this;
    }
}
