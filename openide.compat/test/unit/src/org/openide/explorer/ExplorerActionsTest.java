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
