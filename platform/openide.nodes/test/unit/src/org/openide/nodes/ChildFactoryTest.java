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
package org.openide.nodes;

import java.awt.EventQueue;
import java.beans.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.ChildFactory.Detachable;
import org.openide.util.NbBundle;

/** Test for AsynchChildren, ChildFactory and SynchChildren.
 *
 * @author Tim Boudreau
 */
public class ChildFactoryTest extends NbTestCase {
    private Logger LOG;

    
    public ChildFactoryTest(String name) {
        super(name);
        LOG = Logger.getLogger(ChildFactoryTest.class.getName() + name);
    }
    
    private ProviderImpl factory;
    
    private BatchProviderImpl factory2;
    private AsynchChildren kids2;
    private Node node2;
    private AsynchChildren kids;
    private Node node;
    protected @Override void setUp() throws Exception {
        factory = new ProviderImpl();
        kids = new AsynchChildren<String>(factory);
        factory.setObserver(kids);
        node = new AbstractNode(kids);
        
        factory2 = new BatchProviderImpl();
        kids2 = new AsynchChildren<String>(factory2);
        factory2.setObserver(kids2);
        node2 = new AbstractNode(kids2);
    }
    
    public void testChildrenCreate() {
        ChildFactory<?> f = new ProviderImpl();
        Children kids = Children.create(f, true);
        assertTrue(kids instanceof AsynchChildren);
        
        ChildFactory<?> ff = new ProviderImpl();
        Children kids2 = Children.create(ff, false);
        assertFalse(kids2 instanceof AsynchChildren);
        assertTrue(kids2 instanceof SynchChildren);
        
        RuntimeException e = null;
        Children kids3 = null;
        try {
            kids3 = Children.create(ff, true);
        } catch (RuntimeException ex) {
            e = ex;
        }
        assertNull(kids3);
        assertNotNull("Exception should have been thrown creating two " +
                "Children objects over the same ChildFactory", e);
    }
    
    //A word of caution re adding tests:
    //Almost anything (getNodes(), justCreateNodes(), etc. can trigger a
    //fresh call to Children.addNotify().  Any test that expects a synchronous
    //change in the child nodes as a result of having triggered a call
    //to setKeys() is probably testing a race condition, not the behavior
    //of the children implementation
    
    public void testGetNodesWaits() throws Exception {
        factory.wait = false;
        kids.getNodes(false);
        synchronized (factory.lock) {
            factory.lock.wait(300);
        }
        Thread.yield();
        new NL(node);
        Node[] n = kids.getNodes(true);
        assertEquals(4, n.length);
    }

    public void testFindChildWaits() throws Exception {
        factory.wait = false;
        Node n = kids.findChild("D");
        assertNotNull(n);
    }

    public void testGetNodesWaitsFirstTime() {
        factory.wait = false;
        Node[] n = kids.getNodes(true);
        assertEquals(4, n.length);
    }

    public void testInitialNodeIsWaitNode() throws Exception {
        factory.wait = true;
        kids.addNotify();
        Node[] n = kids.getNodes(false);
        factory.wait = false;
        assertEquals(1, n.length);
        assertEquals(NbBundle.getMessage(AsynchChildren.class, "LBL_WAIT"),
                n[0].getDisplayName());
        assertEquals(Collections.emptyList(), Arrays.asList(n[0].getPropertySets()));
        assertEquals(Collections.emptyList(), Arrays.asList(n[0].getActions(true)));
        factory.wait = false;
        synchronized (factory) {
            factory.wait(2000);
        }
        for (int i = 0; i < 5 && n.length != 4; i++) {
            n = kids.getNodes(true);
            Thread.yield();
        }
        assertEquals(4, n.length);
    }
    
    public void testBatch() throws Exception {
        kids2.addNotify();
        Thread.yield();
        synchronized (factory2.lock) {
            factory2.lock.notifyAll();
        }
        new NL(node2);
        Node[] n = n = kids2.getNodes(true);
        assertEquals(4, n.length);
        assertEquals(2, factory2.callCount);
    }
    
