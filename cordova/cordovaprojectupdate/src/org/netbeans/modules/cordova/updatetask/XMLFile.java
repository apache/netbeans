/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
    
    final protected Document doc;
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
        
        String rest = xpath.substring(lastIndexOf + 1, xpath.length());
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
