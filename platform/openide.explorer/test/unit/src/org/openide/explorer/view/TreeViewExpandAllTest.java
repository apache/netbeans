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

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Denis Sepanov, Tomas Holy
 */

public class TreeViewExpandAllTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TreeViewExpandAllTest.class);
    }

    public TreeViewExpandAllTest(String name) {
        super(name);
    }
    Set<Integer> expandedNodesIndexes = new HashSet<Integer>();

    boolean lazy;
    public void testExpandAllEager() throws InterruptedException, InvocationTargetException {
        lazy = false;
        doTestExpandAll();
    }

    public void testExpandAllLazy() throws InterruptedException, InvocationTargetException {
        lazy = true;
        doTestExpandAll();
    }

    public void doTestExpandAll() throws InterruptedException, InvocationTargetException {

        EventQueue.invokeAndWait(new Runnable() {

            public void run() {
                final BeanTreeView beanTreeView = new BeanTreeView();
                final ExplorerWindow testWindow = new ExplorerWindow();
                testWindow.getContentPane().add(beanTreeView);
                // Node which has 7 levels 0-6
                testWindow.getExplorerManager().setRootContext(new LevelNode(6));

                testWindow.pack();
                testWindow.setVisible(true);
                beanTreeView.expandAll();
            }
        });
        // Whole expanded tree should have nodes O-6
        assertEquals(new HashSet<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5, 6)), expandedNodesIndexes);
    }

    private static final class ExplorerWindow extends JFrame implements ExplorerManager.Provider {

        private final ExplorerManager explManager = new ExplorerManager();

        ExplorerWindow() {
            super("TreeView expandAll test");
        }

        public ExplorerManager getExplorerManager() {
            return explManager;
        }
    }

    class LevelNode extends AbstractNode {

        public LevelNode(int level) {
            super(new LevelNodeChildren(level));
            expandedNodesIndexes.add(level);
        }
    }

    class LevelNodeChildren extends Children.Keys<Integer> {

        int level;

        public LevelNodeChildren(int level) {
            super(lazy);
            this.level = level;
        }

        @Override
        protected void addNotify() {
            if (level > 0) {
                setKeys(new Integer[]{--level});
            }
        }

        @Override
        protected Node[] createNodes(Integer i) {
            return new Node[]{new LevelNode(i)};
        }
    }
}
