/*
 * Copyright (c) 2014 NTT DATA SMS Corporation.
 */

package org.dacci.tsugumi.doc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author tsudasn
 */
public class Link implements PageElement {

    private final Chapter chapter;

    private final PageElement text;

    private String target;

    /**
     * @param book
     * @param text
     */
    public Link(Chapter chapter, PageElement text) {
        this.chapter = chapter;
        this.text = text;

        target = new StringBuffer(text).toString();
    }

    /**
     * @param book
     * @param text
     * @param target
     */
    public Link(Chapter chapter, PageElement text, String target) {
        this.chapter = chapter;
        this.text = text;
        this.target = target;
    }

    /**
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target
     *            the target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * @return the chapter
     */
    public Chapter getChapter() {
        return chapter;
    }

    /**
     * @return the text
     */
    public PageElement getText() {
        return text;
    }

    @Override
    public int length() {
        return text.length();
    }

    @Override
    public char charAt(int index) {
        return text.charAt(index);
    }

    @Override
    public PageElement subSequence(int start, int end) {
        return new Link(chapter, text.subSequence(start, end), target);
    }

    @Override
    public Node build(Document document) {
        Chapter targetChapter = null;
        for (Chapter chapter : chapter.getBook().getChapters()) {
            if (target.equals(chapter.getProperty(Chapter.TITLE))) {
                targetChapter = chapter;
                break;
            }
        }

        if (targetChapter == null) {
            return text.build(document);
        } else {
            Element anchor = document.createElement("a");
            anchor.setAttribute(
                    "href",
                    targetChapter.getResource().getHref(
                            chapter.getResource().getDestination().getParent()));
            anchor.appendChild(text.build(document));

            return anchor;
        }
    }
}
