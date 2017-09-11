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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.openide.util.NbMutexEventProvider;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Tests for control selection mode on TreeView (test on BeanTreeView).
 * Note: here are used TreeView's method which hasn't been introduced yet,
 * will be introduced when the enh 11928 will be implemented.
 * Run test with attached patch in issue 11928.
 *
 * @author Jiri Rechtacek
 * @see "#11928"
 */
@RandomlyFails /* NB-Core-Build #3569 & #3577 timed out in:
Thread AWT-EventQueue-0
	at sun.awt.X11.XlibWrapper.XGetWindowProperty(XlibWrapper.java:-2)
	at sun.awt.X11.WindowPropertyGetter.execute(WindowPropertyGetter.java:70)
	at sun.awt.X11.WindowPropertyGetter.execute(WindowPropertyGetter.java:53)
	at sun.awt.X11.XAtom.getAtomListProperty(XAtom.java:615)
	at sun.awt.X11.XAtom.getAtomListPropertyList(XAtom.java:644)
	at sun.awt.X11.XProtocol.checkProtocol(XProtocol.java:37)
	at sun.awt.X11.XNETProtocol.doStateProtocol(XNETProtocol.java:273)
	at sun.awt.X11.XNETProtocol.supportsState(XNETProtocol.java:25)
	at sun.awt.X11.XWM.setExtendedState(XWM.java:1109)
	at sun.awt.X11.XFramePeer.setExtendedState(XFramePeer.java:332)
	at sun.awt.X11.XFramePeer.setupState(XFramePeer.java:128)
	at sun.awt.X11.XFramePeer.postInit(XFramePeer.java:77)
	at sun.awt.X11.XBaseWindow.init(XBaseWindow.java:117)
	at sun.awt.X11.XBaseWindow.<init>(XBaseWindow.java:150)
	at sun.awt.X11.XWindow.<init>(XWindow.java:88)
	at sun.awt.X11.XComponentPeer.<init>(XComponentPeer.java:101)
	at sun.awt.X11.XCanvasPeer.<init>(XCanvasPeer.java:22)
	at sun.awt.X11.XPanelPeer.<init>(XPanelPeer.java:27)
	at sun.awt.X11.XWindowPeer.<init>(XWindowPeer.java:53)
	at sun.awt.X11.XDecoratedPeer.<init>(XDecoratedPeer.java:36)
	at sun.awt.X11.XFramePeer.<init>(XFramePeer.java:41)
	at sun.awt.X11.XToolkit.createFrame(XToolkit.java:349)
	at java.awt.Frame.addNotify(Frame.java:491)
	at java.awt.Window.pack(Window.java:485)
	at org.openide.windows.DummyWindowManager.topComponentOpen(DummyWindowManager.java:338)
	at org.openide.windows.TopComponent.open(TopComponent.java:434)
	at org.openide.explorer.ExplorerPanel.open(ExplorerPanel.java:196)
	at org.openide.explorer.ExplorerPanel.open(ExplorerPanel.java:189)
	at org.openide.explorer.view.SelectionModeTest.setUp(SelectionModeTest.java:112)
 */
public class SelectionModeTest extends NbTestCase {
    
    ExplorerManager mgr;
    TreeView tree;
    Node[] singleSelection, contiguousSelection, discontiguousSelection;
    
    public SelectionModeTest(String name) {
        super(name);
    }
   
    @Override
    protected boolean runInEQ() {
        return true;
    }

    protected @Override int timeOut() {
        return 500000;
    }
    