    public void testSynchChildren() throws Exception {
        final SynchProviderImpl factory = new SynchProviderImpl();
        final Children ch = Children.create(factory, false);
        assertTrue(ch instanceof SynchChildren);
        factory.assertCreateKeysNotCalled();
        factory.assertCreateNodesForKeyNotCalled();
        final Node nd = new AbstractNode(ch);
        NodeAdapter adap = new NodeAdapter() {};
        nd.addNodeListener(adap);
        
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                ch.getNodes(true);
            }
        });
        ((SynchChildren) ch).active = true;
        synchronized (factory) {
            factory.wait(1000);
        }
        factory.assertCreateKeysCalled();
        factory.assertCreateNodesForKeyCalled();
        Node[] nodes = nd.getChildren().getNodes(true);
        assertEquals(SynchProviderImpl.CONTENTS1.size(), nodes.length);
        int ix = 0;
        for (String s : SynchProviderImpl.CONTENTS1) {
            assertEquals(s, nodes[ix].getName());
            ix++;
        }
        factory.switchChildren();
        nodes = nd.getChildren().getNodes(true);
        assertEquals(SynchProviderImpl.CONTENTS2.size(), nodes.length);
        ix = 0;
        for (String s : factory.CONTENTS2) {
            assertEquals(s, nodes[ix].getName());
            ix++;
        }
    }

    public void testCancel() throws Exception {
        Thread.interrupted();
        factory.wait = true;
        kids.addNotify();
        Thread.yield();
        synchronized (factory.lock) {
            factory.lock.wait(500);
        }
        kids.removeNotify();
        factory.wait = false;
        synchronized (factory) {
            factory.wait(2000);
        }
        assertTrue(kids.cancelled);
        assertTrue(factory.cancelled);
    }

    public void testAddRemoveNotifySynch() throws Exception {
        DetachableImpl r = new DetachableImpl();
        Children ch = Children.create(r, false);
        new AbstractNode (ch);
        ch.addNotify();
        r.assertAdded();
        ch.removeNotify();
        r.assertRemoved();
        r = new DetachableImpl();
        ch = Children.create(r, false);
        Node[] n = ch.getNodes(true);
        assertEquals (2, n.length);
        assertEquals ("foo", n[0].getDisplayName());
        assertEquals ("bar", n[1].getDisplayName());
        ch.removeNotify();
        r.assertRemoved();
    }

    public void testAddRemoveNotifyAsynch() throws Exception {
        DetachableImpl r = new DetachableImpl();
        Children ch = Children.create(r, true);
        new AbstractNode (ch);
        ch.addNotify();
        synchronized(r) {
            r.wait(1000);
        }
        r.assertAdded();
        Node[] n = ch.getNodes(true);
        assertEquals (2, n.length);
        assertEquals ("foo", n[0].getDisplayName());
        assertEquals ("bar", n[1].getDisplayName());
        ch.removeNotify();
        synchronized(r) {
            r.wait(1000);
        }
        r.assertRemoved();
    }

    public void testGetNodesCount() throws Exception {
        DetachableImpl r = new DetachableImpl();
        Children ch = Children.create(r, true);
        Node n = new AbstractNode(ch);
        assertEquals(2, n.getChildren().getNodesCount(true));
        //Also test that return value is correct for unattached children
        //(should wait for background thread to complete)
        r = new DetachableImpl();
        ch = Children.create(r, true);
        assertEquals(2, ch.getNodesCount(true));
        BatchProviderImpl b = new BatchProviderImpl();
        ch = Children.create(b, true);
        assertEquals(4, ch.getNodesCount(true));
    }
    
    public void testDestroyNodesSynch() throws Exception {
        DestroyableImpl r = new DestroyableImpl();
        Children ch = Children.create(r, false);
        new AbstractNode (ch);
        Node[] n = ch.getNodes(true);
        assertEquals (2, n.length);
        assertEquals ("foo", n[0].getDisplayName());
        assertEquals ("bar", n[1].getDisplayName());
        r.refresh(true);
        n = ch.getNodes(true);
        assertEquals (0, n.length);
        Set<Node> destroyed = r.getDestroyed();
        Set<String> expected = new HashSet<String>();
        Collections.addAll(expected, "foo", "bar");
        for (Node node : destroyed) {
            assertTrue(node.getDisplayName(), expected.contains(node.getDisplayName()));
        }
    }

    public void testDestroyNodesAsynch() throws Exception {
        DestroyableImpl r = new DestroyableImpl();
        Children ch = Children.create(r, true);
        new AbstractNode (ch);
        Node[] n = ch.getNodes(true);
        assertEquals (2, n.length);
        assertEquals ("foo", n[0].getDisplayName());
        assertEquals ("bar", n[1].getDisplayName());
        r.refresh(false);
        synchronized(r) {
            r.wait(1000);
        }
        n = ch.getNodes(true);
        assertEquals (0, n.length);
        Set<Node> destroyed = r.getDestroyed();
        Set<String> expected = new HashSet<String>();
        Collections.addAll(expected, r.createWaitNode().getDisplayName(), "foo", "bar");
        for (Node node : destroyed) {
            assertTrue(node.getDisplayName(), expected.contains(node.getDisplayName()));
        }
    }

    public void testIncrementalDisplay() throws Exception { // #206556
        final Semaphore s1 = new Semaphore(0);
        final Semaphore s2 = new Semaphore(0);
        Children c = Children.create(new ChildFactory<Integer>() {
            @Override protected boolean createKeys(List<Integer> keys) {
                try {
                    for (int i = 1; i <= 5; i++) {
                        s1.acquire();
                        keys.add(i);
                        s2.release();
                    }
                    s1.acquire();
                } catch (InterruptedException x) {
                    assert false : x;
                }
                s2.release();
                return true;
            }
            @Override protected Node createNodeForKey(Integer key) {
                Node n = new AbstractNode(Children.LEAF);
                n.setName(key.toString());
                return n;
            }
        }, true);
        assertEquals(1, c.getNodesCount(false));
        for (int i = 1; i <= 5; i++) {
            s1.release();
            s2.acquire();
            assertEquals(i + /* wait node */1, c.getNodesCount(false));
        }
        s1.release();
        s2.acquire();
        assertEquals(5, c.getNodesCount(true));
    }

    public void testBatchNodeRecreation() throws Exception { // #211847
        final List<Integer> nodesCreated = new ArrayList<Integer>();
        final AtomicInteger size = new AtomicInteger(3);
        class F extends ChildFactory<Integer> {
            @Override protected boolean createKeys(List<Integer> keys) {
                List<Integer> arr = new ArrayList<Integer>();
                for (int i = 1; i <= size.get(); i++) {
                    arr.add(i);
                }
                keys.addAll(arr);
                return true;
            }
            @Override protected Node createNodeForKey(Integer key) {
                nodesCreated.add(key);
                Node n = new AbstractNode(Children.LEAF);
                n.setName(key.toString());
                return n;
            }
            void refresh() {
                refresh(false);
            }
        }
        F f = new F();
        Children c = Children.create(f, true);
        Node root = new AbstractNode(c);
        
        // must keep reference to nodes => each node keeps ref to ChildrenArray (its parent ChildrenArray)
        // so it cannot be GCed
        Node[] nodes = root.getChildren().getNodes(true);
        assertEquals(3, nodes.length);
        assertEquals("[1, 2, 3]", nodesCreated.toString());
        LOG.info("Three elements in there!");
        size.set(4);
        f.refresh();
        LOG.info("After refresh");
        nodes = root.getChildren().getNodes(true);
        assertEquals(4, nodes.length);
        assertEquals("[1, 2, 3, 4]", nodesCreated.toString());
    }
    
    public void testIncrementalNodeRecreation() throws Exception { // #211847
        final List<Integer> nodesCreated = new ArrayList<Integer>();
        final AtomicInteger size = new AtomicInteger(3);
        class F extends ChildFactory<Integer> {
            @Override protected boolean createKeys(List<Integer> keys) {
                for (int i = 1; i <= size.get(); i++) {
                    keys.add(i);
                }
                return true;
            }
            @Override protected Node createNodeForKey(Integer key) {
                nodesCreated.add(key);
                Node n = new AbstractNode(Children.LEAF);
                n.setName(key.toString());
                return n;
            }
            void refresh() {
                refresh(false);
            }
        }
        F f = new F();
        Children c = Children.create(f, true);
        Node root = new AbstractNode(c);
        
        assertEquals(3, root.getChildren().getNodes(true).length);
        assertEquals("[1, 2, 3]", nodesCreated.toString());
        LOG.info("Three elements in there!");
        size.set(4);
        f.refresh();
        LOG.info("After refresh");
        assertEquals(4, root.getChildren().getNodes(true).length);
        assertEquals("[1, 2, 3, 4]", nodesCreated.toString());
    }
    
    /**
     * Checks that if the same (equal) keys are added for the 2nd time, the nodes
     * are not re-created. 
     * @throws Exception 
     */
    public void testIncrementalRefreshSameNodes() throws Exception {
        // negative key will not be mapped to a Node.
        List<Integer> nodeKeys = Arrays.asList(1, 2, -1, 3, -2, 4, 5);
        class F extends ChildFactory<Integer> {
            Semaphore sem = new Semaphore(0);
            
            @Override
            protected boolean createKeys(List<Integer> toPopulate) {
                for (Integer i : nodeKeys) {
                    toPopulate.add(i);
                }
                sem.release();
                return true;
            }

            @Override protected Node createNodeForKey(Integer key) {
                if (key < 0) {
                    return null;
                }
                Node n = new AbstractNode(Children.LEAF);
                n.setName(key.toString());
                return n;
            }
            void refresh() {
                refresh(false);
            }
        }
        
        F f = new F();
        
        Children c = Children.create(f, true);
        Node[] first = c.getNodes(true);
        f.sem.acquire();
        f.sem.drainPermits();
        
        f.refresh();
        Node[] second = c.getNodes();
        // needed so that all keys are refreshed, the above getNodes() has triggered
        // createKeys() in async thread.
        f.sem.acquire();
        // get the result.
        second = c.getNodes();
        
        assertEquals(first.length, second.length);
        // since keys did not change the nodes themselves should not change, too.
        for (int i = 0; i < first.length; i++) {
            assertSame(first[i], second[i]);
        }
    }

    public void testIncrementalNodeRemoval() throws Exception { // #211847
        final List<Integer> nodesCreated = new ArrayList<Integer>();
        final AtomicInteger size = new AtomicInteger(3);
        class F extends ChildFactory<Integer> {
            @Override protected boolean createKeys(List<Integer> keys) {
                for (int i = 1; i <= size.get(); i++) {
                    keys.add(i);
                }
                return true;
            }
            @Override protected Node createNodeForKey(Integer key) {
                nodesCreated.add(key);
                Node n = new AbstractNode(Children.LEAF);
                n.setName(key.toString());
                return n;
            }
            void refresh() {
                refresh(false);
            }
        }
        F f = new F();
        Children c = Children.create(f, true);
        Node root = new AbstractNode(c);
        
        assertEquals(3, root.getChildren().getNodes(true).length);
        assertEquals("[1, 2, 3]", nodesCreated.toString());
        LOG.info("Three elements in there!");
        size.set(2);
        f.refresh();
        LOG.info("After refresh");
        assertEquals(2, root.getChildren().getNodes(true).length);
        assertEquals("No new nodes created", "[1, 2, 3]", nodesCreated.toString());
    }
    
    static final class ProviderImpl extends ChildFactory <String> {
        final Object lock = new Object();
        volatile boolean wait = false;
        
        public @Override Node[] createNodesForKey(String key) {
            AbstractNode nd = new AbstractNode(Children.LEAF);
            nd.setDisplayName(key);
            nd.setName(key);
            return new Node[] { nd };
        }
        
        boolean cancelled = false;
        public boolean createKeys(List <String> result) {
            try {
                while (wait) {
                    Thread.yield();
                }
                synchronized (lock) {
                    lock.notifyAll();
                }
                if (Thread.interrupted()) {
                    cancelled = true;
                    return true;
                }
                result.add("A");
                result.add("B");
                result.add("C");
                result.add("D");
                if (Thread.interrupted()) {
                    cancelled = true;
                }
                return true;
            } finally {
                synchronized (this) {
                    notifyAll();
                }
            }
        }
    }
    
    static final class BatchProviderImpl extends ChildFactory <String> {
        boolean firstCycle = true;
        
        public @Override Node[] createNodesForKey(String key) {
            AbstractNode nd = new AbstractNode(Children.LEAF);
            nd.setDisplayName(key);
            return new Node[] { nd };
        }
        
        final Object lock = new Object();
        int callCount = 0;
        public boolean createKeys(List <String> result) {
            callCount++;
            synchronized (lock) {
                try {
                    lock.wait(500);
                } catch (InterruptedException ex) {
                    //re-interrupt
                    Thread.currentThread().interrupt();
                }
            }
            if (Thread.interrupted()) {
                return true;
            }
            boolean wasFirstCycle = firstCycle;
            if (wasFirstCycle) {
                result.add("A");
                result.add("B");
                firstCycle = false;
                return false;
            } else {
                result.add("C");
                result.add("D");
            }
            if (Thread.interrupted()) {
                return true;
            }
            synchronized (this) {
                notifyAll();
            }
            return true;
        }
    }

    public static void assertNodeAndEvent(final NodeEvent ev, final List<Node> snapshot) {
        Children.MUTEX.readAccess(new Runnable() {
            public void run() {
                int cnt = snapshot.size();
                assertEquals("Same number of nodes", ev.getNode().getChildren().getNodesCount(), cnt);
                for (int i = 0; i < cnt; i++) {
                    Node fromEv = snapshot.get(i);
                    if (fromEv instanceof EntrySupportLazy.DummyNode) {
                        continue;
                    }
                    Node fromCh = ev.getNode().getChildren().getNodeAt(i);
                    assertSame("The nodes are same at " + i, fromCh, fromEv);
                }
            }
        });
    }
    
    private static final class NL implements NodeListener {
        NL(Node n) {
            n.addNodeListener(this);
            try {
                waitFor();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                throw new Error(e);
            }
        }
        
        NL() {
            
        }

        
        public void childrenAdded(NodeMemberEvent ev) {
            assertNodeAndEvent(ev, ev.getSnapshot());
            go();
        }
        
        public void childrenRemoved(NodeMemberEvent ev) {
            assertNodeAndEvent(ev, ev.getSnapshot());
            go();
        }
        
        public void childrenReordered(NodeReorderEvent ev) {
            assertNodeAndEvent(ev, ev.getSnapshot());
            go();
        }
        
        public void nodeDestroyed(NodeEvent ev) {
            assertNodeAndEvent(ev, Collections.<Node>emptyList());
        }
        
        public void propertyChange(PropertyChangeEvent arg0) {
        }
        
        private void go() {
            synchronized (this) {
                notifyAll();
            }
        }
        
        void waitFor() throws Exception {
            synchronized (this) {
                wait(1000);
            }
        }
    }
    
    private static final class SynchProviderImpl extends ChildFactory <String> {
        static List <String> CONTENTS1 = Arrays.<String>asList(new String[] {
            "One", "Two", "Three", "Four"
        });
        static List <String> CONTENTS2 = Arrays.<String>asList(new String[] {
            "Five", "Six", "Seven", "Eight", "Nine"
        });
        
        boolean createNodesForKeyCalled = false;
        public @Override Node[] createNodesForKey(String key) {
            createNodesForKeyCalled = true;
            Node result = new AbstractNode(Children.LEAF);
            result.setDisplayName(key);
            result.setName(key);
            return new Node[] { result };
        }
        
        boolean createKeysCalled = false;
        public boolean createKeys(List <String> toPopulate) {
            createKeysCalled = true;
            List <String> l = switched ? CONTENTS2 : CONTENTS1;
            toPopulate.addAll(l);
            return true;
        }
        
        void assertCreateNodesForKeyNotCalled() {
            assertFalse(createNodesForKeyCalled);
        }
        
        void assertCreateKeysNotCalled() {
            assertFalse(createKeysCalled);
        }
        
        boolean assertCreateNodesForKeyCalled() {
            boolean result = createNodesForKeyCalled;
            createNodesForKeyCalled = false;
            assertTrue(result);
            return result;
        }
        
        boolean assertCreateKeysCalled() {
            boolean result = createKeysCalled;
            createKeysCalled = false;
            assertTrue(result);
            return result;
        }
        
        volatile boolean switched = false;
        void switchChildren() {
            switched = !switched;
            refresh(true);
        }
    }

    private static final class DetachableImpl extends Detachable<String> {
        boolean removed;
        boolean added;

        @Override
        protected boolean createKeys(List<String> toPopulate) {
            toPopulate.add("foo");
            toPopulate.add("bar");
            synchronized(this) {
                notifyAll();
            }
            return true;
        }

        @Override
        protected void removeNotify() {
            assertFalse (removed);
            synchronized(this) {
                notifyAll();
            }
            removed = true;
            added = false;
        }

        @Override
        protected Node createNodeForKey(String key) {
            AbstractNode nd = new AbstractNode(Children.LEAF);
            nd.setDisplayName(key);
            return nd;
        }

        @Override
        protected void addNotify() {
            assertFalse (added);
            added = true;
        }

        void assertAdded() {
            assertTrue (added);
        }

        void assertRemoved() {
            assertTrue (removed);
        }
    }

    private static final class DestroyableImpl extends DestroyableNodesFactory<String> {

        private boolean empty;

        private final Set<Node> destroyed = Collections.synchronizedSet(new HashSet<Node>());

        @Override
        protected boolean createKeys(List<String> toPopulate) {
            if (empty) {
                return true;
            }

            toPopulate.add("foo");
            toPopulate.add("bar");
            synchronized (this) {
                notifyAll();
            }
            empty = true;
            return true;
        }

        @Override
        protected Node createNodeForKey(String key) {
            AbstractNode nd = new AbstractNode(Children.LEAF);
            nd.setDisplayName(key);
            return nd;
        }

        @Override
        protected void destroyNodes(Node[] arr) {
            synchronized (destroyed) {
                Collections.addAll(destroyed, arr);
            }
        }

        public Set<Node> getDestroyed() {
            synchronized (destroyed) {
                return new HashSet<Node>(destroyed);
            }
        }
    }
}
