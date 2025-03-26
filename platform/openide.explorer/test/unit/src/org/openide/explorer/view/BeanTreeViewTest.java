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

package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 * Tests for class BeanTreeViewTest
 */
public class BeanTreeViewTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(BeanTreeViewTest.class);
    }

    private static final int NO_OF_NODES = 3;
    static {
        System.setProperty("netbeans.debug.heap", "no wait");
    }
    
    
    public BeanTreeViewTest(String name) {
        super(name);
    }

    public void testOnlyChildRemoveCausesSelectionOfParent() throws Throwable {
        ExplorerManager em = doChildRemovalTest("one", "one");
        final List<Node> arr = Arrays.asList(em.getSelectedNodes());
        assertEquals("One selected: " + arr, 1, arr.size());
        assertEquals("Root selected", em.getRootContext(), arr.get(0));
    }
    
    public void testFirstChildRemovalCausesSelectionOfSibling() throws Throwable {
        doChildRemovalTest("foo");
    }
    public void testSecondChildRemovalCausesSelectionOfSibling() throws Throwable {
        doChildRemovalTest("bar");
    }
    public void testThirdChildRemovalCausesSelectionOfSibling() throws Throwable {
        doChildRemovalTest("bla");
    }

    private static Object holder;
    
    private void doChildRemovalTest(final String name) throws Throwable {
        doChildRemovalTest(name, "foo", "bar", "bla");
    }
    private ExplorerManager doChildRemovalTest(final String name, final String... childrenNames) throws Throwable {

        class AWTTst implements Runnable {
            AbstractNode root = new AbstractNode(new Children.Array());
            Node[] children;
            {
                List<Node> arr = new ArrayList<Node>();
                for (String s : childrenNames) {
                    arr.add(createLeaf(s));
                }
                children = arr.toArray(new Node[0]);
            }
            Panel p;
            BeanTreeView btv;
            JFrame f;
            JTree tree;
            Node operateOn;

            private void initUI() {
                if (p == null) {
                    p = new Panel();
                    btv = new BeanTreeView();
                    f = new JFrame();
                    tree = btv.tree;
                    root.setName("test root");
                    root.getChildren().add(children);
                    p.getExplorerManager().setRootContext(root);
                    p.add(BorderLayout.CENTER, btv);
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.getContentPane().add(BorderLayout.CENTER, p);
                    f.setVisible(true);
                }
            }

            @Override
            public void run() {

                initUI();

                for (int i = 0;; i++) {
                    if (name.equals(children[i].getName())) {
                        // this should select a sibling of the removed node
                        operateOn = children[i];
                        break;
                    }
                }

                try {
                    p.getExplorerManager().setSelectedNodes(new Node[]{operateOn});
                } catch (PropertyVetoException e) {
                    fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                }

                TreePath[] paths = tree.getSelectionPaths();
                assertNotNull("Before removal: one node should be selected, but there are none.", paths);
                assertEquals("Before removal: one node should be selected, but there are none.", 1, paths.length);
                assertEquals("Before removal: one node should be selected, but there are none.", operateOn, Visualizer.findNode(paths[0].getLastPathComponent()));
                assertEquals("Before removal: one node should be selected, but there are none.", operateOn, Visualizer.findNode(tree.getAnchorSelectionPath().getLastPathComponent()));

                // this should select a sibling of the removed node
                root.getChildren().remove(new Node[]{operateOn});
                assertNotNull("After removal: one node should be selected, but there are none.", tree.getSelectionPath());
                children = null;
            }

            public void tryGc() {
                WeakReference<Node> wref = new WeakReference<Node>(operateOn);
                operateOn = null;
                assertGC("Node should be released.", wref);    
            }
        }
        AWTTst awt = new AWTTst();
        holder = awt;
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        awt.tryGc();
        return awt.p.getExplorerManager();
    }
    
    public void testOnlyChildDestroyedCausesSelectionOfParent () throws Throwable {
        // node.destroy called on the last selected node of the root node
        ExplorerManager em = doChildDestroyTest ("one", Collections.singleton("one"), "one");
        final List<Node> arr = Arrays.asList(em.getSelectedNodes());
        assertEquals("One selected: " + arr, 1, arr.size());
        assertEquals("Root selected", em.getRootContext(), arr.get(0));
    }
    
    public void testChildDestroyedMoreInSelection () throws Throwable {
        // two children in selection, should not select parent
        // the second child should be selected in the end
        ExplorerManager em = doChildDestroyTest ("one", Arrays.asList("one", "two"), "one", "two");
        final List<Node> arr = Arrays.asList(em.getSelectedNodes());
        assertEquals("One node selected: " + arr, 1, arr.size());
        assertEquals("second child selected", "two", arr.get(0).getName());
    }
    
    public void testChildDestroyedCausesNextChildSelected () throws Throwable {
        // first selected child is destroyed => selection should move to the next child
        ExplorerManager em = doChildDestroyTest ("one", Collections.singleton("one"), "one", "two");
        final List<Node> arr = Arrays.asList(em.getSelectedNodes());
        assertEquals("One selected: " + arr, 1, arr.size());
        assertEquals("second child selected", "two", arr.get(0).getName());
    }
    
    private ExplorerManager doChildDestroyTest (final String name, final Collection<String> toSelect,
            final String... childrenNames) throws Throwable {
        
        class AWTTst implements Runnable {
            Node[] children;
            {
                List<Node> arr = new ArrayList<Node>();
                for (String s : childrenNames) {
                    arr.add(createLeaf(s));
                }
                children = arr.toArray(new Node[0]);
            }
            AbstractNode root = new AbstractNode(new RefreshableChildren());
            Panel p;
            BeanTreeView btv;
            JFrame f;
            JTree tree;
            Node operateOn;
            
            private void initUI() {
                if (p == null) {
                    p = new Panel();
                    btv = new BeanTreeView();
                    f = new JFrame();
                    tree = btv.tree;
                    ((RefreshableChildren) root.getChildren()).refreshKeys(childrenNames);
                    root.setName("test root");
                    p.getExplorerManager().setRootContext(root);
                    p.add(BorderLayout.CENTER, btv);
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.getContentPane().add(BorderLayout.CENTER, p);
                    f.setVisible(true);
                }
            }

            // children must be Children.Keys(lazy)
            class RefreshableChildren extends Children.Keys<String> {

                public RefreshableChildren () {
                    super(true);
                }
                
                @Override
                protected Node[] createNodes (String key) {
                    Node n = null;
                    for (Node cand : children) {
                        if (cand.getName().equals(key)) {
                            n = cand;
                            break;
                        }
                    }
                    return new Node[] { n };
                }
                
                void refreshKeys (String[] keys) {
                    super.setKeys(keys);
                }
            }

            @Override
            public void run() {

                initUI();

                List<Node> selection = new ArrayList<Node>();
                
                for (int i = 0; i < children.length; i++) {
                    if (name.equals(children[i].getName())) {
                        // this should select a sibling of the removed node
                        operateOn = children[i];
                    }
                    if (toSelect.contains(children[i].getName())) {
                        selection.add(children[i]);
                    }
                }

                try {
                    p.getExplorerManager().setSelectedNodes(selection.toArray(new Node[0]));
                } catch (PropertyVetoException e) {
                    fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                }

                TreePath[] paths = tree.getSelectionPaths();
                assertNotNull("Before removal: one node should be selected, but there are none.", paths);
                assertEquals("Before removal: one node should be selected, but there are none.", selection.size(), paths.length);
                if (selection.size() == 1) {
                    assertEquals("Before removal: one node should be selected, but there are none.", operateOn, Visualizer.findNode(paths[0].getLastPathComponent()));
                    assertEquals("Before removal: one node should be selected, but there are none.", operateOn, Visualizer.findNode(tree.getAnchorSelectionPath().getLastPathComponent()));
                }

                try {
                    // destroy the node
                    operateOn.destroy();
                } catch (IOException ex) {
                    fail(ex.getMessage());
                }
                
                assertNotNull("After removal: one node should be selected, but there are none.", tree.getSelectionPath());
            }

            public void tryGc() throws InterruptedException, InvocationTargetException {
                // somewhere around here it's time to remove the destroyed node from children
                // the same as DataObject and FolderChildren work
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run () {
                        List<String> newKeys = new ArrayList<String>(Arrays.asList(childrenNames));
                        newKeys.remove(name);
                        ((RefreshableChildren) root.getChildren()).refreshKeys(newKeys.toArray(new String[0]));
                        children = null;
                    }
                });
                WeakReference<Node> wref = new WeakReference<Node>(operateOn);
                operateOn = null;
                RequestProcessor selectionProcessor;
                int delay;
                try {
                    Method getSelectionProcessorMethod = ExplorerManager.class.getDeclaredMethod("getSelectionProcessor");
                    getSelectionProcessorMethod.setAccessible(true);
                    selectionProcessor = (RequestProcessor) getSelectionProcessorMethod.invoke(null);
                    Field delayField = ExplorerManager.class.getDeclaredField("SELECTION_SYNC_DELAY");
                    delayField.setAccessible(true);
                    delay = (Integer) delayField.get(null);
                    
                    // Wait for the task in selectionProcessor to start up:
                    Class ticTacClass = Class.forName(RequestProcessor.class.getName()+"$TickTac");
                    Field tickField = ticTacClass.getDeclaredField("TICK");
                    tickField.setAccessible(true);
                    Object tick = tickField.get(null);
                    if (tick != null) {
                        // Waiting for the tick queue to become empty (scheduled tasks removed)
                        Field queueField = ticTacClass.getDeclaredField("queue");
                        queueField.setAccessible(true);
                        Queue queue = (Queue) queueField.get(tick);
                        while (hasOwnersOf(tick, queue, selectionProcessor)) {
                            //System.err.println("Waiting for queue "+Integer.toHexString(System.identityHashCode(queue))+" to become empty... peek = "+queue.peek()+" is empty = "+queue.isEmpty());
                            Thread.sleep(2*delay); // Wait for the queue with scheduled tasks to become empty
                        }
                    }
                } catch (IllegalAccessException ex) {
                    throw new InvocationTargetException(ex);
                } catch (NoSuchMethodException ex) {
                    throw new InvocationTargetException(ex);
                } catch (NoSuchFieldException ex) {
                    throw new InvocationTargetException(ex);
                } catch (ClassNotFoundException ex) {
                    throw new InvocationTargetException(ex);
                }
                
                // Wait for the task, removed from tick queue, to be attached to a processor...
                Thread.sleep(2*delay);  // No reliable way :-(
                // Wait for the task in selectionProcessor to finish
                selectionProcessor.awaitTermination(60, TimeUnit.SECONDS);
                assertGC("Node should be released.", wref);    
            }
            private boolean hasOwnersOf(Object tick, Queue q, RequestProcessor rp) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
                Class itemClass = Class.forName(RequestProcessor.class.getName()+"$Item");
                Field ownerField = itemClass.getDeclaredField("owner");
                ownerField.setAccessible(true);
                synchronized (tick) {
                    for (Object o : q) {
                        Object ownerRP = ownerField.get(o);
                        if (rp.equals(ownerRP)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
        AWTTst awt = new AWTTst();
        holder = awt;
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        awt.tryGc();
        return awt.p.getExplorerManager();
    }
    
    public void testVisibleVisNodesAreNotGCed() throws InterruptedException, Throwable {
        doTestVisibleVisNodesAreNotGCed(false);
    }
    public void testVisibleVisNodesAreNotGCedAfterCollapseExpand() throws InterruptedException, Throwable {
        doTestVisibleVisNodesAreNotGCed(true);
    }

    public void doTestVisibleVisNodesAreNotGCed(final boolean collapseAndExpand) throws InterruptedException, Throwable {
        class AWTTst implements Runnable {

            AbstractNode root = new AbstractNode(new Children.Array());
            Node[] children = {
                createLeaf("foo"),
                createLeaf("bar"),
                createLeaf("bla")
            };
            VisualizerNode[] visNodes;
            Panel p;
            BeanTreeView btv;
            JFrame f;
            JTree tree;

            private void initUI() {
                if (p == null) {
                    p = new Panel();
                    btv = new BeanTreeView();
                    f = new JFrame();
                    tree = btv.tree;
                    root.setName("test root");
                    root.getChildren().add(children);
                    p.getExplorerManager().setRootContext(root);
                    p.add(BorderLayout.CENTER, btv);
                    f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
                    f.getContentPane().add(BorderLayout.CENTER, p);
                    f.setVisible(true);
                }
            }

            public void run() {

                initUI();

                try {
                    p.getExplorerManager().setSelectedNodes(children);
                } catch (PropertyVetoException e) {
                    fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                }

                TreePath[] paths = tree.getSelectionPaths();
                assertEquals("3 nodes should be selected.", 3, paths.length);
                visNodes = new VisualizerNode[NO_OF_NODES];
                for (int i = 0; i < visNodes.length; i++) {
                    visNodes[i] = (VisualizerNode) paths[i].getLastPathComponent();
                }

                try {
                    p.getExplorerManager().setSelectedNodes(new Node[0]);
                } catch (PropertyVetoException e) {
                    fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                }

                paths = tree.getSelectionPaths();
                if (paths != null && paths.length == 0) {
                    paths = null;
                }
                assertNull("Nothing should be selected: " + Arrays.toString(paths), paths);
                
                if (collapseAndExpand) {
                    btv.collapseNode(root);
                    btv.expandNode(root);
                }
            }

            public void checkNotGc() {
                WeakReference<VisualizerNode> wref = new WeakReference<VisualizerNode>(visNodes[1]);
                visNodes = null;
                try {
                    EQFriendlyGC.assertGC("Node should be released.", wref);
                } catch (AssertionFailedError e) {
                    return;
                }
                fail("should not be GC");
            }
        }
        AWTTst awt = new AWTTst();
        holder = awt;
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        awt.checkNotGc();
    }
    
    public void testSelectingRootDoesNotClearExploredContext() throws InterruptedException, Throwable {
        class AWTTst implements Runnable {

            AbstractNode root = new AbstractNode(new Children.Array());
            VisualizerNode visNode;
            Panel p;
            BeanTreeView btv;
            JFrame f;
            JTree tree;

            private void initUI() {
                if (p == null) {
                    p = new Panel();
                    btv = new BeanTreeView();
                    f = new JFrame();
                    tree = btv.tree;
                    root.setName("test root");
                    p.getExplorerManager().setRootContext(root);
                    p.add(BorderLayout.CENTER, btv);
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.getContentPane().add(BorderLayout.CENTER, p);
                    f.setVisible(true);
                }
            }

            @Override
            public void run() {
                initUI();
                try {
                    btv.selectionChanged(new Node[] { root }, p.getExplorerManager());
                } catch (PropertyVetoException ex) {
                    fail(ex.getMessage());
                }
                
                assertSame("Root is explored", root, p.getExplorerManager().getExploredContext());
            }
            
        }
        AWTTst awt = new AWTTst();
        holder = awt;
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }    
    
    public void testVisibleCollapsedNodesAreGCed() throws InterruptedException, Throwable {
        class AWTTst implements Runnable {

            AbstractNode root = new AbstractNode(new Children.Array());
            Node[] children = {
                createLeaf("foo"),
                createLeaf("bar"),
                createLeaf("bla")
            };
            VisualizerNode visNode;
            Panel p;
            BeanTreeView btv;
            JFrame f;
            JTree tree;

            private void initUI() {
                if (p == null) {
                    p = new Panel();
                    btv = new BeanTreeView();
                    f = new JFrame();
                    tree = btv.tree;
                    root.setName("test root");
                    root.getChildren().add(children);
                    p.getExplorerManager().setRootContext(root);
                    p.add(BorderLayout.CENTER, btv);
                    f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
                    f.getContentPane().add(BorderLayout.CENTER, p);
                    f.setVisible(true);
                }
            }

            public void run() {

                initUI();

                try {
                    p.getExplorerManager().setSelectedNodes(new Node[] {children[0]});
                } catch (PropertyVetoException e) {
                    fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                }

                TreePath[] paths = tree.getSelectionPaths();
                assertEquals("one node should be selected.", 1, paths.length);
                visNode = (VisualizerNode) paths[0].getLastPathComponent();

                try {
                    p.getExplorerManager().setSelectedNodes(new Node[0]);
                } catch (PropertyVetoException e) {
                    fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                }
                paths = tree.getSelectionPaths();
                if (paths != null && paths.length == 0) {
                    paths = null;
                }
                assertNull("Nothing should be selected: " + Arrays.toString(paths), paths);

                btv.collapseNode(children[0].getParentNode());
            }
            
            public void checkGc() {
                WeakReference<VisualizerNode> wref = new WeakReference<VisualizerNode>(visNode);
                visNode = null;
                assertGC("Collapsed - should be GCed.", wref);
            }            
        }
        AWTTst awt = new AWTTst();
        holder = awt;
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        awt.checkGc();
    }    
    
    private static Node createLeaf(String name) {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(name);
        return n;
    }
    
    private static class Panel extends JPanel
            implements ExplorerManager.Provider {
        private ExplorerManager em = new ExplorerManager();
        
        public ExplorerManager getExplorerManager() {
            return em;
        }
    }
}