    /** Create tree and a selection of nodes for test.
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void setUp () {
        System.setProperty ("org.openide.util.Lookup", Lkp.class.getName());
        
        // create tree:
        // root +--- parent_one +--- one1
        //                      |--- one2
        //      |--- parent_two +--- two1
        //      |--- leaf
        
        final Children parents = new Children.Array ();
        Node root = new AbstractNode (parents);
        root.setName ("root");
        
        tree = new BeanTreeView ();
        //tree = new ContextTreeView ();

        final org.openide.explorer.ExplorerPanel p = new org.openide.explorer.ExplorerPanel();
        p.setName ("SelectionModeTest");
        
        p.add (tree, BorderLayout.CENTER);
        p.getExplorerManager ().setRootContext (root);
        p.open ();

        final Children ones = new Children.Array ();
        Node parent_one = new AbstractNode (ones);
        parent_one.setName ("parent_one");
        final Children twos = new Children.Array ();
        Node parent_two = new AbstractNode (twos);
        parent_two.setName ("parent_two");

        final Node one1 = new AbstractNode (Children.LEAF);
        one1.setName("one1");

        final Node one2 = new AbstractNode (Children.LEAF);
        one2.setName("one2");

        ones.add(new Node[] { one1, one2 });
        
        final Node two1 = new AbstractNode (Children.LEAF);
        two1.setName("two1");

        twos.add (new Node[] { two1 });
        
        parents.add (new Node[] { parent_one, parent_two });
        
        
        // the test selections
        singleSelection = new Node[] {parent_two};
        contiguousSelection = new Node[] {one1, one2};
        discontiguousSelection = new Node[] {one2, two1};
        
        mgr = p.getExplorerManager();
    }

    /** Test set all nodes selections if the mode SINGLE_TREE_SELECTION is set.
     * @throws Exception  */    
    public void testSingleSelectionMode () throws Exception {
        // try setSelectionMode; if not present then fail
        setSelectionMode (tree, TreeSelectionModel.SINGLE_TREE_SELECTION);
        PropertyVetoException exp = null;
        
        // single
        try {
            // have to be equal
            assertTrue ("[MODE: SINGLE_TREE_SELECTION][NODES: single node] getSelectedNodes is NOT equal setSelectedNodes.", // NO18N
                trySelection (mgr, singleSelection));
        } catch (PropertyVetoException e) {
            // no exp should be thrown
            fail ("[MODE: SINGLE_TREE_SELECTION][NODES: single node] PropertyVetoException can't be thrown."); // NO18N
        }
        
        // contiguous
        try {
            exp = null;
            // cont' be equal
            assertTrue ("[MODE: SINGLE_TREE_SELECTION][NODES: two nodes contiguous] Can't be getSelectedNodes equal setSelectedNodes.", // NO18N
                !trySelection (mgr, contiguousSelection));
        } catch (PropertyVetoException e) {
            // exp should be thrown
            exp = e;
        } finally {
            if (exp==null)
                fail ("[MODE: SINGLE_TREE_SELECTION][NODES: two nodes contiguous] PropertyVetoException was NOT thrown."); // NO18N
        }
        
        // discontiguous
        try {
            exp = null;
            assertTrue ("[MODE: SINGLE_TREE_SELECTION][NODES: two nodes discontiguous] Can't be getSelectedNodes equal setSelectedNodes.", // NO18N
                trySelection (mgr, discontiguousSelection));
        } catch (PropertyVetoException e) {
            // exp should be thrown
            exp = e;
        } finally {
            if (exp==null)
                fail ("[MODE: SINGLE_TREE_SELECTION][NODES: two nodes discontiguous] PropertyVetoException was NOT thrown."); // NO18N
        }
        
    }

