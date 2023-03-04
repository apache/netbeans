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

package org.openide.explorer;

import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import org.netbeans.junit.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import java.util.Arrays;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.openide.util.io.NbMarshalledObject;

/** Testing behaviour of ExplorerActions in order to fix 33566
 */
public class ExplorerActionsTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ExplorerActionsTest.class);
    }

    static {
        // initialize special TopComponent.Registry
        Object x = ActionsInfraHid.UT;
    }
    
    private static javax.swing.Action delete = org.openide.util.actions.SystemAction.get (
        org.openide.actions.DeleteAction.class
    );
    
    public ExplorerActionsTest (String name) {
        super(name);
    }
    
    protected Level logLevel() {
        return Level.FINER;
    }

    protected boolean runInEQ() {
        return true;
    }

    public void testGlobalStateInExplorerActionsIsImportant () throws Exception {
        EP panel = new EP (null);
        ExplorerPanel.setConfirmDelete(false);
        
        doDelete (panel);
    }
    
    public void testGlobalStateCanBeOverriden () throws Exception {
        ExplorerActions actions = new ExplorerActions ();
        actions.setConfirmDelete (false);
        
        ExplorerPanel.setConfirmDelete(true);
        EP panel = new EP (actions);

        doDelete (panel);
    }
    
    public void testGlobalStateOnDeserializedPanel () throws Exception {
        EP panel = new EP (null);
        ExplorerPanel.setConfirmDelete(false);
        setupExplorerManager (panel.getExplorerManager());
        
        NbMarshalledObject mar = new NbMarshalledObject (panel);
        Object obj = mar.get ();
        EP deserializedPanel = (EP) obj;
        
        // activate the actions
        ActionsInfraHid.UT.setActivated (deserializedPanel);
        deserializedPanel.componentActivated();
        
        ActionsInfraHid.UT.setCurrentNodes (deserializedPanel.getExplorerManager().getRootContext ().getChildren ().getNodes ());
        
        // deletes without asking a question, if the question appears something
        // is wrong
        delete.actionPerformed(new java.awt.event.ActionEvent (this, 0, ""));
    }
    
    /** Performs a delete */
    
    private void doDelete (EP panel) throws Exception {
        setupExplorerManager (panel.getExplorerManager());
        // activate the actions
        ActionsInfraHid.UT.setActivated (panel);
        panel.componentActivated();
        
        ActionsInfraHid.UT.setCurrentNodes (panel.getExplorerManager().getSelectedNodes());
        assertTrue ("Delete is allowed", delete.isEnabled());
        
        // deletes without asking a question, if the question appears something
        // is wrong
        delete.actionPerformed(new java.awt.event.ActionEvent (this, 0, ""));
    }
    
    private static class RootNode extends AbstractNode {
        public RootNode () {
            super (new Children.Array ());
        }
        public Node.Handle getHandle () {
            return new H ();
        }
        private static class H implements Node.Handle {
            H() {}
            static final long serialVersionUID = -5158460093499159177L;
            public Node getNode () throws java.io.IOException {
                Node n = new RootNode ();
                n.getChildren().add (new Node[] {
                    new Del ("H1"), new Del ("H2")
                });
                return n;
            }
        }
    }

    private static class Del extends AbstractNode {
        public Del (String name) {
            super (Children.LEAF);
            setName (name);
        }
        public boolean canDestroy () {
            return true;
        }
    }
    
    /** Setups an explorer manager to be ready to delete something.
     * @param em manager 
     */
    private static void setupExplorerManager (ExplorerManager em) throws Exception {
        AbstractNode root = new RootNode ();
        Node[] arr = new Node[] {
            new Del ("1"), new Del ("2")
        };
        root.getChildren().add (arr);
        
        em.setRootContext(root);
        em.setSelectedNodes(root.getChildren().getNodes());
        
        assertEquals (
            "Same nodes selected", 
            Arrays.asList (arr),
            Arrays.asList (root.getChildren ().getNodes ())
        );
    }
    
    /** Special ExplorerPanel that has method how to actiavate itself.
     */
    private static class EP extends ExplorerPanel {
        private ExplorerActions actions;
        Node rootNode = null;
        
        public EP () {
        }

        public EP (ExplorerActions actions) {
            this.actions = actions;
        }
        
        public void componentActivated () {
            super.componentActivated ();
            if (actions != null) {
                actions.attach(getExplorerManager ());
            }
        }
        
    }
}
