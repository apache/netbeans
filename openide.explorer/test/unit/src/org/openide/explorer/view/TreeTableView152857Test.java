/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Children.Keys;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;

/** Fixes fix of issue #152857: Sorting in TreeTableView should not be done on Visualizers
 *
 * @author  Jiri Rechtacek
 */
@RandomlyFails // Thread.sleep (1000) is used.
public class TreeTableView152857Test extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TreeTableView152857Test.class);
    }

    private static final Logger LOG = Logger.getLogger(TreeTableView152857Test.class.getName());

    private TTV view;

    public TreeTableView152857Test (String testName) {
        super (testName);
    }

    @Override
    protected boolean runInEQ () {
        return false;
    }

    public void testRemoveNodeInTTV () throws InterruptedException {
        StringKeys children = new StringKeys (true);
        children.doSetKeys (new String [] {"1", "3", "2"});
        Node root = new TestNode (children, "root");
        view = new TTV (root);
        TreeNode ta = Visualizer.findVisualizer(root);

        DialogDescriptor dd = new DialogDescriptor (view, "", false, null);
        Dialog d = DialogDisplayer.getDefault ().createDialog (dd);
        makeVisible(d);
        ((StringKeys) root.getChildren ()).doSetKeys (new String [] {"1", "2"});
        Thread.sleep (1000);

        assertEquals ("Node on 0nd position is '1'", "1", ta.getChildAt (0).toString ());
        assertEquals ("Node on 1st position is '2'", "2", ta.getChildAt (1).toString ());

        d.setVisible (false);
    }

    public void testSetSelectedRow () throws PropertyVetoException, InterruptedException, InvocationTargetException {
        StringKeys children = new StringKeys (true);
        children.doSetKeys (new String [] {"1", "3", "2"});
        Node root = new TestNode (children, "root");
        Node aChild = root.getChildren ().getNodeAt (1);
        view = new TTV (root);
        DialogDescriptor dd = new DialogDescriptor (view, "", false, null);
        Dialog d = DialogDisplayer.getDefault ().createDialog (dd);
        makeVisible(d);
        SwingUtilities.invokeAndWait (new Runnable () {
            public void run () {
                view.view.tree.setSelectionRow (3);
            }
        });
        Thread.sleep (1000);
        Node [] selectedNodes = view.getExplorerManager ().getSelectedNodes ();
        assertNotNull ("A child found", selectedNodes);
        assertEquals ("Only once child", 1, selectedNodes.length);
        Node aSelectedChild = selectedNodes [0];
        assertEquals ("They are my children", aChild, aSelectedChild);
        d.setVisible (false);
    }

    private void makeVisible(Dialog d) throws InterruptedException {
        d.setVisible(true);
        while (!d.isShowing()) {
            LOG.log(Level.INFO, "Waiting for is showing: {0}", d);
            Thread.sleep (1000);
        }
    }

    public void testSelectedNodes () throws PropertyVetoException, InterruptedException, InvocationTargetException {
        StringKeys children = new StringKeys (true);
        children.doSetKeys (new String [] {"1", "3", "2"});
        Node root = new TestNode (children, "root");
        Node aChild = root.getChildren ().getNodeAt (1);
        view = new TTV (root);
        DialogDescriptor dd = new DialogDescriptor (view, "", false, null);
        Dialog d = DialogDisplayer.getDefault ().createDialog (dd);
        makeVisible(d);
        view.getExplorerManager ().setSelectedNodes (new Node [] { aChild });
        final int rows [] = new int [1];
        SwingUtilities.invokeAndWait (new Runnable () {

            public void run () {
                int [] selectedRows = view.view.tree.getSelectionRows ();
                assertNotNull ("Some rows are selected", selectedRows);
                assertEquals ("Only one selected row", 1, selectedRows.length);
                rows [0] = selectedRows [0];
            }
        });
        Thread.sleep (1000);
        assertEquals ("Child on 3rd position was selected", 3, rows [0]);
        d.setVisible (false);
    }

    @RandomlyFails // NB-Core-Build #7921: expected:<[3]> but was:<[1]> @ view.getTableValueAt(1)
    public void testSorting() throws PropertyVetoException, InterruptedException, InvocationTargetException {
        StringKeys children = new StringKeys(true);
        children.doSetKeys(new String[]{"1", "3", "2", "2", "1"});
        Node root = new TestNode(children, "root");
        TreeNode ta = Visualizer.findVisualizer(root);
        view = new TTV(root);
        DialogDescriptor dd = new DialogDescriptor(view, "", false, null);
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        makeVisible(d);

        view.sort(0, true);
        Thread.sleep(1000);
        assertEquals("1", ta.getChildAt(0).toString());
        assertEquals("3", ta.getChildAt(1).toString());
        assertEquals("2", ta.getChildAt(2).toString());
        assertEquals("2", ta.getChildAt(3).toString());
        assertEquals("1", ta.getChildAt(4).toString());

        assertEquals("1", view.getTableValueAt(1));
        assertEquals("1", view.getTableValueAt(2));
        assertEquals("2", view.getTableValueAt(3));
        assertEquals("2", view.getTableValueAt(4));
        assertEquals("3", view.getTableValueAt(5));


        view.sort(0, false);
        Thread.sleep(1000);
        assertEquals("1", ta.getChildAt(0).toString());
        assertEquals("3", ta.getChildAt(1).toString());
        assertEquals("2", ta.getChildAt(2).toString());
        assertEquals("2", ta.getChildAt(3).toString());
        assertEquals("1", ta.getChildAt(4).toString());

        assertEquals("3", view.getTableValueAt(1));
        assertEquals("2", view.getTableValueAt(2));
        assertEquals("2", view.getTableValueAt(3));
        assertEquals("1", view.getTableValueAt(4));
        assertEquals("1", view.getTableValueAt(5));
        Thread.sleep(1000);

        view.noSorting();
        Thread.sleep(1000);
        assertEquals("1", ta.getChildAt(0).toString());
        assertEquals("3", ta.getChildAt(1).toString());
        assertEquals("2", ta.getChildAt(2).toString());
        assertEquals("2", ta.getChildAt(3).toString());
        assertEquals("1", ta.getChildAt(4).toString());

        assertEquals("1", view.getTableValueAt(1));
        assertEquals("3", view.getTableValueAt(2));
        assertEquals("2", view.getTableValueAt(3));
        assertEquals("2", view.getTableValueAt(4));
        assertEquals("1", view.getTableValueAt(5));
        Thread.sleep(1000);

        d.setVisible(false);
    }

    @RandomlyFails // NB-Core-Build #7155: row 1 vs. count 1 with UI javax.swing.plaf.metal.MetalTreeUI@1f94884 (from view.getTableValueAt(1))
    public void testSetChildren() throws InterruptedException {
        TestNode root = new TestNode(Children.LEAF, "root");
        TreeNode ta = Visualizer.findVisualizer(root);
        view = new TTV(root);
        DialogDescriptor dd = new DialogDescriptor(view, "", false, null);
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        makeVisible(d);
        assertEquals(0, ta.getChildCount());

        view.sort(0, false);
        root.doSetChildren(new StringKeys("0", "1", "2"));
        Thread.sleep(1000);
        assertEquals(3, ta.getChildCount());
        assertEquals("2", view.getTableValueAt(1));
        assertEquals("1", view.getTableValueAt(2));
        assertEquals("0", view.getTableValueAt(3));

        view.sort(0, true);
        root.doSetChildren(new StringKeys("5", "6"));
        Thread.sleep(1000);
        assertEquals(2, ta.getChildCount());
        assertEquals("5", view.getTableValueAt(1));
        assertEquals("6", view.getTableValueAt(2));

        root.doSetChildren(Children.LEAF);
        Thread.sleep(1000);
        assertEquals(0, ta.getChildCount());
    }

    private static class StringKeys extends Keys<String> {

        public StringKeys (boolean lazy) {
            super (lazy);
        }

        public StringKeys(String ...keys) {
            super();
            setKeys(keys);
        }

        @Override
        protected Node[] createNodes (String key) {
            AbstractNode n = new TestNode (Children.LEAF, key);
            n.setName (key);
            return new Node[]{n};
        }

        void doSetKeys (String[] keys) {
            setKeys (keys);
        }
    }

    private class TTV extends JPanel implements ExplorerManager.Provider {

        private final ExplorerManager manager = new ExplorerManager ();
        private TreeTableView view;

        private TTV (Node rootNode) {
            setLayout (new BorderLayout ());
            manager.setRootContext (rootNode);

            Node.Property[] props = rootNode.getPropertySets ()[0].getProperties ();
            view = new TreeTableView ();
            view.setProperties (props);

            //view.setRootVisible (false);

            add (view, BorderLayout.CENTER);
        }

        public ExplorerManager getExplorerManager () {
            return manager;
        }

        String getTableValueAt(int pos) {
            return view.treeTable.getModel().getValueAt(pos, 0).toString();
        }

        void sort(int column, boolean ascending) {
            try {
                Method setSortingColumn = view.getClass().getDeclaredMethod("setSortingColumn", new Class[]{int.class});
                setSortingColumn.setAccessible(true);
                setSortingColumn.invoke(view, column);
                Method setSortingOrder = view.getClass().getDeclaredMethod("setSortingOrder", new Class[]{boolean.class});
                setSortingOrder.setAccessible(true);
                setSortingOrder.invoke(view, ascending);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        void noSorting() {
            try {
                Method noSorting = view.getClass().getDeclaredMethod("noSorting");
                noSorting.setAccessible(true);
                noSorting.invoke(view);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static class TestNode extends AbstractNode {

        public TestNode (String name) {
            super (Children.LEAF);
            setName (name);
        }

        public TestNode (Children children, String name) {
            super (children);
            setName (name);
        }

        void doSetChildren(Children ch) {
            setChildren(ch);
        }

        @Override
        protected Sheet createSheet () {
            Sheet s = super.createSheet ();
            Sheet.Set ss = s.get (Sheet.PROPERTIES);
            if (ss == null) {
                ss = Sheet.createPropertiesSet ();
                s.put (ss);
            }
            Property [] props = new Property [2];

            DummyProperty dp = new DummyProperty (getName ());
            dp.setValue ("ComparableColumnTTV", Boolean.TRUE);
            props [0] = dp;

            Property p_tree = new Node.Property<Boolean> (Boolean.class) {

                @Override
                public boolean canRead () {
                    return true;
                }

                @Override
                public Boolean getValue () throws IllegalAccessException, InvocationTargetException {
                    return Boolean.TRUE;
                }

                @Override
                public boolean canWrite () {
                    return false;
                }

                @Override
                public void setValue (Boolean val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                    throw new UnsupportedOperationException ("Not supported yet.");
                }
            };

            p_tree.setValue ("TreeColumnTTV", Boolean.TRUE);
            p_tree.setValue ("ComparableColumnTTV", Boolean.TRUE);
            p_tree.setValue ("SortingColumnTTV", Boolean.TRUE);
            props [1] = p_tree;

            ss.put (props);

            return s;
        }

        private class DummyProperty extends Property<String> {

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
