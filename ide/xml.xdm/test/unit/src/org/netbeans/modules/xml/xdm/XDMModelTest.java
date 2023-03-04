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
 * XMLModelTest.java
 * JUnit based test
 *
 * Created on August 5, 2005, 12:13 PM
 */
package org.netbeans.modules.xml.xdm;

import java.beans.PropertyChangeListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.List;
import javax.swing.undo.UndoManager;
import junit.framework.*;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xdm.nodes.*;
import org.netbeans.modules.xml.xdm.visitor.PathFromRootVisitor;
import org.netbeans.modules.xml.xdm.visitor.FlushVisitor;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.NodeList;

/**
 *
 * @author Administrator
 */
public class XDMModelTest extends TestCase {
    
    public XDMModelTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new XDMModelTest("testAdd"));
        suite.addTest(new XDMModelTest("testAddNegative"));
        suite.addTest(new XDMModelTest("testAddToSelfClosing"));
        suite.addTest(new XDMModelTest("testAppend"));
        suite.addTest(new XDMModelTest("testDelete"));
        suite.addTest(new XDMModelTest("testFlush"));
        suite.addTest(new XDMModelTest("testModify"));
        suite.addTest(new XDMModelTest("testModifyNegative"));
        suite.addTest(new XDMModelTest("testSyncAndNamespace"));
        suite.addTest(new XDMModelTest("testSyncToEmptyRoot"));
