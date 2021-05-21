/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.spi.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.ContextAwareAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
public class ContextActionTest extends NbTestCase {

    public ContextActionTest(String x) {
        super(x);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    private InstanceContent content;
    private Lookup lkp;

    //This timeout is just to make sure if a test fails, the test suite
    //exits eventually.  If some tests fail on slow machines, try increasing
    //it.
    private static final int TIMEOUT = 100;

    @Before
    @Override
    public void setUp() {
        //This will cause ContextAction instances to invoke notifyAll()
        //when their enablement changes
        ContextAction.unitTest = true;
        MockServices.setServices(Provider.class);
        ContextGlobalProvider x = Lookup.getDefault().lookup(ContextGlobalProvider.class);
        assertNotNull(x);
        assertTrue(x instanceof Provider);
        Provider p = (Provider) x;
        content = p.content;
        lkp = p.lkp;
        //some sanity checks
        setContent("hello");
        assertEquals("hello", lkp.lookupAll(String.class).iterator().next());
        assertEquals("hello", Utilities.actionsGlobalContext().lookupAll(String.class).iterator().next());
        clearContent();
        assertEquals(null, lkp.lookup(String.class));
        assertEquals(null, Utilities.actionsGlobalContext().lookup(String.class));
        assertEquals(0, Utilities.actionsGlobalContext().lookupAll(Object.class).size());
    }
    static Provider instance;

    public static class Provider implements ContextGlobalProvider {

        private final Lookup lkp;
        private final InstanceContent content;

        public Provider() {
            lkp = new AbstractLookup(content = new InstanceContent());
            instance = this;
        }

        public Lookup createGlobalContext() {
            return lkp;
        }
    }

    void setContent(Object... stuff) {
        content.set(Arrays.asList(stuff), null);
    }

    void clearContent() {
        content.set(Collections.EMPTY_SET, null);
    }

    public void testEnablement() {
        System.out.println("testEnablement");
        assertEquals(0, lkp.lookupAll(String.class).size()); //sanity check
        A a = new A();
        assertFalse(a.isEnabled());
        setContent("testEnablement");
        assertEquals(1, lkp.lookupAll(String.class).size()); //sanity check
        assertTrue(a.isEnabled());
        clearContent();
        assertFalse(a.isEnabled());
    }

    public void testEnablementFired() throws Exception {
        System.out.println("testEnablementFired");
        A a = new A();
        assertEquals(0, lkp.lookupAll(String.class).size());
        PCL pcl = new PCL();
        a.addPropertyChangeListener(pcl);
        assertFalse(a.isEnabled());
        setContent("testEnablementFired");
        synchronized (a) {
            a.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        assertTrue(a.isEnabled());
        pcl.assertEnabledChangedTo(true);
        clearContent();
        assertFalse(a.isEnabled());
        synchronized (a) {
            a.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertEnabledChangedTo(false);
        setContent("woo");
        synchronized (a) {
            a.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertEnabledChangedTo(true);
        setContent("hello", "goodbye");
        synchronized (a) {
            a.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertEnabledChangedTo(false);
        assertFalse(a.isEnabled());
        clearContent();
        synchronized (a) {
            a.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertNotFired();
        a.removePropertyChangeListener(pcl);
        setContent("hmm");
        assertTrue(a.isEnabled());
        pcl.assertNotFired();
    }

    public void testContextInstancesDoNotInterfereWithEachOtherOrParent() throws Exception {
        System.out.println("testContextInstancesDoNotInterfereWithEachOtherOrParent");
        A a = new A();
        assertNull(Utilities.actionsGlobalContext().lookup(String.class)); //sanity check
        assertEquals("A", a.getValue(Action.NAME));
        Action a1 = a.createContextAwareInstance(Lookup.EMPTY);
        assertFalse(a.isEnabled());
        assertEquals("A", a1.getValue(Action.NAME));
        Action a2 = a.createContextAwareInstance(Lookups.fixed("testGeneralBehavior"));
        assertTrue(a2.isEnabled());
        assertFalse(a.isEnabled());
        setContent("foo");
        assertTrue(a.isEnabled());
        assertFalse(a1.isEnabled());
        assertTrue(a2.isEnabled());
        clearContent();
        assertFalse(a.isEnabled());
        assertTrue(a2.isEnabled());
    }

    public void testContextInstancesAreIndependent() throws Exception {
        System.out.println("testContextInstancesAreIndependent");
        A a = new A();
        assertNull(Utilities.actionsGlobalContext().lookup(String.class)); //sanity check
        InstanceContent ic = new InstanceContent();
        Lookup l = new AbstractLookup(ic);
        Action a3 = a.createContextAwareInstance(l);
        assertFalse(a3.isEnabled());
        PCL pcl = new PCL();
        a3.addPropertyChangeListener(pcl);
        setContent("fuddle");
        a.assertNotPerformed();
        assertTrue(a.isEnabled());
        assertFalse(a3.isEnabled());
        synchronized (a3) {
            //should time out if test is going to pass
            a3.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertNotFired();
        ic.set(Collections.singleton("boo"), null);
        synchronized (a3) {
            a3.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertEnabledChangedTo(true);
        clearContent();
        assertTrue(a3.isEnabled());
        assertFalse(a.isEnabled());
    }

    public void testGetSetValue() {
        System.out.println("testGetSetValue");
        A a = new A();
        Action a1 = a.createContextAwareInstance(Lookup.EMPTY);
        Action a2 = a.createContextAwareInstance(Lookups.fixed("testGeneralBehavior"));
        a.putValue("foo", "bar");
        assertEquals("bar", a.getValue("foo"));
        assertEquals("bar", a1.getValue("foo"));
        assertEquals("bar", a2.getValue("foo"));

        a1.putValue("x", "y");
        assertNull(a.getValue("x"));
        assertEquals("y", a1.getValue("x"));
        a.putValue("x", "z");
        assertEquals("y", a1.getValue("x"));
        assertEquals("z", a.getValue("x"));
    }

    public void testSingleNotEnabledOnMoreThanOne() throws Exception {
        System.out.println("testSingleNotEnabledOnMoreThanOne");
        A a = new A();
        assertFalse(a.createContextAwareInstance(Lookups.fixed("moo", "goo")).isEnabled());
        assertTrue(a.createContextAwareInstance(Lookups.fixed("foo")).isEnabled());
    }

    public void testExactCount() throws Exception {
        System.out.println("testExactCount");
        C c = new C();
        assertFalse(c.isEnabled());
        setContent("1");
        assertFalse(c.isEnabled());
        setContent("1", "2", "3", "4", "5");
        assertTrue(c.isEnabled());
        clearContent();
        assertFalse(c.isEnabled());
        setContent("1", "2", "3", "4", "5");
        assertTrue(c.isEnabled());
        setContent("1", "2", "3", "4", "5", "6");
        assertFalse(c.isEnabled());
    }

    public void testNesting() throws Exception {
        System.out.println("testNesting");
        D d = new D();
        Thing thingOne = new Thing("a");
        Thing thingTwo = new Thing("b");
        Thing thingThree = new Thing("c");
        Thing thingFour = new Thing("d");
        Thing badThing = new Thing("no");

        FakeNode node1 = new FakeNode(thingOne);
        FakeNode node2 = new FakeNode(thingTwo);
        FakeNode node3 = new FakeNode(thingThree);
        FakeNode node4 = new FakeNode(thingFour);
        FakeNode nodeBad = new FakeNode(badThing);

        PCL pcl = new PCL();
        d.addPropertyChangeListener(pcl);

        assertFalse(d.isEnabled());
        setContent(node1);
        assertTrue(d.isEnabled());
        synchronized (d) {
            d.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertEnabledChangedTo(true);

        clearContent();
        synchronized (d) {
            d.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertEnabledChangedTo(false);

        assertFalse(d.isEnabled());
        setContent(node1, node2, node3, node4);
        assertTrue(d.isEnabled());
        setContent(node1, node2, node3, node4, nodeBad);
        synchronized (d) {
            d.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertEnabledChangedTo(false);

        assertFalse(d.isEnabled());

        setContent(node1, node2, node3, node4);
        assertTrue(d.isEnabled());

        d.actionPerformed((ActionEvent) null);
        Set<Thing> expected = new HashSet<Thing>(Arrays.asList(
                thingOne, thingTwo, thingThree, thingFour));
        d.assertTargets(expected);

        clearContent();
        assertFalse(d.isEnabled());

        FakeNode doesntHaveAThing = new FakeNode("Hello");

        d = new D(true);
        assertFalse(d.isEnabled());
        setContent(node1);
        assertTrue(d.isEnabled());
        clearContent();
        assertFalse(d.isEnabled());
        setContent(node1, node2, node3, node4);
        assertTrue(d.isEnabled());
        clearContent();
        assertFalse(d.isEnabled());
        setContent(node1, node2, node3, doesntHaveAThing);
        assertFalse(d.isEnabled());
    }

    public void testDeepDelegation() throws Exception {
        System.out.println("testDeepDelegation");
        E e = new E();
        IndirectAction a = new IndirectAction(FakeNode.class,
                new IndirectAction(FakeProject.class, e, false), false);
        Thing thing1 = new Thing("a");
        Thing thing2 = new Thing("b");
        Thing thing3 = new Thing("c");
        Thing thing4 = new Thing("d");
        Thing badThing = new Thing("no");

        FakeProject proj1 = new FakeProject(thing1);
        FakeProject proj2 = new FakeProject(thing2);
        FakeProject proj3 = new FakeProject(thing3);
        FakeProject proj4 = new FakeProject(thing4);
        FakeProject projBad = new FakeProject(badThing);

        FakeNode node1 = new FakeNode(proj1);
        FakeNode node2 = new FakeNode(proj2);
        FakeNode node3 = new FakeNode(proj3);
        FakeNode node4 = new FakeNode(proj4);
        FakeNode nodeBad = new FakeNode(projBad);

        assertFalse(a.isEnabled());
        setContent(node1, node2);
        assertTrue(a.isEnabled());
        setContent(node1, node2, node3);
        assertTrue(a.isEnabled());
        clearContent();
        assertFalse(a.isEnabled());
        setContent(node1, node2, node4);
        assertTrue(a.isEnabled());
        setContent(node1, node2, nodeBad);
        assertFalse(a.isEnabled());

        setContent(node1, node2);
        a.actionPerformed((ActionEvent) null);
        e.assertTargets(new HashSet<Thing>(Arrays.asList(thing1, thing2)));

        clearContent();
        PCL pcl = new PCL();
        a.addPropertyChangeListener(pcl);
        setContent(node1, node3);
        synchronized (a) {
            a.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertEnabledChangedTo(true);
        clearContent();

        synchronized (a) {
            a.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertEnabledChangedTo(false);
        setContent(node1, node3);
        synchronized (a) {
            a.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertEnabledChangedTo(true);

        setContent(nodeBad);
        synchronized (a) {
            a.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertEnabledChangedTo(false);
    }

    public void testIndirectionWithSomeNonMatches() throws Exception {
        System.out.println("testIndirectionWithSomeNonMatches");
        E e = new E();
        IndirectAction a = new IndirectAction(FakeNode.class,
                new IndirectAction(FakeProject.class, e, true), true);
        Thing thing1 = new Thing("a");
        Thing thing2 = new Thing("b");

        FakeProject proj1 = new FakeProject(thing1);
        FakeProject proj2 = new FakeProject(thing2);

        FakeProject projNonMatch = new FakeProject("foo");

        FakeNode node1 = new FakeNode(proj1);
        FakeNode node2 = new FakeNode(proj2);

        FakeNode nodeNonMatch = new FakeNode("bar");
        FakeNode nodeWithProjNonMatch = new FakeNode(projNonMatch);

        setContent(node1, node2);
        assertTrue(a.isEnabled());
        clearContent();
        assertFalse(a.isEnabled());

        setContent(node1, node2);
        setContent(nodeNonMatch);
        assertFalse(a.isEnabled());

        setContent(nodeNonMatch, node1);
        assertFalse(a.isEnabled());

        setContent(node1);
        assertTrue(a.isEnabled());
        setContent(node1, nodeWithProjNonMatch);
        assertFalse(a.isEnabled());

        setContent(nodeWithProjNonMatch);
        assertFalse(a.isEnabled());
    }

    public void testMerge() throws Exception {
        System.out.println("testMerge");
        E looksInProject = new E();

        IndirectAction a =
                new IndirectAction(FakeProject.class, looksInProject, true);

        E looksInGlobalSelection = new E();

        looksInGlobalSelection.putValue(Action.NAME, "Global");
        looksInProject.putValue(Action.NAME, "DataObject");

        ContextAwareAction merge = ContextAction.merge(looksInGlobalSelection, a);

//        merge.addPropertyChangeListener(new PCL());

        Thing thing = new Thing("thing");
        FakeProject project = new FakeProject(thing);

        assertFalse(merge.isEnabled());
        assertFalse(looksInGlobalSelection.isEnabled());
        assertFalse(looksInProject.isEnabled());

        setContent(thing);
        assertTrue(looksInGlobalSelection.isEnabled());
        assertTrue(merge.isEnabled());
        assertFalse(a.isEnabled());
        assertEquals("Global", merge.getValue(Action.NAME));

        setContent(project);
        assertFalse(looksInGlobalSelection.isEnabled());
        assertTrue(merge.isEnabled());
        assertTrue(a.isEnabled());
        assertEquals("DataObject", merge.getValue(Action.NAME));

        assertTrue (merge.isEnabled());
        merge.actionPerformed((ActionEvent) null);
        looksInGlobalSelection.assertNotFired();
        looksInProject.assertTargets(Collections.singleton(thing));

        clearContent();
        assertFalse(merge.isEnabled());
        assertFalse(a.isEnabled());

        setContent(project);
        assertTrue(a.isEnabled());
        assertFalse(looksInGlobalSelection.isEnabled());
        assertTrue(merge.isEnabled());
        assertEquals("DataObject", merge.getValue(Action.NAME));

        clearContent();
        assertEquals("Global", merge.getValue(Action.NAME));
    }

    public void testIndirectActionFiresEnablementCorrectly() throws Exception {
        System.out.println("testIndirectActionFiresEnablementCorrectly");
        E looksInProject = new E();

        IndirectAction overProjectsLookup =
                new IndirectAction(FakeProject.class, looksInProject, true);

        E looksInGlobalSelection = new E();

        looksInGlobalSelection.putValue(Action.NAME, "Global");
        looksInProject.putValue(Action.NAME, "DataObject");

        PCL pcl = new PCL();
        overProjectsLookup.addPropertyChangeListener(pcl);

        Thing thing = new Thing("thing");
        FakeProject project = new FakeProject(thing);
        setContent(project);
        synchronized (overProjectsLookup) {
            overProjectsLookup.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertEnabledChangedTo(true);
    }

    public void testMergeFiresAppropriateChanges() throws Exception {
        System.out.println("testMergeFiresAppropriateChanges");
        E looksInProject = new E();

        IndirectAction overProjectsLookup =
                new IndirectAction(FakeProject.class, looksInProject, true);

        E looksInGlobalSelection = new E();

        looksInGlobalSelection.putValue(Action.NAME, "Global");
        looksInProject.putValue(Action.NAME, "DataObject");

        ContextAwareAction merge = ContextAction.merge(looksInGlobalSelection, overProjectsLookup);

        PCL pcl = new PCL();
        merge.addPropertyChangeListener(pcl);

        looksInGlobalSelection.putValue("whatzit", "foo");
        looksInProject.putValue("whatzit", "bar");

        //We need to request it from merge at least once for it
        //to know about the key and fire changes
        assertEquals("foo", merge.getValue("whatzit"));

        Thing thing = new Thing("thing");
        FakeProject project = new FakeProject(thing);

        assertFalse(merge.isEnabled());
        assertFalse(looksInGlobalSelection.isEnabled());
        assertFalse(looksInProject.isEnabled());

        setContent(project);
        assertTrue(merge.isEnabled());
        assertFalse(looksInGlobalSelection.isEnabled());
        assertTrue(overProjectsLookup.isEnabled());
        synchronized (merge) {
            merge.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }

        pcl.assertEnabledChangedTo(true);
        assertTrue(overProjectsLookup.isEnabled());
        Object val = merge.getValue("whatzit");
        assertEquals("bar", val);
        assertEquals("Merge should not proxy value from disabled action when it " +
                "is not disabled itself, but " +
                "got " + val, "bar", val);

        setContent(thing);
        assertTrue(looksInGlobalSelection.isEnabled());
        assertTrue(merge.isEnabled());
        assertFalse(overProjectsLookup.isEnabled());
        assertEquals("Global", merge.getValue(Action.NAME));
        assertEquals("foo", merge.getValue("whatzit"));
        synchronized (merge) {
            merge.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        assertEquals("foo", pcl.assertFired("whatzit"));

        clearContent();
        synchronized (merge) {
            merge.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }

        setContent(project);
        synchronized (merge) {
            merge.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        pcl.assertFired("whatzit");
        assertTrue(merge.isEnabled());

        setContent(thing);
        assertTrue(merge.isEnabled());
        synchronized (merge) {
            merge.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }

        assertEquals("Global", pcl.assertFired(Action.NAME));
        assertEquals("foo", pcl.assertFired("whatzit"));
    }

    public void testSurviveFocusChange() throws InterruptedException {
        System.out.println("testSurviveFocusChange");
        B b = new B();
        assertFalse(b.isEnabled());
        setContent("testSurviveFocusChange");
        assertTrue(b.isEnabled());
        PCL pcl = new PCL();
        b.addPropertyChangeListener(pcl);
        setContent("x");
        assertTrue(b.isEnabled());
        b.actionPerformed((ActionEvent) null);
        b.assertPerformed("x");
        clearContent();
        assertTrue(b.isEnabled());
        b.actionPerformed((ActionEvent) null);
        b.assertPerformed("x");
        setContent("a", "b");
        assertFalse(b.isEnabled());
        synchronized (b) {
            b.wait(TIMEOUT);
        }
        synchronized (pcl) {
            pcl.wait(TIMEOUT);
        }
        assertFalse(b.createContextAwareInstance(Lookup.EMPTY).isEnabled());
        assertTrue(b.createContextAwareInstance(Lookups.fixed("foo")).isEnabled());
        assertFalse(b.createContextAwareInstance(Lookups.fixed("moo", "goo")).isEnabled());
//
//        setContent("y");
//        assertTrue(b.isEnabled());
        setContent(new Thing("foo"));
        assertFalse("Setting selection to a non-empty lookup containing no" +
                "objects of type " + b.type + " should disable a " +
                "survive focus action", b.isEnabled());
    }

    public void testStubsDisposed() throws Exception {
        System.out.println("testStubsDisposed");
        E e = new E();
        IndirectAction a = new IndirectAction(FakeNode.class,
                new IndirectAction(FakeProject.class, e, true), true);

        Thing thing1 = new Thing("a");
        FakeProject proj1 = new FakeProject(thing1);
        FakeNode node1 = new FakeNode(proj1);

        assertNull(a.stub);
        PCL pcl = new PCL();
        a.addPropertyChangeListener(pcl);

        Reference<IndirectAction> aref = new WeakReference<IndirectAction>(a);
        Reference<E> eref = new WeakReference<E>(e);
        a = null;
        e = null;
        assertGC("Wrapped action still referenced", eref);
        assertGC("Indirect action still referenced", aref);

        e = new E();
        a = new IndirectAction(FakeNode.class,
                new IndirectAction(FakeProject.class, e, true), true);

        Set<Object> rootsHint = new HashSet<Object>(Arrays.asList(
                content,
                pcl));

        assertNull(a.stub);
        assertNull(e.stub);

        pcl = new PCL();

        a.addPropertyChangeListener(pcl);

        assertNotNull(a.stub);
        assertNotNull(e.stub);
        setContent(node1);
        Reference<ActionStub<?>> outerStubRef = new WeakReference<>(a.stub);
        Reference<ActionStub<?>> innerStubRef = new WeakReference<>(e.stub);
        //Let the change get fired before we move on;  otherwise the runnable
        //on the event queue can hold a reference that causes this test to 
        //randomly fail
        synchronized (pcl) {
            pcl.wait(500);
        }
        a.removePropertyChangeListener(pcl);
        //diagnostics
        Reference<PCL> pclRef = new WeakReference(pcl);
        pcl = null;
        assertGC("PCL still alive", pclRef, rootsHint);

        assertGC("IndirectAction's stub still exists after all listeners " +
                "removed", outerStubRef, rootsHint);
        assertGC("Wrapped action's stub still exists after all listeners " +
                "removed", innerStubRef, rootsHint);

        aref = new WeakReference<>(a);
        eref = new WeakReference<>(e);
        a = null;
        e = null;

        assertGC("Wrapped action still referenced", eref, rootsHint);
        assertGC("Indirect action still referenced", aref, rootsHint);

        clearContent();
        Reference<Thing> thingRef = new WeakReference<>(thing1);
        thing1 = null;
        proj1 = null;
        node1 = null;
        assertGC("target still referenced", thingRef, rootsHint);
    }

    public void testNameMutability() throws Exception {
        System.out.println("testNameMutability");
        F f = new F();
        f.addPropertyChangeListener(new PCL()); //change() will not be called if nobody cares

        assertEquals("Do something", f.getValue(Action.NAME));
        Action a = f.createContextAwareInstance(Lookup.EMPTY);
        assertEquals("Do something", a.getValue(Action.NAME));

        System.err.println("set content to " + new Thing("X"));
        setContent(new Thing("X"));
        synchronized (f) {
            f.wait(500);
        }
        System.err.println("done wait");
        f.assertChanged();
        String name = (String) f.getValue(Action.NAME);
        assertEquals("Do something to X", name);

        setContent(new Thing("X"), new Thing("Y"));
        assertEquals("Do something to 2 Things", f.getValue(Action.NAME));
        Action a1 = f.createContextAwareInstance(Utilities.actionsGlobalContext());
        assertEquals("Do something to 2 Things", a1.getValue(Action.NAME));

        Lookup other = Lookups.singleton(new Thing("Z"));
        Action a2 = f.createContextAwareInstance(other);
        assertEquals("Do something to Z", a2.getValue(Action.NAME));

        clearContent();
        assertEquals("Do something", f.getValue(Action.NAME));
        assertEquals("Do something", a1.getValue(Action.NAME));

        assertEquals("Do something to Z", a2.getValue(Action.NAME));
    }

    public void testMergeHandlesDisplayNameCorrectly() throws Exception {
        System.out.println("testMergeHandlesDisplayNameCorrectly");
        F f = new F();
        G g = new G();
//        MergeAction m = new MergeAction(new ContextAction[]{f, g});
        Action m = ContextAction.merge(f, g);
        m.addPropertyChangeListener(new PCL());
        Thing thing1 = new Thing("tx1");
        Thing thing2 = new Thing("tx2");
        OtherThing otherThing1 = new OtherThing("ox1");
        OtherThing otherThing2 = new OtherThing("ox2");
        assertEquals("Do something", m.getValue(Action.NAME));
        assertFalse(m.isEnabled());

        setContent(thing1);
        assertTrue(m.isEnabled());
        assertEquals("Do something to tx1", m.getValue(Action.NAME));
        setContent(otherThing1);
        assertTrue(m.isEnabled());
        assertEquals("Do something else to ox1", m.getValue(Action.NAME));
        setContent(thing1);
        assertTrue(m.isEnabled());
        assertEquals("Do something to tx1", m.getValue(Action.NAME));
        setContent(thing2);
        assertTrue(m.isEnabled());
        assertEquals("Do something to tx2", m.getValue(Action.NAME));
        clearContent();

        setContent(thing1, thing2);
        assertEquals("Do something to 2 Things", m.getValue(Action.NAME));
        setContent(new Object());
        assertFalse(m.isEnabled());
        assertEquals("Do something", m.getValue(Action.NAME));
        setContent(otherThing1, otherThing2);
        assertEquals("Do something else to 2 OtherThings", m.getValue(Action.NAME));
    }

    public void testMergeHandlesDisplayNameCorrectlyWhenNotListenedTo() throws Exception {
        System.out.println("testMergeHandlesDisplayNameCorrectlyWhenNotListenedTo");
        F f = new F();
        G g = new G();
        MergeAction m = new MergeAction(new ContextAction[]{f, g});
        Thing thing1 = new Thing("tx1");
        Thing thing2 = new Thing("tx2");
        OtherThing otherThing1 = new OtherThing("ox1");
        OtherThing otherThing2 = new OtherThing("ox2");
        assertEquals("Do something", m.getValue(Action.NAME));
        assertFalse(m.isEnabled());

        setContent(thing1);
        assertTrue(m.isEnabled());
        assertEquals("Do something to tx1", m.getValue(Action.NAME));
        setContent(otherThing1);
        assertTrue(m.isEnabled());
        assertEquals("Do something else to ox1", m.getValue(Action.NAME));
        setContent(thing1);
        assertTrue(m.isEnabled());
        assertEquals("Do something to tx1", m.getValue(Action.NAME));
        setContent(thing2);
        assertTrue(m.isEnabled());
        assertEquals("Do something to tx2", m.getValue(Action.NAME));
        clearContent();

        setContent(thing1, thing2);
        assertEquals("Do something to 2 Things", m.getValue(Action.NAME));
        setContent(new Object());
        assertFalse(m.isEnabled());
        assertEquals("Do something", m.getValue(Action.NAME));
        setContent(otherThing1, otherThing2);
        assertEquals("Do something else to 2 OtherThings", m.getValue(Action.NAME));
    }

    public void testMergeExclusivity() {
        System.out.println("testMergeExclusivity");
        F f = new F();
        G g = new G();
        MergeAction m = new MergeAction(new ContextAction[]{f, g}, true);
        Thing thing1 = new Thing("tx1");
        Thing thing2 = new Thing("tx2");
        Thing badThing = new Thing("bad");
        OtherThing otherThing1 = new OtherThing("ox1");
        OtherThing otherThing2 = new OtherThing("ox2");
        OtherThing otherBadThing = new OtherThing("bad");

        setContent (thing1);
        assertTrue (m.isEnabled());
        setContent (otherThing1);
        assertTrue (m.isEnabled());
        setContent (thing1, otherThing1);
        assertTrue (f.isEnabled());
        assertTrue (g.isEnabled());
        assertFalse (m.isEnabled());
        setContent (thing1, otherBadThing);
        assertTrue (m.isEnabled());
        setContent (badThing);
        assertFalse (m.isEnabled());
        setContent (badThing, otherBadThing);
        assertFalse (m.isEnabled());
        setContent (thing1, thing2);
        assertTrue (m.isEnabled());
    }

    private class PCL implements PropertyChangeListener {

        PropertyChangeEvent evt;
        Map<String, Object> fired = new HashMap<String, Object>();

        void clear() {
            fired.clear();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            fired.put(evt.getPropertyName(), evt.getNewValue());
            if ("enabled".equals(evt.getPropertyName())) {
                this.evt = evt;
            }
            synchronized (this) {
                notifyAll();
            }
        }

        void assertNotFired(String prop) {
            assertFalse(fired.containsKey(prop));
        }

        public Object assertFired(String key) {
            assertTrue(fired.keySet().contains(key));
            return fired.remove(key);
        }

        void assertEnabledChangedTo(boolean val) {
            PropertyChangeEvent old = this.evt;
            this.evt = null;
            assertNotNull(old);
            Boolean b = Boolean.valueOf(val);
            assertEquals("Enabled not changed to " + val + ", but " + b,
                    b, old.getNewValue());
        }

        void assertNotFired() {
            assertNull(evt);
        }
    }

    private static class A extends Single<String> {

        String perfString;

        A() {
            super(String.class, "A", null);
        }

        @Override
        protected void actionPerformed(String target) {
            perfString = target;
        }

        void assertPerformed(String expected) {
            String old = perfString;
            perfString = null;
            assertNotNull(old);
            assertEquals(expected, old);
        }

        void assertNotPerformed() {
            assertNull(perfString);
        }
    }

    private static class B extends SurviveSelectionChange<String> {

        String perfString;

        B() {
            super(String.class, "B", null);
        }

        @Override
        protected void actionPerformed(Collection<? extends String> target) {
            perfString = target.size() == 0 ? null : target.iterator().next();
        }

        void assertPerformed(String expected) {
            String old = perfString;
            perfString = null;
            assertNotNull(old);
            assertEquals(expected, old);
        }

        @Override
        protected boolean checkQuantity(int numberOfObjects) {
            return numberOfObjects == 1;
        }
    }

    private static class C extends ContextAction<String> {

        Set<String> perfStrings;

        C() {
            super(String.class);
        }

        @Override
        protected boolean checkQuantity(int numberOfObjects) {
            return numberOfObjects == 5;
        }

        void assertPerformed(Set<String> expected) {
            Set<String> old = perfStrings;
            perfStrings = null;
            assertNotNull(old);
            assertEquals(expected, old);
        }

        @Override
        protected void actionPerformed(Collection<? extends String> targets) {
            perfStrings = new HashSet<String>(targets);
        }
    }

    private static class FakeNode implements Lookup.Provider {

        private final InstanceContent content = new InstanceContent();
        private final AbstractLookup lkp = new AbstractLookup(content);

        FakeNode(Object... contents) {
            content.set(Arrays.asList(contents), null);
        }

        public Lookup getLookup() {
            return lkp;
        }
    }

    private static class FakeProject implements Lookup.Provider {

        private final InstanceContent content = new InstanceContent();
        private final AbstractLookup lkp = new AbstractLookup(content);

        FakeProject(Object... contents) {
            content.set(Arrays.asList(contents), null);
        }

        public Lookup getLookup() {
            return lkp;
        }
    }

    private static final class Thing {

        private final String s;

        Thing(String s) {
            this.s = s;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Thing other = (Thing) obj;
            if ((this.s == null) ? (other.s != null) : !this.s.equals(other.s)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + (this.s != null ? this.s.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return s;
        }
    }

    private static final class OtherThing {

        private final String s;

        OtherThing(String s) {
            this.s = s;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final OtherThing other = (OtherThing) obj;
            if ((this.s == null) ? (other.s != null) : !this.s.equals(other.s)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 11 * hash + (this.s != null ? this.s.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return s;
        }
    }

    private static final class D extends LookupProviderAction<FakeNode, Thing> {

        private Set<Thing> performedOn;

        D() {
            this(false);
        }

        D(boolean all) {
            super(FakeNode.class, Thing.class, all);
        }

        @Override
        protected void perform(Collection<? extends Thing> delegates) {
            this.performedOn = new HashSet<Thing>(delegates);
        }

        @Override
        protected boolean enabled(Collection<? extends Thing> targets) {
            return !targets.contains(new Thing("no"));
        }

        void assertTargets(Set<Thing> expected) {
            Set<Thing> old = performedOn;
            performedOn = null;
            assertNotNull("Expected targets " + expected + " but got " +
                    old, old);
            assertEquals("Expected targets " + expected + " but got " +
                    old, expected, old);
        }
    }

    private static final class E extends ContextAction<Thing> {

        private Set<Thing> performedOn;

        E() {
            super(Thing.class);
        }

        @Override
        protected void actionPerformed(Collection<? extends Thing> targets) {
            this.performedOn = new HashSet<Thing>(targets);
        }

        @Override
        protected boolean isEnabled(Collection<? extends Thing> targets) {
            return !targets.contains(new Thing("no"));
        }

        void assertTargets(Set<Thing> expected) {
            Set<Thing> old = performedOn;
            performedOn = null;
            assertNotNull("Expected targets " + expected + " but got " +
                    old, old);
            assertEquals("Expected targets " + expected + " but got " +
                    old, expected, old);
        }

        void assertNotFired() {
            assertNull(performedOn);
        }
    }

    private static final class F extends ContextAction<Thing> {

        F() {
            super(Thing.class);
            putValue(NAME, "Do something");
        }
        Collection<? extends Thing> targets;

        void assertTargets(Collection<? extends Thing> expected) {
            assertNotNull(targets);
            assertEquals(expected, new HashSet<Thing>(targets));
        }

        @Override
        protected void actionPerformed(Collection<? extends Thing> targets) {
            this.targets = targets;
        }

        @Override
        protected boolean isEnabled(Collection<? extends Thing> targets) {
            for (Thing t : targets) {
                if ("bad".equals(t.toString())) {
                    return false;
                }
            }
            return true;
        }

        void assertChanged() {
            boolean old = changed;
            changed = false;
            assertTrue ("Change not called", old);
        }

        boolean changed;
        @Override
        protected void change(Collection<? extends Thing> collection, Action instance) {
            changed = true;
            if (collection.size() == 0) {
                instance.putValue(NAME, "Do something");
            } else if (collection.size() == 1) {
                instance.putValue(NAME, "Do something to " + collection.iterator().next());
            } else {
                instance.putValue(NAME, "Do something to " + collection.size() + " Things");
            }
        }
    }

    private static final class G extends ContextAction<OtherThing> {

        G() {
            super(OtherThing.class);
            putValue(NAME, "Do something");
        }
        Collection<? extends OtherThing> targets;

        void assertTargets(Collection<? extends OtherThing> expected) {
            assertNotNull(targets);
            assertEquals(expected, new HashSet<OtherThing>(targets));
        }

        @Override
        protected void actionPerformed(Collection<? extends OtherThing> targets) {
            this.targets = targets;
        }

        @Override
        protected boolean isEnabled(Collection<? extends OtherThing> targets) {
            for (OtherThing t : targets) {
                if ("bad".equals(t.toString())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void change(Collection<? extends OtherThing> collection, Action instance) {
            if (collection.size() == 0) {
                instance.putValue(NAME, "Do something else ");
            } else if (collection.size() == 1) {
                instance.putValue(NAME, "Do something else to " + collection.iterator().next());
            } else {
                instance.putValue(NAME, "Do something else to " + collection.size() + " OtherThings");
            }
        }
    }
}
