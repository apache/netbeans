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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Children.Keys;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomáš Holý, Jaroslav Tulach
 */
public class AreVisualizersSynchronizedTest extends NbTestCase {

    {
        System.setProperty("org.openide.explorer.VisualizerNode.prefetchCount", "0");
    }

    public AreVisualizersSynchronizedTest(String name) {
        super(name);
    }
    private static class StrKeys extends Keys<String> {

        public StrKeys(boolean lazy) {
            super(lazy);
        }

        @Override
        protected Node[] createNodes(String key) {
            if (key.contains("Empty")) {
                return null;
            } else {
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(key);
                return new Node[]{n};
            }
        }
        void doSetKeys(String[] keys) {
            setKeys(keys);
        }
    }

    public void testDelayedReordersReallyWork() throws InterruptedException {
        StrKeys children = new StrKeys(true);
        final Node root = new AbstractNode(children);

        class Block extends CountDownLatch implements Runnable {
            CountDownLatch blocking = new CountDownLatch(1);
            CountDownLatch finished = new CountDownLatch(1);
            TreeNode tree;
            int cntBefore = -1;
            private List<TreeNode> children;

            public Block(int i) {
                super(i);
            }

            public void run() {
                tree = Visualizer.findVisualizer(root);
                cntBefore = tree.getChildCount();

                try {
                    blocking.countDown();
                    await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }

                ArrayList<TreeNode> ch = new ArrayList<TreeNode>();
                for (int i = 0; i < tree.getChildCount(); i++) {
                    ch.add(tree.getChildAt(i));
                }
                children = ch;
                finished.countDown();
            }

            public void blockAWT() throws InterruptedException {
                SwingUtilities.invokeLater(this);
                blocking.await();
            }

            /**
             * @return the children
             */
            public List<TreeNode> getChildren() throws InterruptedException {
                finished.await();
                return children;
            }
        }

        Block b0 = new Block(1);
        b0.blockAWT();
        b0.countDown();
        
        assertEquals("No node before", 0, b0.cntBefore);
        assertEquals("No nodes after", 0, b0.getChildren().size());

        children.doSetKeys(new String[] {"1", "2"});

        Block b = new Block(1);
        b.blockAWT();
        assertEquals("Two nodes before", 2, b.cntBefore);

        children.doSetKeys(new String[] {"1", "3", "2"});
        children.doSetKeys(new String[] {"3", "2", "1"});


        b.countDown();

        Block b2 = new Block(1);
        b2.blockAWT();
        assertEquals("Three children", 3, b2.cntBefore);
        b2.countDown();

        assertEquals("1", b.getChildren().get(0).toString());
        assertEquals("2", b.getChildren().get(1).toString());

        assertEquals("3", b2.getChildren().get(0).toString());
        assertEquals("2", b2.getChildren().get(1).toString());
        assertEquals("1", b2.getChildren().get(2).toString());
    }

    /** simulates removal of form file (one visible and one hidden entry) */
    public void testRemoveReallyWorks() throws InterruptedException, InvocationTargetException {
        StrKeys children = new StrKeys(true);
        final Node root = new AbstractNode(children);

        class VisualInAwt implements Runnable {
            TreeNode tree;
            List<TreeNode> children;

            public void run() {
//                if (!Children.MUTEX.isReadAccess() && !Children.MUTEX.isWriteAccess()) {
//                    Children.MUTEX.readAccess(this);
//                    return;
//                }

                tree = Visualizer.findVisualizer(root);
                ArrayList<TreeNode> ch = new ArrayList<TreeNode>();
                TreeNode n;
                for (int i = 0; i < tree.getChildCount(); i++) {
                    n = tree.getChildAt(i);
                    ch.add(n);
                }
                children = ch;
            }
        }

        VisualInAwt visInAwt = new VisualInAwt();

        children.doSetKeys(new String[] {"0", "Empty1", "1", "Empty2", "2", "Empty3", "3"});
        SwingUtilities.invokeAndWait(visInAwt);
        /*class Block implements
                Runnable {

            public synchronized void run() {
                notify();
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        Block b = new Block();
        synchronized (b) {
            SwingUtilities.invokeLater(b);
            b.wait();
        }*/
        
        // here we get some empty nodes, removal should be scheduled
        assertEquals("0", visInAwt.children.get(0).toString());
        assertEquals("", visInAwt.children.get(1).toString());
        assertEquals("1", visInAwt.children.get(2).toString());
        assertEquals("", visInAwt.children.get(3).toString());
        assertEquals("2", visInAwt.children.get(4).toString());
        assertEquals("", visInAwt.children.get(5).toString());
        assertEquals("3", visInAwt.children.get(6).toString());

       /* synchronized(b) {
            b.notifyAll();
        }*/
        
        SwingUtilities.invokeAndWait(visInAwt);
        // here hidden nodes should be already removed
        assertEquals("0", visInAwt.children.get(0).toString());
        assertEquals("1", visInAwt.children.get(1).toString());
        assertEquals("2", visInAwt.children.get(2).toString());
        assertEquals("3", visInAwt.children.get(3).toString());

        children.doSetKeys(new String[] {"0", "Empty1", "1", "Empty3", "3"});
        SwingUtilities.invokeAndWait(visInAwt);
        // check removal of "form" was done correctly
        assertEquals("0", visInAwt.children.get(0).toString());
        assertEquals("1", visInAwt.children.get(1).toString());
        assertEquals("3", visInAwt.children.get(2).toString());
    }
}
