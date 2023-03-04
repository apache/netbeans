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

package org.openide.explorer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Jaroslav Tulach
 */
public class ExplorerManagerTest extends NbTestCase
        implements PropertyChangeListener {
    private ExplorerManager em;
    private Keys keys;
    private Node root;
    private LinkedList<PropertyChangeEvent> events;

    static {
        ExplorerManager.SCHEDULE_REMOVE_ASYNCH = false;
    }
    
    public ExplorerManagerTest(String testName) {
        super(testName);
    }
    
    /** This code is supposed to run in AWT test.
     */
    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void setUp() throws Exception {
        em = new ExplorerManager();
        keys = new Keys();
        root = new AbstractNode(keys);
        Node[] justAsk = root.getChildren().getNodes(true);
        em.setRootContext(root);
        events = new LinkedList<PropertyChangeEvent>();
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        assertFalse("No read lock held", Children.MUTEX.isReadAccess());
        assertFalse("No write lock held", Children.MUTEX.isWriteAccess());
        
        events.add(ev);
    }
    
    public void testScalingSelectionChange() throws Exception {
        
        int [] sizes = new int[] { 10, 100, 1000, 10000 };
        for (int i = 0; i<sizes.length; i++) {
            int count = computeEqualsCount(sizes[i]);
            assertTrue("n*log(n) complexity of selection change", 
                     count < sizes[i] * Math.log(sizes[i]));
        }
    }
    
    private int computeEqualsCount(int size) throws Exception {
        Integer [] arr = new Integer[size];
        for (int i = 0; i<arr.length; i++) {
            arr[i] = i;
        }
        ExplorerManager myem = new ExplorerManager();
        IntKeys mykeys = new IntKeys();
        mykeys.keys(arr);
        Node myroot = new AbstractNode(mykeys);
        myem.setRootContext(myroot);
        
        Node[] ch = myroot.getChildren().getNodes(true);
        
        Node[] neuCh = new Node[ch.length-1];
        System.arraycopy(ch, 0, neuCh, 0, neuCh.length);
        myem.setSelectedNodes(neuCh);
        IntKeys.eqCounter = 0;
        myem.setSelectedNodes(ch);
        myem.setSelectedNodes(neuCh);

        return IntKeys.eqCounter;
    }
    
    public void testNormalSelectionChange() throws Exception {
        final Node a = keys.key("a key");
        
        em.addPropertyChangeListener(this);
        
        em.setSelectedNodes(new Node[] { a });
        Node[] arr = em.getSelectedNodes();
        assertEquals("One selected", 1, arr.length);
        assertEquals("A is there", a, arr[0]);
        
        
        assertEquals("One event", 1, events.size());
        PropertyChangeEvent ev = (PropertyChangeEvent)events.removeFirst();
        assertEquals("Name is good", ExplorerManager.PROP_SELECTED_NODES, ev.getPropertyName());

        events.clear();
        em.setSelectedNodes(new Node[] { a });
        assertEquals("No change: " + events, 0, events.size());
    }
    
    public void testCannotSetNodesNotUnderTheRoot() throws Exception {
        final Node a = new AbstractNode(Children.LEAF);
        em.setSelectedNodes(new Node[]{a});
        assertEquals(0, em.getSelectedNodes().length);
    }
    
    
    public void testSetNodesSurviveChangeOfNodes() throws Exception {
        final Node a = keys.key("toRemove");
        
        class ChangeTheSelectionInMiddleOfMethod implements VetoableChangeListener {
            public int cnt;
            
            public void vetoableChange(PropertyChangeEvent evt) {
                cnt++;
                keys.keys(new String[0]);
            }
        }
        
        ChangeTheSelectionInMiddleOfMethod list = new ChangeTheSelectionInMiddleOfMethod();
        em.addVetoableChangeListener(list);
        
        em.setSelectedNodes(new Node[] { a });
        
        assertEquals("Vetoable listener called", 1, list.cnt);
        assertEquals("Node is dead", null, a.getParentNode());
        
        // handling of removed nodes is done asynchronously
        em.waitFinished();
        
        Node[] arr = em.getSelectedNodes();
        assertEquals("No nodes can be selected", 0, arr.length);
    }
    
    public void testCannotVetoSetToEmptySelection() throws Exception {
        final Node a = keys.key("toRemove");
        
        em.setSelectedNodes(new Node[] { a });
        
        class NeverCalledVeto implements VetoableChangeListener {
            public int cnt;
            
            public void vetoableChange(PropertyChangeEvent evt) {
                cnt++;
                keys.keys(new String[0]);
            }
        }
        
        NeverCalledVeto list = new NeverCalledVeto();
        em.addVetoableChangeListener(list);
        
        em.setSelectedNodes(new Node[0]);
        
        assertEquals("Veto not called", 0, list.cnt);
        Node[] arr = em.getSelectedNodes();
        assertEquals("No nodes can be selected", 0, arr.length);
    }

    @RandomlyFails // NB-Core-Build #1110
    public void testGarbageCollectOfExploreredContextIssue124712() throws Exception {
        class K extends Children.Keys<String> {
            public void keys(String... keys) {
                setKeys(keys);
            }
            
            @Override
            protected Node[] createNodes(String key) {
                AbstractNode an = new AbstractNode(new K());
                an.setName(key);
                return new Node[] { an };
            }
        }
        
        K myKeys = new K();
        myKeys.keys("a", "b", "c");
        AbstractNode myRoot = new AbstractNode(myKeys);
        myRoot.setName("root");
        em.setRootContext(myRoot);
        
        Node b = myRoot.getChildren().getNodes()[1];
        assertEquals("b", b.getDisplayName());
        ((K)b.getChildren()).keys("1", "2", "3");
        
        em.setExploredContext(b);
        em.setSelectedNodes(new Node[] { b.getChildren().getNodes()[2] });
        
        Reference<?> refB = new WeakReference<Object>(b);
        Reference<?> ref1 = new WeakReference<Object>(b.getChildren().getNodes()[0]);
        Reference<?> ref2 = new WeakReference<Object>(b.getChildren().getNodes()[1]);
        Reference<?> ref3 = new WeakReference<Object>(b.getChildren().getNodes()[2]);
        
        myKeys.keys();
        b = null;
        
        ref = em;
        em.waitFinished();
        
        assertEquals("Explored context is the root context", myRoot, em.getExploredContext());
        assertEquals("No selected nodes", 0, em.getSelectedNodes().length);
        
        assertGC("1", ref1);
        assertGC("2", ref2);
        assertGC("3", ref3);
        assertGC("b", refB);
    }
    
    public void testGarbageCollectOfDeepExploreredContextIssue124712() throws Exception {
        class K extends Children.Keys<String> {
            public void keys(String... keys) {
                setKeys(keys);
            }
            
            @Override
            protected Node[] createNodes(String key) {
                AbstractNode an = new AbstractNode(new K());
                an.setName(key);
                return new Node[] { an };
            }
        }
        
        K myKeys = new K();
        myKeys.keys("a", "b", "c");
        AbstractNode myRoot = new AbstractNode(myKeys);
        myRoot.setName("root");
        em.setRootContext(myRoot);
        
        Node mezi = myRoot.getChildren().getNodes()[1];
        ((K)mezi.getChildren()).keys("a", "b", "c");
        
        Node b = mezi.getChildren().getNodes()[1];
        assertEquals("b", b.getDisplayName());
        ((K)b.getChildren()).keys("1", "2", "3");
        
        em.setExploredContext(b);
        
        Reference<?> refB = new WeakReference<Object>(b);
        Reference<?> ref1 = new WeakReference<Object>(b.getChildren().getNodes()[0]);
        Reference<?> ref2 = new WeakReference<Object>(b.getChildren().getNodes()[1]);
        Reference<?> ref3 = new WeakReference<Object>(b.getChildren().getNodes()[2]);
        
        myKeys.keys();
        b = null;
        
        ref = em;
        em.waitFinished();
        
        assertEquals("Explored context is the root context", myRoot, em.getExploredContext());
        assertEquals("No selected nodes", 0, em.getSelectedNodes().length);
        
        assertGC("1", ref1);
        assertGC("2", ref2);
        assertGC("3", ref3);
        assertGC("b", refB);
    }
    public void testGarbageCollectWithStrangeSelectionIssue124712() throws Exception {
        class K extends Children.Keys<String> {
            public void keys(String... keys) {
                setKeys(keys);
            }
            
            @Override
            protected Node[] createNodes(String key) {
                AbstractNode an = new AbstractNode(new K());
                an.setName(key);
                return new Node[] { an };
            }
        }
        
        K myKeys = new K();
        myKeys.keys("a", "b", "c");
        AbstractNode myRoot = new AbstractNode(myKeys);
        myRoot.setName("root");
        em.setRootContext(myRoot);
        
        Node mezi = myRoot.getChildren().getNodes()[1];
        ((K)mezi.getChildren()).keys("a", "b", "c");
        
        Node b = mezi.getChildren().getNodes()[1];
        assertEquals("b", b.getDisplayName());
        ((K)b.getChildren()).keys("1", "2", "3");
        
        em.setExploredContext(b);
        em.setSelectedNodes(new Node[] { mezi });
        em.setSelectedNodes(new Node[0]);
        
        em.waitFinished();
        
        Reference<?> refB = new WeakReference<Object>(b);
        Reference<?> ref1 = new WeakReference<Object>(b.getChildren().getNodes()[0]);
        Reference<?> ref2 = new WeakReference<Object>(b.getChildren().getNodes()[1]);
        Reference<?> ref3 = new WeakReference<Object>(b.getChildren().getNodes()[2]);
        
        myKeys.keys();
        b = null;
        
        ref = em;
        em.waitFinished();
        
        assertEquals("Explored context is the root context", myRoot, em.getExploredContext());
        assertEquals("No selected nodes", 0, em.getSelectedNodes().length);
        
        assertGC("1", ref1);
        assertGC("2", ref2);
        assertGC("3", ref3);
        assertGC("b", refB);
    }

    public void testSetRootContext() throws InterruptedException {
        final int count = 1000;
        final AtomicBoolean failed = new AtomicBoolean(false);

        class SetRootContext extends Thread {

            final Node root;

            public SetRootContext(Node root) {
                this.root = root;
            }

            @Override
            public void run() {
                try {
                    for (int i = 0; i < count; i++) {
                        em.setRootContext(root);
                    }
                } catch (IllegalArgumentException e) {
                    failed.set(true);
                }
            }
        }

        Thread t1 = new SetRootContext(new AbstractNode(Children.LEAF));
        Thread t2 = new SetRootContext(new AbstractNode(Children.LEAF));
        Thread t3 = new SetRootContext(new AbstractNode(Children.LEAF));
        Thread t4 = new SetRootContext(new AbstractNode(Children.LEAF));

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        if (failed.get()) {
            fail("IAE during setRootContext()");
        }
    }

    private static Object ref;
    
    private static final class Keys extends Children.Keys<String> {
        public Node key(String k) {
            keys(new String[] { k });
            return getNodes()[0];
        }
        public void keys(String[] keys) {
            super.setKeys(keys);
        }
        protected Node[] createNodes(String o) {
            AbstractNode an = new AbstractNode(Children.LEAF);
            an.setName(o);
            return new Node[] { an };
        }
    }
    
    private static final class IntKeys extends Children.Keys<Integer> {
        static int eqCounter;

        public void keys(Integer[] keys) {
            super.setKeys(keys);
        }
        protected Node[] createNodes(Integer o) {
            AbstractNode an = new CountingNode();
            an.setName(Integer.toString(o));
            return new Node[] { an };
        }
        private static class CountingNode extends AbstractNode {
            CountingNode() {
                super (Children.LEAF);
            }
            
            @Override
            public boolean equals(Object obj) {
                eqCounter++;
                return super.equals(obj);
            }

            @Override
            public int hashCode() {
                return super.hashCode();
            }
        }
    }
}
