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
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Test for bug 218096 - OutlineView node may not refresh on events when sibling
 * node expanded.
 *
 * @author jhavlin
 */
public class OutlineView218096Test extends NbTestCase {

    public OutlineView218096Test(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void test218096() {
        String[][] nodesDesc = new String[][]{
            new String[]{"a", "b", "c"},
            new String[]{"D", "E", "F"},
            new String[]{"g", "h", "i"}};
        Node rootNode = new TestNode(new StringListKeys(nodesDesc), "root");
        OutlineViewComponent comp = new OutlineViewComponent(rootNode);
        Node[] rootChildNodes = rootNode.getChildren().getNodes();
        assertEquals(3, rootChildNodes.length);
        Node[] firstNodeChildren = rootChildNodes[0].getChildren().getNodes();
        assertEquals(3, firstNodeChildren.length);
        comp.view.expandNode(rootChildNodes[0]);
        assertEquals(6, comp.view.getOutline().getRowCount());
        VisualizerNode dParentVisNode = (VisualizerNode) comp.view.getOutline()
                .getModel().getValueAt(4, 0);
        assertEquals("Dparent", dParentVisNode.getDisplayName());
        TestNode.MarkedCookie mc = rootChildNodes[1].getLookup().lookup(
                TestNode.MarkedCookie.class);
        final AtomicBoolean notifiedAboutChange = new AtomicBoolean(false);
        comp.view.getOutline().getModel().addTableModelListener(
                new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getFirstRow() <= 4 && e.getLastRow() >= 4) {
                    notifiedAboutChange.set(true);
                }
            }
        });
        mc.setMarked(true);
        assertTrue("Table model should be notified about name change",
                notifiedAboutChange.get());
    }

    private class OutlineViewComponent extends JPanel
            implements ExplorerManager.Provider {

        private final ExplorerManager manager = new ExplorerManager();
        private OutlineView view;

        private OutlineViewComponent(Node rootNode) {
            setLayout(new BorderLayout());
            manager.setRootContext(rootNode);
            view = new OutlineView("test-outline-view-component");
            view.getOutline().setRootVisible(false);
            add(view, BorderLayout.CENTER);
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }
    }

    private static class TestNode extends AbstractNode {

        private boolean marked = false;

        class MarkedCookie {

            void setMarked(boolean marked) {
                TestNode.this.marked = marked;
                TestNode.this.fireDisplayNameChange(null, null);
            }
        }

        public TestNode(String name) {
            this(Children.LEAF, name, new InstanceContent());
        }

        public TestNode(Children children, String name) {
            this(children, name, new InstanceContent());
        }

        private TestNode(Children children, String name, InstanceContent ic) {
            super(children, new AbstractLookup(ic));
            ic.add(new MarkedCookie());
            setName(name);
        }

        @Override
        public String getDisplayName() {
            return getName() + (marked ? "!" : "");
        }
    }

    private static class StringListKeys extends Children.Keys<String[]> {

        public StringListKeys(String[][] keys) {
            super();
            setKeys(keys);
        }

        @Override
        protected Node[] createNodes(String[] key) {
            return new Node[]{
                new TestNode(new StringKeys(key), key[0] + "parent")};
        }
    }

    private static class StringKeys extends Children.Keys<String> {

        public StringKeys(String[] keys) {
            super(false);
            setKeys(keys);
        }

        @Override
        protected Node[] createNodes(String key) {
            return new Node[]{new TestNode(key)};
        }
    }
}
