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
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;


/**
 * Test whether the old behaviour of ExplorerPanel is correctly simulated
 * by new API. Inherits testing methods from ExplorerPanel tests, just
 * setup is changed.
 *
 * @author Jaroslav Tulach
 */
public class ExplorerActionsCompatTest extends ExplorerPanelTest {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ExplorerActionsCompatTest.class);
    }

    public ExplorerActionsCompatTest(java.lang.String testName) {
        super(testName);
    }
    
    /** Creates a manager to operate on.
     */
    protected Object[] createManagerAndContext (boolean confirm) {
        ExplorerManager em = new ExplorerManager ();
        ActionMap map = new ActionMap ();
        map.put (DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(em));
        map.put (DefaultEditorKit.cutAction, ExplorerUtils.actionCut(em));
        map.put (DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(em));
        map.put ("delete", ExplorerUtils.actionDelete(em, confirm));
        
        return new Object[] { em, org.openide.util.lookup.Lookups.singleton(map) };
    }
    
    /** Instructs the actions to stop/
     */
    protected void stopActions(ExplorerManager em) {
        ExplorerUtils.activateActions (em, false);
    }
    /** Instructs the actions to start again.
     */
    protected void startActions (ExplorerManager em) {
        ExplorerUtils.activateActions (em, true);
    }
    
    
    public void testActionDeleteDoesNotAffectStateOfPreviousInstances () throws Exception {
        ExplorerManager em = new ExplorerManager ();
        Action a1 = ExplorerUtils.actionDelete(em, false);
        Action a2 = ExplorerUtils.actionDelete(em, true);
        
        Node node = new AbstractNode (Children.LEAF) {
            public boolean canDestroy () {
                return true;
            }
        };
        em.setRootContext(node);
        em.setSelectedNodes(new Node[] { node });
        
        assertTrue ("A1 enabled", a1.isEnabled());
        assertTrue ("A2 enabled", a2.isEnabled());
        
        // this should not show a dialog
        a1.actionPerformed (new java.awt.event.ActionEvent (this, 0, ""));
    }
}
