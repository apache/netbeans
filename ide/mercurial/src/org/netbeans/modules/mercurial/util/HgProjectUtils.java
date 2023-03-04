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
