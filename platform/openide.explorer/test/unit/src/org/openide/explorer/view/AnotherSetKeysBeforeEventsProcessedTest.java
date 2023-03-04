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
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Children.Keys;
import org.openide.util.Exceptions;

/**
 *
 * @author Holy
 */
public class AnotherSetKeysBeforeEventsProcessedTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(AnotherSetKeysBeforeEventsProcessedTest.class);
    }

    public AnotherSetKeysBeforeEventsProcessedTest(String name) {
        super(name);
    }
    private static class StrKeys extends Keys<String> {

        public StrKeys() {
            //super(true);
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
    
    private static class Panel extends JPanel
            implements ExplorerManager.Provider {
        private ExplorerManager em = new ExplorerManager();
        
        public ExplorerManager getExplorerManager() {
            return em;
        }
    }
    StrKeys children = new StrKeys();
    AbstractNode root = new AbstractNode(children);
    AtomicBoolean ab = new AtomicBoolean(false);

    class AwtRun implements Runnable {

        VisualizerNode visNode;
        Panel p;
        BeanTreeView btv;
        JFrame f;
        JTree tree;
        boolean ok;
        
        public void run() {
            p = new Panel();
            btv = new BeanTreeView();
            f = new JFrame();
            tree = btv.tree;
            try {
                root.setName("test root");
                p.getExplorerManager().setRootContext(root);
                p.add(BorderLayout.CENTER, btv);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.getContentPane().add(BorderLayout.CENTER, p);
                f.setVisible(true);
                ab.set(true);
                while (ab.get() == true) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                Node[] nodes = children.getNodes();
                try {
                    p.getExplorerManager().setSelectedNodes(new Node[]{nodes[2]});
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
                TreePath[] paths = tree.getSelectionPaths();
                ok = true;
            } finally {
                ab.set(true);
            }
        }
    }
    
    public void test() throws InterruptedException {
        children.doSetKeys(new String[] {"1", "2"});
        Node[] nodes = children.getNodes();
        AwtRun run = new AwtRun();
        SwingUtilities.invokeLater(run);
        while (ab.get() == false) {
            Thread.sleep(50);
        }
        children.doSetKeys(new String[] {"1", "3", "2"});
        children.doSetKeys(new String[] {"3", "2", "1"});
        ab.set(false);
        while (ab.get() == false) {
            Thread.sleep(50);
        }
        VisualizerNode vn = (VisualizerNode) Visualizer.findVisualizer(nodes[0]);

        assertTrue("Executed OK", run.ok);
    }
}
