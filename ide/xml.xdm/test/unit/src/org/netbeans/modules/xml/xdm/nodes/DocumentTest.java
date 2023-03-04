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

/*
 * DocumentTest.java
 * JUnit based test
 *
 * Created on October 21, 2005, 2:21 PM
 */

package org.netbeans.modules.xml.xdm.nodes;

import junit.framework.*;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;

/**
 *
 * @author ajit
 */
public class DocumentTest extends TestCase {
    
    public DocumentTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        xmlModel = Util.loadXDMModel("nodes/xdm.xml");
        xmlModel.sync();
        doc = xmlModel.getDocument();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DocumentTest.class);
        
        return suite;
    }

    /**
     * Test of getNodeType method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testGetNodeType() {
        
        short expResult = org.w3c.dom.Node.DOCUMENT_NODE;
        short result = doc.getNodeType();
        assertEquals("getNodeType must return DOCUMENT_NODE",expResult, result);
    }

    /**
     * Test of getNodeName method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testGetNodeName() {
        
        String expResult = "#document";
        String result = doc.getNodeName();
        assertEquals("getNodeName must return #document",expResult, result);
    }

    /**
     * Test of createElement method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testCreateElement() {
        
        String tagName = "newElement";
        org.w3c.dom.Element result = doc.createElement(tagName);
        assertEquals(tagName, result.getTagName());
    }

    /**
     * Test of createAttribute method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testCreateAttribute() {
        
        String name = "attrName";
        org.w3c.dom.Attr result = doc.createAttribute(name);
        assertEquals(name, result.getName());
    }

    /**
     * Test of createElementNS method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testCreateElementNS() {
        
        String namespaceURI = "";
        String qualifiedName = "xs:element";
        org.w3c.dom.Element result = doc.createElementNS(namespaceURI, qualifiedName);
        assertEquals("element", result.getLocalName());
        assertEquals("xs", result.getPrefix());
    }

    /**
     * Test of createAttributeNS method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testCreateAttributeNS() {
        
        String namespaceURI = "";
        String qualifiedName = "xs:attribute";
        org.w3c.dom.Attr result = doc.createAttributeNS(namespaceURI, qualifiedName);
        assertEquals("attribute", result.getLocalName());
        assertEquals("xs", result.getPrefix());
    }

    /**
     * Test of getDocumentElement method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testGetDocumentElement() {
        
        Element expResult = (Element)doc.getChildNodes().item(0);
        Element result = (Element)doc.getDocumentElement();
        assertNotNull(result);
        assertEquals(expResult, result);
    }

    /**
     * Test of getXmlVersion method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testGetXmlVersion() {
        
        String expResult = "1.0";
        String result = doc.getXmlVersion();
        assertEquals(expResult, result);
    }

    /**
     * Test of getXmlEncoding method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testGetXmlEncoding() {
        
        String expResult = "UTF-8";
        String result = doc.getXmlEncoding();
        assertEquals(expResult, result);
    }

    /**
     * Test of getXmlStandalone method, of class org.netbeans.modules.xml.xdm.nodes.Document.
     */
    public void testGetXmlStandalone() {
        
        boolean expResult = false;
        boolean result = doc.getXmlStandalone();
        assertEquals(expResult, result);
    }

    private XDMModel xmlModel;
    private Document doc;
}
