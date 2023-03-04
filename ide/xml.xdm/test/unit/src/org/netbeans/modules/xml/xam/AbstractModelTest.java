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

package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.text.Document;
import junit.framework.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.undo.UndoManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.xam.ComponentEvent.EventType;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.TestComponent3.A;
import org.netbeans.modules.xml.xam.TestComponent3.B;
import org.netbeans.modules.xml.xam.TestComponent3.C;
import org.netbeans.modules.xml.xam.TestComponent3.D;
import org.netbeans.modules.xml.xam.TestComponent3.E;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.diff.Change;
import org.netbeans.modules.xml.xdm.diff.Difference;
import org.netbeans.modules.xml.xdm.nodes.Element;

/**
 *
 * @author Nam Nguyen
 */
public class AbstractModelTest extends NbTestCase {
    PropertyListener plistener;
    TestComponentListener listener;
    TestModel3 mModel;
    Document mDoc;

    static class PropertyListener implements PropertyChangeListener {
        List<PropertyChangeEvent> events  = new ArrayList<PropertyChangeEvent>();

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            events.add(evt);
        }
        
        public void assertEvent(String propertyName, Object old, Object now) {
            assertEvent(propertyName, null, old, now);
        }
        
        public void assertEvent(String propertyName, Object source, Object old, Object now) {
            for (PropertyChangeEvent e : events) {
                if (propertyName.equals(e.getPropertyName())) {
                    if (source != null && source != e.getSource()) {
                        continue;
                    }
                    if (old != null && ! old.equals(e.getOldValue()) ||
                        old == null && e.getOldValue() != null) {
                        continue;
                    }
                    if (now != null && ! now.equals(e.getNewValue()) ||
                        now == null && e.getNewValue() != null) {
                        continue;
                    }
                    return; //matched
                }
            }
            assertTrue("Expect property change event on "+propertyName+" with "+old+" and "+now, false);
        }
        
        public void assertNoEvent(String propertyName) {
            for (PropertyChangeEvent e : events) {
                if (propertyName.equals(e.getPropertyName())) {
                    assertTrue("Got unexpected event "+propertyName, false);
                }
            }
        }
        
