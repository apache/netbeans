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
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Tests for class ContextTreeViewTest
 */
public class ContextTreeViewTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ContextTreeViewTest.class);
    }

    private static final int NO_OF_NODES = 3;
    
    
    public ContextTreeViewTest(String name) {
        super(name);
    }
    
    public void testLeafNodeReallyNotDisplayed() throws Throwable {
        final AbstractNode root = new AbstractNode(new Children.Array());
        root.setName("test root");
        
        
        
        root.getChildren().add(new Node[] {
            createLeaf("kuk"),
            createLeaf("huk"),
        });
        
        class AWTTst implements Runnable {
            public void run() {
                Panel p = new Panel();
                p.getExplorerManager().setRootContext(root);

                ContextTreeView ctv = new ContextTreeView();
                p.add(BorderLayout.CENTER, ctv);

                JFrame f = new JFrame();
                f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
                f.getContentPane().add(BorderLayout.CENTER, p);
                f.setVisible(true);

                final JTree tree = ctv.tree;
        
                // wait a while till the frame is realized and ctv.addNotify called
                Object r = tree.getModel().getRoot();
                assertEquals("There is root", Visualizer.findVisualizer(root), r);
                
                int cnt = tree.getModel().getChildCount(r);
                if (cnt != 0) {
                    fail("Should be zero " + cnt + " but there was:  " +
                            tree.getModel().getChild(r, 0) + " and " +
                            tree.getModel().getChild(r, 1)
                            );
                }
                assertEquals("No children as they are leaves", 0, cnt);

                Node n = Visualizer.findNode(r);
                n.setName("Try Rename!");
            }
        }
        AWTTst awt = new AWTTst();
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
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
        
        public ExplorerManager getExplorerManager() {
            return em;
        }
    }
}
