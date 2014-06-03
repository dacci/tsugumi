/*
 * Copyright (c) 2014 dacci.org
 */

package org.dacci.tsugumi;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.dacci.tsugumi.doc.Book;

/**
 * @author dacci
 */
public class EPubBuilder {

    private final DocumentBuilder builder;

    private final Transformer transformer;

    /**
     * @throws BuilderException
     */
    public EPubBuilder() throws BuilderException {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "2");
        } catch (ParserConfigurationException | TransformerException e) {
            throw new BuilderException(e);
        }
    }

    /**
     * @param book
     */
    public void build(Book book) throws BuilderException {
    }

    /**
     * @param path
     */
    public void saveToDirectory(Path path) throws IOException {
    }

    /**
     * @param path
     */
    public void saveToFile(Path path) throws IOException {
    }
}
