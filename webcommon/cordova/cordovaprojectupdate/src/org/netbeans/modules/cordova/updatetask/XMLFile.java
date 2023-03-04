/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cordova.updatetask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Jan Becicka
 */
public class XMLFile {
    
    protected final Document doc;
    private File file;
    private final XPath xPath;

    protected XMLFile(File f) throws IOException {
        this (new FileInputStream(f));
        file = f;
    }
    
    protected XMLFile(InputStream resource) throws IOException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(resource);
            xPath = XPathFactory.newInstance().newXPath();
        } catch (ParserConfigurationException ex) {
            throw new IOException(ex);
        } catch (SAXException ex) {
            throw new IOException(ex);
        } finally {
            resource.close();
        }
    }
    
    protected final Node getXpathNode(String axpath) {
        try {
            return (Node) xPath.evaluate(axpath, doc, XPathConstants.NODE);
        } catch (XPathExpressionException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    protected final NodeList getXpathNodes(String axpath) {
        try {
            return (NodeList) xPath.evaluate(axpath, doc, XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    
    protected final Node getXpathAttribute(String xpath, String attribute) {
        return getXpathNode(xpath).getAttributes().getNamedItem(attribute);
    }

    protected final Node getNode(String axpath) {
        assert axpath !=null;
        assert axpath.startsWith("/");
        try {
            return (Node) xPath.evaluate(axpath, doc, XPathConstants.NODE);
        } catch (XPathExpressionException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    protected final String getTextContent(Node node) {
        if (node==null) {
            return null;
        }
        return node.getTextContent();
        
    }
    
    protected final String getTextContent(String xpath) {
        Node node = getNode(xpath);
        if (node == null) {
            return null;
        }
        return node.getTextContent();
    }
    
    protected final void setTextContent(String xpath, String value) {
        createXpath(xpath).setTextContent(value);
        
    }
    
    private Node createXpath(String xpath) {
        Node n = getNode(xpath);
        if (n != null) {
            return n;
        }
        
        int indexOf = xpath.indexOf("/", 1); // NOI18N
        Node node = getNode(xpath.substring(0, indexOf));
        Node lastNode = node;
        int lastIndexOf = indexOf;
        while (node != null) {
            indexOf = xpath.indexOf("/", indexOf+1); // NOI18N
            lastNode = node;
            node = indexOf <0 ? null : getNode(xpath.substring(0, indexOf));
        }
        
        String rest = xpath.substring(lastIndexOf + 1);
        for (String newTag:rest.split("/")) { // NOI18N
            lastNode = lastNode.appendChild(doc.createElement(newTag));
        }
        
        return lastNode;
    } 

    protected String getAttributeText(String path, String attrName) {
        return getAttributeText(getNode(path), attrName);
    }

    protected void setAttributeText(String path, String attrName, String value) {
        final Attr attr = doc.createAttribute(attrName);
        attr.setValue(value);
        createXpath(path).getAttributes().setNamedItem(attr);
    }
    
    protected final String getAttributeText(Node node, String attrName) {
        if (node==null) {
            return null;
        }
        Node namedItem = node.getAttributes().getNamedItem(attrName);
        if (namedItem == null) {
            return null;
        }
        return namedItem.getTextContent();
    }

    final void printDocument(OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no"); // NOI18N
        transformer.setOutputProperty(OutputKeys.METHOD, "xml"); // NOI18N
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // NOI18N
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // NOI18N
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8"))); // NOI18N
    }
    
    public final void save() throws IOException {
        final FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            printDocument(fileOutputStream);
        } catch (TransformerException ex) {
            throw new IOException(ex);
        } finally {
            fileOutputStream.close();
        }
    }
    
}
