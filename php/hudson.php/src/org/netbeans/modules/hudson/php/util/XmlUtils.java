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
package org.netbeans.modules.hudson.php.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Various utility methods to work with XML files.
 */
public final class XmlUtils {

    private static final Logger LOGGER = Logger.getLogger(XmlUtils.class.getName());


    private XmlUtils() {
    }

    public static Document parse(File xmlFile) throws IOException, SAXException {
        FileInputStream fileInputStream = new FileInputStream(xmlFile);
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            try {
                InputSource inputSource = new InputSource(bufferedInputStream);
                return XMLUtil.parse(inputSource, false, false, null, new EntityResolver() {
                    @Override
                    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                        return new InputSource(new ByteArrayInputStream(new byte[0]));
                    }
                });
            } finally {
                bufferedInputStream.close();
            }
        } finally {
            fileInputStream.close();
        }
    }

    public static void save(Document document, File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            try {
                XMLUtil.write(document, bufferedOutputStream, document.getXmlEncoding());
            } finally {
                bufferedOutputStream.close();
            }
        } finally {
            fileOutputStream.close();
        }
    }

    public static Node query(Document document, String xpathExpression) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression xPathExpression = xPath.compile(xpathExpression);
            Object result = xPathExpression.evaluate(document, XPathConstants.NODE);
            if (result == null) {
                return null;
            }
            if (!(result instanceof Node)) {
                LOGGER.log(Level.FINE, "Node expected for XPath ''{0}'' but ''{1}'' returned", new Object[] {xpathExpression, result.getClass().getName()});
                return null;
            }
            return (Node) result;
        } catch (XPathExpressionException ex) {
            LOGGER.log(Level.INFO, "XPath error for: " + xpathExpression, ex);
        }
        return null;
    }

    public static void commentNode(Document document, Node node) {
        Node parentNode = node.getParentNode();
        parentNode.replaceChild(document.createComment(XmlUtils.asString(node, false)), node);
    }

    public static String getNodeValue(Document document, Node node) {
        return node.getFirstChild().getNodeValue();
    }

    public static void setNodeValue(Document document, Node node, String newValue) {
        node.getFirstChild().setNodeValue(newValue);
    }

    public static String asString(Node node, boolean formatted) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            if (formatted) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // NOI18N
            }
            if (!(node instanceof Document)) {
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // NOI18N
            }
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(node);
            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (TransformerException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

}
