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

import junit.framework.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.TestComponent3.A;
import org.netbeans.modules.xml.xam.TestComponent3.Aa;
import org.netbeans.modules.xml.xam.TestComponent3.B;
import org.netbeans.modules.xml.xam.TestComponent3.C;
import org.netbeans.modules.xml.xam.TestComponent3.D;
import org.netbeans.modules.xml.xam.TestComponent3.TestComponentReference;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Token;
import org.netbeans.modules.xml.xdm.nodes.TokenType;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author Nam Nguyen
 */
public class AbstractComponentTest extends TestCase {
    
    TestModel3 mModel;
    TestComponent3 p;
    A mA1;
    B mB1;
    C mC1;
    Listener listener;
    TestComponentListener clistener;
    
    public AbstractComponentTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
    }
    
    protected void defaultSetup() throws Exception {
        mModel = Util.loadModel("resources/Empty.xml");
	mModel.startTransaction();
        p = TestComponent3.class.cast(mModel.getRootComponent());
        assertEquals("setup", "test-1", p.getName());
        
        mA1 = new A(mModel, 1);
        mB1 = new B(mModel, 1);
        mC1 = new C(mModel, 1);
        p.appendChild("setup", mA1);
        p.appendChild("setup", mB1);
        p.appendChild("setup", mC1);
        mModel.endTransaction();
        assertEquals("setup.children", "[a1, b1, c1]", p.getChildren().toString());
        
        listener = new Listener();
        mModel.addPropertyChangeListener(listener);
        clistener = new TestComponentListener();
        mModel.addComponentListener(clistener);
    }
    

    @Override
    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractComponentTest.class);
        
        return suite;
    }

    private class Listener implements PropertyChangeListener {
        private String event;
        private Object old;
        private Object now;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            assertNotNull(evt);
            if (evt.getPropertyName().equals("setup")) {
                return;
            }
            event = evt.getPropertyName();
            old = evt.getOldValue();
            now = evt.getNewValue();
        }

        public String getEvent() { return event; }
        public Object getOld() { return old; }
        public Object getNow() { return now; }
        public void reset() { event = null; old = null; now = null; }
    }
    
    private void assertEventListener(String name, Object old, Object now) {
        assertEquals(name+".event", name, listener.getEvent());
        assertEquals(name+".old", old, listener.getOld());
        assertEquals(name+".now", now, listener.getNow());
        listener.reset();
    }
    
    class TestComponentListener implements ComponentListener {
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
            assertTrue("Expect component change event" + type +" on source " + source, false);
        }
    }    
    
    public void testInsertAtIndex() throws Exception {
        defaultSetup();
        String propertyName = "testInsertAtIndex";
        TestComponent3 parent = new TestComponent3(mModel, "test", TestComponent3.NS_URI);
        B b0 = new B(mModel, 0);
        B b1 = new B(mModel, 1);
        B b2 = new B(mModel, 2);
        B b3 = new B(mModel, 3);
        parent.insertAtIndex(propertyName, b1, 0, B.class);
        assertEquals("testInsertAtIndex.res", "[b1]", parent.getChildren().toString());
        assertTrue(parent == b1.getParent());

        mModel.startTransaction();
        mModel.getRootComponent().appendChild("test-setup", parent);
        parent.insertAtIndex(propertyName, b2, 1, B.class);
        mModel.endTransaction();
        assertEquals("testInsertAtIndex.res", "[b1, b2]", parent.getChildren().toString());
        assertTrue(parent == b2.getParent());
        
        try {
            parent.insertAtIndex(propertyName, b0, 0, B.class);
            assertFalse("Did not get expected IllegalStateException", true);
        } catch(IllegalStateException ex) {
            // expected
        }

        mModel.startTransaction();
        parent.insertAtIndex(propertyName, b0, 0, B.class);
        mModel.endTransaction();
        assertEquals("testInsertAtIndex.res", "[b0, b1, b2]", parent.getChildren().toString());
        assertTrue(parent == b0.getParent());

        mModel.startTransaction();
        parent.insertAtIndex(propertyName, b3, 3, B.class);
        mModel.endTransaction();
        assertEquals("testInsertAtIndex.res", "[b0, b1, b2, b3]", parent.getChildren().toString());
        assertTrue(parent == b3.getParent());
    }
    
    public void testInsertAtIndexRelative() throws Exception {
        defaultSetup();
        String propertyName = "testInsertAtIndexRelative";
	mModel.startTransaction();
        B b2 = new B(mModel, 2);
        p.insertAtIndex(propertyName, b2, 1, B.class);
	mModel.endTransaction();
        assertEventListener(propertyName, null, b2);
        List<B> res1 = p.getChildren(B.class);
        assertEquals("testInsertAtIndexRelative.res1", "[b1, b2]", res1.toString());
        List<TestComponent3> res2 = p.getChildren();
        assertEquals("testInsertAtIndexRelative.res2", "[a1, b1, b2, c1]", res2.toString());
    }

    // a1 b1 c1 -> a1 b0 b1 c1
    public void testInsertAtIndexRelative0() throws Exception {
        defaultSetup();
        String propertyName = "testInsertAtIndexRelative0";
	mModel.startTransaction();
        B b0 = new B(mModel, 0);
        p.insertAtIndex(propertyName, b0, 0, B.class);
        mModel.endTransaction();
        assertEventListener(propertyName, null, b0);
        assertTrue(p == b0.getParent());
        
        List<B> res1 = p.getChildren(B.class);
        assertEquals("testInsertAtIndexRelative0.res1", "[b0, b1]", res1.toString());
        List<TestComponent3> res2 = p.getChildren();
        assertEquals("testInsertAtIndexRelative0.res2", "[a1, b0, b1, c1]", res2.toString());
    }

    // a1 b1 c1 -> a1 b1 c1 d1
    public void testInsertAtIndexRelative0Empty() throws Exception {
        defaultSetup();
        String propertyName = "testInsertAtIndexRelative0Empty";
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
        p.insertAtIndex(propertyName, d1, 0, D.class);
        mModel.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<D> res1 = p.getChildren(D.class);
        assertEquals("testInsertAtIndexRelative0Empty.res1", "[d1]", res1.toString());
        List<TestComponent3> res2 = p.getChildren();
        assertEquals("testInsertAtIndexRelative0Empty.res2", "[a1, b1, c1, d1]", res2.toString());
        assertTrue(p == d1.getParent());
    }
    
    // a1 b1 c1 -> d1 a1 b1 c1
    public void testAddBeforeA() throws Exception {
        defaultSetup();
        String propertyName = "testAddBeforeA";
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
        p.addBefore(propertyName, d1, TestComponent3._A);
        mModel.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent3> res2 = p.getChildren();
        assertEquals("testAddBeforeA.res2", "[d1, a1, b1, c1]", res2.toString());
    }
    
    // a1 b1 c1 c2 -> a1 b1 d1 c1 c2
    public void testAddBeforeC() throws Exception {
        defaultSetup();
        String propertyName = "testAddBeforeC";
	mModel.startTransaction();
        C c2 = new C(mModel, 2);
        p.insertAtIndex("setup", c2, 3, TestComponent3.class);
        mModel.endTransaction();
        listener.reset();
        
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
        p.addBefore(propertyName, d1, TestComponent3._C);
        mModel.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent3> res = p.getChildren();
        assertEquals("testAddBeforeC.res", "[a1, b1, d1, c1, c2]", res.toString());
    }
    
    // a1 b1 c1 -> a1 d1 b1 c1
    public void testAddBeforeBC() throws Exception {
        defaultSetup();
        String propertyName = "testAddBeforeBC";
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
        p.addBefore(propertyName, d1, TestComponent3._BC);
        assertTrue(p == d1.getParent());
        mModel.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent3> res = p.getChildren();
        assertEquals("testAddBeforeBC.res", "[a1, d1, b1, c1]", res.toString());
    }
    
    // a1 b1 c1 -> d1 a1 b1 c1
    public void testAddBeforeAC() throws Exception {
        defaultSetup();
        String propertyName = "testAddBeforeAC";
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
        p.addBefore(propertyName, d1, TestComponent3._AC);
        mModel.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent3> res = p.getChildren();
        assertEquals("testAddBeforeAC.res", "[d1, a1, b1, c1]", res.toString());
    }
    
    // Out-of-order case
    // a1 b1 c1 -> d1 a1 b1 c1 or IllegalArgumentException
    public void testAddBeforeBAC() throws Exception {
        defaultSetup();
        String propertyName = "testAddBeforeBAC";
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
	
        p.addBefore(propertyName, d1, TestComponent3._BAC);
        mModel.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent3> res = p.getChildren();
        assertEquals("testAddBeforeBAC.res", "[d1, a1, b1, c1]", res.toString());
    }
    
    // a1 b1 c1 -> d1 a1 b1 c1
    public void testAddAfterA() throws Exception {
        defaultSetup();
        String propertyName = "testAddAfterA";
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
        p.addAfter(propertyName, d1, TestComponent3._A);
        mModel.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent3> res2 = p.getChildren();
        assertEquals("testAddAfterA.res2", "[a1, d1, b1, c1]", res2.toString());
    }
    
    // a1 b1 c1 c2 -> a1 b1 d1 c1 c2
    public void testAddAfterC() throws Exception {
        defaultSetup();
        String propertyName = "testAddAfterC";
	mModel.startTransaction();
        C c2 = new C(mModel, 2);
        p.addAfter("setup", c2, TestComponent3._AB);
        mModel.endTransaction();
        listener.reset();
        
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
        p.addAfter(propertyName, d1, TestComponent3._C);
        mModel.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent3> res = p.getChildren();
        assertEquals("testAddAfterC.res", "[a1, b1, c1, c2, d1]", res.toString());
    }
    
    // a1 b1 c1 -> a1 d1 b1 c1
    public void testAddAfterAC() throws Exception {
        defaultSetup();
        String propertyName = "testAddAfterAC";
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
        p.addAfter(propertyName, d1, TestComponent3._AC);
        mModel.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<TestComponent3> res = p.getChildren();
        assertEquals("testAddAfterAC.res", "[a1, b1, c1, d1]", res.toString());
    }
    
    // a1 b1 c1 -> d1 a1 b1 c1
    public void testAddAfterAB() throws Exception {
        defaultSetup();
        String propertyName = "testAddAfterAB";
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
        p.addAfter(propertyName, d1, TestComponent3._AB);
        mModel.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertTrue(p == d1.getParent());
        
        List<TestComponent3> res = p.getChildren();
        assertEquals("testAddAfterAC.res", "[a1, b1, d1, c1]", res.toString());
    }
    
    // Out-of-order case
    // a1 b1 c1 -> d1 a1 b1 c1 or IllegalArgumentException
    public void testAddAfterBAC() throws Exception {
        defaultSetup();
        String propertyName = "testAddAfterBAC";
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
        p.addAfter(propertyName, d1, TestComponent3._BAC);
        mModel.endTransaction();
        assertEventListener(propertyName, null, d1);
        
        List<TestComponent3> res = p.getChildren();
        assertEquals("testAddAfterBAC.res", "[a1, b1, c1, d1]", res.toString());
    }
    
    // a1 b1 c1 -> a1 d1 b1 c1
    public void testSetA() throws Exception {
        defaultSetup();
        String propertyName = "testSetA";
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
        p.setChild(D.class, propertyName, d1, TestComponent3._A);
        mModel.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertEquals("testSetA.res", "[a1, d1, b1, c1]", p.getChildren().toString());
        assertTrue(p == d1.getParent());
    }
    
    // a1 b1 c1 -> a1 b1 c1 d1
    public void testSetBC() throws Exception {
        defaultSetup();
        String propertyName = "testSetBC";
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
        p.setChild(D.class, propertyName, d1, TestComponent3._BC);
        mModel.endTransaction();
        assertEventListener(propertyName, null, d1);
        assertEquals("testSetBC.res", "[a1, b1, c1, d1]", p.getChildren().toString());
        assertTrue(p == d1.getParent());

	mModel.startTransaction();
        D d2 = new D(mModel, 2);
        p.setChild(D.class, propertyName, d2, TestComponent3._BC);
        mModel.endTransaction();
        assertEventListener(propertyName, d1, d2);
        assertEquals("testSetBC.res", "[a1, b1, c1, d2]", p.getChildren().toString());
        assertTrue(p == d2.getParent());
    }
    
    // a1 b1 b2 c1 d1 -> a1 b1 b2 c2 d1
    public void testSetAfterAB() throws Exception {
        defaultSetup();
        String propertyName = "testSetC";
	mModel.startTransaction();
        B b2 = new B(mModel, 2);
        p.addAfter("setup", b2, TestComponent3._A);
        mModel.endTransaction();
        assertEquals("testSetC.res", "[a1, b1, b2, c1]", p.getChildren().toString());
        assertTrue(p == b2.getParent());
	mModel.startTransaction();
        D d1 = new D(mModel, 1);
        p.addAfter("setup", d1, TestComponent3._ABC);
        C c2 = new C(mModel, 2);
        p.setChild(C.class, propertyName, c2, TestComponent3._AB);
        mModel.endTransaction();
        assertEventListener(propertyName, mC1, c2);
        assertEquals("testSetC.res", "[a1, b1, b2, c2, d1]", p.getChildren().toString());
        assertTrue(p == c2.getParent());
    }
    
    // c1 -> a1 b1 c1
    public void testSpecificOrdering() throws Exception {
        // setup
        mModel = Util.loadModel("resources/Empty.xml");
	mModel.startTransaction();
        p = TestComponent3.class.cast(mModel.getRootComponent());
        assertEquals("setup", "test-1", p.getName());
        mC1 = new C(mModel, 1);
        p.appendChild("setup", mC1);
        mModel.endTransaction();
        assertEquals("testSpecificOrdering.setup", "[c1]", p.getChildren().toString());
        
	mModel.startTransaction();
        mA1 = new A(mModel, 1);
        mB1 = new B(mModel, 1);
        p.setChildBefore(A.class, "a", mA1, TestComponent3._BC);
        p.setChildBefore(B.class, "b", mB1, TestComponent3._C);
        mModel.endTransaction();
        assertEquals("testSpecificOrdering.res", "[a1, b1, c1]", p.getChildren().toString());
        assertTrue(p == mA1.getParent());
        assertTrue(p == mB1.getParent());
    }
    
    public void testGetSetAttribute() throws Exception {
        defaultSetup();
        String v = mA1.getAttribute(TestAttribute3.VALUE);
        assertNull("testAttribute.initial.value", v);
        int i = mA1.getIndex();
        assertEquals("testAttribute.initial.index", 1, i);
        
        String v2 = "testSetAttribute.set.value"; 
        int i2 = 20;
	mModel.startTransaction();
        mA1.setValue(v2);
	mModel.endTransaction();
        assertEventListener(TestAttribute3.VALUE.getName(), v, v2);
	mModel.startTransaction();
        mA1.setIndex(i2);
	mModel.endTransaction();
        assertEventListener(TestAttribute3.INDEX.getName(), i, Integer.valueOf(i2));
        
        v = v2; i = i2;
        v2 = "testSetAttribute.set.value.again"; 
        i2 = 21;
	mModel.startTransaction();
        mA1.setValue(v2);
	mModel.endTransaction();
        assertEventListener(TestAttribute3.VALUE.getName(), v, v2);
	mModel.startTransaction();
        mA1.setIndex(i2);
	mModel.endTransaction();
        assertEventListener(TestAttribute3.INDEX.getName(), Integer.valueOf(i), Integer.valueOf(i2));
    }
    
    public void testSetGetChild() throws Exception {
        defaultSetup();
	mModel.startTransaction();
        D myD = new D(mModel, -1);
        p.setChildBefore(D.class, "myD", myD, TestComponent3._BC);
	mModel.endTransaction();
        assertEquals("testSetGetChild.order", "[a1, d-1, b1, c1]", p.getChildren().toString());
        assertEquals("testSetGetChild.equals", myD, p.getChild(D.class));
        assertTrue(p == myD.getParent());
        
	mModel.startTransaction();
        D myD2 = new D(mModel, -2);
        p.setChildBefore(D.class, "myD", myD2, TestComponent3._BC);
	mModel.endTransaction();
        assertEventListener("myD", myD, myD2);
        assertEquals("testSetGetChild2.count", 1, p.getChildren(D.class).size());
        assertEquals("testSetGetChild2.order", "[a1, d-2, b1, c1]", p.getChildren().toString());
        assertEquals("testSetGetChild2.equals", myD2, p.getChild(D.class));
        assertTrue(p == myD2.getParent());
    }
    
    public void testRemoveChild() throws Exception {
        defaultSetup();
        mModel.startTransaction();
        p.removeChild(mB1.getName(), mB1);
        mModel.endTransaction();
        
        assertEventListener(mB1.getName(), mB1, null);
        assertNull("testRemoveChild.gone", p.getChild(B.class));
        assertEquals("testRemoveChild.count", 0, p.getChildren(B.class).size());
        assertEquals("testRemoveChild.count.all", 2, p.getChildren().size());
        assertNull(mB1.getParent());
    }
    
    public void testRemoveAttribute() throws Exception {
        defaultSetup();
        mModel.startTransaction();
        A myA = p.getChild(A.class);
        assertEquals("testRemoveAttribute.init", "1", myA.getAttribute(TestAttribute3.INDEX));
        myA.setAttribute(TestAttribute3.INDEX.getName(), TestAttribute3.INDEX, null);
        assertEquals("testRemoveAttribute.result", -1, myA.getIndex());
        mModel.endTransaction();
        
        assertEventListener(TestAttribute3.INDEX.getName(), Integer.valueOf(1), null);
    }
    
    public void testGetParent() throws Exception {
        defaultSetup();
        for (TestComponent3 tc : p.getChildren()) {
            assertTrue("parent pointer not null", tc.getParent() == p);
        }
        mModel.startTransaction();
        p.removeChild("testGetParent.removeChild", mA1);
        assertNull("removed component should have null parent", mA1.getParent());
        mModel.endTransaction();
        
        mModel = Util.loadModel("resources/test1.xml");
        A a1 = mModel.getRootComponent().getChild(A.class);
        assertTrue("test getParent from loaded doc", a1.getParent() == mModel.getRootComponent());
    }
    
    public void testAnyAttribute() throws Exception {
        defaultSetup();
        mModel.startTransaction();
        A a1 = p.getChild(A.class);
        String ns = "testAnyAttribute";
        String prefix = "any";
        String attrName = "any1";
        QName attr = new QName(ns, attrName, prefix);
        String value = "any attribute test";
        a1.setAnyAttribute(attr, value);
        mModel.endTransaction();
        
        QName noPrefixAttr = new QName(ns, attrName);
        assertEquals(value, a1.getAnyAttribute(noPrefixAttr));
        assertEquals(prefix, a1.getPeer().lookupPrefix(ns));
        
        mModel = Util.dumpAndReloadModel(mModel);
        mModel.addPropertyChangeListener(listener);
        mModel.addComponentListener(clistener);
        a1 = mModel.getRootComponent().getChild(A.class);
        assertEquals(value, a1.getAnyAttribute(noPrefixAttr));
        assertEquals(prefix, a1.getPeer().lookupPrefix(ns));
        
        mModel.startTransaction();
        a1.setAnyAttribute(attr, null);
        mModel.endTransaction();
        assertNull(a1.getAnyAttribute(attr));
        
        assertEventListener(attr.getLocalPart(), value, null);
        clistener.assertEvent(ComponentEvent.EventType.VALUE_CHANGED, a1);
    }

    public void testCopyAndResetNS() throws Exception {
        defaultSetup();
        mModel = Util.loadModel("resources/test3.xml");
        p = mModel.getRootComponent();
        Aa aa1 = p.getChild(Aa.class);
        assertEquals(TestComponent3.NS2_URI, aa1.getNamespaceURI());
        D d = aa1.getChild(D.class);
        assertEquals(TestComponent3.NS_URI, d.getNamespaceURI());
        Aa aa2 = (Aa) aa1.copy(p);
        assertEquals(TestComponent3.NS2_URI, aa2.getNamespaceURI());
        assertEquals(TestComponent3.NS_URI, aa2.lookupNamespaceURI(""));
        D dCopy = aa2.getChild(D.class);
        assertEquals(TestComponent3.NS_URI, dCopy.getNamespaceURI());
        assertEquals(TestComponent3.NS_URI, dCopy.lookupNamespaceURI(""));
        
        mModel.startTransaction();
        aa2.setAttribute("testCopy.setup", TestAttribute3.INDEX, 2);
        aa2.removePrefix("myNS");
        aa2.getPeer().setPrefix(null);
        p.appendChild("testCopy.setup", aa2);
        mModel.endTransaction();
        
        assertEquals(2, aa2.getIndex());
        assertNull(aa2.lookupNamespaceURI("myNS"));
        //Util.dumpToFile(model.getBaseDocument(), new File("C:\\temp\\testCopy_after.xml"));
    }
    
    public void testThreeAppendsThenCopy() throws Exception {
        defaultSetup();
        A compA = mModel.createA(mA1);
        B compB = mModel.createB(compA);
        C compC = mModel.createC(compB);
        mModel.startTransaction();
        mA1.appendChild("compA", compA);
        compA.appendChild("compB", compB);
        compB.appendChild("compC", compC);
        mModel.endTransaction();
        
        assertEquals(compA, mA1.getChild(A.class));
        assertEquals(compB, compA.getChild(B.class));
        int length = compB.getPeer().getChildNodes().getLength();
        assertEquals("Got B children count="+length, 3, length);
        assertEquals(compC, compB.getChild(C.class));
        
        assertEquals(compA.getPeer().getChildNodes().item(1), compB.getPeer());
        
        A copyA = (A) compA.copy(mB1);
        B childOfCopy = copyA.getChild(B.class);
        length = childOfCopy.getPeer().getChildNodes().getLength();
        assertEquals("Got childOfCopy children count="+length, 3, length);
        C grandChildOfCopy = childOfCopy.getChild(C.class);
        assertNotNull(grandChildOfCopy);
    }

    public void testCopyHierarchy() throws Exception {
        defaultSetup();
        mModel = Util.loadModel("resources/test3.xml");
        p = mModel.getRootComponent();
        B b= p.getChild( B.class );
        mModel.startTransaction();
        C c = new C(mModel, 0);
        b.addBefore( "c" , c , Collections.EMPTY_LIST );
              assertNotNull( b.getChild( C.class ) );
              D d = new D( mModel , 0 );
        c.addBefore( "d" , d , Collections.EMPTY_LIST );
              assertNotNull( c.getChild( D.class ));
              B component = (B)b.copy( p );
              assertNotNull( component.getChild( C.class ));
              c = component.getChild( C.class );
              assertNotNull( c.getChild( D.class ));

        mModel.endTransaction();
    }

    public void testCopyAndAppendWithReference() throws Exception {
        defaultSetup();
        mModel = Util.loadModel("resources/test3_reference.xml");
        p = mModel.getRootComponent();
        TestModel3 model2 = Util.loadModel("resources/test3.xml");
        TestComponent3 recipient = model2.getRootComponent();
        Aa aa1 = p.getChild(Aa.class);
        D aa1Child = aa1.getChild(D.class);
        B aa1GrandChild = aa1Child.getChild(B.class);
        assertEquals("tns", aa1GrandChild.lookupPrefix("myTargetNS"));
        
        Aa copy = (Aa) aa1.copy(p);
        D copyChild = copy.getChild(D.class);
        assertEquals("myTargetNS", copyChild.lookupNamespaceURI("tns"));
        B copyGrandChild = copyChild.getChild(B.class);
        assertEquals("tns", aa1GrandChild.lookupPrefix("myTargetNS"));
        TestComponentReference<TestComponent3> ref = copyGrandChild.getRef(TestComponent3.class);
        assertEquals("tns:a1", ref.getRefString());
        
        try {
            ref.get();
            assertFalse("ref should not be accessible in copy", true);
        } catch(IllegalStateException e) {
            //OK
        }
        assertEquals(p.getTargetNamespace(), ref.getQName().getNamespaceURI());
        
        A recipientA1 = model2.getRootComponent().getChild(A.class);
        assertEquals("a1", model2.getRootComponent().getChild(A.class).getName());
        
        recipient.getModel().startTransaction();
        recipient.insertAtIndex(copy.getPeer().getLocalName(), copy, 0);
        recipient.getModel().endTransaction();
        
        Aa inserted = recipient.getChildren(Aa.class).get(0);
        assertTrue(inserted == copy);
        
        // assert model pointers
        assertTrue(recipient.getModel() == inserted.getModel());
        D insertedChild = inserted.getChild(D.class);
        assertTrue(recipient.getModel() == insertedChild.getModel());
        B insertedGrandChild = insertedChild.getChild((B.class));
        assertTrue(recipient.getModel() == insertedGrandChild.getModel());
        assertTrue(ref.get() == recipientA1);
    }
    
    public void testReAddDeep() throws Exception {
        defaultSetup();
        mModel = Util.loadModel("resources/test1_deep.xml");
        p = mModel.getRootComponent();
        A a1 = p.getChild(A.class);
        A a1Copy = (A) a1.copy(p);
        
        mModel.startTransaction();
        p.removeChild("testReAddDeep", a1);
        mModel.endTransaction();
        assertNull(p.getChild(A.class));
        
        try {
            mModel.startTransaction();
            p.appendChild("testReAddDeep", a1);
            assertFalse("Failed to get IllegalStateException", true);
        } catch(IllegalStateException ex) {
            //OK
        } finally {
            mModel.endTransaction();
        }
        mModel.startTransaction();
        p.appendChild("testReAddDeep", a1Copy);
        mModel.endTransaction();
        
        A a1ReAdded = p.getChild(A.class);
        assertEquals(3, a1ReAdded.getChildren(B.class).size());
        assertNotSame(a1, a1ReAdded);
    }
    
    public void testAddToSelfClosingRootElement() throws Exception {
        TestModel3 refmod = Util.loadModel("resources/Empty_selfClosing.xml");
        assertEquals(0, refmod.getRootComponent().getPeer().getChildNodes().getLength());
        
        mModel = Util.loadModel("resources/Empty.xml");
        p = mModel.getRootComponent();
        assertEquals(1, p.getPeer().getChildNodes().getLength());
        
        Util.setDocumentContentTo(mModel.getBaseDocument(), "resources/Empty_selfClosing.xml");
        mModel.sync();

        A a = mModel.createA(p);
        mModel.startTransaction();
        p.addAfter("test", a, TestComponent3._B);
        a.setValue("foo");
        mModel.endTransaction();
        assertEquals(3, p.getPeer().getChildNodes().getLength());
        
        File f = Util.dumpToTempFile(mModel.getBaseDocument());
        TestModel3 model2 = Util.loadModel(f);
        assertEquals(3, model2.getRootComponent().getPeer().getChildNodes().getLength());
    }
    
    public void testSetText() throws Exception {
        mModel = Util.loadModel("resources/test_removeChildren.xml");
        p = mModel.getRootComponent();
        mA1 = p.getChild(A.class);
        String a1Leading = "\n function match(a,b) if (a > 0 && b < 7) <a>     \n    ";
        assertEquals(a1Leading, p.getLeadingText(mA1));

        mModel.startTransaction();
        p.setText("test", "---a1---", mA1, true);
        mModel.endTransaction();
        assertFalse(p.getPeer().getChildNodes().item(1) instanceof Text);

        mModel.startTransaction();
        p.setText("test", "---b1---", mA1, false);
        mModel.endTransaction();
        
        assertEquals("a", p.getPeer().getChildNodes().item(1).getLocalName());
        assertEquals("---b1---", ((Text)p.getPeer().getChildNodes().item(2)).getNodeValue());
        assertEquals("b", p.getPeer().getChildNodes().item(3).getLocalName());

        mB1 = p.getChild(B.class);
        mC1 = p.getChild(C.class);

        mModel.startTransaction();
        p.setText("test", "---c1---", mC1, true);
        mModel.endTransaction();
        
        assertEquals("b", p.getPeer().getChildNodes().item(3).getLocalName());
        assertEquals("---c1---", ((Text)p.getPeer().getChildNodes().item(4)).getNodeValue());
        assertEquals("c", p.getPeer().getChildNodes().item(5).getLocalName());

        mModel.startTransaction();
        p.setText("test", "---(c1)---", mB1, false);
        mModel.endTransaction();
        
        assertEquals("b", p.getPeer().getChildNodes().item(3).getLocalName());
        assertEquals("---(c1)---", p.getLeadingText(mC1));
        assertEquals("c", p.getPeer().getChildNodes().item(5).getLocalName());

        mModel.startTransaction();
        p.setText("test", "---d1---", mC1, false);
        mModel.endTransaction();
        //Util.dumpToFile(model.getBaseDocument(), new File("c:/temp/test1.xml"));
        
        assertEquals("c", p.getPeer().getChildNodes().item(5).getLocalName());
        assertEquals("---d1---", p.getTrailingText(mC1));
        assertEquals(7, p.getPeer().getChildNodes().getLength());

        mModel.startTransaction();
        p.setText("test", null, mA1, true);
        p.setText("test", null, mB1, false);
        p.setText("test", null, mC1, true);
        p.setText("test", null, mC1, false);
        mModel.endTransaction();

        assertNull(p.getLeadingText(mA1));
        assertNull(p.getTrailingText(mB1));
        assertNull(p.getLeadingText(mC1));
        assertNull(p.getTrailingText(mC1));
    }
          
    public void testGetXmlFragmentInclusiveMiddle() throws Exception {
        TestModel3 model = Util.loadModel("resources/test_xmlfragment.xml");
        TestComponent3 root = model.getRootComponent();
        TestComponent3.B b = model.getRootComponent().getChildren(TestComponent3.B.class).get(0);
        String result = b.getXmlFragmentInclusive();
        assertTrue(result.startsWith("<b index='1'>"));
        assertTrue(result.endsWith("</b>"));
    }

    public void testGetXmlFragmentInclusiveEdgeWithCDATA() throws Exception {
        TestModel3 model = Util.loadModel("resources/test_xmlfragment.xml");
        TestComponent3 root = model.getRootComponent();
        TestComponent3.C c = model.getRootComponent().getChildren(TestComponent3.C.class).get(1);
        String result = c.getXmlFragmentInclusive();
        assertEquals("<c index='2'/>", result);
    }

    public void testGetXmlFragmentInclusiveDeepWithComment() throws Exception {
        TestModel3 model = Util.loadModel("resources/test_xmlfragment.xml");
        TestComponent3 root = model.getRootComponent();
        TestComponent3.B b = model.getRootComponent().getChildren(TestComponent3.B.class).get(0);
        TestComponent3.B bb = b.getChildren(TestComponent3.B.class).get(0);
        String result = bb.getXmlFragmentInclusive();
        assertEquals("<b index='1' value=\"c\"/>", result);
    }

    public void testGetXmlFragmentInclusiveOnRoot() throws Exception {
        TestModel3 model = Util.loadModel("resources/test_xmlfragment.xml");
        TestComponent3 root = model.getRootComponent();
        String result = root.getXmlFragmentInclusive();
        assertTrue(result.startsWith("<test xmlns=\"http://www.test"));
        assertTrue(result.endsWith("</test  >"));
    }

    public void testGetXmlFragmentInclusiveNoTextNode() throws Exception {
        TestModel3 model = Util.loadModel("resources/test_xmlfragment.xml");
        TestComponent3 root = model.getRootComponent();
        TestComponent3.A a = model.getRootComponent().getChildren(TestComponent3.A.class).get(0);
        String result = a.getXmlFragmentInclusive();
        assertEquals("<a index='1'>CharDataString</a>", result);
    }
    
    public void testAddComponentDoFixupOnChildDefaultPrefix() throws Exception {
        TestModel3 model = Util.loadModel("resources/test1_prefix.xml");
        TestComponent3 root = model.getRootComponent();
        assertEquals(root.getNamespaceURI(), root.lookupNamespaceURI("ns"));
        TestComponent3.Aa aa = model.createAa(root);
        model.startTransaction();
        root.appendChild("testAddComponentDoFixupDefaultPrefix", aa);
        model.endTransaction();
        TestComponent3.A a = root.getChild(TestComponent3.A.class);
        assertEquals(TestComponent3.NS_URI, a.getNamespaceURI());
        assertEquals(TestComponent3.NS2_URI, aa.getNamespaceURI());
        assertEquals("ns1", aa.getPeer().getPrefix());
    }
    
    public void testAdd_ToStandalone_ComponentDoFixupOnChildDefaultPrefix() throws Exception {
        TestModel3 model = Util.loadModel("resources/test1_prefix.xml");
        TestComponent3 root = model.getRootComponent();
        assertEquals(root.getNamespaceURI(), root.lookupNamespaceURI("ns"));
        TestComponent3.Aa aa = model.createAa(root);
        TestComponent3.Aa aaChild = model.createAa(aa);
        aa.appendChild("appendToStandAloneAa", aaChild);
        model.startTransaction();
        root.appendChild("testAddComponentDoFixupDefaultPrefix", aa);
        model.endTransaction();
        TestComponent3.A a = root.getChild(TestComponent3.A.class);
        assertEquals(TestComponent3.NS_URI, a.getNamespaceURI());
        assertEquals(TestComponent3.NS2_URI, aa.getNamespaceURI());
        assertEquals(TestComponent3.NS2_URI, aaChild.getNamespaceURI());
        assertEquals("ns1", aa.getPeer().getPrefix());
        assertEquals("ns1", aaChild.getPeer().getPrefix());
    }
    
    public void testSetRefOnStandAlone() throws Exception {
        mModel = Util.loadModel("resources/test4_reference.xml");
        p = mModel.getRootComponent();
        TestComponent3.A aa = mModel.createA(p);
        TestModel3 model2 = Util.loadModel("resources/test4.xml");
        TestComponent3 root2 = model2.getRootComponent();
        A a1 = root2.getChild(A.class);
        mModel.startTransaction();
        aa.setRef(a1, A.class);
        p.appendChild("testSetRefOnStandAlone", aa);
        mModel.endTransaction();
        assertEquals(root2.getTargetNamespace(), p.lookupNamespaceURI("ns1"));
        assertEquals("myTargetNS3", p.lookupNamespaceURI("ns"));
    }
    
    public void testAddComponentDoFixupOnRefDefaultPrefix() throws Exception {
        mModel = Util.loadModel("resources/test4_reference.xml");
        p = mModel.getRootComponent();
        B b1 = p.getChild(B.class);
        TestModel3 model2 = Util.loadModel("resources/test4.xml");
        TestComponent3 root2 = model2.getRootComponent();
        A a1 = root2.getChild(A.class);
        mModel.startTransaction();
        b1.setRef(a1, A.class);
        mModel.endTransaction();
        assertEquals(root2.getTargetNamespace(), p.lookupNamespaceURI("ns1"));
        assertEquals("myTargetNS3", p.lookupNamespaceURI("ns"));
    }
    
    // TODO support PI inside normal element
    public void FIXME_testProcessingInstruction() throws Exception {
        mModel = Util.loadModel("resources/PI_after_prolog.xml");
        p = mModel.getRootComponent();
        A a1 = p.getChild(A.class);
        assertEquals(132, a1.findPosition());
        B b1 = p.getChild(B.class);
        Element peer = (Element) b1.getPeer();
        List<Token> tokens = peer.getTokens();
        assertEquals(TokenType.TOKEN_PI_START_TAG, tokens.get(2).getType());
        assertEquals(TokenType.TOKEN_PI_NAME, tokens.get(3).getType());
        assertEquals("Siebel-Property-Set", tokens.get(4).getValue());
        assertEquals(TokenType.TOKEN_PI_VAL, tokens.get(6).getType());
        assertEquals("SkipValidation=\"true\"", tokens.get(6).getValue());
        NodeList nl = peer.getChildNodes();
        assertEquals(2, nl.getLength());    
    }
}
    
