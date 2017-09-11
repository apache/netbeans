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

package org.netbeans.modules.project.ui.actions;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFileChooser;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.OpenProjectListSettings;
import org.netbeans.modules.project.ui.ProjectChooserAccessory;
import org.netbeans.modules.project.ui.ProjectTab;
import org.netbeans.modules.project.ui.ProjectUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

public class OpenProject extends BasicAction {
    
    private static final String DISPLAY_NAME = NbBundle.getMessage( OpenProject.class, "LBL_OpenProjectAction_Name" ); // NOI18N
    private static final String _SHORT_DESCRIPTION = NbBundle.getMessage( OpenProject.class, "LBL_OpenProjectAction_Tooltip" ); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(OpenProject.class);
        
    public OpenProject() {
        super( DISPLAY_NAME, null);
        putValue("iconBase","org/netbeans/modules/project/ui/resources/openProject.png"); //NOI18N
        putValue(SHORT_DESCRIPTION, _SHORT_DESCRIPTION);
    }

    public @Override void actionPerformed(ActionEvent evt) {
        JFileChooser chooser = ProjectChooserAccessory.createProjectChooser( true ); // Create the jFileChooser
        chooser.setMultiSelectionEnabled( true );
        
        // Check to see if the current selection matches a file/folder owned by a non-open project;
        // if so, use that as the starting directory, as a convenience in case that is what should be opened.
        // XXX may also want to check lookup for FileObject
        for (DataObject d : Utilities.actionsGlobalContext().lookupAll(DataObject.class)) {
            Project selected = FileOwnerQuery.getOwner(d.getPrimaryFile());
            if (selected != null && !OpenProjectList.getDefault().isOpen(selected)) {
                File dir = FileUtil.toFile(selected.getProjectDirectory());
                if (dir != null) {
                    chooser.setCurrentDirectory(dir.getParentFile());
                    chooser.setSelectedFiles(new File[] {dir});
                    break;
                }
            }
        }
        show(chooser);
    }

    private static void show(final JFileChooser chooser) {
            final File[] projectDirs;

            if( Boolean.getBoolean("nb.native.filechooser") && Utilities.isMac() ) { //NOI18N
                String oldFileDialogProp = System.getProperty("apple.awt.fileDialogForDirectories"); //NOI18N
                System.setProperty("apple.awt.fileDialogForDirectories", "true"); //NOI18N
                FileDialog fileDialog = new FileDialog(WindowManager.getDefault().getMainWindow(), DISPLAY_NAME);
                fileDialog.setMode(FileDialog.LOAD);
                fileDialog.setTitle(chooser.getDialogTitle());
                fileDialog.setVisible(true);
                if( null != oldFileDialogProp ) {
                    System.setProperty("apple.awt.fileDialogForDirectories", oldFileDialogProp); //NOI18N
                } else {
                    System.clearProperty("apple.awt.fileDialogForDirectories"); //NOI18N
                }

                if( fileDialog.getDirectory() != null && fileDialog.getFile() != null ) {
                    String selFile = fileDialog.getFile();
                    File dir = new File( fileDialog.getDirectory() );
                    projectDirs = new File[] { new File( dir, selFile ) };
                } else {
                    projectDirs = null;
                }
            } else {
                int option = chooser.showOpenDialog( WindowManager.getDefault().getMainWindow() ); // Sow the chooser

                if ( option == JFileChooser.APPROVE_OPTION ) {

                    if ( chooser.isMultiSelectionEnabled() ) {                    
                        projectDirs = chooser.getSelectedFiles();
                    }
                    else {
                        projectDirs = new File[] { chooser.getSelectedFile() };
                    }
                } else {
                    projectDirs = null;
                }
            }
            
            if( projectDirs != null ) {
                RP.post(new Runnable() {
                    @Override public void run() {
                        ArrayList<Project> projects = new ArrayList<Project>( projectDirs.length );
                        for (File d : projectDirs) {
                            Project p = OpenProjectList.fileToProject(FileUtil.normalizeFile(d));
                            if ( p != null ) {
                                projects.add( p );
                            }
                        }

                        if ( projects.isEmpty() ) {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                        NbBundle.getMessage( OpenProject.class, "MSG_notProjectDir"), // NOI18N
                                        NotifyDescriptor.WARNING_MESSAGE));
                            EventQueue.invokeLater(new Runnable() {
                                    @Override public void run() {
                                        show(chooser);
                                    }
                            });
                        }
                        else {
                            Project projectsArray[] = new Project[ projects.size() ];
                            projects.toArray( projectsArray );

                            OpenProjectListSettings opls = OpenProjectListSettings.getInstance();
                            //236680 - wait until project is opened
                            OpenProjectList.getDefault().addPropertyChangeListener(new PropertyChangeListener() {

                                @Override
                                public void propertyChange(PropertyChangeEvent evt) {
                                    if(evt.getPropertyName().equals(OpenProjectList.PROPERTY_OPEN_PROJECTS)) {
                                        List<Project> oldProjectList = new ArrayList<Project>(Arrays.asList((Project [])evt.getOldValue()));
                                        List<Project> newProjectList = new ArrayList<Project>(Arrays.asList((Project [])evt.getNewValue()));
                                        newProjectList.removeAll(oldProjectList);
                                        final Project projectToExpand = newProjectList.size() > 0 ? newProjectList.get(0):null;
                                        if(projectToExpand != null) {
                                            ProjectTab.RP.post(new Runnable() {
                                                @Override public void run() {
                                                        ProjectUtilities.selectAndExpandProject(projectToExpand);
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                            OpenProjectList.getDefault().open( 
                                projectsArray,                    // Put the project into OpenProjectList
                                opls.isOpenSubprojects(),         // And optionaly open subprojects
                                true,                             // open asynchronously
                                null);
                            opls.setLastOpenProjectDir( chooser.getCurrentDirectory().getPath() );

                            EventQueue.invokeLater(new Runnable() {
                                    @Override public void run() {
                                        ProjectUtilities.makeProjectTabVisible();
                                    }
                            });
                        }
                    }
                });
            }
    }
        
}