    /** Test set all nodes selections if the mode CONTIGUOUS_TREE_SELECTION is set.
     * @throws Exception  */
    @RandomlyFails // NB-Core-Build #1074 & 1077
    public void testContigousSelection () throws Exception {
        // try setSelectionMode; if not present then fail
        setSelectionMode (tree, TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        PropertyVetoException exp = null;
        
        // single
        try {
            // have to be equal
            assertTrue ("[MODE: CONTIGUOUS_TREE_SELECTION][NODES: single node] getSelectedNodes is NOT equal setSelectedNodes.", // NO18N
                trySelection (mgr, singleSelection));
        } catch (PropertyVetoException e) {
            // no exp should be thrown
            fail ("[MODE: CONTIGUOUS_TREE_SELECTION][NODES: single node] PropertyVetoException can't be thrown."); // NO18N
        }
        
        // contiguous
        try {
            exp = null;
            // have to be equal
            assertTrue ("[MODE: CONTIGUOUS_TREE_SELECTION][NODES: two nodes contiguous] getSelectedNodes is NOT equal setSelectedNodes.", // NO18N
                trySelection (mgr, contiguousSelection));
        } catch (PropertyVetoException e) {
            // no exp should be thrown
            fail ("[MODE: CONTIGUOUS_TREE_SELECTION][NODES: two nodes contiguous] PropertyVetoException can't be thrown."); // NO18N
        }
        
        // discontiguous
        try {
            // cont' be equal
            exp = null;
            assertTrue ("[MODE: CONTIGUOUS_TREE_SELECTION][NODES: two nodes discontiguous] Can't be getSelectedNodes equal setSelectedNodes.", // NO18N
                trySelection (mgr, discontiguousSelection));
        } catch (PropertyVetoException e) {
            // exp should be thrown
            exp = e;
        } finally {
            if (exp==null)
                fail ("[MODE: CONTIGUOUS_TREE_SELECTION][NODES: two nodes discontiguous] PropertyVetoException was NOT thrown."); // NO18N
        }
        
    }

    /** Test set all nodes selections if the mode DISCONTIGUOUS_TREE_SELECTION is set.
     * @throws Exception  */    
    public void testDiscontigousSelection () throws Exception {
        // try setSelectionMode; if not present then fail
        setSelectionMode (tree, TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        
        // single
        try {
            // have to be equal
            assertTrue ("[MODE: DISCONTIGUOUS_TREE_SELECTION][NODES: single node] getSelectedNodes is NOT equal setSelectedNodes.", // NO18N
                trySelection (mgr, singleSelection));
        } catch (PropertyVetoException e) {
            // no exp should be thrown
            fail ("[MODE: DISCONTIGUOUS_TREE_SELECTION][NODES: single node] PropertyVetoException can't be thrown."); // NO18N
        }
        
        // contiguous
        try {
            // have to be equal
            assertTrue ("[MODE: DISCONTIGUOUS_TREE_SELECTION][NODES: two nodes contiguous] getSelectedNodes is NOT equal setSelectedNodes.", // NO18N
                trySelection (mgr, contiguousSelection));
        } catch (PropertyVetoException e) {
            // no exp should be thrown
            fail ("[MODE: DISCONTIGUOUS_TREE_SELECTION][NODES: two nodes contiguous] PropertyVetoException can't be thrown."); // NO18N
        }
        
        // discontiguous
        try {
            // have to be equal
            assertTrue ("[MODE: DISCONTIGUOUS_TREE_SELECTION][NODES: two nodes discontiguous] Can't be getSelectedNodes equal setSelectedNodes.", // NO18N
                trySelection (mgr, discontiguousSelection));
        } catch (PropertyVetoException e) {
            // no exp should be thrown
            fail ("[MODE: DISCONTIGUOUS_TREE_SELECTION][NODES: two nodes discontiguous] PropertyVetoException was NOT thrown."); // NO18N
        }
        
    }
    
    
    /** Try set array of nodes and check a array which is get back.
     * @param mgr Explorer manager
     * @param selected arrar of nodes which will be set
     * @throws PropertyVetoException may be thrown from setSelecedNodes
     * @return  true if Explorer Manager returned same array as was set.*/    
    private boolean trySelection (ExplorerManager mgr, Node[] selected) throws PropertyVetoException {
        mgr.setSelectedNodes (selected);
        if (selected!=null) {
            return Arrays.equals (selected, mgr.getSelectedNodes ());
        }
        return true;
    }
    
    /** Set selection on TreeView if the method is present. If not then the test failed.
     * @param TreeView tree instance TreeView
     * @param int mode selection mode */    
    private void setSelectionMode (TreeView tree, int mode) {
        try {
            Class c = tree.getClass ();
            Method m = c.getMethod ("setSelectionMode", new Class[] {Integer.TYPE});
            m.invoke (tree, new Object[] {new Integer (mode)});
        } catch (NoSuchMethodException nsme) {
            fail ("The method setSelectionMode can't be called on this object. See enh #11928.");
        } catch (IllegalAccessException iae) {
            fail ("IllegalAccessException thrown from setSelectionMode.");
        } catch (InvocationTargetException ite) {
            fail ("InvocationTargetException thrown from setSelectionMode.");
        }
    }

    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            setLookups(Lookups.singleton(new NbMutexEventProvider()));
        }
    }

}
