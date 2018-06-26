/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
