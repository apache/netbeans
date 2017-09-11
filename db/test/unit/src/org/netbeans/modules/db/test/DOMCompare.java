/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.db.test;

import java.util.regex.Pattern;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class for comparing DOM documents. It is namespace aware (hopefully),
 * but can only compare text and elements nodes and the attributes list of
 * element nodes.
 *
 * @author Andrei Badea
 */
public class DOMCompare {

    private DOMCompare() {
    }

    public static boolean compareDocuments(Document doc1, Document doc2) {
        Element e1 = doc1.getDocumentElement();
        Element e2 = doc2.getDocumentElement();
        
        return compareElements(e1, e2);
    }
    
    private static boolean compareElements(Element e1, Element e2) {
        if (!e1.getLocalName().equals(e2.getLocalName())) {
            System.out.println("Different local names " + e1.getLocalName() + " and " + e2.getLocalName());
            return false;
        }
        if (!compareStrings(e1.getNamespaceURI(), e2.getNamespaceURI())) {
            System.out.println("Different namespaces " + e1.getNamespaceURI() + " and " + e2.getNamespaceURI());
            return false;
        }
        if (!compareElementAttrs(e1, e2)) {
            return false;
        }
        if (!compareElementChildren(e1, e2)) {
            return false;
        }
        return true;
    }
    
    private static boolean compareElementAttrs(Element e1, Element e2) {
        NamedNodeMap at1 = e1.getAttributes();
        NamedNodeMap at2 = e2.getAttributes();
        if (at1.getLength() != at2.getLength()) {
            System.out.println("Different number of attributes");
        }
        for (int i = 0; i < at1.getLength(); i++) {
            Attr attr1 = (Attr)at1.item(i);
            Attr attr2 = (Attr)at2.getNamedItemNS(attr1.getNamespaceURI(), attr1.getLocalName());
            if (attr2 == null) {
                System.out.println("Attribute " + attr1.getNodeName() + " not found");
                return false;
            }
            if (!compareStrings(attr1.getNodeValue(), attr2.getNodeValue())) {
                System.out.println("Different attributes " + attr1.getNodeName() + " and " + attr2.getNodeName());
                return false;
            }
        }
        return true;
    }
    
    private static boolean compareElementChildren(Element e1, Element e2) {
        NodeList ch1 = e1.getChildNodes();
        NodeList ch2 = e2.getChildNodes();
        int i1 = 0;
        int i2 = 0;
        for (;;) {
            while (i1 < ch1.getLength()) {
                Node node = ch1.item(i1);
                int type = node.getNodeType();
                if (type == Node.ELEMENT_NODE) {
                    break;
                } else if (type == Node.TEXT_NODE) {
                    if (!isWhitespace(node.getNodeValue())) {
                        break;
                    }
                } else if(type == Node.CDATA_SECTION_NODE) {
                    break;
                } else {
                    System.out.println("Unsupported node type " + type);
                    return false;
                }
                i1++;
            }
            while (i2 < ch2.getLength()) {
                Node node = ch2.item(i2);
                int type = node.getNodeType();
                if (type == Node.ELEMENT_NODE) {
                    break;
                } else if (type == Node.TEXT_NODE) {
                    if (!isWhitespace(node.getNodeValue())) {
                        break;
                    }
                } else if(type == Node.CDATA_SECTION_NODE) {
                    break;
                } else {
                    System.out.println("Unsupported node type " + type);
                    return false;
                }
                i2++;
            }
            if (i1 < ch1.getLength() && i2 < ch2.getLength()) {
                if (ch1.item(i1).getNodeType() != ch2.item(i2).getNodeType()) {
                    System.out.println("Different element types: " + ch1.item(i1).getNodeType() + " and " + ch2.item(i2).getNodeType());
                    return false;
                }
                switch (ch1.item(i1).getNodeType()) {
                    case Node.ELEMENT_NODE: 
                        if (!compareElements((Element)ch1.item(i1), (Element)ch2.item(i2))) {
                            System.out.println("Different elements " + getElementStringRep((Element)ch1.item(i1)) + " and " + getElementStringRep((Element)ch2.item(i2)));
                            return false;
                        }
                        break;
                    case Node.TEXT_NODE:
                    case Node.CDATA_SECTION_NODE:
                        if (!compareStrings(ch1.item(i1).getNodeValue(), ch2.item(i2).getNodeValue())) {
                            System.out.println("Different text content '" + ch1.item(i1).getNodeValue() + "' and '" + ch2.item(i2).getNodeValue() + "'");
                            return false;
                        }
                        break;
                    default:
                        assert false;
                }
                i1++; 
                i2++;
            } else {
                if (i1 >= ch1.getLength() && i2 >= ch2.getLength()) {
                    return true;
                } else {
                    System.out.println("More children in " + getElementStringRep((Element)e1));
                    return false;
                }
            }
        }
    }
    
    private static String getElementStringRep(Element el) {
        String result = el.getLocalName();
        if (el.getNamespaceURI() != null) {
            result = el.getNamespaceURI() + ":" + result;
        }
        return result;
    }
    
    private static boolean compareStrings(String str1, String str2) {
        return (str1 == null) ? str2 == null : str1.equals(str2);
    }
    
    private static boolean isWhitespace(String s) {
        return Pattern.matches("^[ \t\r\n]+$", s);
    }
}
