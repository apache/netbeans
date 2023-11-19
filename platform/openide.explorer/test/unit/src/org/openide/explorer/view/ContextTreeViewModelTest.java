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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Tests for class ContextTreeViewTest
 */
public class ContextTreeViewModelTest extends NbTestCase {
    
    private static final int NO_OF_NODES = 3;


    private Logger LOG = Logger.getLogger("TEST-" + getName());
    
    public ContextTreeViewModelTest(String name) {
        super(name);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }
    
    public void testCheckThatTheModelFiresChangesAboutOnlyVisibleNodes() throws Throwable {
        final AbstractNode root = new AbstractNode(new Children.Array());
        root.setName("test root");

        LOG.info("node created");
        
        root.getChildren().add(new Node[] {
            createNode("kuk", true),
            createNode("huk", true),
        });

        LOG.info("two subnodes added");
        
        
        NodeTreeModel m = new ContextTreeView.NodeContextModel();

        LOG.info("model created");

        m.setNode(root);

        LOG.info("its root node set");

        TreeNode visual = (TreeNode)m.getRoot();

        LOG.info("getting the root: " + visual);
        
        waitEQ();
        
        
        assertEquals("Leaf nodes are not counted", 0, m.getChildCount(visual));


        Listener listener = new Listener(visual);
        m.addTreeModelListener(listener);

        LOG.info("listener added");

        Node n = createNode("blik", true);

        LOG.info("node created");
        
        root.getChildren().add(new Node[] { n });

        LOG.info("node added");

        assertEquals("Leaf nodes are not counted even when added", 0, m.getChildCount(visual));
        assertEquals("Really added", n.getParentNode(), root);
        listener.assertEvents("No events", 0);

        LOG.info("events ok");

        root.getChildren().remove(new Node[] { n });
        listener.assertEvents("Still no events", 0);
        assertNull("Removed", n.getParentNode());

        LOG.info("node removed");
        
        Node nonLeaf = createNode("nonleaf", false);
        root.getChildren().add(new Node[] { nonLeaf });

        LOG.info("non-leaf added");

        assertEquals("One child is there", 1, m.getChildCount(visual));
        listener.assertEvents("This node is visible there", 1);
        listener.assertIndexes("Added at position zero", new int[] { 0 });
        assertEquals("Really added", nonLeaf.getParentNode(), root);

        LOG.info("all checked");

        assertAgressiveGC();
        
        root.getChildren().remove(new Node[] { nonLeaf });

        LOG.info("non-leaf removed");

        assertEquals("One child is away", 0, m.getChildCount(visual));
        assertNull("Removed", nonLeaf.getParentNode());
        listener.assertEvents("And it has been removed", 1);
        listener.assertIndexes("Removed from position zero", new int[] { 0 });

        LOG.info("all ok");
    }
    
    public void testABitMoreComplexAddAndRemoveEventCheck() throws Throwable {
        final AbstractNode root = new AbstractNode(new Children.Array());
        root.setName("test root");

        LOG.info("root created");

        root.getChildren().add(new Node[] {
            createNode("kuk", false),
            createNode("huk", false),
        });


        LOG.info("two nodes added");
        
        NodeTreeModel m = new ContextTreeView.NodeContextModel();
        m.setNode(root);

        LOG.info("root set");

        TreeNode visual = (TreeNode)m.getRoot();
        waitEQ();

        LOG.info("visual is here: " + visual);
        
        
        assertEquals("Initial size is two", 2, m.getChildCount(visual));
        
        Listener listener = new Listener(visual);
        m.addTreeModelListener(listener);

        LOG.info("listener added");
        
        Node[] arr = {
            createNode("add1", false), createNode("add2", false)
        };

        assertAgressiveGC();

        LOG.info("adding children");
        root.getChildren().add(arr);

        LOG.info("children added");

        listener.assertEvents("One addition", 1);
        listener.assertIndexes("after the two first", new int[] { 2, 3 });

        LOG.info("removing children");
        root.getChildren().remove(arr);
        LOG.info("children removed");

        listener.assertEvents("One removal", 1);
        listener.assertIndexes("from end", new int[] { 2, 3 });

        LOG.info("all well");
    }
    
