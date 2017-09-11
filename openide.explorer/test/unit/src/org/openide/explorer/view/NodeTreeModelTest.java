/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
