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

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Tests for the quick search feature in the treeview.
 */
public class TreeViewQuickSearchTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TreeViewQuickSearchTest.class);
    }

    private JFrame f;
    
    public TreeViewQuickSearchTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 150000;
    }

    @Override
    protected void setUp() throws Exception {
        f = null;
    }
    
    @Override
    protected void tearDown() throws Exception {
        while (f != null && f.isShowing()) {
            f.setVisible(false);
        }
    }
    
    public void testQuickSearch() throws Throwable {
        final AbstractNode root = new AbstractNode(new Children.Array());
        root.setName("test root");
        
        final Node[] children = {
            createLeaf("foo1"),
            createLeaf("foo2"),
            createLeaf("bar1"),
            createLeaf("bar2"),
            createLeaf("alpha"),
        };
        
        root.getChildren().add(children);
        
        final Exception[]problem = new Exception[1];
        final Integer[] phase = new Integer[1];
        phase[0] = 0;
        class AWTTst implements Runnable {
            
            private Panel p;        
            private BeanTreeView btv;
            private JTree tree;

            private void initFrame() {
                if (f == null) {
                    p = new Panel();
                    p.getExplorerManager().setRootContext(root);
                    btv = new BeanTreeView();
                    p.add(BorderLayout.CENTER, btv);
                    tree = btv.tree;
                    f = new JFrame();
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.getContentPane().add(BorderLayout.CENTER, p);
                    f.pack();
                    f.setVisible(true);
                }
            }
            
            @Override
            public void run() {
                initFrame();
                try {
                    if (phase[0] == 0) {
                        btv.tree.requestFocus();
                        try {
                            p.getExplorerManager().setSelectedNodes(new Node[] { root });
                        } catch (PropertyVetoException e) {
                            fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                        }
                    }
                    if (phase[0] == 1) {
                        KeyEvent ke = new KeyEvent(btv.tree, KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'A');
                        btv.tree.dispatchEvent(ke);
                    }
                    if (phase[0] == 2) {
                        Node operateOn = children[4];
                        TreePath[] paths = tree.getSelectionPaths();
                        assertNotNull("One node should be selected, but there are none.", paths);
                        assertEquals("One node should be selected, but there are none.", 1, paths.length);
                        assertEquals("Wrong node selected.", operateOn, Visualizer.findNode(paths[0].getLastPathComponent()));
                    }
                } catch (Exception ex) {
                    problem[0] = ex;
                }
            }
        }
        AWTTst awt = new AWTTst();
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        if (problem[0] != null) {
            throw problem[0];
        }
        Thread.sleep(1000);
        phase[0] = 1;
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        if (problem[0] != null) {
            throw problem[0];
        }
        phase[0] = 2;
        Thread.sleep(1000);
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        if (problem[0] != null) {
            throw problem[0];
        }
    }
    
    public void testQuickSearchEnable() throws Throwable {
        final AbstractNode root = new AbstractNode(new Children.Array());
        root.setName("test root");
        
        final Node[] children = {
            createLeaf("foo1"),
            createLeaf("foo2"),
            createLeaf("bar1"),
            createLeaf("bar2"),
            createLeaf("alpha"),
        };
        
        root.getChildren().add(children);
        
        final BeanTreeView[] btvPtr = new BeanTreeView[] { null };
        final Exception[] problem = new Exception[1];
        final Integer[] phase = new Integer[1];
        phase[0] = 0;
        class AWTTst implements Runnable {

            private Panel p;
            private BeanTreeView btv;

            private void initFrame() {
                if (f == null) {
                    p = new Panel();
                    p.getExplorerManager().setRootContext(root);
                    btv = new BeanTreeView();
                    btvPtr[0] = btv;
                    p.add(BorderLayout.CENTER, btv);
                    f = new JFrame();
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.getContentPane().add(BorderLayout.CENTER, p);
                    f.pack();
                    f.setVisible(true);
                    
                }
            }
            
            @Override
            public void run() {
                initFrame();
                if (phase[0] == 0) {
                    btv.tree.requestFocus();
                    try {
                        p.getExplorerManager().setSelectedNodes(new Node[]{root});
                    } catch (PropertyVetoException e) {
                        fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                    }
                    return;
                }
                KeyEvent ke = new KeyEvent(btv.tree, KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'A');
                btv.tree.dispatchEvent(ke);

                if (phase[0] != 0) {
                    if (btv.isQuickSearchAllowed()) {
                        assertNotNull("Quick Search enabled ", btv.getSearchPanel());
                    } else {
                        assertNull("Quick Search disabled", btv.getSearchPanel());
                    }
                }
            }
        }
        AWTTst awt = new AWTTst();
        SwingUtilities.invokeAndWait(awt);
        if (problem[0] != null) {
            throw problem[0];
        }
        Thread.sleep(1000);
        phase[0] = 1;
        btvPtr[0].setQuickSearchAllowed(true);
        SwingUtilities.invokeAndWait(awt);
        if (problem[0] != null) {
            throw problem[0];
        }
        
        Thread.sleep(1000);
        btvPtr[0].setQuickSearchAllowed(false);
        SwingUtilities.invokeAndWait(awt);
    }

    @RandomlyFails //NB-Core-Build #10413 - arrow down does not work on deadlock??
    public void testQuickSearchSubNodesFirst() throws Throwable {
        final AbstractNode root1 = new AbstractNode(new Children.Array());
        root1.setName("test root 1");

        final Node[] children1 = {
            createLeaf("foo1"),
            createLeaf("bar1"),};
        root1.getChildren().add(children1);

        final AbstractNode root2 = new AbstractNode(new Children.Array());
        root2.setName("test root 2");

        final Node[] children2 = {
            createLeaf("aaafoo2"),
            createLeaf("foo2"),
            createLeaf("bar2"),};
        root2.getChildren().add(children2);

        final AbstractNode mainRoot = new AbstractNode(new Children.Array());
        mainRoot.setName("main root");
        final Node[] mainChildren = {
            root1,
            root2
        };
        mainRoot.getChildren().add(mainChildren);

        final Exception[] problem = new Exception[1];
        final Integer[] phase = new Integer[1];
        phase[0] = 0;
        class AWTTst implements Runnable {

            private Panel p;
            private BeanTreeView btv;
            private JTree tree;

            private void initFrame() {
                if (f == null) {
                    p = new Panel();
                    p.getExplorerManager().setRootContext(mainRoot);
                    btv = new BeanTreeView();
                    p.add(BorderLayout.CENTER, btv);
                    tree = btv.tree;
                    f = new JFrame();
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.getContentPane().add(BorderLayout.CENTER, p);
                    f.pack();
                    f.setVisible(true);
                }
            }

            @Override
            public void run() {
                initFrame();
                try {
                    if (phase[0] == 0) {
                        btv.tree.requestFocus();
                        btv.expandAll();
                        try {
                            p.getExplorerManager().setSelectedNodes(new Node[]{root2});
                        } catch (PropertyVetoException e) {
                            fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                        }
                    } else if (phase[0] == 1) {
                        KeyEvent ke = new KeyEvent(btv.tree, KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'F');
                        btv.tree.dispatchEvent(ke);
                    } else if (phase[0] == 2) {
                        //select subnodes starting with F first
                        Node operateOn = children2[1];
                        TreePath[] paths = tree.getSelectionPaths();
                        assertNotNull("One node should be selected, but there are none.", paths);
                        assertEquals("One node should be selected, but there are none.", 1, paths.length);
                        assertEquals("Wrong node selected - subnodes starting with F first", operateOn, Visualizer.findNode(paths[0].getLastPathComponent()));
                    } else if (phase[0] == 3) {
                        KeyEvent ke = new KeyEvent(btv.tree, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
                        btv.tree.dispatchEvent(ke);
                    } else if (phase[0] == 4) {
                        // the search other nodes starting with F
                        Node operateOn = children1[0];
                        TreePath[] paths = tree.getSelectionPaths();
                        assertNotNull("One node should be selected, but there are none.", paths);
                        assertEquals("One node should be selected, but there are none.", 1, paths.length);
                        assertEquals("Wrong node selected - other nodes starting with F", operateOn, Visualizer.findNode(paths[0].getLastPathComponent()));
                    } else if (phase[0] == 5) {
                        KeyEvent ke = new KeyEvent(btv.tree, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
                        btv.tree.dispatchEvent(ke);
                    } else if (phase[0] == 6) {
                        // and finally select subnodes not starting with F
                        Node operateOn = children2[0];
                        TreePath[] paths = tree.getSelectionPaths();
                        assertNotNull("One node should be selected, but there are none.", paths);
                        assertEquals("One node should be selected, but there are none.", 1, paths.length);
                        assertEquals("Wrong node selected - subnodes not starting with F", operateOn, Visualizer.findNode(paths[0].getLastPathComponent()));
                    }
                } catch (Exception ex) {
                    problem[0] = ex;
                }
            }
        }
        AWTTst awt = new AWTTst();
        for (int i = 0; i < 6; i++) {
            phase[0] = i;
            Thread.sleep(1000);
            try {
                SwingUtilities.invokeAndWait(awt);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
            if (problem[0] != null) {
                throw problem[0];
            }
        }
    }
    
    private static Node createLeaf(String name) {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(name);
        return n;
    }
    
    private static class Panel extends JPanel
            implements ExplorerManager.Provider {
        private ExplorerManager em = new ExplorerManager();
        
        @Override
        public ExplorerManager getExplorerManager() {
            return em;
        }
    }
}
