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
                                opls.isTrustAndPrime(),
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
