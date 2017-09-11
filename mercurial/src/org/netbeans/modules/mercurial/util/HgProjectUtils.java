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

package org.netbeans.modules.mercurial.util;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.util.ContextAwareAction;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.netbeans.modules.mercurial.Mercurial;
import java.util.logging.Level;
import org.netbeans.api.project.ProjectInformation;


public class HgProjectUtils {
    private static final String ProjectTab_ID_LOGICAL = "projectTabLogical_tc"; // NOI18N    
    
    public static void renameProject(Project p, Object caller) {
        if( p == null) return;
        
        ContextAwareAction action = (ContextAwareAction) CommonProjectActions.renameProjectAction();
        Lookup ctx = Lookups.singleton(p);
        Action ctxAction = action.createContextAwareInstance(ctx);
        ctxAction.actionPerformed(new ActionEvent(caller, 0, "")); // NOI18N
    }

    public static void openProject(Project p, Object caller) {
        if( p == null) return;
        
        Project[] projects = new Project[] {p};
        OpenProjects.getDefault().open(projects, false);
        selectAndExpandProject(p);
    }
    
    public static void selectAndExpandProject( final Project p ) {
        if( p == null) return;
        
        // invoke later to select the being opened project if the focus is outside ProjectTab
        SwingUtilities.invokeLater(new Runnable() {
            
            final ExplorerManager.Provider ptLogial = findDefault(ProjectTab_ID_LOGICAL);
            
            public void run() {
                Node root = ptLogial.getExplorerManager().getRootContext();
                // Node projNode = root.getChildren ().findChild( p.getProjectDirectory().getName () );
                Node projNode = root.getChildren().findChild( ProjectUtils.getInformation( p ).getName() );
                if ( projNode != null ) {
                    try {
                        ptLogial.getExplorerManager().setSelectedNodes( new Node[] { projNode } );
                    } catch (Exception ignore) {
                        // may ignore it
                    }
                }
            }
        });
    }
    
    public static String getProjectName( final File root ) {
        if(root == null || !root.isDirectory()) return null;
        final ProjectManager projectManager = ProjectManager.getDefault();
        FileObject rootFileObj = FileUtil.toFileObject(FileUtil.normalizeFile(root));
        // This can happen if the root is "ssh://<something>"
        if (rootFileObj == null || projectManager == null) {
            return null;
        }
 
        String res = null;
        if (projectManager.isProject(rootFileObj)){
            try {
                Project prj = projectManager.findProject(rootFileObj);

                res = getProjectName(prj);
            } catch (Exception ex) {
                Mercurial.LOG.log(Level.FINE, "getProjectName() file: {0} {1}", new Object[] {rootFileObj.getPath(), ex.toString()}); // NOI18N
            }finally{
                return res;
            } 
        }else{
            return res;
        }
    }

    public static String getProjectName( final Project p ) {        
        if( p == null) return null;
        
        ProjectInformation pi = ProjectUtils.getInformation(p);
        return pi == null ? null : pi.getDisplayName();
    }
      
    private static synchronized ExplorerManager.Provider findDefault( String tcID ) {
        TopComponent tc = WindowManager.getDefault().findTopComponent( tcID );
        return (ExplorerManager.Provider) tc;
    }    
    
    // Should not be creating an instance of this class
    private HgProjectUtils() {
    }
    
}
