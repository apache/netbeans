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
