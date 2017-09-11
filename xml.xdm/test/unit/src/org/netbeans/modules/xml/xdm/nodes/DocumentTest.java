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
