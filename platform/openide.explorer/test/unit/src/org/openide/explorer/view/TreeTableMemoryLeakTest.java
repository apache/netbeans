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

import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Matteo Di Giovinazzo <digiovinazzo@streamsim.com>
 */
public class TreeTableMemoryLeakTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TreeTableMemoryLeakTest.class);
    }

    private Node root;
    private Children.Array children;
    private Node child;
    private TreeTableView ttv;

    public TreeTableMemoryLeakTest() {
        super("MainTest");
    }

    public void testMain() {
        child = new AbstractNode(Children.LEAF) {

            {
                setName("child");
            }
        };


        children = new Children.Array();
        children.add(new Node[]{child});
        root = new AbstractNode(children) {

            {
                setName("root");
            }
        };


        try {
            SwingUtilities.invokeAndWait(new MyRunnable());
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }

        Reference<Node> ref = new WeakReference<Node>(child);
        child = null;

        assertGC("child in TTV must be GCed", ref, Collections.singleton(ttv));
        assertGC("child in general must be GCed", ref);
    }

    private class MyRunnable implements Runnable {

        public MyRunnable() {
        }

        @Override
        public void run() {

            // create panel (implementing ExplorerManager.Provider) with TTV
            MyPanel panel = new MyPanel();
            panel.setLayout(new GridLayout(1, 2));
            ttv = new TreeTableView();
            panel.add(ttv);

            // set root and keep the same root
            panel.setExplorerManagerRoot(root);


            //comment the next line to make the test works
            ttv.expandNode(root);

            // remove child
            children.remove(new Node[]{child});
            root = null;
            children = null;
        }
    }

    private static class MyPanel extends JPanel implements ExplorerManager.Provider {

        private ExplorerManager manager;

        public MyPanel() {
            super();
            manager = new ExplorerManager();
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

        private void setExplorerManagerRoot(Node root) {
            manager.setRootContext(root);
        }
    }
}