    public void testRemoveInMiddle() throws Throwable {
        final AbstractNode root = new AbstractNode(new Children.Array());
        root.setName("test root");


        LOG.info("root added");

        root.getChildren().add(new Node[] { createNode("Ahoj", false) });
        
        LOG.info("a node added");

        Node[] first = {
            createNode("kuk", false),
            createNode("huk", false),
        };


        LOG.info("adding more nodes");
        
        root.getChildren().add(first);

        LOG.info("nodes added");
        
        
        NodeTreeModel m = new ContextTreeView.NodeContextModel();
        m.setNode(root);

        LOG.info("root set");
        TreeNode visual = (TreeNode)m.getRoot();

        LOG.info("visual is here: " + visual);

        waitEQ();
        
        
        assertEquals("Initial size is two", 3, m.getChildCount(visual));
        
        Listener listener = new Listener(visual);
        m.addTreeModelListener(listener);


        LOG.info("listener added");
        
        Node[] arr = {
            createNode("add1", false), createNode("add2", false)
        };

        assertAgressiveGC();
        
        root.getChildren().add(arr);

        LOG.info("arr added");

        listener.assertEvents("One addition", 1);
        listener.assertIndexes("after the three first", new int[] { 3, 4 });

        LOG.info("arr ok");

        root.getChildren().remove(first);


        LOG.info("arr removed");

        listener.assertEvents("One removal", 1);
        listener.assertIndexes("from end", new int[] { 1, 2 });

        LOG.info("all ok");
    }
    
    private static Node createNode(String name, boolean leaf) {
        AbstractNode n = new AbstractNode(leaf ? Children.LEAF : new Children.Array());
        n.setName(name);
        return n;
    }

    private static void assertAgressiveGC() {
        for (int i = 0; i < 10; i++) {
            System.gc();
        }
    }
    
    private void waitEQ() throws Throwable {
        /*
        try {
            javax.swing.SwingUtilities.invokeAndWait (new Runnable () { public void run () { } } );
        } catch (java.lang.reflect.InvocationTargetException ex) {
            throw ex.getTargetException ();
        }
         */
    }
    
    private static class Panel extends JPanel
            implements ExplorerManager.Provider {
        private ExplorerManager em = new ExplorerManager();
        
        public ExplorerManager getExplorerManager() {
            return em;
        }
    }
    
    private class Listener implements TreeModelListener {
        private int cnt;
        private int[] indexes;


        private TreeNode keep;
        private List<TreeNode> all = new ArrayList<>();

        public Listener(TreeNode keep) {
            this.keep = keep;
            addChildren();
        }
        
        public void assertEvents(String msg, int cnt) throws Throwable {
            waitEQ();
            assertEquals(msg, cnt, this.cnt);
            this.cnt = 0;
        }
        public void assertIndexes(String msg, int[] arr) throws Throwable {
            waitEQ();
            assertNotNull(msg + " there has to be some", indexes);
            boolean bad = false;
            if (arr.length != indexes.length) {
                bad = true;
            } else {
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] != indexes[i]) {
                        bad = true;
                    }
                }
            }
            if (bad) {
                fail(msg + " expected: " + toStr(arr) + " was: " + toStr(indexes));
            }
            
            this.indexes = null;
        }
        
        private String toStr(int[] arr) {
            StringBuffer sb = new StringBuffer();
            String sep = "[";
            for (int i = 0; i < arr.length; i++) {
                sb.append(sep);
                sb.append(arr[i]);
                sep = ", ";
            }
            sb.append(']');
            return sb.toString();
        }
        
        public void treeNodesChanged(TreeModelEvent treeModelEvent) {
            cnt++;
        }
        
        public void treeNodesInserted(TreeModelEvent treeModelEvent) {
            cnt++;
            indexes = treeModelEvent.getChildIndices();
        }
        
        public void treeNodesRemoved(TreeModelEvent treeModelEvent) {
            cnt++;
            indexes = treeModelEvent.getChildIndices();
        }
        
        public void treeStructureChanged(TreeModelEvent treeModelEvent) {
            cnt++;
        }

        private void addChildren() {
            int cnt = keep.getChildCount();
            for (int i = 0; i < cnt; i++) {
                all.add(keep.getChildAt(i));
            }
        }
    }
}