//        Disabled as referenced files were partly not donated by oracle to apache
//        suite.addTest(new XDMModelTest("testXDMModelSize"));
        return suite;
    }
    
    public void testAddNegative() throws Exception {
        // verify that a node which is already in the tree cannot be added
        Node company = (Node)model.getDocument().getChildNodes().item(0);
        Node employee = (Node)company.getChildNodes().item(1);
        try {
            model.add(company, employee, 0);
            fail("adding a node already in the tree should throw exception");
        } catch (IllegalArgumentException iae) {
            
        }
    }
    
    public void testModifyNegative() throws Exception {
        // verify that a node which is already in the tree cannot be added
        Node company = (Node)model.getDocument().getChildNodes().item(0);
        Node employee = (Node)company.getChildNodes().item(1);
        try {
            model.modify(employee, employee);
            fail("modifying a node already in the tree should throw exception");
        } catch (IllegalArgumentException iae) {
            
        }
        
        // now try to substitute a different node
        try {
            Element e = (Element)model.getDocument().createElement("");
            model.modify(employee, e);
            fail("attempting to modify a node with a non equal node should throw exception");
        } catch (IllegalArgumentException iae) {
            
        }
    }
    
    /**
     * Test of add method, of class xml.nodes.XMLModel.
     */
    public void testAdd() {
        Document original = model.getDocument();
        um.setLimit(10);
        model.addUndoableEditListener(um);
        FlushVisitor fv = new FlushVisitor();
        String originalText = fv.flushModel(original);
//		 Expected model
//		 Document
//		   Element -- company
//		      Element -- employee
//		          Attribute -- ssn xx-xx-xxxx
//		          Attribute -- id
//		          Attribute -- phone
//		        Text -- Vidhya
//		      End Element
//		   End Element
        
        // first add another child element
        Node company = (Node)model.getDocument().getChildNodes().item(0);
        Node employee = (Node)company.getChildNodes().item(1);
        Element customer = (Element)model.getDocument().createElement("customer");
        
        TestListener tl = new TestListener();
        model.addPropertyChangeListener(tl);
        model.add(employee,customer,0);
        
        String modifiedText = fv.flushModel(model.getDocument());
        assertNotSame("text should have been modified to add new attribute", originalText,modifiedText);
        assertTrue("only one event should be fired " + tl.getEventsFired(), tl.getEventsFired()==1);
        assertEquals("event should be modified", model.PROP_ADDED, tl.getLastEventName());
        
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        assertNotNull("customer should now be in the tree",
                pfrv.findPath(model.getDocument(), customer));
        
        // TODO verify some of the nodes which should not have been cloned
        
        // now verify that the new child element is added in right location
        company = (Node)model.getDocument().getChildNodes().item(0);
        employee = (Node)company.getChildNodes().item(1);
        Element customer2 = (Element) employee.getChildNodes().item(0);
        
        assertEquals("expected name was not set", customer2.getLocalName(), customer.getLocalName());
        
        // verify undo / redo
        assertTrue("undo manager should have one event", um.canUndo());
        assertFalse("undo manager should not allow redo", um.canRedo());
        
        Document newD = model.getDocument();
        um.undo();
        assertSame("model not original tree", model.getDocument(), original);
        um.redo();
        assertSame("model not new tree", model.getDocument(), newD);
        
        //Adding a brand new element for testing
        company = (Node)model.getDocument().getChildNodes().item(0);
        Element emp = (Element)model.getDocument().createElement("employee");
        
        tl.resetFiredEvents();
        model.add(company,emp,0);
        
        assertTrue("only one event should be fired " + tl.getEventsFired(), tl.getEventsFired()==1);
        assertEquals("event should be added", model.PROP_ADDED, tl.getLastEventName());
        
        pfrv = new PathFromRootVisitor();
        assertNotNull("new employee should now be in the tree",
                pfrv.findPath(model.getDocument(), emp));
    }
    
    public void testAddToSelfClosing() throws Exception {
	sd = Util.getResourceAsDocument("selfClosing.xml");
	Lookup lookup = Lookups.singleton(sd);
	ModelSource ms = new ModelSource(lookup, true);
        model = new XDMModel(ms);
        model.sync();
	
        Document original = model.getDocument();
        //Adding a brand new element for testing
        Node company = (Node)model.getDocument().getChildNodes().item(0);
        Element emp = (Element)model.getDocument().createElement("employee");
        
        model.add(company,emp,0);
	model.flush();
	model.sync();
	company = (Node)model.getDocument().getChildNodes().item(0);
	emp = (Element) company.getFirstChild();
	assertEquals("employee is not local name", emp.getLocalName(), "employee");
	List<Token> tokens = emp.getTokens();
	int endTokenCount = 0;
	for (Token t: tokens) {
	    if (t.getType().equals(TokenType.TOKEN_ELEMENT_END_TAG)) {
		endTokenCount++;
	    }
	}
	assertEquals("employee should be created using self-closing tag", 1, endTokenCount);
    }
    
    /**
     * Test of append method, of class xml.nodes.XMLModel.
     */
    public void testAppend() {
        Document original = model.getDocument();
        um.setLimit(10);
        model.addUndoableEditListener(um);
//		 Expected model
//		 Document
//		   Element -- company
//		      Element -- employee
//		        Attribute -- ssn xx-xx-xxxx
//		        Attribute -- id
//		        Attribute -- phone
//		        Text -- Vidhya
//		      End Element
//		   End Element
        
        // first append another child element
        Node company = (Node)model.getDocument().getChildNodes().item(0);
        Node employee = (Node)company.getChildNodes().item(1);
        Element customer = (Element)model.getDocument().createElement("customer");
        
        TestListener tl = new TestListener();
        model.addPropertyChangeListener(tl);
        model.append(employee,customer);
        
        assertTrue("only one event should be fired " + tl.getEventsFired(), tl.getEventsFired()==1);
        assertEquals("event should be modified", model.PROP_ADDED, tl.getLastEventName());
        
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        assertNotNull("customer should now be in the tree",
                pfrv.findPath(model.getDocument(), customer));
        
        // TODO verify some of the nodes which should not have been cloned
        
        // now verify that the new child element is added in right location (at the end)
        company = (Node)model.getDocument().getChildNodes().item(0);
        employee = (Node)company.getChildNodes().item(1);
        Element customer2 = (Element) employee.getChildNodes().item(employee.getChildNodes().getLength()-1);
        
        assertEquals("expected name was not set", customer2.getLocalName(), customer.getLocalName());
        
        //Appending a brand new element with attributes
        company = (Node)model.getDocument().getChildNodes().item(0);
        Element emp2 = (Element)model.getDocument().createElement("employee");
        Attribute att = (Attribute)model.getDocument().createAttribute("id2");
        att.setValue("987");
        emp2.setAttributeNode(att);
        
        tl.resetFiredEvents();
        model.append(company, emp2);
        
        assertTrue("only one event should be fired " + tl.getEventsFired(), tl.getEventsFired()==1);
        assertEquals("event should be added", model.PROP_ADDED, tl.getLastEventName());
        
        pfrv = new PathFromRootVisitor();
        assertNotNull("new employee should now be in the tree",
                pfrv.findPath(model.getDocument(), emp2));
    }
    
    /**
     * Test of delete method, of class xml.nodes.XMLTreeGenerator.
     */
    public void testDelete() {
        Document original = model.getDocument();
        um.setLimit(10);
        model.addUndoableEditListener(um);
        // Expected model
        // Document
        //   Element -- company
        //      Element -- employee
        //        Attribute -- ssn xx-xx-xxxx
        //        Attribute -- id
        //        Attribute -- phone
        //        Text -- Vidhya
        //      End Element
        //   End Element
        
        // first get text element
        Node company = (Node)model.getDocument().getChildNodes().item(0);
        Node employee = (Node)company.getChildNodes().item(1);
        Text txt = (Text)employee.getChildNodes().item(0);
        
        TestListener tl = new TestListener();
        model.addPropertyChangeListener(tl);
        model.delete(txt);
        
        assertTrue("only one event should be fired " + tl.getEventsFired(), tl.getEventsFired()==1);
        assertEquals("event should be modified", model.PROP_DELETED, tl.getLastEventName());
        
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        assertNull("txt should no longer be in the tree",
                pfrv.findPath(model.getDocument(), txt));
        
        // TODO verify some of the nodes which should not have been cloned
        
        // verify undo / redo
        assertTrue("undo manager should have one event", um.canUndo());
        assertFalse("undo manager should not allow redo", um.canRedo());
        
        Document newD = model.getDocument();
        um.undo();
        assertSame("model not original tree", model.getDocument(), original);
        um.redo();
        assertSame("model not new tree", model.getDocument(), newD);
    }
    
    /**
     * Test of modify method, of class xml.nodes.XMLTreeGenerator.
     */
    public void testModify() throws Exception {
        Document original = model.getDocument();
        um.setLimit(10);
        model.addUndoableEditListener(um);
        // Expected model
        // Document
        //   Element -- company
        //      Element -- employee
        //        Attribute -- ssn xx-xx-xxxx
        //        Attribute -- id
        //        Attribute -- phone
        //        Text -- Vidhya
        //      End Element
        //   End Element
        
        // first get employee element
        Node company = (Node)model.getDocument().getChildNodes().item(0);
        Element employee = (Element)company.getChildNodes().item(1);
        Element employee2 = (Element)employee.cloneNode(true);
        String attrName = "sss";  
        String attrValue0 = employee.getAttribute(attrName);
        String attrValue = "111-11-2222";
        employee2.setAttribute(attrName, attrValue);
        TestListener tl = new TestListener();
        
        model.addPropertyChangeListener(tl);
        model.modify(employee,employee2);
        
        assertTrue("only one event should be fired", tl.getEventsFired()==1);
        assertEquals("event should be modified", model.PROP_MODIFIED, tl.getLastEventName());
        
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        assertNotSame("original company should not be in tree",
                company, pfrv.findPath(model.getDocument(), company).get(0));
        assertNull("original employee should not be tree",
                pfrv.findPath(model.getDocument(), employee));
        
        // now verify that the new employee is what we set
        company = (Node)model.getDocument().getChildNodes().item(0);
        employee = (Element)company.getChildNodes().item(1);
        assertEquals("expected name was not set", employee.getAttribute(attrName), attrValue);
        
        // verify undo / redo
        assertTrue("undo manager should have one event", um.canUndo());
        assertFalse("undo manager should not allow redo", um.canRedo());
        
        Document newD = model.getDocument();
        um.undo();
        assertSame("model not original tree", model.getDocument(), original);
        company = (Node)model.getDocument().getChildNodes().item(0);
        employee = (Element)company.getChildNodes().item(1);
        assertEquals("expected name was not set", attrValue0, employee.getAttribute(attrName));
        
        um.redo();
        assertSame("model not new tree", model.getDocument(), newD);
        company = (Node)model.getDocument().getChildNodes().item(0);
        employee = (Element)company.getChildNodes().item(1);
        assertEquals("expected name was not set", attrValue, employee.getAttribute(attrName));
    }
    
    public void testFlush() throws Exception {
        Document original = model.getDocument();
        um.setLimit(10);
        model.addUndoableEditListener(um);
        String origContent = sd.getText(0,sd.getLength());
        model.flush();
        String flushContent = sd.getText(0,sd.getLength());
        assertEquals("expected same content after flush", origContent, flushContent);
        
        Document oldDoc = model.getDocument();
        assertSame("Models before and after flush are same ", original, oldDoc);
        
        //Force sync to make sure the new model is the same as the current one
        model.sync();
        
        Document newDoc = model.getDocument();
        assertSame("Models before and after flush/sync are same ", oldDoc, newDoc);
        //TODO should have a good way of testing old and new models.
    }
    
    public void testSyncAndNamespace() throws Exception {
        javax.swing.text.Document swdoc = Util.getResourceAsDocument("TestSyncNamespace.wsdl");
        XDMModel m = Util.loadXDMModel(swdoc);
        Element root = (Element) m.getCurrentDocument().getDocumentElement();
        NodeList nl = root.getChildNodes();
        Element messageE = null;
        for (int i=0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element e = (Element) nl.item(i);
                if (e.getLocalName().equals("message")) {
                    messageE = e;
                }
            }
        }
        assertNotNull(messageE);
        assertEquals("http://schemas.xmlsoap.org/wsdl/" , messageE.getNamespaceURI());
        
        Util.setDocumentContentTo(swdoc, "TestSyncNamespace_1.wsdl");
        m.sync();
        
        nl = messageE.getChildNodes();
        Element partE = null;
        for (int i=0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element e = (Element) nl.item(i);
                if (e.getLocalName().equals("part")) {
                    partE = e;
                }
            }
        }
        assertNotNull(partE);
        assertEquals("http://schemas.xmlsoap.org/wsdl/" , messageE.getNamespaceURI());
        assertEquals("http://schemas.xmlsoap.org/wsdl/" , partE.getNamespaceURI());
    }

    public void testSyncToEmptyRoot() throws Exception {
        javax.swing.text.Document swdoc = Util.getResourceAsDocument("resources/test1.xml");
        XDMModel m = Util.loadXDMModel(swdoc);
        Document old = m.getCurrentDocument();
        Element root = (Element) m.getCurrentDocument().getDocumentElement();
        assertEquals(7, root.getChildNodes().getLength());
        Util.setDocumentContentTo(swdoc, "resources/Empty.xml");
        m.prepareSync();
        m.sync();
        assertNotSame(m.getCurrentDocument(), old);
        root = (Element) m.getCurrentDocument().getDocumentElement();
        assertEquals(1, root.getChildNodes().getLength());
    }
    
    static class TestListener implements PropertyChangeListener {
        private String eventName;
        private int count = 0;
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            eventName = evt.getPropertyName();
            count++;
        }
        
        public int getEventsFired() {
            return count;
        }
        
        public String getLastEventName() {
            return eventName;
        }
        
        public void resetFiredEvents() {
            count = 0;
        }
    }
    
    public void testXDMModelSize() throws Exception {
        System.out.println("XDM Mem usage");
        MemoryUsage usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long mem0 = usage.getUsed();
        javax.swing.text.Document swdoc = Util.getResourceAsDocument("resources/fields.xsd");
        usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();                                                              
        long mem1 = usage.getUsed();
        
        long memuse = mem1-mem0;
        
        System.out.println("Document creation = " + memuse + " bytes");

        Lookup lookup = Lookups.singleton(swdoc);
        usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long mem2 = usage.getUsed();
        ModelSource ms = new ModelSource(lookup, true);
        usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long mem3 = usage.getUsed();
        memuse = mem3-mem2;
        
        System.out.println("Model source creation = " + memuse + " bytes"); 

        XDMModel m = new XDMModel(ms);
        m.sync();
        usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long mem4 = usage.getUsed();
        memuse = mem4-mem3;
        System.out.println("XDM creation = " + memuse + " bytes");
        //System.out.println("Time taken to create XDM model: " + (endTime - startTime));
        
    }
        
      
    protected void setUp() throws Exception {
        um = new UndoManager();
        sd = Util.getResourceAsDocument("test.xml");
	Lookup lookup = Lookups.singleton(sd);
	ModelSource ms = new ModelSource(lookup, true);
        model = new XDMModel(ms);
        model.sync();
    }
    
    private javax.swing.text.Document sd;
    private XDMModel model;
    private UndoManager um;
}