        public PropertyChangeEvent getEvent(String propertyName, Object source) {
            for (PropertyChangeEvent e : events) {
                if (propertyName.equals(e.getPropertyName()) && source == e.getSource()) {
                    return e;
                }
            }
            return null;
        }
    }
    
    static class TestComponentListener implements ComponentListener {
        List<ComponentEvent> accu = new ArrayList<ComponentEvent>();
        @Override
        public void valueChanged(ComponentEvent evt) {
            accu.add(evt);
        }
        @Override
        public void childrenAdded(ComponentEvent evt) {
            accu.add(evt);
        }
        @Override
        public void childrenDeleted(ComponentEvent evt) {
            accu.add(evt);
        }
        public void reset() { accu.clear(); }
        public int getEventCount() { return accu.size(); }
        public List<ComponentEvent> getEvents() { return accu; }
    
        private void assertEvent(ComponentEvent.EventType type, DocumentComponent source) {
            for (ComponentEvent e : accu) {
                if (e.getEventType().equals(type) &&
                    e.getSource() == source) {
                    return;
                }
            }
            assertTrue("Expect component change event " + type +" on source " + source +
                    ". Instead received: " + accu, false);
        }
    }    
    
    public AbstractModelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        listener = new TestComponentListener();
        plistener = new PropertyListener();
    }
    
    private void defaultSetup() throws Exception {
        mDoc = Util.getResourceAsDocument("resources/test1.xml");
        mModel = Util.loadModel(mDoc);
        mModel.addComponentListener(listener);
        mModel.addPropertyChangeListener(plistener);
    }
    
    @Override
    protected void tearDown() throws Exception {
        if (mModel != null) {
            mModel.removePropertyChangeListener(plistener);
            mModel.removeComponentListener(listener);
        }
    }

    public static Test suite() {
        return new TestSuite(AbstractModelTest.class);
    }
    
    public void testTransactionOnComponentListener() throws Exception {
        defaultSetup();
        assertEquals("testComponentListener.ok", State.VALID, mModel.getState());
        A a1 = mModel.getRootComponent().getChild(A.class);
        
        try {
            a1.setValue("testComponentListener.a1");
            assertFalse("Mutate without transaction, should have thrown IllegalStateException", true);
        } catch(IllegalStateException e) {
            //OK
        }
        
        mModel.startTransaction();
        a1.setValue("testComponentListener.a1"); // #1
        B b2 = new B(mModel, 2);
        mModel.getRootComponent().addAfter(b2.getName(), b2, TestComponent3._A); // #2
        C c1 = mModel.getRootComponent().getChild(C.class);
        mModel.getRootComponent().removeChild("testComponentListener.remove.c1", c1); // #3
        assertEquals("testComponentListener.noEventBeforeCommit", 0, listener.getEventCount());
        mModel.endTransaction();

        assertEquals(3, listener.getEventCount());
        TestComponent3 root = mModel.getRootComponent();
        listener.assertEvent(EventType.VALUE_CHANGED, a1);
        listener.assertEvent(EventType.CHILD_ADDED, root);
        listener.assertEvent(EventType.CHILD_REMOVED, root);
    }
    
    public void testStateTransition() throws Exception {
        defaultSetup();
        assertEquals("testState.invalid", State.VALID, mModel.getState());

        Util.setDocumentContentTo(mDoc, "resources/Bad.xml");
        try {
            mModel.sync();
            assertFalse("not getting expected ioexception", true);
        } catch (IOException io) {
            assertEquals("Expected state not well-formed", State.NOT_WELL_FORMED, mModel.getState());
            plistener.assertEvent(Model.STATE_PROPERTY, Model.State.VALID, Model.State.NOT_WELL_FORMED);
        }
        
        Util.setDocumentContentTo(mDoc, "resources/test1.xml");
        mModel.sync();
        assertEquals("testState.valid", State.VALID, mModel.getState());
        plistener.assertEvent(Model.STATE_PROPERTY, Model.State.NOT_WELL_FORMED, Model.State.VALID);
    }
    
    public void testSyncRemoveAttribute() throws Exception {
        defaultSetup();
        UndoManager um = new UndoManager();
        mModel.addUndoableEditListener(um);
        
        A a1 = mModel.getRootComponent().getChild(TestComponent3.A.class);
        assertNull("setup", a1.getValue());
        
        mModel.startTransaction();
        String testValue = "edit #1: testRemoveAttribute";
        a1.setValue(testValue);
        mModel.endTransaction();
        assertEquals(testValue, a1.getValue());

        um.undo();
        assertNull("after undo expect no attribute 'value'", a1.getValue());
        
        um.redo();
        assertEquals(testValue, a1.getValue());

        Util.setDocumentContentTo(mDoc, "resources/test1.xml");
        mModel.sync();
        assertNull("sync back to original, expect no attribute 'value'", a1.getValue());
        plistener.assertEvent("value", testValue, null);
        listener.assertEvent(ComponentEvent.EventType.VALUE_CHANGED, a1);
        
        um.undo();
        mModel.getAccess().flush(); // after fix for 83963 need flush after undo/redo
        
        assertEquals(testValue, a1.getValue());
        mModel = Util.dumpAndReloadModel(mModel);
        a1 = mModel.getRootComponent().getChild(A.class);
        assertEquals(testValue, a1.getValue());
    }
    
    public void testMultipleMutationUndoRedo() throws Exception {
        mModel = Util.loadModel("resources/Empty.xml");
        UndoManager urListener = new UndoManager();
        mModel.addUndoableEditListener(urListener);
		
        //setup
        mModel.startTransaction();
        B b2 = new B(mModel, 2);
        mModel.getRootComponent().addAfter(b2.getName(), b2, TestComponent3._A);
        String v = "testComponentListener.b2";
        b2.setValue(v);
        mModel.endTransaction();
        
        b2 = mModel.getRootComponent().getChild(B.class);
        assertEquals(v, b2.getAttribute(TestAttribute3.VALUE));
        
        urListener.undo();
        b2 = mModel.getRootComponent().getChild(B.class);
        assertNull(b2);

        urListener.redo();
        b2 = mModel.getRootComponent().getChild(B.class);
        assertEquals(v, b2.getAttribute(TestAttribute3.VALUE));
    }

    public void testUndoRedo() throws Exception {
        defaultSetup();
        UndoManager urListener = new UndoManager();
        mModel.addUndoableEditListener(urListener);
		
        //setup
        mModel.startTransaction();
        A a1 = mModel.getRootComponent().getChild(A.class);
        String v = "testComponentListener.a1";
        a1.setValue(v);
        mModel.endTransaction();
        assertEquals("edit #1: initial set a1 attribute 'value'", v, a1.getAttribute(TestAttribute3.VALUE));

        urListener.undo();
        String val = a1.getAttribute(TestAttribute3.VALUE);
        assertNull("undo edit #1, expect attribute 'value' is null, got "+val, val);
        urListener.redo();
        assertEquals(v, a1.getAttribute(TestAttribute3.VALUE));
        
        mModel.startTransaction();
        B b2 = new B(mModel, 2);
        mModel.getRootComponent().addAfter(b2.getName(), b2, TestComponent3._A);
        b2.setValue(v);
        mModel.endTransaction();
        assertEquals("edit #2: insert b2", 2, mModel.getRootComponent().getChildren(B.class).size());
        
        mModel.startTransaction();
        C c1 = mModel.getRootComponent().getChild(C.class);
        mModel.getRootComponent().removeChild("testComponentListener.remove.c1", c1);
        mModel.endTransaction();
        assertNull("edit #3: remove c1", mModel.getRootComponent().getChild(C.class));
        
        urListener.undo();
        c1 = mModel.getRootComponent().getChild(C.class);
        assertEquals("undo edit #3", 1, c1.getIndex());

        urListener.redo();			
        assertNull("redo edit #3", mModel.getRootComponent().getChild(C.class));
        
        urListener.undo();	
        assertEquals("undo edit #3 after redo", 1, mModel.getRootComponent().getChildren(C.class).size());
        assertNotNull("c should be intact", mModel.getRootComponent().getChild(C.class));
        
        urListener.undo();		
        assertEquals("undo edit #2", 1, mModel.getRootComponent().getChildren(B.class).size());
        assertNotNull("c should be intact", mModel.getRootComponent().getChild(C.class));
        
        urListener.undo();
        a1 = mModel.getRootComponent().getChild(A.class);
        val = a1.getAttribute(TestAttribute3.VALUE);
        assertNull("undo edit #1, expect attribute 'value' is null, got "+val, val);
        assertNotNull("c should be intact", mModel.getRootComponent().getChild(C.class));
        
        urListener.redo();
        assertNotNull("c should be intact", mModel.getRootComponent().getChild(C.class));

        urListener.redo();
        assertEquals("redo edit #1 and #2", 2, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("testUndo.1", 1, mModel.getRootComponent().getChildren(C.class).size());
    }
    
    public void testSyncUndoRedo() throws Exception {
        defaultSetup();
        UndoManager urListener = new UndoManager();
        mModel.addUndoableEditListener(urListener);
        assertEquals("setup: initial", 1, mModel.getRootComponent().getChildren(C.class).size());
        
        Util.setDocumentContentTo(mDoc, "resources/test2.xml");
        mModel.sync();
        assertEquals("setup: sync", 0, mModel.getRootComponent().getChildren(C.class).size());

        urListener.undo();
        assertEquals("undo sync", 1, mModel.getRootComponent().getChildren(C.class).size());

        urListener.redo();
        assertEquals("undo sync", 0, mModel.getRootComponent().getChildren(C.class).size());
    }
    
    public void testSourceEditSyncUndo() throws Exception {
        defaultSetup();
        UndoManager urListener = new UndoManager();
        Document doc = mModel.getBaseDocument();
        mModel.addUndoableEditListener(urListener);
        
        mModel.startTransaction();
        B b2 = new B(mModel, 2);
        mModel.getRootComponent().addAfter(b2.getName(), b2, TestComponent3._A);
        mModel.endTransaction();
        assertEquals("first edit setup", 2, mModel.getRootComponent().getChildren(B.class).size());
        
        // see fix for issue 83963, with this fix we need coordinate edits from
        // on XDM model and on document buffer.  This reduce XDM undo/redo efficiency,
        // but is the best we can have to satisfy fine-grained text edit undo requirements.
        mModel.removeUndoableEditListener(urListener);
        doc.addUndoableEditListener(urListener);
        
        Util.setDocumentContentTo(doc, "resources/test2.xml");
        assertEquals("undo sync", 1, mModel.getRootComponent().getChildren(C.class).size());
        mModel.sync();
        doc.removeUndoableEditListener(urListener);
        
        assertEquals("sync setup", 1, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("sync setup", 0, mModel.getRootComponent().getChildren(C.class).size());
        
        // setDocumentContentTo did delete all, then insert, hence 2 undo's'
        urListener.undo(); urListener.undo(); 
        mModel.sync(); // the above undo's are just on document buffer, needs sync (inefficient).
        assertEquals("undo sync", 1, mModel.getRootComponent().getChildren(C.class).size());
        assertEquals("undo sync", 2, mModel.getRootComponent().getChildren(B.class).size());

        urListener.undo();
        assertEquals("undo first edit before sync", 1, mModel.getRootComponent().getChildren(B.class).size());

        urListener.redo();
        assertEquals("redo first edit", 1, mModel.getRootComponent().getChildren(C.class).size());
        assertEquals("redo first edit", 2, mModel.getRootComponent().getChildren(B.class).size());

        // needs to back track the undo's, still needs sync'
        urListener.redo(); urListener.redo();
        mModel.sync();
        assertEquals("redo to sync", 1, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("redo to sync", 0, mModel.getRootComponent().getChildren(C.class).size());
    }
	
    public void testCopyPasteUndoRedo() throws Exception {
        defaultSetup();
        UndoManager urListener = new UndoManager();
        mModel.addUndoableEditListener(urListener);
        
        mModel.startTransaction();
        B b2 = new B(mModel, 2);
        mModel.getRootComponent().addAfter(b2.getName(), b2, TestComponent3._A);
        mModel.endTransaction();
        assertEquals("first edit setup", 2, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("first edit setup", 1, mModel.getRootComponent().getChildren(C.class).size());
		
        B b2Copy = (B) b2.copy(mModel.getRootComponent());

        mModel.startTransaction();
        mModel.getRootComponent().addAfter(b2Copy.getName(), b2Copy, TestComponent3._A);
        mModel.endTransaction();
        
        assertEquals("paste", 3, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("paste", 1, mModel.getRootComponent().getChildren(C.class).size());
        
        urListener.undo();
        assertEquals("undo paste", 2, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("undo paste", 1, mModel.getRootComponent().getChildren(C.class).size());
		
        urListener.redo();
        assertEquals("redo paste", 3, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("redo paste", 1, mModel.getRootComponent().getChildren(C.class).size());
    }	
	
    public void testCutPasteUndoRedo() throws Exception {
        defaultSetup();
        UndoManager urListener = new UndoManager();
        mModel.addUndoableEditListener(urListener);
        
        mModel.startTransaction();
        B b2 = new B(mModel, 2);
        mModel.getRootComponent().addAfter(b2.getName(), b2, TestComponent3._A);
        mModel.endTransaction();
        assertEquals("first edit setup", 2, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("first edit setup", 1, mModel.getRootComponent().getChildren(C.class).size());
		
        B b2Copy = (B) b2.copy(mModel.getRootComponent());

        mModel.startTransaction();
        mModel.getRootComponent().removeChild(b2.getName(), b2);
        mModel.endTransaction();
        
        assertEquals("cut", 1, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("cut", 1, mModel.getRootComponent().getChildren(C.class).size());
        
        mModel.startTransaction();
        mModel.getRootComponent().addAfter(b2Copy.getName(), b2Copy, TestComponent3._A);
        mModel.endTransaction();
		
        assertEquals("paste", 2, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("paste", 1, mModel.getRootComponent().getChildren(C.class).size());
		
        urListener.undo();
        assertEquals("undo paste", 1, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("undo paste", 1, mModel.getRootComponent().getChildren(C.class).size());
		
        urListener.undo();
        assertEquals("undo cut", 2, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("undo cut", 1, mModel.getRootComponent().getChildren(C.class).size());
		
        urListener.undo();
        assertEquals("undo first sync", 1, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("undo first sync", 1, mModel.getRootComponent().getChildren(C.class).size());
		
        urListener.redo();
        assertEquals("redo first sync", 2, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("redo first sync", 1, mModel.getRootComponent().getChildren(C.class).size());
		
        urListener.redo();
        assertEquals("redo cut", 1, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("redo cut", 1, mModel.getRootComponent().getChildren(C.class).size());
		
        urListener.redo();
        assertEquals("redo paste", 2, mModel.getRootComponent().getChildren(B.class).size());
        assertEquals("redo paste", 1, mModel.getRootComponent().getChildren(C.class).size());
    }	
    
    public void testFindComponentByPosition() throws Exception {
        TestModel3 model = Util.loadModel("resources/forTestFindComponentOnly.xml");
        DocumentComponent c = model.findComponent(142);
        C c11 = (C) c;
        assertEquals(11, c11.getIndex());
    }
    
    public void testStartTransactionAfterModelSyncedIntoUnparseableState() throws Exception {
        defaultSetup();
        Util.setDocumentContentTo(mDoc, "resources/Bad.xml");
        try {
            mModel.sync();
            assertFalse("Did not get expected IOException", true);
        } catch (IOException ex) {
            // OK
        }
        assertTrue(State.NOT_WELL_FORMED == mModel.getState());

        try {
            assertFalse("Did not get expected failure to start", mModel.startTransaction());
        } finally {
            mModel.endTransaction(); // should be OK
        }
    }
    
    // sync with ns change will cause identity change and subsequently component delete/added events.
    public void testNamespaceAttribute() throws Exception {
        mDoc = Util.getResourceAsDocument("resources/test3.xml");
        mModel = Util.loadModel(mDoc);
        mModel.addComponentListener(listener);
        TestComponent3 root = mModel.getRootComponent();
        
        Util.setDocumentContentTo(mDoc, "resources/test3_changedNSonA2.xml");
        mModel.sync();
        
        listener.assertEvent(ComponentEvent.EventType.CHILD_REMOVED, root);
        listener.assertEvent(ComponentEvent.EventType.CHILD_ADDED, root);
    }

    // Dual root namespaces test model
    public class TestModel2 extends TestModel3 {
        public static final String NS_URI = "http://www.test.com/TestModel2";

        public TestModel2(Document doc) {
            super(doc);
        }

        @Override
        public TestComponent3 createRootComponent(org.w3c.dom.Element root) {
            if ((NS_URI.equals(root.getNamespaceURI()) ||
                TestComponent3.NS_URI.equals(root.getNamespaceURI())) &&
                "test".equals(root.getLocalName())) {
                      testRoot = new TestComponent3(this, root);
            } else {
                testRoot = null;
            }
            return testRoot;
        }
    }

    // sync with ns change will cause identity change and subsequently component delete/added events.
    public void testRootNSChangeOK() throws Exception {
        Document doc = Util.getResourceAsDocument("resources/test1.xml");
        assert doc != null;
        TestModel2 model = new TestModel2(doc);
        model.sync();
        model.addComponentListener(listener);
        TestComponent3 root = model.getRootComponent();
        assertEquals(TestComponent3.NS_URI, model.getRootComponent().getNamespaceURI());
        
        Util.setDocumentContentTo(doc, "resources/test1_rootnschange.xml");
        model.sync();
        assertEquals(Model.State.VALID, model.getState());
        assertTrue(root != model.getRootComponent());
        assertEquals(TestModel2.NS_URI, model.getRootComponent().getNamespaceURI());
    }
	
    public void testRootNSChangeException() throws Exception {
        defaultSetup();
        
        Util.setDocumentContentTo(mDoc, "resources/test1_rootnschange.xml");
        try {
            mModel.sync();
            assertTrue("Should have thrown IOException", false);
        } catch(IOException ioe) {
            //OK
        }
        assertEquals(Model.State.NOT_WELL_FORMED, mModel.getState());
    }
	
    // sync with some non-ns attribute change in root element
    public void testRootChange() throws Exception {
        defaultSetup();
        TestComponent3 root = mModel.getRootComponent();
        Util.setDocumentContentTo(mDoc, "resources/test1_rootchange.xml");
        mModel.sync();
        
        assertEquals("root is same", root, mModel.getRootComponent());
    }	
	
    public void testRootDeleted() throws Exception {
        Document doc = Util.getResourceAsDocument("resources/test1.xml");
        assert doc != null;
        TestModel2 model = new TestModel2(doc);
        model.sync();

        Util.setDocumentContentTo(doc, "resources/test1_rootdeleted.xml");
        model.sync();
        assertEquals(Model.State.NOT_WELL_FORMED, model.getState());
        assertNotNull(model.getRootComponent());

        Util.setDocumentContentTo(doc, "resources/test1.xml");
        model.sync();
        assertEquals(Model.State.VALID, model.getState());
        assertNotNull(model.getRootComponent());
    }

    public void testPrettyPrint() throws Exception {
        defaultSetup();
        assertEquals("testPrettyPrint.ok", State.VALID, mModel.getState());
		//System.out.println("doc: "+model.getBaseDocument().getText(0, model.getBaseDocument().getLength()));		
		mModel.startTransaction();
        assertEquals("testPrettyPrint", 7, mModel.getRootComponent().getPeer().getChildNodes().getLength());
        A a1 = mModel.getRootComponent().getChild(A.class);
        a1.setValue("testPrettyPrint.a1");
        B b2 = new B(mModel, 2);
        mModel.getRootComponent().addAfter(b2.getName(), b2, TestComponent3._A);
		assertEquals("testPrettyPrint", 9, mModel.getRootComponent().getPeer().getChildNodes().getLength());
        B b3 = new B(mModel, 3);
        mModel.getRootComponent().addAfter(b3.getName(), b3, TestComponent3._B);
		assertEquals("testPrettyPrint", 11, mModel.getRootComponent().getPeer().getChildNodes().getLength());
        mModel.endTransaction();
		//System.out.println("doc after pretty: "+model.getBaseDocument().getText(0, model.getBaseDocument().getLength()));
    }	
	
    public void testUndoPrettyPrint() throws Exception {
        defaultSetup();
        assertEquals("testUndoPrettyPrint.ok", State.VALID, mModel.getState());
		//System.out.println("doc: "+model.getBaseDocument().getText(0, model.getBaseDocument().getLength()));		
		mModel.startTransaction();
        assertEquals("testUndoPrettyPrint", 7, mModel.getRootComponent().getPeer().getChildNodes().getLength());
        A a1 = mModel.getRootComponent().getChild(A.class);
        a1.setValue("testUndoPrettyPrint.a1");
        B b2 = new B(mModel, 2);
        mModel.getRootComponent().addAfter(b2.getName(), b2, TestComponent3._A);
		assertEquals("testUndoPrettyPrint", 9, mModel.getRootComponent().getPeer().getChildNodes().getLength());
        B b3 = new B(mModel, 3);
        mModel.getRootComponent().addAfter(b3.getName(), b3, TestComponent3._B);
		assertEquals("testUndoPrettyPrint", 11, mModel.getRootComponent().getPeer().getChildNodes().getLength());
        mModel.endTransaction();
		//System.out.println("doc after pretty: "+model.getBaseDocument().getText(0, model.getBaseDocument().getLength()));
		
		mModel.startTransaction();
        assertEquals("testUndoPrettyPrint", 11, mModel.getRootComponent().getPeer().getChildNodes().getLength());
        List<B> bList = mModel.getRootComponent().getChildren(B.class);
		b2 = bList.get(1);
        mModel.getRootComponent().removeChild(b2.getName(), b2);
		assertEquals("testUndoPrettyPrint", 9, mModel.getRootComponent().getPeer().getChildNodes().getLength());
        b3 = bList.get(2);
        mModel.getRootComponent().removeChild(b3.getName(), b3);
		assertEquals("testUndoPrettyPrint", 7, mModel.getRootComponent().getPeer().getChildNodes().getLength());
        mModel.endTransaction();
		//System.out.println("doc after undopretty: "+model.getBaseDocument().getText(0, model.getBaseDocument().getLength()));		
     }
    
    public void testUndoRedoWithIdentity() throws Exception {
        mModel = Util.loadModel("resources/test1_name.xml");
        UndoManager ur = new UndoManager();
        mModel.addUndoableEditListener(ur);

        E e1 = mModel.getRootComponent().getChild(E.class);
        assertNull(e1.getValue());
        
        mModel.startTransaction();
        String v = "new test value";
        e1.setValue(v);
        mModel.endTransaction();
        assertEquals(v, e1.getValue());

        ur.undo();
        assertNull("expect null, get "+e1.getValue(), e1.getValue());
        
        ur.redo();
        assertEquals(v, e1.getValue());
    }

    public void testUndoRedoWithoutIdentity() throws Exception {
        mModel = Util.loadModel("resources/test1_noname.xml");
        UndoManager ur = new UndoManager();
        mModel.addUndoableEditListener(ur);

        E e1 = mModel.getRootComponent().getChild(E.class);
        assertNull(e1.getValue());
        
        mModel.startTransaction();
        String v = "new test value";
        e1.setValue(v);
        mModel.endTransaction();
        assertEquals(v, e1.getValue());

        ur.undo();
        assertNull("expect null, get "+e1.getValue(), e1.getValue());
        
        ur.redo();
        assertEquals(v, e1.getValue());
    }

    interface FaultInjector {
        void injectFaultAndCheck(Object actor) throws Exception;
    }
    
    private void setupSyncFault(FaultInjector injector) throws Exception {
        defaultSetup();
        C c = mModel.getRootComponent().getChild(C.class);
        assertEquals(1, c.getIndex());
        
        try {
            injector.injectFaultAndCheck(null);
            assertFalse("Did not see NPE", true);
        } catch(NullPointerException ex) {
            plistener.assertEvent(Model.STATE_PROPERTY, Model.State.VALID, Model.State.NOT_SYNCED);
            plistener.assertEvent(Model.STATE_PROPERTY, Model.State.NOT_SYNCED, Model.State.VALID);
            c = mModel.getRootComponent().getChild(C.class);
            assertEquals("ioexception", Model.State.VALID, mModel.getState());
            assertNull("insynced with after tree", c);
        }
    }
    
    public void testSyncWithFaultInFindComponent() throws Exception {
        setupSyncFault(new FaultInjector() {
            @Override
            public void injectFaultAndCheck(Object actor) throws Exception {
                mModel.injectFaultInFindComponent();
                Util.setDocumentContentTo(mDoc, "resources/test2.xml");
                mModel.sync();
            }
        });
    }

    public void testSyncWithFaultInSyncUpdater() throws Exception {
        setupSyncFault(new FaultInjector() {
            @Override
            public void injectFaultAndCheck(Object actor) throws Exception {
                mModel.injectFaultInSyncUpdater();
                Util.setDocumentContentTo(mDoc, "resources/test2.xml");
                mModel.sync();
            }
        });
    }

    public void testSyncWithFaultInEventFiring() throws Exception {
        setupSyncFault(new FaultInjector() {
            @Override
            public void injectFaultAndCheck(Object actor) throws Exception {
                mModel.injectFaultInEventFiring();
                Util.setDocumentContentTo(mDoc, "resources/test2.xml");
                mModel.sync();
            }
        });
    }

    private void setupForUndoRedoFault(FaultInjector i, boolean redo) throws Exception {
        defaultSetup();
        final UndoManager ur = new UndoManager();
        mModel.addUndoableEditListener(ur);
        A a = mModel.getRootComponent().getChild(A.class);
        
        mModel.startTransaction();
        String v = "new test value";
        a.setValue(v);
        C c = mModel.getRootComponent().getChild(C.class);
        mModel.removeChildComponent(c);
        mModel.endTransaction();
        assertEquals("setup success", v, a.getValue());
        try {
            i.injectFaultAndCheck(ur);
            assertFalse("Did not see NullPointerException", true);
        } catch (NullPointerException cue) {
            plistener.assertEvent(Model.STATE_PROPERTY, Model.State.VALID, Model.State.NOT_SYNCED);
            plistener.assertEvent(Model.STATE_PROPERTY, Model.State.NOT_SYNCED, Model.State.VALID);
            a = mModel.getRootComponent().getChild(A.class);
            if (redo) {
                assertEquals("no cannotredoexception, redone", v, a.getValue());
                assertNull("still redone", mModel.getRootComponent().getChild(C.class));
            } else {
                assertNull("no cannotundoexception, undone", a.getValue());
                assertNotNull("still undone", mModel.getRootComponent().getChild(C.class));
            }
        }
        //TODO findout what if calling redo here
    }
    
    public void testUndoWithFaultInFindComponent() throws Exception {
        setupForUndoRedoFault(new FaultInjector() {
            @Override
            public void injectFaultAndCheck(Object actor) throws Exception { 
                mModel.injectFaultInFindComponent();
                ((UndoManager)actor).undo();
            }
        }, false);
    }

    public void testUndoWithFaultInSyncUpdater() throws Exception {
        setupForUndoRedoFault(new FaultInjector() {
            @Override
            public void injectFaultAndCheck(Object actor) throws Exception { 
                mModel.injectFaultInSyncUpdater();
                ((UndoManager)actor).undo();
            }
        }, false);
    }

    public void testUndoWithFaultInEventFiring() throws Exception {
        setupForUndoRedoFault(new FaultInjector() {
            @Override
            public void injectFaultAndCheck(Object actor) throws Exception { 
                mModel.injectFaultInEventFiring();
                ((UndoManager)actor).undo();
            }
        }, false);
    }

    public void testRedoWithFaultInFindComponent() throws Exception {
        setupForUndoRedoFault(new FaultInjector() {
            @Override
            public void injectFaultAndCheck(Object actor) throws Exception { 
                ((UndoManager)actor).undo();
                mModel.injectFaultInFindComponent();
                ((UndoManager)actor).redo();
            }
        }, true);
    }

    public void testRedoWithFaultInSyncUpdater() throws Exception {
        setupForUndoRedoFault(new FaultInjector() {
            @Override
            public void injectFaultAndCheck(Object actor) throws Exception { 
                ((UndoManager)actor).undo();
                mModel.injectFaultInSyncUpdater();
                ((UndoManager)actor).redo();
            }
        }, true);
    }

    public void testRedoWithFaultInEventFiring() throws Exception {
        setupForUndoRedoFault(new FaultInjector() {
            @Override
            public void injectFaultAndCheck(Object actor) throws Exception { 
                ((UndoManager)actor).undo();
                mModel.injectFaultInEventFiring();
                ((UndoManager)actor).redo();
            }
        }, true);
    }
    
    public void testSyncWithPositionChangeAndAttributeChange() throws Exception {
        List<Difference> diffs = Util.diff("resources/test1.xml", "resources/test1_1.xml");
        
        assertEquals(diffs.toString(), 3, diffs.size());
        assertTrue(diffs.toString(), ((Change)diffs.get(2)).isAttributeChanged());

        defaultSetup();
        Util.setDocumentContentTo(mDoc, "resources/test1_1.xml");
        mModel.sync();
        assertEquals("diffs="+diffs, "foo", mModel.getRootComponent().getChild(B.class).getValue());
    }

    public void testSyncWithReorder() throws Exception {
        mDoc = Util.getResourceAsDocument("resources/testreorder.xml");
        mModel = Util.loadModel(mDoc);
        mModel.addComponentListener(listener);
        mModel.addPropertyChangeListener(plistener);
        
        Util.setDocumentContentTo(mDoc, "resources/testreorder_1.xml");
        //model.sync();
        Util.setDocumentContentTo(mDoc, "resources/testreorder_2.xml");
        //model.sync();
        Util.setDocumentContentTo(mDoc, "resources/testreorder_3.xml");
        mModel.sync();
        assertEquals("Expect a2 is now first", 2, mModel.getRootComponent().getChildren(A.class).get(0).getIndex());
    }
    
    public void testSyncWithoutProlog() throws Exception {
        List<Difference> diffs = Util.diff("resources/test1.xml", "resources/noprolog.xml");
        assertEquals("should also include change in prolog", 1, diffs.size());
        
        defaultSetup();
        Util.setDocumentContentTo(mDoc, "resources/noprolog.xml");
        mModel.sync();
        org.netbeans.modules.xml.xdm.nodes.Document doc = 
            (org.netbeans.modules.xml.xdm.nodes.Document) mModel.getDocument();
        assertEquals("expect resulting document has no prolog", 0, doc.getTokens().size());
    }    

    public void testSyncWithChangedProlog() throws Exception {
        List<Difference> diffs = Util.diff("resources/test1.xml", "resources/test1_changedProlog.xml");
        assertEquals("should also include change in prolog", 1, diffs.size());

        defaultSetup();
        org.netbeans.modules.xml.xdm.nodes.Document oldDoc = 
            (org.netbeans.modules.xml.xdm.nodes.Document) mModel.getDocument();
        
        Util.setDocumentContentTo(mDoc, "resources/test1_changedProlog.xml");
        mModel.sync();
        
        org.netbeans.modules.xml.xdm.nodes.Document doc = 
            (org.netbeans.modules.xml.xdm.nodes.Document) mModel.getDocument();
        assertEquals("expect resulting document has no prolog", 6, doc.getTokens().size());
        String tokens = doc.getTokens().toString();
        assertFalse("prolog should changes: "+tokens, oldDoc.getTokens().toString().equals(tokens));
    }    

    public void testSyncWithChangedPrologAndOthers() throws Exception {
        List<Difference> diffs = Util.diff("resources/test1.xml", "resources/test1_changedProlog2.xml");
        assertEquals("should also include change in prolog "+diffs, 9, diffs.size());

        defaultSetup();
        org.netbeans.modules.xml.xdm.nodes.Document oldDoc = 
            (org.netbeans.modules.xml.xdm.nodes.Document) mModel.getDocument();
        
        Util.setDocumentContentTo(mDoc, "resources/test1_changedProlog2.xml");
        mModel.sync();
        
        org.netbeans.modules.xml.xdm.nodes.Document doc = 
            (org.netbeans.modules.xml.xdm.nodes.Document) mModel.getDocument();
        assertEquals("expect resulting document has no prolog", 6, doc.getTokens().size());
        String tokens = doc.getTokens().toString();
        assertFalse("prolog should changes: "+tokens, oldDoc.getTokens().toString().equals(tokens));
        javax.xml.namespace.QName attr = new javax.xml.namespace.QName("targetNamespace");
        assertEquals("foo", mModel.getRootComponent().getAnyAttribute(attr));
        assertEquals("b1 should be replaced by b2", 2, mModel.getRootComponent().getChild(B.class).getIndex());
        assertNull("c1 should be deleted", mModel.getRootComponent().getChild(C.class));
    }    


    
    private static class Handler implements ComponentListener {

        @Override
        public void valueChanged(ComponentEvent evt) {
        }

        @Override
        public void childrenDeleted(ComponentEvent evt) {
        }

        @Override
        public void childrenAdded(ComponentEvent evt) {
            if (evt.getSource().getClass().isAssignableFrom(TestComponent3.class)) {
                D myD = ((TestComponent3)evt.getSource()).getChild(D.class);
                myD.appendChild("test", new B(myD.getModel(), 2));
            }
        }
    }
    
    public void testMutationInComponentEventHandler() throws Exception {
        defaultSetup();
        mModel.addComponentListener(new Handler());
        mModel.startTransaction();
        mModel.getRootComponent().appendChild("test", new D(mModel, 2));
        mModel.endTransaction();
        mModel = Util.dumpAndReloadModel(mModel);
        D d = mModel.getRootComponent().getChild(D.class);
        assertNotNull(d.getChild(B.class));
    }
    
    public void testUndoRedoOnMutationFromEvent() throws Exception {
        defaultSetup();
        mModel.addComponentListener(new Handler());
        UndoManager um = new UndoManager();
        mModel.addUndoableEditListener(um);

        mModel.startTransaction();
        mModel.getRootComponent().appendChild("test", new D(mModel, 2));
        mModel.endTransaction();

        um.undo();
        D d = mModel.getRootComponent().getChild(D.class);
        assertNull(d);
        um.redo();
        d = mModel.getRootComponent().getChild(D.class);
        assertNotNull(d.getChild(B.class));
    }
    
    public void testXmlContentPropertyChangeEventRemove() throws Exception {
        setUp();
        mDoc = Util.getResourceAsDocument("resources/testXmlContentEvent.xml");
        mModel = Util.loadModel(mDoc);
        mModel.addComponentListener(listener);
        mModel.addPropertyChangeListener(plistener);

        A a = mModel.getRootComponent().getChild(A.class);
        assertEquals(0, a.getChildren().size());
        
        Util.setDocumentContentTo(mDoc, "resources/testXmlContentEvent_1.xml");
        mModel.sync();
        listener.assertEvent(EventType.VALUE_CHANGED, a);
        PropertyChangeEvent event = plistener.getEvent("nondomain", a);
        List<Element> now = (List<Element>) event.getNewValue();
        List<Element> old = (List<Element>) event.getOldValue();
        assertEquals(0, now.size());
        assertEquals(1, old.size());
        assertEquals("101", old.get(0).getXmlFragmentText());
        plistener.assertNoEvent(DocumentComponent.TEXT_CONTENT_PROPERTY);
    }

    public void testXmlContentPropertyChangeEventAdd() throws Exception {
        setUp();
        mDoc = Util.getResourceAsDocument("resources/testXmlContentEvent_1.xml");
        mModel = Util.loadModel(mDoc);
        mModel.addComponentListener(listener);
        mModel.addPropertyChangeListener(plistener);

        A a = mModel.getRootComponent().getChild(A.class);
        assertEquals(0, a.getChildren().size());
        
        Util.setDocumentContentTo(mDoc, "resources/testXmlContentEvent.xml");
        mModel.sync();
        listener.assertEvent(EventType.VALUE_CHANGED, a);
        PropertyChangeEvent event = plistener.getEvent("nondomain", a);
        List<Element> now = (List<Element>) event.getNewValue();
        List<Element> old = (List<Element>) event.getOldValue();
        assertEquals(0, old.size());
        assertEquals(1, now.size());
        assertEquals("101", now.get(0).getXmlFragmentText());
        plistener.assertNoEvent(DocumentComponent.TEXT_CONTENT_PROPERTY);
    }

    public void testXmlContentPropertyChangeEventChange() throws Exception {
        setUp();
        mDoc = Util.getResourceAsDocument("resources/testXmlContentEvent.xml");
        mModel = Util.loadModel(mDoc);
        mModel.addComponentListener(listener);
        mModel.addPropertyChangeListener(plistener);

        A a = mModel.getRootComponent().getChild(A.class);
        assertEquals(0, a.getChildren().size());
        
        Util.setDocumentContentTo(mDoc, "resources/testXmlContentEvent_2.xml");
        mModel.sync();
        listener.assertEvent(EventType.VALUE_CHANGED, a);
        PropertyChangeEvent event = plistener.getEvent("nondomain", a);
        List<Element> now = (List<Element>) event.getNewValue();
        List<Element> old = (List<Element>) event.getOldValue();
        assertEquals(1, now.size());
        assertEquals(1, old.size());
        assertEquals("101", old.get(0).getXmlFragmentText());
        assertEquals("1001", now.get(0).getXmlFragmentText());
        plistener.assertEvent(DocumentComponent.TEXT_CONTENT_PROPERTY, a, " <nondomain>101</nondomain>", " <nondomain>1001</nondomain>");
    }
    
    //////////////////////////////////////////////////////////////
    // The following two tests must be reviewed at a later time //
    //////////////////////////////////////////////////////////////
    
    public void testUndoOnMutationFromSyncEvent() throws Exception {
        defaultSetup();
        mModel.addComponentListener(new Handler());
        UndoManager um = new UndoManager();
        mModel.addUndoableEditListener(um);

        Util.setDocumentContentTo(mDoc, "resources/test1_2.xml");
        mModel.sync();
        D d = mModel.getRootComponent().getChild(D.class);
        assertNotNull(d.getChild(B.class));
        um.undo();
        mModel.getAccess().flush(); // after fix for 83963 need manual flush after undo/redo

        assertNull(mModel.getRootComponent().getChild(D.class));
        mModel = Util.dumpAndReloadModel(mModel);
        assertNull(mModel.getRootComponent().getChild(D.class));
    }

    public void testFlushOnMutationFromSyncEvent() throws Exception {
        defaultSetup();
        mModel.addComponentListener(new Handler());
        Util.setDocumentContentTo(mDoc, "resources/test1_2.xml");
        mModel.sync();
        D d = mModel.getRootComponent().getChild(D.class);
        assertNotNull(d.getChild(B.class));
        
        mModel = Util.dumpAndReloadModel(mModel);
        d = mModel.getRootComponent().getChild(D.class);
        assertNotNull(d.getChild(B.class));
    }
}
