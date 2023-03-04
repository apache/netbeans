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

package org.openide.explorer.view;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  Marian Petras, Andrei Badea
 */
public final class TreeViewTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TreeViewTest.class);
    }

    protected @Override int timeOut() {
        return 50000;
    }
    
    private ExplorerWindow testWindow;
    private volatile boolean isScrolledDown;
    private final Object semaphore = new Object();
    private CharSequence log;
    
    public TreeViewTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        testWindow = null;
    }
    
    @Override
    protected void runTest() throws Throwable {
        VisualizerNode.LOG.setLevel(Level.FINE);
        log = Log.enable(VisualizerNode.LOG.getName(), Level.FINE);
        super.runTest();
        if (log.length() > 0 && log.toString().indexOf("Children.MUTEX") >= 0) {
            fail("something has been logged:\n" + log);
        }
    }

    /**
     * Tests whether <code>JTree</code>'s property <code>scrollsOnExpand</code>
     * is taken into account in
     * <code>TreeView.TreePropertyListener.treeExpanded(...)</code>.
     */
    @RandomlyFails // NB-Core-Build #8278: Check the view has scrolled
    public void testAutoscrollOnOff() throws InterruptedException, InvocationTargetException {
        assert !EventQueue.isDispatchThread();
        
        class Detector implements Runnable {
            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    EventQueue.invokeLater(this);
                    return;
                }
                
                isScrolledDown = !testWindow.treeView.isUp();
                
                synchronized (semaphore) {
                    semaphore.notify();
                }
            }
        }

        class Tester implements Runnable {
            private final boolean autoscroll;
            private final int part;
            Tester(boolean autoscroll, int part) {
                this.autoscroll = autoscroll;
                this.part = part;
            }
            public void run() {
                assert (part == 1) || (part == 2);
                if (part == 1) {
                    testWindow.treeView.collapse();
                    testWindow.treeView.scrollUp();
                    assert testWindow.treeView.isUp();
                } else {
                    testWindow.treeView.setAutoscroll(autoscroll);
                    testWindow.treeView.expand(); //<-- posts a request to the RequestProcessor
                    RequestProcessor.getDefault().post(new Detector(), 1000 /*ms*/);
                }
            }
        }
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                testWindow = new ExplorerWindow();
            }
        });
        testWindow.showWindow();
        EventQueue.invokeLater(new Tester(true, 1));
        Thread.sleep(2000);      //wait for update of the screen
        EventQueue.invokeLater(new Tester(true, 2));
        synchronized (semaphore) {
            semaphore.wait();
        }
        assertTrue("Check the view has scrolled", isScrolledDown);

        EventQueue.invokeLater(new Tester(false, 1));
        Thread.sleep(2000);      //wait for update of the screen
        EventQueue.invokeLater(new Tester(false, 2));
        synchronized (semaphore) {
            semaphore.wait();
        }
        assertTrue("Check the view has not scrolled", !isScrolledDown);

        EventQueue.invokeLater(new Tester(true, 1));    //just collapse the tree
        Thread.sleep(2000);
    }

    public void testExpandNodePreparedOutsideOfAWT() throws Exception {
        assertFalse(EventQueue.isDispatchThread());

        class OutOfAWT extends Keys {
            Exception noAWTAddNotify;
            Exception noAWTCreateNodes;

            public OutOfAWT(boolean lazy, String... args) {
                super(lazy, args);
            }

            @Override
            protected void addNotify() {
                if (EventQueue.isDispatchThread()) {
                    noAWTAddNotify = new Exception();
                }
                super.addNotify();
            }

            @Override
            protected Node[] createNodes(Object key) {
                if (EventQueue.isDispatchThread()) {
                    noAWTCreateNodes = new Exception();
                }
                return super.createNodes(key);
            }
        }
        AbstractNode root = new AbstractNode(new Children.Array());
        final OutOfAWT ch = new OutOfAWT(false, "A", "B", "C");
        AbstractNode an = new AbstractNode(ch);
        root.getChildren().add(new Node[] { an });
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                testWindow = new ExplorerWindow();
            }
        });
        testWindow.showWindow();
        testWindow.getExplorerManager().setRootContext(root);

        testWindow.treeView.expandNode(an);
        Thread.sleep(2000);

        if (ch.noAWTAddNotify != null) {
            throw ch.noAWTAddNotify;
        }
        if (ch.noAWTCreateNodes != null) {
            throw ch.noAWTCreateNodes;
        }
    }
    
    
    private static final class TestTreeView extends BeanTreeView {
        
        private final Node rootNode;
        final JScrollBar vertScrollBar;
        private transient ExplorerManager explManager;
        
        TestTreeView() {
            super();
            tree.setAutoscrolls(true);

            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            vertScrollBar = getVerticalScrollBar();
            
            rootNode = new AbstractNode(new TreeChildren());
            rootNode.setDisplayName("Root node");
            
            tree.setRowHeight(20);
            
            Dimension prefSize = new Dimension(200, 6 * tree.getRowHeight() + 8);
            prefSize.width = (int) (prefSize.width * 1.25f)
                             + vertScrollBar.getWidth();
            setPreferredSize(prefSize);
        }
        
        @Override
        public void addNotify() {
            super.addNotify();
            explManager = ExplorerManager.find(this);
            explManager.setRootContext(rootNode);
            collapse();
        }
        
        void setAutoscroll(boolean autoscroll) {
            tree.setScrollsOnExpand(autoscroll);
        }
        
        void scrollUp() {
            vertScrollBar.setValue(vertScrollBar.getMinimum());
        }
        
        boolean isUp() {
            return vertScrollBar.getValue()
                   == vertScrollBar.getMinimum();
        }
        
        void expand() {
            tree.expandRow(4);
        }
        
        void collapse() {
            tree.collapseRow(4);
        }
        
        static final class TreeChildren extends Children.Array {
            
            private static final char[] letters
                    = new char[] {'A', 'B', 'C', 'D', 'E'};
            
            TreeChildren() {
                this(-1);
            }
            
            TreeChildren(final int first) {
                super();
                
                Node[] childNodes = new Node[5];
                int i;
                if (first == -1) {
                    for (i = 0; i < childNodes.length; i++) {
                        AbstractNode childNode = new AbstractNode(new TreeChildren(i));
                        childNode.setDisplayName("Child node " + i);
                        childNodes[i] = childNode;
                    }
                } else {
                    for (i = 0; i < childNodes.length; i++) {
                        AbstractNode childNode = new AbstractNode(Children.LEAF);
                        StringBuffer buf = new StringBuffer(3);
                        childNode.setDisplayName(buf.append(first)
                                                    .append('.')
                                                    .append(letters[i])
                                                    .toString());
                        childNodes[i] = childNode;
                    }
                }
                add(childNodes);
            }
            
        }
        
        
    }
    
    
    private final class ExplorerWindow extends JFrame
            implements ExplorerManager.Provider, Runnable {
        
        private final ExplorerManager explManager = new ExplorerManager();
        TestTreeView treeView;
        
        ExplorerWindow() {
            super("TreeView test");                                     //NOI18N
            getContentPane().add(treeView = new TestTreeView());
        }
        
        public ExplorerManager getExplorerManager() {
            return explManager;
        }
        public void run() {
            pack();
            setVisible(true);
        }

        void waitShown() throws InterruptedException {
            while (!isShowing()) {
                Thread.sleep(100);
            }
            Thread.sleep(500);
        }

        void showWindow() {
            try {
                EventQueue.invokeAndWait(this);
                waitShown();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            }
            waitAWT();
        }
    }

    /**
     * Used as the preferred actions by the nodes below
     */
    private static class MyAction extends NodeAction {

        public boolean enable(Node[] nodes) {
            return true;
        }

        public void performAction(Node[] nodes) {
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return "My Action";
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new MyDelegateAction(actionContext);
        }
    }
    
    /**
     * Returned by MyAction.createContextAwareInstance().
     */
    private static class MyDelegateAction extends AbstractAction {
        Lookup contextLookup;
        
        public MyDelegateAction(Lookup contextLookup) {
            this.contextLookup = contextLookup;
        }
        
        public void actionPerformed(ActionEvent e) {
        }
    }

    private static class NodeWhichHasItselfInLookup extends AbstractNode {
        public NodeWhichHasItselfInLookup() {
            super(Children.LEAF);
        }

        @Override
        public Action getPreferredAction() {
            return SystemAction.get(MyAction.class);
        }
    }

    private static class NodeWhichDoesntHaveItselfInLookup extends AbstractNode {
        public NodeWhichDoesntHaveItselfInLookup() {
            super(Children.LEAF, Lookup.EMPTY);
        }

        @Override
        public Action getPreferredAction() {
            return SystemAction.get(MyAction.class);
        }
    }
    
    /**
     * Tests that the context lookup created by TreeView.takeAction() only contains
     * the node once when the node contains itself in its lookup.
     */
    public void testTakeActionNodeInLookup() {
        doTestTakeAction(new NodeWhichHasItselfInLookup());        
    }

    /**
     * Tests that the context lookup created by TreeView.takeAction() only contains
     * the node once when the node doesn't contain itself in its lookup.
     */
    public void testTakeActionNodeNotInLookup() {
        doTestTakeAction(new NodeWhichDoesntHaveItselfInLookup());
    }
    
    /**
     * Tests that the context lookup created by TreeView.takeAction() only contains
     * the node once when the node contains itself in its lookup and is filtered by a FilterNode.
     */
    public void testTakeActionNodeInLookupAndFiltered() {
        doTestTakeAction(new FilterNode(new NodeWhichHasItselfInLookup()));        
    }

    /**
     * Tests that the context lookup created by TreeView.takeAction() only contains
     * the node once when the node doesn't contain itself in its lookup
     * and is filtered by a FilterNode.
     */
    public void testTakeActionNodeNotInLookupAndFiltered() {
        doTestTakeAction(new FilterNode(new NodeWhichDoesntHaveItselfInLookup()));
    }
    
    private void doTestTakeAction(Node node) {
        // if the preferred action instanceof ContextAwareAction
        // calls its createContextAwareInstance() method
        Action a = TreeView.takeAction(node.getPreferredAction(), node);
        int count = ((MyDelegateAction)a).contextLookup.lookup(new Lookup.Template(Node.class)).allInstances().size();
        assertEquals("The context lookup created by TreeView.takeAction() should contain the node only once.", 1, count);
    }

    private static void waitAWT() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    private void setSelectedNodes(final Node[] arr) throws Exception {
        class R implements Runnable {
            Exception e;
            public void run() {
                try {
                    testWindow.getExplorerManager().setSelectedNodes(arr);
                } catch (PropertyVetoException ex) {
                    e = ex;
                }
            }
        }
        R run = new R();
        SwingUtilities.invokeAndWait(run);
        if (run.e != null) {
            throw run.e;
        }
    }
    
    public void testPreventGCOfVisibleNodesEager() throws Exception {
        doPreventGCOfVisibleNodes(false);
    }
    public void testPreventGCOfVisibleNodesLazy() throws Exception {
        doPreventGCOfVisibleNodes(true);
    }
    private void doPreventGCOfVisibleNodes(boolean lazy) throws Exception {
        assert !EventQueue.isDispatchThread();

        Keys keys = new Keys(lazy, "A", "B", "C");
        AbstractNode node = new AbstractNode(keys);
        node.setName(getName());
        AbstractNode root = node;

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                testWindow = new ExplorerWindow();
            }
        });
        testWindow.showWindow();
        testWindow.getExplorerManager().setRootContext(root);
        assertSame("Root is OK", root, testWindow.getExplorerManager().getRootContext());

        Node[] arr = node.getChildren().getNodes();
        testWindow.getExplorerManager().setExploredContext(node);
        setSelectedNodes(arr);

        waitAWT();

        Reference<Object> ref = new WeakReference<Object>(arr[2]);
        root = null;
        node = null;
        arr = null;
        setSelectedNodes(new Node[0]);

        waitAWT();
        
        try {
            EQFriendlyGC.assertGC("Cannot GC the children", ref);
        } catch (Error ex) {
            // OK
            return;
        }
        fail("Node shall not be GCed: " + ref.get());
    }

    @RandomlyFails // http://deadlock.netbeans.org/job/NB-Core-Build/9880/testReport/
    public void testNodesGCedAfterSetChildrenLazy() throws Exception {
        doTestNodesGCedAfterSetChildren(true);
    }

    @RandomlyFails // NB-Core-Build #9918: Unstable, NB-Core-Build #9919 on the same sources passed
    public void testNodesGCedAfterSetChildrenEager() throws Exception {
        doTestNodesGCedAfterSetChildren(false);
    }

    void doTestNodesGCedAfterSetChildren(boolean lazy) throws Exception {
        Keys keys = new Keys(lazy, "A", "B", "C");
        class MyNode extends AbstractNode {

            public MyNode(Children children) {
                super(children);
            }
            void callSetChildren(Children newCh) {
                setChildren(newCh);
            }
        }
        MyNode root = new MyNode(keys);
        root.setName(getName());
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                testWindow = new ExplorerWindow();
            }
        });
        testWindow.showWindow();
        testWindow.getExplorerManager().setRootContext(root);
        testWindow.getExplorerManager().setExploredContext(root);
        Node[] nodes = root.getChildren().getNodes();
        try {
            testWindow.getExplorerManager().setSelectedNodes(nodes);
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
        waitAWT();
        Reference<Object> ref = new WeakReference<Object>(nodes[2]);
        nodes = null;
        root.callSetChildren(Children.LEAF);
        waitAWT();
        EQFriendlyGC.assertGC("should gc children", ref);
    }

    @RandomlyFails // NB-Core-Build Unstable: #9954, locally passes
    public void testSetSelectedNodeIsSynchronizedEager() throws Exception {
        doSetSelectedNodeIsSynchronized(false);
    }

    public void testSetSelectedNodeIsSynchronizedLazy() throws Exception {
        doSetSelectedNodeIsSynchronized(true);
    }

    private void doSetSelectedNodeIsSynchronized(boolean lazy) throws Exception {
        assert !EventQueue.isDispatchThread();

        final Keys keys = new Keys(lazy, "A", "B", "C");
        AbstractNode node = new AbstractNode(keys);
        node.setName(getName());
        final AbstractNode root = node;

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                testWindow = new ExplorerWindow();
            }
        });
        testWindow.getExplorerManager().setRootContext(root);
        testWindow.showWindow();

        waitAWT();
        testWindow.getExplorerManager().setRootContext(root);
        assertSame("Root is OK", root, testWindow.getExplorerManager().getRootContext());

        Node[] arr = node.getChildren().getNodes();
        testWindow.getExplorerManager().setExploredContext(node);

        final AwtBlock block = new AwtBlock();
        block.block();
        
        class SetSelectedFromAwt implements Runnable {
            Throwable e = null;

            public void run() {
                Node[] arr2 = root.getChildren().getNodes();
                try {
                    testWindow.getExplorerManager().setSelectedNodes(arr2);
                } catch (Throwable ex) {
                    e = ex;
                }
            }
        }
        SetSelectedFromAwt setSel = new SetSelectedFromAwt();
        SwingUtilities.invokeLater(setSel);

        keys.keys("A", "B", "C", "D");
        block.unblock();
        waitAWT();
        assertEquals("Selection should be updated", Arrays.asList(keys.getNodes()), 
                Arrays.asList(testWindow.getExplorerManager().getSelectedNodes()));
        if (setSel.e != null) {
            fail();
        }      
    }

    public void testPartialNodeSelectionEager() throws Exception {
        doTestPartialNodeSelection(false);
    }

    @RandomlyFails // NB-Core-Build Unstable: #9953, locally passes
    public void testPartialNodeSelectionLazy() throws Exception {
        doTestPartialNodeSelection(true);
    }

    private void doTestPartialNodeSelection(boolean lazy) throws Exception {
        assert !EventQueue.isDispatchThread();

        final Keys keys = new Keys(lazy, "A", "B", "-B", "C");
        AbstractNode node = new AbstractNode(keys);
        node.setName(getName());
        final AbstractNode root = node;

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                testWindow = new ExplorerWindow();
            }
        });
        testWindow.getExplorerManager().setRootContext(root);
        testWindow.showWindow();
        testWindow.getExplorerManager().setRootContext(root);
        assertSame("Root is OK", root, testWindow.getExplorerManager().getRootContext());

        Node[] arr = node.getChildren().getNodes();
        testWindow.getExplorerManager().setExploredContext(node);

        final CountDownLatch block1 = new CountDownLatch(1);
        final CountDownLatch block2 = new CountDownLatch(1);
        final AtomicBoolean exc = new AtomicBoolean(false);
        
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Node[] arr2 = root.getChildren().getNodes();
                block1.countDown();
                try {
                    block2.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                try {
                    testWindow.getExplorerManager().setSelectedNodes(arr2);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    exc.set(true);
                }
            }
        });
        
        block1.await();
        keys.keys("B", "D");
        block2.countDown();
        waitAWT();
        
        assertEquals("B should be selected", Arrays.asList(keys.getNodes()[0]), 
                Arrays.asList(testWindow.getExplorerManager().getSelectedNodes()));        
        if (exc.get()) {
            fail();
        }
    }
    
    class AwtBlock implements Runnable {

        public synchronized void run() {
            notify();
            try {
                wait();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        synchronized void block() {
            SwingUtilities.invokeLater(this);
            try {
                wait();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        synchronized void unblock() {
            notifyAll();
        }
    }
    
    class Block {
        synchronized void block() {
            try {
                wait();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        synchronized void unblock() {
            notifyAll();
        }
    }

    /** Sample keys.
    */
    public static class Keys extends Children.Keys {
        /** Constructor.
         */
        public Keys (boolean lazy, String... args) {
            super(lazy);
            if (args != null && args.length > 0) {
                setKeys (args);
            }
        }

        /** Changes the keys.
         */
        public void keys (String... args) {
            super.setKeys (args);
        }

        /** Create nodes for a given key.
         * @param key the key
         * @return child nodes for this key or null if there should be no
         *   nodes for this key
         */
        protected Node[] createNodes(Object key) {
            if (key.toString().startsWith("-")) {
                return null;
            }
            AbstractNode an = new AbstractNode (Children.LEAF);
            an.setName (key.toString ());

            return new Node[] { an };
        }

    }
    
}
