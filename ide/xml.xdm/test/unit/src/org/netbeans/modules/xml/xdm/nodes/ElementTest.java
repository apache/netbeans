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
 * ElementTest.java
 * JUnit based test
 *
 * Created on October 21, 2005, 2:21 PM
 */

package org.netbeans.modules.xml.xdm.nodes;

import java.util.HashMap;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import junit.framework.*;
import org.netbeans.modules.xml.xam.TestComponent3;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author ajit
 */
public class ElementTest extends TestCase {
    
    public ElementTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        xmlModel = Util.loadXDMModel("nodes/xdm.xml");
        xmlModel.sync();
        elem = (Element)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ElementTest.class);
        
        return suite;
    }
    
    public void testGetNodeType() {
        short expResult = org.w3c.dom.Node.ELEMENT_NODE;
        short result = elem.getNodeType();
        assertEquals("getNodeType must return ATTRIBUTE_NODE",expResult, result);
    }
    
    public void testGetNodeName() {
        String expResult = "employee";
        String result = elem.getNodeName();
        assertEquals(expResult, result);
    }
    
    public void testGetTagName() {
        String expResult = "employee";
        String result = elem.getTagName();
        assertEquals(expResult, result);
        
        Element instance = new Element("xs:element");
        expResult = "xs:element";
        result = instance.getTagName();
        assertEquals(expResult, result);
    }
    
    public void testGetPrefix() {
        assertNull(elem.getPrefix());
        
        Element instance = new Element("xs:element");
        String expResult = "xs";
        String result = instance.getPrefix();
        assertEquals(expResult, result);
    }
    
    public void testSetPrefix() {
        Element origElem = elem;
        String origPrefix = elem.getPrefix();
        String newPrefix = "xs";
        try {
            elem.setPrefix(newPrefix);
            assertTrue("setPrefix must throw exception for element node in tree",false);
        } catch (Exception e) {
            assertTrue(true);
        }
        Element newElem = (Element)elem.clone(true,false,false);
        try {
            newElem.setPrefix(newPrefix);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setPrefix must not throw exception for element node not in tree",false);
        }
        xmlModel.modify(elem,newElem);
        elem = (Element)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1);
        assertEquals(newPrefix,elem.getPrefix());
        //make sure old tree is not changed
        assertEquals(origPrefix,origElem.getPrefix());
        
        // try to remove prefix
        Element modifiedElem = elem;
        newElem = (Element)elem.clone(true,false,false);
        try {
            newElem.setPrefix("");
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setPrefix must not throw exception for element node not in tree",false);
        }
        xmlModel.modify(elem,newElem);
        elem = (Element)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1);
        assertNull(elem.getPrefix());
        //make sure modifiedElem has prefix = xs
        assertEquals(newPrefix,modifiedElem.getPrefix());
    }
    
    public void testGetLocalName() {
        String expResult = "employee";
        String result = elem.getLocalName();
        assertEquals(expResult, result);
        
        Element instance = new Element("xs:element");
        expResult = "element";
        result = instance.getLocalName();
        assertEquals(expResult, result);
    }
    
    public void testSetLocalName() {
        Element origElem = elem;
        String origName = elem.getLocalName();
        String newName = "employee1";
        try {
            elem.setLocalName(newName);
            assertTrue("setLocalName must throw exception for element node in tree",false);
        } catch (Exception e) {
            assertTrue(true);
        }
        Element newElem = (Element)elem.clone(true,false,false);
        try {
            newElem.setLocalName(newName);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setLocalName must not throw exception for element node not in tree",false);
        }
        xmlModel.modify(elem,newElem);
        elem = (Element)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1);
        assertEquals(newName,elem.getLocalName());
        //make sure old tree is not changed
        assertEquals(origName,origElem.getLocalName());
    }

    public void testGetAttributes() {
        NamedNodeMap attributes = elem.getAttributes();
        assertEquals(4, attributes.getLength());
        assertNotNull(attributes.getNamedItem("ssn"));
        assertSame(attributes.getNamedItem("ssn"),attributes.item(0));
        assertNotNull(attributes.getNamedItem("id"));
        assertSame(attributes.getNamedItem("id"),attributes.item(1));
        assertNotNull(attributes.getNamedItem("address"));
        assertSame(attributes.getNamedItem("address"),attributes.item(2));
        assertNotNull(attributes.getNamedItem("phone"));
        assertSame(attributes.getNamedItem("phone"),attributes.item(3));
        assertNull(attributes.getNamedItem("ssn1"));
        
        Element company = (Element)xmlModel.getDocument().getDocumentElement();
        NamedNodeMap companyAttrs = company.getAttributes();
        assertEquals(1, companyAttrs.getLength());
        assertNotNull(companyAttrs.getNamedItem("xmlns"));
    }

    public void testGetChildNodes() {
        Element company = (Element)xmlModel.getDocument().getDocumentElement();
        NodeList children = company.getChildNodes();
        assertEquals(7, children.getLength());
        assertTrue(children.item(0) instanceof Text);
        assertTrue(children.item(1) instanceof Element);
        assertEquals("employee",children.item(1).getNodeName());
        assertTrue(children.item(2) instanceof Text);
        assertTrue(children.item(3) instanceof Text);
        assertEquals(" comment ",children.item(3).getNodeValue());
        assertTrue(children.item(4) instanceof Text);
        assertTrue(children.item(5) instanceof Element);
        assertEquals("employee",children.item(5).getNodeName());
        assertTrue(children.item(6) instanceof Text);
    }

    public void testGetAttribute() {
        String name = "ssn";
        String expResult = "xx-xx-xxxx";
        String result = elem.getAttribute(name);
        assertEquals(expResult, result);
        // try for non-existent attribute
        assertNull(elem.getAttributeNode("ssn1"));
    }

    public void testGetAttributeNode() {
        String name = "ssn";
        Attribute expResult = (Attribute)elem.getAttributes().item(0);
        Attribute result = elem.getAttributeNode(name);
        assertSame(expResult, result);
        // try for non-existent attribute
        assertNull(elem.getAttributeNode("ssn1"));
    }

    public void testHasAttribute() {
        String name="ssn";
        boolean expResult = true;
        boolean result = elem.hasAttribute(name);
        assertEquals(expResult, result);
        // try for non-existent attribute
        assertFalse(elem.hasAttribute("ssn1"));
    }
    
    private Element getEmployee(XDMModel model) {
	Element company = (Element) 
	    xmlModel.getDocument().getChildNodes().item(0);
	NodeList children = company.getChildNodes();
	Element employee = null;
	for (int i = 0; i < children.getLength(); i++) {
	    org.w3c.dom.Node child = children.item(i);
	    if (child instanceof Element) {
		Element e = (Element) child;
		if (e.getLocalName().equals("employee")) {
		    employee = e;
		}
	    }
		
	}
	return employee;
    }
    
    public void testConversionFromSelfClosingElement() throws Exception {
        xmlModel = Util.loadXDMModel("nodes/self-closing.xml");
	Element employee = getEmployee(xmlModel);
	assertNotNull("could not find employee element", employee);
	
	final String TEST_ELEMENT_NAME = "testChild"; 
	org.w3c.dom.Element childNode = 
	    xmlModel.getDocument().createElement(TEST_ELEMENT_NAME);
	Element clonedEmployee = (Element) employee.cloneNode(true);
	clonedEmployee.appendChild(childNode);
	xmlModel.modify(employee,clonedEmployee);
	
	xmlModel.flush();
	xmlModel.sync();
	
	employee = getEmployee(xmlModel);
	assertNotNull("employee does not have child element", 
	    employee.getFirstChild());
	assertEquals("childElement not equal to testChild",
	    employee.getFirstChild().getLocalName(), TEST_ELEMENT_NAME);
    }

    public void testCloneNodeNamespacesConsolidateToRoot() throws Exception {
        xmlModel = Util.loadXDMModel("nodes/cloned-node.xml");
        Element root = (Element) xmlModel.getDocument().getDocumentElement();
        Element cloned = Util.getChildElementByTag(root, "a1:A");
        Element clone = (Element)cloned.cloneNode(true);
        
        assertFalse(clone.isInTree());
        assertTrue(clone.getModel() == null);
        assertEquals("namespaceA", clone.getNamespaceURI());
        assertEquals("namespaceA", clone.getAttribute("xmlns:a1"));
        assertEquals("namespaceB", clone.lookupNamespaceURI(""));
        Element cloneChildB = Util.getChildElementByTag(clone, "B");
        assertEquals("namespaceB", cloneChildB.lookupNamespaceURI("b1"));
        
        javax.swing.text.Document doc = Util.getResourceAsDocument("nodes/clone-receiver.xml");
        XDMModel dest = Util.loadXDMModel(doc);
        Element receiverRoot = (Element) dest.getDocument().getDocumentElement();
        Element middle = Util.getChildElementByTag(receiverRoot, "m:middle");
        middle = (Element) dest.append(middle, clone).get(0);

        //dest.flush();
        //Util.ToFile(doc, new File("c:\\temp\\test1.xml"));
        
        receiverRoot = (Element) dest.getDocument().getDocumentElement();
        assertTrue(middle == Util.getChildElementByTag(receiverRoot, "m:middle"));
        Element pasted = Util.getChildElementByTag(middle, "a1:A");
        assertEquals("namespaceB", receiverRoot.lookupNamespaceURI(""));
        assertEquals("namespaceA", receiverRoot.lookupNamespaceURI("a1"));
        assertNull(pasted.getAttributeNode("xmlns:a1"));
        assertNull(pasted.getAttributeNode("xmlns"));
    }
    
    public void testInsertWithFullConsolidation() throws Exception {
        xmlModel = Util.loadXDMModel("nodes/cloned-node.xml");
        Element root = (Element) xmlModel.getDocument().getDocumentElement();
        Element cloned = Util.getChildElementByTag(root, "a1:A");
        Element clone = (Element)cloned.cloneNode(true);
        assertEquals("namespaceB", clone.getAttribute("xmlns:b1"));
        assertEquals("namespaceB", clone.getAttribute("xmlns"));
        
        javax.swing.text.Document doc = Util.getResourceAsDocument("nodes/clone-receiver3.xml");
        XDMModel dest = Util.loadXDMModel(doc);
        dest.setQNameValuedAttributes(new HashMap<QName,List<QName>>());
        Element receiverRoot = (Element) dest.getDocument().getDocumentElement();
        Element middle = Util.getChildElementByTag(receiverRoot, "m:middle");
        middle = (Element) dest.add(middle, clone, 0).get(0);

        dest.flush();
        
        receiverRoot = (Element) dest.getDocument().getDocumentElement();
        assertTrue(middle == Util.getChildElementByTag(receiverRoot, "m:middle"));
        Element pasted = Util.getChildElementByTag(middle, "a2:A");
        assertNull(receiverRoot.lookupNamespaceURI(""));
        assertNull(receiverRoot.lookupNamespaceURI("a1"));
        assertNull(pasted.getAttributeNode("xmlns:a1"));
        assertNull(pasted.getAttributeNode("xmlns:b1"));
        assertNull(pasted.getAttributeNode("xmlns"));
        Element pastedChild = Util.getChildElementByTag(pasted, "b1:B");
        assertEquals("value1", pastedChild.getAttributeNS("namespaceB","attrB"));
        assertNull(pastedChild.getAttributeNode("xmlns:b1"));
    }
    
    public void testInsertWithLimitedConsolidationDueToPrefixedAttribute() throws Exception {
        xmlModel = Util.loadXDMModel("nodes/cloned-node.xml");
        Element root = (Element) xmlModel.getDocument().getDocumentElement();
        Element cloned = Util.getChildElementByTag(root, "a1:A");
        Element clone = (Element)cloned.cloneNode(true);
        assertEquals("namespaceB", clone.getAttribute("xmlns:b1"));
        assertEquals("namespaceB", clone.getAttribute("xmlns"));
        
        javax.swing.text.Document doc = Util.getResourceAsDocument("nodes/clone-receiver2.xml");
        XDMModel dest = Util.loadXDMModel(doc);
        dest.setQNameValuedAttributes(new HashMap<QName,List<QName>>());
        Element receiverRoot = (Element) dest.getDocument().getDocumentElement();
        Element middle = Util.getChildElementByTag(receiverRoot, "m:middle");
        middle = (Element) dest.add(middle, clone, 0).get(0);

        dest.flush();
        //Util.dumpToFile(doc, new File("c:\\temp\\test1.xml"));
        
        receiverRoot = (Element) dest.getDocument().getDocumentElement();
        assertTrue(middle == Util.getChildElementByTag(receiverRoot, "m:middle"));
        Element pasted = Util.getChildElementByTag(middle, "a2:A");
        assertNull(receiverRoot.lookupNamespaceURI(""));
        assertNull(receiverRoot.lookupNamespaceURI("a1"));
        assertNull(pasted.getAttributeNode("xmlns:a1"));
        assertNull(pasted.getAttributeNode("xmlns:b1"));
        assertNull(pasted.getAttributeNode("xmlns"));
        Element pastedChild = Util.getChildElementByTag(pasted, "B");
        assertEquals("value1", pastedChild.getAttributeNS("namespaceB","attrB"));
        assertEquals("namespaceB", pastedChild.getAttribute("xmlns:b1"));
    }

    public void testElementGetAttributes() throws Exception {
        xmlModel = Util.loadXDMModel("nodes/elementAttributes.xml");
        /*
            The test case to verify:

            have an element something like

            <e someNs:myAttribute="7" myAttribute="8" otherNs:myAttribute="9">

            if you invoke Element.getAttributeByNS("someNS", "myAttribute) == 7
                          Element.getAttribute("myAttribute") == 8 // verify the semantics
            in DOM
                          Element.getAttributeByNS("otherNS", "myAttribute") == 9 these
            should return the distinct values not always the same value. 
         */
        Element test = (Element) xmlModel.getDocument().getChildNodes().item(0);
        Element e = (Element) test.getChildNodes().item(1);
        String attrVal1 = e.getAttributeNS("http://org.company/schemas/test1.xsd", "myAttribute");
        assertEquals("someNs:myAttribute", "7", attrVal1);
        String attrVal2 = e.getAttribute("myAttribute");
        assertEquals("myAttribute", "8", attrVal2);
        String attrVal3 = e.getAttributeNS("http://org.company/schemas/test2.xsd", "myAttribute");
        assertEquals("otherNs:myAttribute", "9", attrVal3);
    }
    
    public void testConsolidateNamespace() throws Exception {
        javax.swing.text.Document doc = Util.getResourceAsDocument("nodes/test2.xml");
        XDMModel model = Util.loadXDMModel(doc);
        model.setQNameValuedAttributes(new HashMap<QName,List<QName>>());
        Element root = (Element) model.getDocument().getDocumentElement();
        Element a1 = (Element) model.getDocument().createElementNS(TestComponent3.NS_URI, "a1");
        assertEquals(TestComponent3.NS_URI, a1.getAttribute(XMLConstants.XMLNS_ATTRIBUTE));

        Element a2 = (Element) model.getDocument().createElementNS(TestComponent3.NS_URI, "a2");
        a1.appendChild(a2);
        assertNull(a2.getAttributeNode(XMLConstants.XMLNS_ATTRIBUTE));
        
        model.append(root, a1);
        assertNull(a1.getAttributeNode(XMLConstants.XMLNS_ATTRIBUTE));
    }
    
    public void testConsolidateDefaultToDeclaredPrefix() throws Exception {
        xmlModel = Util.loadXDMModel("nodes/cloned-node4.xml");
        Element root = (Element) xmlModel.getDocument().getDocumentElement();
        Element cloned = Util.getChildElementByTag(root, "A");
        Element clone = (Element)cloned.cloneNode(true);
        
        assertFalse(clone.isInTree());
        assertTrue(clone.getModel() == null);
        assertEquals("namespaceA", clone.getNamespaceURI());
        assertEquals("namespaceA", clone.getAttribute("xmlns"));
        assertEquals("namespaceA", clone.lookupNamespaceURI(""));
        Element cloneChildB = Util.getChildElementByTag(clone, "B");
        assertEquals("namespaceB", cloneChildB.lookupNamespaceURI("b1"));
        
        javax.swing.text.Document doc = Util.getResourceAsDocument("nodes/clone-receiver4.xml");
        XDMModel dest = Util.loadXDMModel(doc);
        dest.setQNameValuedAttributes(new HashMap<QName,List<QName>>());
        Element receiverRoot = (Element) dest.getDocument().getDocumentElement();
        Element middle = Util.getChildElementByTag(receiverRoot, "m:middle");
        middle = (Element) dest.append(middle, clone).get(0);

        dest.flush();
        //Util.dumpToFile(doc, new File("c:/temp/test2.xml"));
        
        receiverRoot = (Element) dest.getDocument().getDocumentElement();
        assertTrue(middle == Util.getChildElementByTag(receiverRoot, "m:middle"));
        Element pasted = Util.getChildElementByTag(middle, "a1:A");
        assertEquals("namespaceA", receiverRoot.lookupNamespaceURI("a1"));
        assertNull(pasted.getAttributeNode("xmlns:a1"));
        assertNull(pasted.getAttributeNode("xmlns"));
    }

    public void testSetAttributeWithXmlEscape() throws Exception {
        javax.swing.text.Document doc = Util.getResourceAsDocument("nodes/test2.xml");
        XDMModel model = Util.loadXDMModel(doc);
        Element root = (Element) model.getDocument().getDocumentElement();
        Element a1 = Util.getChildElementByTag(root, "tst:a");
        String shouldEscapeXML = "a < b && c == '3'";
        model.setAttribute(a1, "newattr", shouldEscapeXML);
        model.flush();
        String doctext = doc.getText(0, doc.getLength()-1);
        //System.out.println(doctext);
        assertEquals(-1, doctext.indexOf(shouldEscapeXML));
        assertEquals(124, doctext.indexOf("&lt;"));
        assertEquals(131, doctext.indexOf("&amp;&amp;"));
        assertEquals(147, doctext.indexOf("&apos;3&apos;"));
    }
    
    private XDMModel xmlModel;
    private Element elem;
}
