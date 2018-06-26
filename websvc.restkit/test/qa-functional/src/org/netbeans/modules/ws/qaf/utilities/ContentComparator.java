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
package org.netbeans.modules.ws.qaf.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jungi
 */
public final class ContentComparator {

    /** Creates a new instance of ContentComparator */
    private ContentComparator() {
    }

    /**
     *
     *  Compares the content of two xml files. Ignores whitespaces.
     *
     *@param f1 ususally goldenfile
     *@param f2 other file which we want to compare against goldenfile (or any other file)
     *@return true iff both files have the same content except of whitespaces
     */
    public static boolean equalsXML(File f1, File f2) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document d1 = db.parse(f1);
            Document d2 = db.parse(f2);
            return compare(d1.getDocumentElement(), d2.getDocumentElement());
        } catch (ParserConfigurationException e) {
            System.err.println("Exception from test - comparing XML files");
            e.printStackTrace(System.err);
        } catch (SAXException e) {
            System.err.println("Exception from test - comparing XML files");
            e.printStackTrace(System.err);
        } catch (IOException e) {
            System.err.println("Exception from test - comparing XML files");
            e.printStackTrace(System.err);
        }
        return false;
    }

    /**
     *  Compares two manifest files. First check is for number of attributes,
     * next one is comparing name-value pairs.
     *
     *@param mf1, mf2 manifests to compare
     *@param ignoredEntries array of manifest entries to ignore
     *@return true if files contains the same entries/values in manifest
     */
    public static boolean equalsManifest(File mf1, File mf2, String[] ignoredEntries) {
        if (ignoredEntries == null) {
            ignoredEntries = new String[]{};
        }
        try {
            Manifest m1 = new Manifest(new FileInputStream(mf1));
            Manifest m2 = new Manifest(new FileInputStream(mf2));
            Attributes a1 = m1.getMainAttributes();
            Attributes a2 = m2.getMainAttributes();
            if (a1.size() != a2.size()) {
                return false;
            }
            for (Iterator<Object> i = a1.keySet().iterator(); i.hasNext();) {
                Attributes.Name a = (Attributes.Name) i.next();
                boolean b = true;
                for (int j = 0; j < ignoredEntries.length; j++) {
                    if (a.toString().equals(ignoredEntries[j])) {
                        a2.remove(a);
                        b = false;
                        break;
                    }
                }
                if (b && (a1.get(a).equals(a2.get(a)))) {
                    a2.remove(a);
                }
            }
            return a2.isEmpty();
        } catch (FileNotFoundException fnfe) {
            System.err.println("Exception from test - comparing manifests");
            fnfe.printStackTrace(System.err);
        } catch (IOException ioe) {
            System.err.println("Exception from test - comparing manifests");
            ioe.printStackTrace(System.err);
        }
        return false;
    }

    //gf
    //bad
    private static boolean compare(Node n1, Node n2) {
        List<Node> l1 = new LinkedList<Node>();
        List<Node> l2 = new LinkedList<Node>();
        l1.add(n1);
        l2.add(n2);
        while (!l1.isEmpty() && !l2.isEmpty()) {
            Node m1 = l1.remove(0);
            Node m2 = l2.remove(0);
            //check basic things - node name, value, attributes - iff they're OK, we can continue
            if (sameNode(m1, m2)) {
                //now compare children
                NodeList nl = m1.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    Node e = nl.item(i);
                    if (e.getNodeType() == Node.TEXT_NODE) {
                        //ignore empty places
                        if (e.getNodeValue().trim().equals("")) {
                            continue;
                        }
                    }
                    l1.add(nl.item(i));
                }
                nl = m2.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    Node e = nl.item(i);
                    if (e.getNodeType() == Node.TEXT_NODE) {
                        //ignore empty places
                        if (e.getNodeValue().trim().equals("")) {
                            continue;
                        }
                    }
                    l2.add(nl.item(i));
                }
            } else {
                //nodes are not equals - print some info
                System.err.println("================================================");
                System.err.println("m1: " + m1.getNodeName() + "; \'" + m1.getNodeValue() + "\'");
                System.err.println("m2: " + m2.getNodeName() + "; \'" + m2.getNodeValue() + "\'");
                System.err.println("================================================");
                return false;
            }
        }
        return true;
    }

    //attrs, name, value
    private static boolean sameNode(Node n1, Node n2) {
        //check node name
        if (!n1.getNodeName().equals(n2.getNodeName())) {
            System.err.println("================================================");
            System.err.println("Expected node: " + n1.getNodeName() + ", got: " + n2.getNodeName());
            System.err.println("================================================");
            return false;
        }
        //check node value
        if (!((n1.getNodeValue() != null)
                ? n1.getNodeValue().equals(n2.getNodeValue())
                : (n2.getNodeValue() == null))) {
            System.err.println("================================================");
            System.err.println("Expected node value: " + n1.getNodeValue() + ", got: " + n2.getNodeValue());
            System.err.println("================================================");
            return false;
        }
        //check node attributes
        NamedNodeMap nnm1 = n1.getAttributes();
        NamedNodeMap nnm2 = n2.getAttributes();
        if ((nnm1 == null && nnm2 != null) || (nnm1 != null && nnm2 == null)) {
            return false;
        }
        if (nnm1 == null && nnm2 == null) {
            return true;
        }
        for (int i = 0; i < nnm1.getLength(); i++) {
            Node x = nnm1.item(i);
            Node y = nnm2.item(i);
            if (!(x.getNodeName().equals(y.getNodeName()) && x.getNodeValue().equals(y.getNodeValue()))) {
                //nodes are not equals - print some info
                System.err.println("================================================");
                System.err.println("Expected attribute: " + x.getNodeName() + "=\'" + x.getNodeValue() + "\'," + " got: " + y.getNodeName() + "=\'" + y.getNodeValue() + "\'");
                System.err.println("================================================");
                return false;
            }
        }
        return true;
    }
}
