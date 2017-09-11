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

import java.awt.GraphicsEnvironment;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreeSelectionModel;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.junit.NbTestCase;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Children.Array;

/*
 * RootContextTest.java tests a change the root context and set selected node
 * in each root context.
 *
 * @author  Jiri Rechtacek
 */
public class RootContextTest extends NbTestCase {
    static {
        System.setProperty("sun.awt.datatransfer.timeout", "0");
    }
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(RootContextTest.class);
    }

    public RootContextTest (java.lang.String testName) {
        super (testName);
    }
    
    // helper variables
    Node[] arr1, arr2;
    Node root1, root2;
    boolean initialized = false;

    
    // initialize the test variables
    public void initTest () {
        
        // do init only once
        if (initialized) {
            return ;
        } else {
            initialized = true;
        }

        arr1 = new Node [3];
        arr1[0] = new AbstractNode (Children.LEAF);
        arr1[0].setName ("One");
        
        arr1[1] = new AbstractNode (Children.LEAF);
        arr1[1].setName ("Two");
        
        arr1[2] = new AbstractNode (Children.LEAF);
        arr1[2].setName ("Three");
        
        Array ch1 = new Array ();
        ch1.add (arr1);
        
        arr2 = new Node [3];
        arr2[0] = new AbstractNode (Children.LEAF);
        arr2[0].setName ("Aaa");
        
        arr2[1] = new AbstractNode (Children.LEAF);
        arr2[1].setName ("Bbb");
        
        arr2[2] = new AbstractNode (Children.LEAF);
        arr2[2].setName ("Ccc");

        Array ch2 = new Array ();
        ch2.add (arr2);
        
        root1 = new AbstractNode (ch1);
        root1.setName ("Root1");
        root2 = new AbstractNode (ch2);
        root2.setName ("Root2");
        
    }
    
    /** Run all tests in AWT thread */
    protected boolean runInEQ() {
        return true;
    }
    
    // assure the node selections with given manager
    public void doViewTest (final ExplorerManager mgr) throws Exception {
        mgr.setRootContext (root1);
        mgr.setSelectedNodes (new Node[] {arr1[0], arr1[2]});

        Node[] selNodes = mgr.getSelectedNodes ();
        assertEquals ("Root context is ", "Root1", mgr.getRootContext ().getName ());
        assertEquals ("Count of the selected node is ", 2, selNodes.length);
        // pending: an order migth be different
        //Arrays.sort (selNodes);
        assertEquals ("Selected node is ", "One", selNodes[0].getName ());
        assertEquals ("Selected node is ", "Three", selNodes[1].getName ());

        mgr.setRootContext (root2);
        mgr.setSelectedNodes(new Node[] { arr2[1] });

        selNodes = mgr.getSelectedNodes ();
        assertEquals ("Root context is ", "Root2", mgr.getRootContext ().getName ());
        assertEquals ("Count of the selected node is ", 1, selNodes.length);
        assertEquals ("Selected node is ", "Bbb", selNodes[0].getName ());

    }
    
    // test for each type of view
    
    public void testBeanTreeView() throws Exception {
        
        initTest ();
        
        TreeView view = new BeanTreeView ();
        view.setSelectionMode (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        
        ExplorerPanel panel = new ExplorerPanel ();
        //#47021
        panel.setName("preferredID");
        ExplorerManager mgr = panel.getExplorerManager ();
        
        panel.add (view);
        panel.open ();
        
        doViewTest(mgr);
        
    }
    
    public void testContextTreeView() throws Exception {
        
        initTest ();
        
        TreeView view = new ContextTreeView ();
        view.setSelectionMode (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        
        ExplorerPanel panel = new ExplorerPanel ();
        //#47021
        panel.setName("preferredID");
        ExplorerManager mgr = panel.getExplorerManager ();
        
        panel.add (view);
        panel.open ();
        
        doViewTest(mgr);
        
    }
    
    public void testTreeTableView() throws Exception {
        
        initTest ();
        
        TreeTableView view = new TreeTableView ();
        view.setSelectionMode (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        
        ExplorerPanel panel = new ExplorerPanel ();
        //#47021
        panel.setName("preferredID");
        ExplorerManager mgr = panel.getExplorerManager ();
        
        panel.add (view);
        panel.open ();
        
        doViewTest(mgr);
        
    }
    
    public void testListView() throws Exception {
        
        initTest ();
        
        ListView view = new ListView (); 
        view.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        ExplorerPanel panel = new ExplorerPanel ();
        //#47021
        panel.setName("preferredID");
        ExplorerManager mgr = panel.getExplorerManager ();
        
        panel.add (view);
        panel.open ();
        
        doViewTest(mgr);

    }
    
    public void testListTableView() throws Exception {
        
        initTest ();
        
        ListTableView view = new ListTableView ();
        view.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        ExplorerPanel panel = new ExplorerPanel ();
        //#47021
        panel.setName("preferredID");
        ExplorerManager mgr = panel.getExplorerManager ();
        
        panel.add (view);
        panel.open ();
        
        doViewTest(mgr);
        
    }
    
}
