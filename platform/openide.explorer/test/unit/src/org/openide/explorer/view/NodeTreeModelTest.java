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

import java.util.HashSet;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NodeTreeModelTest extends NbTestCase {

    public NodeTreeModelTest(String n) {
        super(n);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    public void setUp() {
    }

    @Override
    public void tearDown() {
    }

    public void testAddNodesDoesNotRecreateAllOfThem() {
        Keys keys = new Keys(true);
        AbstractNode an = new AbstractNode(keys);

        NodeTreeModel model = new NodeTreeModel();
        model.setNode(an);

        assertEquals("Node set", Visualizer.findVisualizer(an), model.getRoot());
        assertEquals("No children", 0, model.getChildCount(model.getRoot()));
        keys.keys("1", "2", "3");
        assertEquals("3 children", 3, model.getChildCount(model.getRoot()));
        keys.keys("2", "1", "3");
        assertEquals("still 3 children", 3, model.getChildCount(model.getRoot()));
        keys.keys("2");
        assertEquals("1 children", 1, model.getChildCount(model.getRoot()));

        assertEquals("No nodes created yet", 0, keys.cnt);
    }

    public void testAddNodesProvideEventsWithChildrenObjects() {
        Keys keys = new Keys(true);
        AbstractNode an = new AbstractNode(keys);

        final NodeTreeModel model = new NodeTreeModel();
        class L implements TreeModelListener {


            public void treeNodesChanged(TreeModelEvent e) {
                assertEvent(e, false);
            }

            public void treeNodesInserted(TreeModelEvent e) {
                assertEvent(e, false);
            }

            public void treeNodesRemoved(TreeModelEvent e) {
                assertEvent(e, true);
            }

            public void treeStructureChanged(TreeModelEvent e) {
                assertEvent(e, false);
            }

            private void assertEvent(TreeModelEvent e, boolean removed) {
                Object parent = e.getTreePath().getLastPathComponent();
                int[] arr = e.getChildIndices();
                if (arr == null) {
                    assertNull("No children either", e.getChildren());
                    return;
                }
                if (removed) {
                    for (int i = 0; i < arr.length; i++) {
                        Object my = e.getChildren()[i];
                        for (int j = 0; j < model.getChildCount(parent); j++) {
                            Object object = model.getChild(parent, j);
                            if (object.equals(my)) {
                                fail("My shall not be present at all");
                            }
                        }
                    }
                    return;
                }
                for (int i = 0; i < arr.length; i++) {
                    Object ch = model.getChild(parent, arr[i]);
                    Object my = e.getChildren()[i];
                    assertEquals(i + "th element is same", ch, my);
                }
            }
        }
        L listener = new L();
        model.addTreeModelListener(listener);
        model.setNode(an);


        assertEquals("Node set", Visualizer.findVisualizer(an), model.getRoot());
        assertEquals("No children", 0, model.getChildCount(model.getRoot()));
        keys.keys("1", "2", "3");
        assertEquals("3 children", 3, model.getChildCount(model.getRoot()));
        keys.keys("2", "1", "3");
        assertEquals("still 3 children", 3, model.getChildCount(model.getRoot()));
        keys.keys("2");
        assertEquals("1 children", 1, model.getChildCount(model.getRoot()));




    }

    /** Sample keys.
    */
    public static class Keys extends Children.Keys {
        int cnt;

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
            AbstractNode an = new AbstractNode (Children.LEAF);
            an.setName (key.toString ());

            cnt++;
            return new Node[] { an };
        }

    }
}
