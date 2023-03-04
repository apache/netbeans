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
package org.netbeans.test.j2ee.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private static final Logger LOGGER = Logger.getLogger(ContentComparator.class.getName());

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
            LOGGER.log(Level.WARNING, "Exception from test - comparing XML files", e); //NOI18N
        } catch (SAXException e) {
            LOGGER.log(Level.WARNING, "Exception from test - comparing XML files", e); //NOI18N
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Exception from test - comparing XML files", e); //NOI18N
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
            ignoredEntries = new String[] {};
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
            LOGGER.log(Level.WARNING, "Exception from test - comparing manifests", fnfe); //NOI18N
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Exception from test - comparing manifests", ioe); //NOI18N
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
            //XXX
            // //ejb-jar/display-name element randomly disappears from ejb-jar.xml
            // therefore we skip the check for this node here
            //see issue #122947 for details
            if ("display-name".equals(m1.getNodeName()) && "ejb-jar".equals(m1.getParentNode().getNodeName())) { //NOI18N
                LOGGER.warning("skiping /ejb-jar/display-name from golden file"); //NOI18N
                m1 = l1.remove(0);
            }
            if ("display-name".equals(m2.getNodeName()) && "ejb-jar".equals(m2.getParentNode().getNodeName())) { //NOI18N
                m2 = l2.remove(0);
                LOGGER.warning("skiping /ejb-jar/display-name from created file"); //NOI18N
            }
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
                LOGGER.warning("================================================"); //NOI18N
                LOGGER.warning("m1: " + m1.getNodeName() + "; \'" + m1.getNodeValue() + "\'"); //NOI18N
                LOGGER.warning("m2: " + m2.getNodeName() + "; \'" + m2.getNodeValue() + "\'"); //NOI18N
                LOGGER.warning("================================================"); //NOI18N
                return false;
            }
        }
        return true;
    }

    //attrs, name, value
    private static boolean sameNode(Node n1, Node n2) {
        //check node name
        if (!n1.getNodeName().equals(n2.getNodeName())) {
            LOGGER.warning("================================================"); //NOI18N
            LOGGER.warning("Expected node: " + n1.getNodeName() + ", got: " + n2.getNodeName()); //NOI18N
            LOGGER.warning("================================================"); //NOI18N
            return false;
        }
        //check node value
        if (!((n1.getNodeValue() != null)
                ? n1.getNodeValue().equals(n2.getNodeValue())
                : (n2.getNodeValue() == null))) {
            LOGGER.warning("================================================"); //NOI18N
            LOGGER.warning("Expected node value: " + n1.getNodeValue() + ", got: " + n2.getNodeValue()); //NOI18N
            LOGGER.warning("================================================"); //NOI18N
            return false;
        }
        //check node attributes
        NamedNodeMap nnm1 = n1.getAttributes();
        NamedNodeMap nnm2 = n2.getAttributes();
        if ((nnm1 == null && nnm2 != null)
                || (nnm1 != null && nnm2 == null)) {
            return false;
        }
        if (nnm1 == null && nnm2 == null) {
            return true;
        }
        for (int i = 0; i < nnm1.getLength(); i++) {
            Node x = nnm1.item(i);
            Node y = nnm2.item(i);
            if (!(x.getNodeName().equals(y.getNodeName())
                    && x.getNodeValue().equals(y.getNodeValue()))) {
                //nodes are not equals - print some info
                LOGGER.warning("================================================"); //NOI18N
                LOGGER.warning("Expected attribute: " + x.getNodeName() + "=\'" +  x.getNodeValue() + "\'," //NOI18N
                        + " got: " + y.getNodeName() + "=\'" +  y.getNodeValue() + "\'"); //NOI18N
                LOGGER.warning("================================================"); //NOI18N
                return false;
            }
        }
        return true;
    }

}
