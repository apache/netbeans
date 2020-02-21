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

package org.netbeans.modules.cnd.remote.projectui.wizard.ide;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** The util methods for projectui module.
 *
 */
public class ProjectUtilities {
    
    private ProjectUtilities() {}

    /** Makes the project tab visible
     * @param requestFocus if set to true the project tab will not only become visible but also
     *        will gain focus
     */
    public static void makeProjectTabVisible() {
        return;
        //if (Boolean.getBoolean("project.tab.no.selection")) {
        //    return;
        //}
        //ProjectTab ptLogical = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);
        //ptLogical.open();
        //ptLogical.requestActive();
    }
    
    public static void selectAndExpandProject( final Project p ) {
        
        // invoke later to select the being opened project if the focus is outside ProjectTab
        SwingUtilities.invokeLater (new Runnable () {
            final String ID_LOGICAL = "projectTabLogical_tc"; // NOI18N                            
            final TopComponent ptLogial = WindowManager.getDefault().findTopComponent( ID_LOGICAL );
            @Override
            public void run () {
                if (ptLogial instanceof ExplorerManager.Provider) {
                    Node root = ((ExplorerManager.Provider)ptLogial).getExplorerManager ().getRootContext ();
                    // Node projNode = root.getChildren ().findChild( p.getProjectDirectory().getName () );
                    Node projNode = null;
                    for (Node n : root.getChildren().getNodes()) {
                        Project prj = n.getLookup().lookup(Project.class);
                        if (prj != null && prj.getProjectDirectory().equals(p.getProjectDirectory())) {
                            projNode = n;
                            break;
                        }
                    }
                    if (projNode == null) {
                        // fallback..
                        projNode = root.getChildren ().findChild( ProjectUtils.getInformation( p ).getName() );
                    }

                    if ( projNode != null ) {
                        try {                            
                            ((ExplorerManager.Provider)ptLogial).getExplorerManager ().setSelectedNodes( new Node[] { projNode } );
                            // Not in public inteface
                            //((ExplorerManager.Provider)ptLogial).expandNode( projNode );
                        } catch (Exception ignore) {
                            // may ignore it
                        }
                    }
                }
            }
        });
        
    }
    
    /** Invokes the preferred action on given object and tries to select it in
     * corresponding view, e.g. in logical view if possible otherwise
     * in physical project's view.
     * Note: execution this methods can invokes new threads to assure the action
     * is called in EQ.
     *
     * @param newDo new data object
     */   
    public static void openAndSelectNewObject (final DataObject newDo) {
        // call the preferred action on main class
        Mutex.EVENT.writeAccess (new Runnable () {
            @Override
            public void run () {
                final Node node = newDo.getNodeDelegate ();
                Action a = node.getPreferredAction();
                if (a instanceof ContextAwareAction) {
                    a = ((ContextAwareAction) a).createContextAwareInstance(node.getLookup ());
                }
                if (a != null) {
                    a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                }

                // next action -> expand && select main class in package view
                //final ProjectTab ptLogical = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);
                //final ProjectTab ptPhysical = ProjectTab.findDefault(ProjectTab.ID_PHYSICAL);
                //ProjectTab.RP.post(new Runnable() {
                //    public @Override void run() {
                //        ProjectTab tab = ptLogical;
                //        Node n = tab.findNode(newDo.getPrimaryFile());
                //        if (n == null) {
                //            tab = ptPhysical;
                //            n = tab.findNode(newDo.getPrimaryFile());
                //        }
                //        if (n != null) {
                //            tab.selectNode(n);
                //        }
                //    }
                //});
            }
        });
    }

    public static class WaitCursor implements Runnable {
        
        private boolean show;
        
        private WaitCursor( boolean show ) {
            this.show = show;
        }
       
        public static void show() {            
            invoke( new WaitCursor( true ) );
        }
        
        public static void hide() {
            invoke( new WaitCursor( false ) );            
        }
        
        private static void invoke( WaitCursor wc ) {
            if (GraphicsEnvironment.isHeadless()) {
                return;
            }
            if ( SwingUtilities.isEventDispatchThread() ) {
                wc.run();
            }
            else {
                SwingUtilities.invokeLater( wc );
            }
        }
        
        @Override
        public void run() {
            try {            
                JFrame f = (JFrame)WindowManager.getDefault ().getMainWindow ();
                Component c = f.getGlassPane ();
                c.setVisible ( show );
                c.setCursor (show ? Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR) : null);
            } 
            catch (NullPointerException npe) {
                Exceptions.printStackTrace(npe);
            }
        }
    }
    
}
