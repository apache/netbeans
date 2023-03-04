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
 *
 * @author  Jiri Rechtacek
 */
public final class OutlineViewTest extends NbTestCase {

    private OutlineViewComponent component;
    private OutlineView view;
    private Node toExpand200_299,  toExpand0_99;

    public OutlineViewTest (String testName) {
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
            if (toExpand0_99 == null) {
                toExpand0_99 = childrenNodes[i];
            }
            toExpand200_299 = childrenNodes[i];
        }

        Children.Array children = new Children.Array ();
        children.add (childrenNodes);

        Node rootNode = new TestNode (children, "[0 - 1000]");

        component = new OutlineViewComponent (rootNode);
        view = component.getOutlineView ();
    }

    public void testNaturallySortingTree () throws InterruptedException, IllegalAccessException, InvocationTargetException {
        view.expandNode (toExpand0_99);

        // should look like
        // - [1-index from 0 to 99]
        //   [0]
        //   [1]
        //   [2]
        //   ....
        // + [10-index from 100 to 199]
        // + [2-index from 200 to 299]
        assertEquals ("[1-index from 0 to 99]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[0]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[1]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[2]", view.getOutline ().getValueAt (3, 0).toString ());
        assertEquals ("[10-index from 100 to 199]", view.getOutline ().getValueAt (101, 0).toString ());
        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (102, 0).toString ());
    }

    public void testDescendingSortingTreeWithNaturallyStringOrdering () throws InterruptedException, IllegalAccessException, InvocationTargetException {
        ETableColumnModel etcm = (ETableColumnModel) view.getOutline ().getColumnModel ();
        ETableColumn etc = (ETableColumn) etcm.getColumn (0); // tree column
        etcm.setColumnSorted (etc, false, 1); // descending order
        etc.setNestedComparator (testComarator);
        view.expandNode (toExpand200_299);

//        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (component, "", true, null);
//        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault ().createDialog (dd);
//        d.setVisible (true);

        // should look like
        // - [2-index from 200 to 299]
        //   [299]
        //   [298]
        // + [10-index from 100 to 199]
        //   ....
        // + [1-index from 0 to 99]
        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[10-index from 100 to 199]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[299]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[298]", view.getOutline ().getValueAt (3, 0).toString ());
    }

    public void testDescendingSortingTreeWithNaturallyStringOrderingViaETable () throws InterruptedException, IllegalAccessException, InvocationTargetException {
        ETableColumnModel etcm = (ETableColumnModel) view.getOutline ().getColumnModel ();
        ETableColumn etc = (ETableColumn) etcm.getColumn (0); // tree column
        etc.setNestedComparator (testComarator);
        view.getOutline ().setColumnSorted (0, false, 1); // descending order
        view.expandNode (toExpand200_299);

        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[10-index from 100 to 199]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[299]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[298]", view.getOutline ().getValueAt (3, 0).toString ());
    }

    public void testAscendingSortingTreeWithNaturallyStringOrdering () throws InterruptedException, IllegalAccessException, InvocationTargetException {
        ETableColumnModel etcm = (ETableColumnModel) view.getOutline ().getColumnModel ();
        ETableColumn etc = (ETableColumn) etcm.getColumn (0); // tree column
        etcm.setColumnSorted (etc, true, 1); // ascending order
        view.expandNode (toExpand0_99);

//        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (component, "", true, null);
//        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault ().createDialog (dd);
//        d.setVisible (true);

        // should look like
        // - [1-index from 0 to 99]
        //   [0]
        //   [10]
        //   [11]
        //   ....
        // + [10-index from 100 to 199]
        // + [2-index from 200 to 299]
        assertEquals ("[1-index from 0 to 99]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[0]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[10]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[11]", view.getOutline ().getValueAt (3, 0).toString ());
        assertEquals ("[10-index from 100 to 199]", view.getOutline ().getValueAt (101, 0).toString ());
        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (102, 0).toString ());
    }

    public void testAscendingSortingTreeWithNaturallyStringOrderingViaETable () throws InterruptedException, IllegalAccessException, InvocationTargetException {
        ETableColumnModel etcm = (ETableColumnModel) view.getOutline ().getColumnModel ();
        view.getOutline ().setColumnSorted (0, true, 1); // ascending order
        view.expandNode (toExpand0_99);

        assertEquals ("[1-index from 0 to 99]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[0]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[10]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[11]", view.getOutline ().getValueAt (3, 0).toString ());
        assertEquals ("[10-index from 100 to 199]", view.getOutline ().getValueAt (101, 0).toString ());
        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (102, 0).toString ());
    }

    public void testDescendingSortingTreeWithCustomComparator () throws InterruptedException, IllegalAccessException, InvocationTargetException {
        ETableColumnModel etcm = (ETableColumnModel) view.getOutline ().getColumnModel ();
        ETableColumn etc = (ETableColumn) etcm.getColumn (0); // tree column
        etc.setNestedComparator (testComarator);
        etcm.setColumnSorted (etc, false, 1); // descending order
        view.expandNode (toExpand200_299);

//        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (component, "", true, null);
//        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault ().createDialog (dd);
//        d.setVisible (true);

        // should look like
        // + [10-index from 100 to 199]
        // - [2-index from 200 to 299]
        //   [299]
        //   [298]
        //   ....
        // + [1-index from 0 to 99]
        assertEquals ("[10-index from 100 to 199]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[299]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[298]", view.getOutline ().getValueAt (3, 0).toString ());
    }

    public void testDescendingSortingTreeWithCustomComparatorViaETable() throws InterruptedException, IllegalAccessException, InvocationTargetException {
        ETableColumnModel etcm = (ETableColumnModel) view.getOutline ().getColumnModel ();
        ETableColumn etc = (ETableColumn) etcm.getColumn (0); // tree column
        etc.setNestedComparator (testComarator);
        view.getOutline().setColumnSorted (0, false, 1); // descending order
        view.expandNode (toExpand200_299);

        assertEquals ("[10-index from 100 to 199]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[299]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[298]", view.getOutline ().getValueAt (3, 0).toString ());
    }

    public void testAscendingSortingTreeWithCustomComparator () throws InterruptedException, IllegalAccessException, InvocationTargetException {
        ETableColumnModel etcm = (ETableColumnModel) view.getOutline ().getColumnModel ();
        ETableColumn etc = (ETableColumn) etcm.getColumn (0); // tree column
        etc.setNestedComparator (testComarator);
        etcm.setColumnSorted (etc, true, 1); // ascending order

//        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (component, "", true, null);
//        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault ().createDialog (dd);
//        d.setVisible (true);

        view.expandNode (toExpand0_99);

        // should look like
        // - [1-index from 0 to 99]
        //   [0]
        //   [1]
        //   [2]
        //   ....
        // + [2-index from 200 to 299]
        // + [10-index from 100 to 199]
        assertEquals ("[1-index from 0 to 99]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[0]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[1]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[2]", view.getOutline ().getValueAt (3, 0).toString ());
        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (101, 0).toString ());
    }
    
    public void testAscendingSortingTreeWithCustomComparatorViaETable() throws InterruptedException, IllegalAccessException, InvocationTargetException {
        ETableColumnModel etcm = (ETableColumnModel) view.getOutline ().getColumnModel ();
        ETableColumn etc = (ETableColumn) etcm.getColumn (0); // tree column
        etc.setNestedComparator (testComarator);
        view.getOutline().setColumnSorted (0, true, 1); // ascending order
        view.expandNode (toExpand0_99);

        assertEquals ("[1-index from 0 to 99]", view.getOutline ().getValueAt (0, 0).toString ());
        assertEquals ("[0]", view.getOutline ().getValueAt (1, 0).toString ());
        assertEquals ("[1]", view.getOutline ().getValueAt (2, 0).toString ());
        assertEquals ("[2]", view.getOutline ().getValueAt (3, 0).toString ());
        assertEquals ("[2-index from 200 to 299]", view.getOutline ().getValueAt (101, 0).toString ());
    }
    
    public void testColumnSortability() throws Exception {
        ETableColumnModel etcm = (ETableColumnModel) view.getOutline().getColumnModel();
        ETableColumn etc = (ETableColumn) etcm.getColumn(1);
        boolean sortable = etc.isSortingAllowed();
        assertEquals("Has to be sortable, initially.", true, sortable);
        view.setPropertyColumnAttribute("unitTestPropName", "SortableColumn", Boolean.FALSE);
        sortable = etc.isSortingAllowed();
        assertEquals("Should not be sortable after attribute change.", false, sortable);
        view.setPropertyColumnAttribute("unitTestPropName", "SortableColumn", Boolean.TRUE);
        sortable = etc.isSortingAllowed();
        assertEquals("Sortable, again.", true, sortable);
    }
    
    public void testPropertiesPersistence() throws Exception {
        OutlineView ov = new OutlineView ("test-outline-view-component");
        Outline outline = ov.getOutline();
        ov.addPropertyColumn("c1", "Column 1", "Description 1");
        ov.addPropertyColumn("c2", "Column 2", "Description 2");
        Properties p = new Properties();
        outline.writeSettings(p, "test");
        
        OutlineView ov2 = new OutlineView ("test-outline-view-component");
        Outline outline2 = ov2.getOutline();
        outline2.readSettings(p, "test");
        
        int cc = outline.getColumnCount();
        int cc2 = outline2.getColumnCount();
        assertEquals("Column count", cc, cc2);
        for (int c = 0; c < cc; c++) {
            String cn = outline.getColumnName(c);
            String cn2 = outline2.getColumnName(c);
            assertEquals("Column "+c+" name", cn, cn2);
            OutlineViewOutlineColumn oc = (OutlineViewOutlineColumn) outline.getColumnModel().getColumn(c);
            OutlineViewOutlineColumn oc2 = (OutlineViewOutlineColumn) outline2.getColumnModel().getColumn(c);
            String shortDescription = oc.getShortDescription(null);
            String shortDescription2 = oc2.getShortDescription(null);
            assertEquals("Column "+c+" short description", shortDescription, shortDescription2);
        }
        
        ETableColumnModel etcm = (ETableColumnModel) outline2.getColumnModel();
        etcm.setColumnHidden(etcm.getColumn(1), true);
        outline2.writeSettings(p, "test");
        
        ov2 = new OutlineView ("test-outline-view-component");
        outline2 = ov2.getOutline();
        outline2.readSettings(p, "test");
        cc2 = outline2.getColumnCount();
        assertEquals("Column count", cc - 1, cc2);
        OutlineViewOutlineColumn oc = (OutlineViewOutlineColumn) outline.getColumnModel().getColumn(2);
        OutlineViewOutlineColumn oc2 = (OutlineViewOutlineColumn) outline2.getColumnModel().getColumn(1);
        String shortDescription = oc.getShortDescription(null);
        String shortDescription2 = oc2.getShortDescription(null);
        assertEquals("Last column short description", shortDescription, shortDescription2);
    }

    /**
     * Test for bug 236331 - After reading the persistence settings all the
     * OutlineView property cells become empty.
     *
     * @throws Exception
     */
    public void testPropertiesPersistence2() throws Exception {

        TestNode[] childrenNodes = new TestNode[2];
        childrenNodes[0] = new TestNode(Children.LEAF, "First");
        childrenNodes[1] = new TestNode(Children.LEAF, "Second");
        Children.Array children = new Children.Array();
        children.add(childrenNodes);
        Node rootNode = new TestNode(children, "Invisible Root");

        OutlineViewComponentWithLabels comp
                = new OutlineViewComponentWithLabels(rootNode);
        OutlineView ov = comp.getOutlineView();
        Outline o = ov.getOutline();

        ov.expandNode(rootNode);
        o.moveColumn(1, 0);
        assertEquals("First", getDummyValue(o.getValueAt(0, 0)));
        assertEquals("Second", getDummyValue(o.getValueAt(1, 0)));

        Properties p = new Properties();
        o.writeSettings(p, "test");

        OutlineViewComponentWithLabels comp2
                = new OutlineViewComponentWithLabels(rootNode);
        OutlineView ov2 = comp2.getOutlineView();
        Outline o2 = ov2.getOutline();

        ov2.readSettings(p, "test");
        ov2.expandNode(rootNode);
        assertNotSame(o, o2);

        // ensure the order of columns was restored
        assertEquals("First", getDummyValue(o2.getValueAt(0, 0)));
        assertEquals("Second", getDummyValue(o2.getValueAt(1, 0)));
    }

    private String getDummyValue(Object prop) {
        if (prop instanceof TestNode.DummyProperty) {
            TestNode.DummyProperty dummyProp = (TestNode.DummyProperty) prop;
            Object val = dummyProp.getValue("unitTestPropName");
            return val == null ? null : val.toString();
        } else {
            fail("DummyProperty expected, but was " + prop
                    + " (" + (prop == null ? "null" : prop.getClass()) + "). "
                    + "The order of columns is probably incorrect.");
            return null;
        }
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

    /**
     * View component with outline where column display names are different from
     * column property names. Used in {@link #testPropertiesPersistence2()}.
     */
    private class OutlineViewComponentWithLabels extends JPanel implements
            ExplorerManager.Provider {

        private final ExplorerManager manager = new ExplorerManager();
        private OutlineView view;

        private OutlineViewComponentWithLabels(Node rootNode) {
            setLayout(new BorderLayout());
            manager.setRootContext(rootNode);

            view = new OutlineView("test-outline-view-component");
            view.setPropertyColumns("unitTestPropName", "TestProperty");

            view.getOutline().setRootVisible(false);

            add(view, BorderLayout.CENTER);
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

        public OutlineView getOutlineView() {
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

    private Comparator testComarator = new Comparator () {

        public int compare (Object o1, Object o2) {
            assertTrue (o1 + " instanceof String", o1 instanceof Node);
            assertTrue (o2 + " instanceof String", o2 instanceof Node);
            Node n1 = (Node) o1;
            Node n2 = (Node) o2;

            // my comparator
            return getInteger (n1.getDisplayName ()).compareTo (getInteger (n2.getDisplayName ()));
        }

        private Integer getInteger (Object o) {
            String s = o.toString ();
            assertTrue (s + "startsWith (\"[\") && s.endsWith (\"]\")", s.startsWith ("[") && s.endsWith ("]"));
            int end = s.indexOf ("-");
            if (end != -1) {
                s = s.substring (1, end);
            } else {
                s = s.substring (1, s.length () - 1);
            }
            //System.out.println ("###: " + o.toString () + " => " + Integer.parseInt (s));
            return Integer.parseInt (s);
        }
    };
}
