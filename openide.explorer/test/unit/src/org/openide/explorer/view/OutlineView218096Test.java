/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
