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

import java.awt.GraphicsEnvironment;
import javax.swing.ListSelectionModel;
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
 * Note: It's just helper test for investigate issue 37875 (failing RootContextTest)
 * Will be removed when issue 37875 is closed.
 *
 * @author  Jiri Rechtacek
 */
public class SelectNodeInListViewTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(SelectNodeInListViewTest.class);
    }

    public SelectNodeInListViewTest (java.lang.String testName) {
        super (testName);
    }
    
    /** Run all tests in AWT thread */
    protected boolean runInEQ () {
        return true;
    }
    
    public void testSelectedNodeInListView () throws Exception {
    
        // helper variables
        Node[] arr1, arr2;
        Node root1, root2;
    
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
        
        ListView view = new ListView ();
        view.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        ExplorerPanel panel = new ExplorerPanel ();
        ExplorerManager mgr = panel.getExplorerManager ();
        
        panel.add (view);
        panel.open ();
        
        Node[] selNodes = mgr.getSelectedNodes ();
        log ("Default root is " + mgr.getRootContext ());
        //assertNull ( "Root context is null", mgr.getRootContext ());
        assertEquals ("Count of the selected node is ", 0, selNodes.length);

        mgr.setRootContext (root1);
        log ("New root is " + mgr.getRootContext ());
        mgr.setSelectedNodes (new Node[] {arr1[0], arr1[2]});

        selNodes = mgr.getSelectedNodes ();
        assertEquals ("Root context is ", "Root1", mgr.getRootContext ().getName ());
        assertEquals ("Count of the selected node is ", 2, selNodes.length);
        
    }

}
