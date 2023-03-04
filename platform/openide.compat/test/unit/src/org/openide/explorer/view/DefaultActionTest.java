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

/*
 *
 */
package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JScrollPane;
import junit.framework.Test;
import junit.framework.TestSuite;



import org.netbeans.junit.NbTestCase;

import org.openide.explorer.ExplorerPanel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;


/**
 * Test DefaulAction of node selected in a view.
 * @author Jiri Rechtacek
 */
public class DefaultActionTest extends NbTestCase {
    static {
        System.setProperty("sun.awt.datatransfer.timeout", "0");
    }
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(DefaultActionTest.class);
    }

    public DefaultActionTest (String name) {
        super(name);
    }
   
    @Override protected boolean runInEQ() {
        return true;
    }
    
    private boolean performed;
    private Node root, investigatedNode;
    private List fails = new ArrayList ();
    
    protected void setUp () {
        final Children children = new Children.Array ();
        root = new AbstractNode (children);
        final NodeAction a = new NodeAction () {
            protected void performAction (Node[] activatedNodes) {
                assertActionPerformed (activatedNodes);
            }                 
            
            public boolean asynchronous () {
                return false;
            }

            protected boolean enable (Node[] activatedNodes) {
                return true;
            }
            
            public boolean isEnabled () {
                return true;
            }
            
            public HelpCtx getHelpCtx () {
                return null;
            }
            
            public String getName () {
                return "Test default action";
            }
        };
        investigatedNode = new AbstractNode (Children.LEAF, Lookup.EMPTY) {
            public String getName () {
                return "Node with default action";
            }
            public Action getPreferredAction () {
                return a;
            }
        };
        children.add (new Node[] { investigatedNode });
    }
    
    private TopComponent prepareExplorerPanel (JScrollPane view) {
        final ExplorerPanel p = new ExplorerPanel ();
        p.setSize (200, 200);
        p.add (view, BorderLayout.CENTER);
        p.getExplorerManager ().setRootContext (root);

        try {
            p.getExplorerManager ().setSelectedNodes (root.getChildren().getNodes ());
        } catch (PropertyVetoException pve) {
            fail (pve.getMessage ());
        }
        
        return p;
    }

    private void invokeDefaultAction (final TopComponent tc) {
        performed = false;
        try {
            Node[] nodes = tc.getActivatedNodes ();
            assertNotNull ("View has the active nodes.", nodes);
            Node n = nodes.length > 0 ? nodes[0] : null;
            assertNotNull ("View has a active node.", n);
            
            final Action action = n.getPreferredAction ();
            action.actionPerformed (new ActionEvent (n, ActionEvent.ACTION_PERFORMED, ""));
            
            // wait to invoke action is propagated
            Thread.sleep (300);
        } catch (Exception x) {
            fail (x.getMessage ());
        }
    }
    
    public void testNodeInLoopup () throws Exception {
        TreeView tv = new BeanTreeView ();
        TopComponent tc = prepareExplorerPanel (tv);
        TreeView.PopupSupport supp = tv.defaultActionListener;
        tv.manager = ((ExplorerPanel)tc).getExplorerManager ();
        supp.actionPerformed (null);
        assertDefaultActionWasPerformed ("BeanTreeView");
    }
    
    public void testListView () {
        TopComponent tc = prepareExplorerPanel (new ListView ());
        invokeDefaultAction (tc);
        assertDefaultActionWasPerformed ("ListView");
        tc.close ();
    }
    
    public void testBeanTreeView () {
        TopComponent tc = prepareExplorerPanel (new BeanTreeView ());
        invokeDefaultAction (tc);
        assertDefaultActionWasPerformed ("BeanTreeView");
    }
    
    public void testTreeTableView () {
        TopComponent tc = prepareExplorerPanel (new TreeTableView ());
        invokeDefaultAction (tc);
        assertDefaultActionWasPerformed ("TreeTableView");
    }
    
    public void testTreeContextView () {
        TopComponent tc = prepareExplorerPanel (new ContextTreeView ());
        invokeDefaultAction (tc);
        assertDefaultActionWasPerformed ("ContextTreeView");
    }
    
    void assertDefaultActionWasPerformed (String nameOfView) {
        assertTrue ("[" + nameOfView + "] DefaultAction was preformed.", performed);
    }
    
    void assertActionPerformed (Node[] nodes) {
        log ("Action performed.");
        assertNotNull ("Activated nodes exist.", nodes);
        assertEquals ("Only one node is activated.", 1, nodes.length);
        assertEquals ("It's the testedNode.", investigatedNode, nodes[0]);
        performed = true;
    }
    
}
