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
 * AttributeTest.java
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
public class AttributeTest extends TestCase {
    
    public AttributeTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        xmlModel = Util.loadXDMModel("nodes/xdm.xml");
        um = new javax.swing.undo.UndoManager();
        um.setLimit(10);
        xmlModel.addUndoableEditListener(um);
        xmlModel.sync();
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AttributeTest.class);
        
        return suite;
    }

    /**
     * Test of getNodeType method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetNodeType() {
        
        short expResult = org.w3c.dom.Node.ATTRIBUTE_NODE;
        short result = attr.getNodeType();
        assertEquals("getNodeType must return ATTRIBUTE_NODE",expResult, result);
    }

    /**
     * Test of getNodeName method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetNodeName() {
        
        String expResult = "ssn";
        String result = attr.getNodeName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNodeValue method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetNodeValue() {
        
        String expResult = "xx-xx-xxxx";
        String result = attr.getNodeValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOwnerElement method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetOwnerElement() {
        
        Element expResult = (Element)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1);
        
        org.w3c.dom.Element result = attr.getOwnerElement();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLocalName method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetLocalName() {
        
        String expResult = "ssn";
        String result = attr.getLocalName();
        assertEquals(expResult, result);

        Attribute instance = new Attribute("xs:attribute");
        expResult = "attribute";
        result = instance.getLocalName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPrefix method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetPrefix() {
        
        assertNull(attr.getPrefix());

        Attribute instance = new Attribute("xs:attribute");
        String expResult = "xs";
        String result = instance.getPrefix();
        assertEquals(expResult, result);
    }

    /**
     * Test of setPrefix method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testSetPrefix() {
        Attribute oldAttr = attr;
        String oldPrefix = attr.getPrefix();
        String newPrefix = "xs";
        try {
            attr.setPrefix(newPrefix);
            assertTrue("setPrefix must throw exception for attribute node in tree",false);
        } catch (Exception e) {
            assertTrue(true);
        }
        Attribute newAttr = (Attribute)attr.clone(true,false,false);
        try {
            newAttr.setPrefix(newPrefix);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setPrefix must not throw exception for attribute node not in tree",false);
        }
        xmlModel.modify(attr,newAttr);
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertEquals(newPrefix,attr.getPrefix());

        //try undo and make sure original tree is not changed
        um.undo();
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertSame(oldAttr,attr);
        assertNull(attr.getPrefix());
        um.redo();
        
        // try to remove prefix
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        oldAttr = attr;
        newAttr = (Attribute)attr.clone(true,false,false);
        try {
            newAttr.setPrefix("");
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setPrefix must not throw exception for attribute node not in tree",false);
        }
        
        // now that xdmmodelundoableedit can absorb, need to control absorption
        um.discardAllEdits();
        xmlModel.modify(attr,newAttr);
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertNull(attr.getPrefix());

        //try undo and make sure previous tree is not changed
        um.undo();
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertSame(oldAttr,attr);
        assertEquals(newPrefix,attr.getPrefix());
    }

    /**
     * Test of getName method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetName() {
        
        String expResult = "ssn";
        String result = attr.getName();
        assertEquals(expResult, result);

        Attribute instance = new Attribute("xs:attribute");
        expResult = "xs:attribute";
        result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setName method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testSetName() {
        
        Attribute oldAttr = attr;
        String oldName = attr.getName();
        String newName = "ssn1";
        try {
            attr.setName(newName);
            assertTrue("setName must throw exception for attribute node in tree",false);
        } catch (Exception e) {
            assertTrue(true);
        }
        Attribute newAttr = (Attribute)attr.clone(true,false,false);
        try {
            newAttr.setName(newName);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setName must not throw exception for attribute node not in tree",false);
        }
        xmlModel.modify(attr,newAttr);
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertEquals(newName,attr.getName());

        //try undo and make sure old tree is not changed
        um.undo();
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertSame(oldAttr,attr);
        assertEquals(oldName,attr.getName());
    }

    /**
     * Test of getValue method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testGetValue() {
        
        String expResult = "xx-xx-xxxx";
        String result = attr.getValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of setValue method, of class org.netbeans.modules.xml.xdm.nodes.Attribute.
     */
    public void testSetValue() {
        
        Attribute oldAttr = attr;
        String oldValue = oldAttr.getValue();
        String newValue = "123-45-6789";
        try {
            attr.setValue(newValue);
            assertTrue("setValue must throw exception for attribute node in tree",false);
        } catch (Exception e) {
            assertTrue(true);
        }
        Attribute newAttr = (Attribute)attr.clone(true,false,false);
        try {
            newAttr.setValue(newValue);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setValue must not throw exception for attribute node not in tree",false);
        }
        xmlModel.modify(attr,newAttr);
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertEquals(newValue,attr.getValue());
        //try undo and make sure old tree is not changed
        um.undo();
        attr = (Attribute)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getAttributes().item(0);
        assertSame(oldAttr,attr);
        assertEquals(oldValue,attr.getValue());
    }
    
    private XDMModel xmlModel;
    private Attribute attr;
    private javax.swing.undo.UndoManager um;
}
