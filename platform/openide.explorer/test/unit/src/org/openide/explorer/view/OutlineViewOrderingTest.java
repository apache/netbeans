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
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Properties;
import javax.swing.JPanel;
import javax.swing.table.TableColumnModel;
import org.netbeans.junit.NbTestCase;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView.OutlineViewOutline.OutlineViewOutlineColumn;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;

/**
 * @author Jaroslav Tulach
 */
public final class OutlineViewOrderingTest extends NbTestCase {

    private OutlineViewComponent component;
    private OutlineView view;
    private TestNode rootNode;

    public OutlineViewOrderingTest (String testName) {
        super (testName);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }

    @Override
    public void setUp () {
        TestNode[] childrenNodes = new TestNode[3];
        for (int i = 0; i < childrenNodes.length; i ++) {
            TestNode[] leafNodes = new TestNode[100];
            for (int j = 0; j < leafNodes.length; j ++) {
                leafNodes[j] = new TestNode ("[" + (100 * i + j) + "]");
            }
            Children.Array leafs = new Children.Array ();
            leafs.add (leafNodes);
            //childrenNodes [i] = new TestNode (leafs, "[" + (i * 100) + "-" + ((i + 1) *100 - 1) + "]");
            switch (i) {
                case 0:
                    childrenNodes[i] = new TestNode (leafs, "[1-index from 0 to 99]");
                    break;
                case 1:
                    childrenNodes[i] = new TestNode (leafs, "[10-index from 100 to 199]");
                    break;
                case 2:
                    childrenNodes[i] = new TestNode (leafs, "[2-index from 200 to 299]");
                    break;
                default:
                    fail ("Unexcepted value " + i);
            }
        }

        Children.Array children = new Children.Array ();
        children.add (childrenNodes);

        rootNode = new TestNode (children, "[0 - 1000]");

        component = new OutlineViewComponent (rootNode);
        view = component.getOutlineView ();
    }

    public void testRemoveNodeColumn() throws InterruptedException, IllegalAccessException, InvocationTargetException {

        final TableColumnModel model = view.getOutline().getColumnModel();
        model.removeColumn(model.getColumn(0));
        assertEquals("One column visible", 1, model.getColumnCount());

        component.addNotify();
        final Node n0 = rootNode.getChildren().getNodeAt(0);

        rootNode.getChildren().remove(new Node[] { n0});
        assertEquals("One column visible after remove", 1, model.getColumnCount());
        rootNode.getChildren().add(new Node[] { n0});
        assertEquals("One column visible after add", 1, model.getColumnCount());
    }

    private class OutlineViewComponent extends JPanel implements ExplorerManager.Provider {

        private final ExplorerManager manager = new ExplorerManager ();
        private OutlineView view;

        private OutlineViewComponent (Node rootNode) {
            setLayout (new BorderLayout ());
            manager.setRootContext (rootNode);

            Node.Property[] props = rootNode.getPropertySets ()[0].getProperties ();
            view = new OutlineView ("test-outline-view-component");
            view.setProperties (props);

            view.getOutline ().setRootVisible (false);

            add (view, BorderLayout.CENTER);
        }

        public ExplorerManager getExplorerManager () {
            return manager;
        }

        public OutlineView getOutlineView () {
            return view;
        }
    }

    static class TestNode extends AbstractNode {

        public TestNode (String name) {
            super (Children.LEAF);
            setName (name);
        }

        public TestNode (Children children, String name) {
            super (children);
            setName (name);
        }

        @Override
        protected Sheet createSheet () {
            Sheet s = super.createSheet ();
            Sheet.Set ss = s.get (Sheet.PROPERTIES);
            if (ss == null) {
                ss = Sheet.createPropertiesSet ();
                s.put (ss);
            }
            ss.put (new DummyProperty (getName ()));
            return s;
        }

        void forcePropertyChangeEvent () {
            firePropertyChange ("unitTestPropName", null, new Object ());
        }

        class DummyProperty extends Property<String> {

            public DummyProperty (String val) {
                super (String.class);
                setName ("unitTestPropName");
                try {
                    setValue (val);
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace (ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace (ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace (ex);
                }
            }

            public boolean canRead () {
                return true;
            }

            public String getValue () throws IllegalAccessException,
                    InvocationTargetException {
                return (String) getValue ("unitTestPropName");
            }

            public boolean canWrite () {
                return true;
            }

            public void setValue (String val) throws IllegalAccessException,
                    IllegalArgumentException,
                    InvocationTargetException {
                setValue ("unitTestPropName", val);
            }
        }
    }
}
