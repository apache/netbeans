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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.ExitDialog;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/** Action for invoking project customizer
 */
public class CustomizeProject extends ProjectAction implements Presenter.Popup {

    private static final String namePattern = NbBundle.getMessage( CustomizeProject.class, "LBL_CustomizeProjectAction_Name" ); // NOI18N
    private static final String namePatternPopup = NbBundle.getMessage( CustomizeProject.class, "LBL_CustomizeProjectAction_Popup_Name" ); // NOI18N
        
    public CustomizeProject() {
        this( null );
    }
    
    public CustomizeProject( Lookup context ) {
        super( (String)null, namePattern, namePatternPopup, null, context );
        refresh(getLookup(), true);
    }
            
    
   
    protected @Override void refresh(Lookup context, boolean immediate) {
     
        super.refresh(context, immediate);
        
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
                            
        if ( projects.length != 1 || projects[0].getLookup().lookup( CustomizerProvider.class ) == null ) {
            setEnabled( false );
            // setDisplayName( ActionsUtil.formatProjectSensitiveName( namePattern, new Project[0] ) );
        }
        else { 
            setEnabled( true );
            // setDisplayName( ActionsUtil.formatProjectSensitiveName( namePattern, projects ) );
        }
        
        
    }   
    
    @Override
    public void actionPerformed( Lookup context ) {
        final Pair<List<Project>, List<FileObject>> data = ActionsUtil.mineFromLookup(context);
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                Project[] projects = ActionsUtil.getProjects(data);

                if ( projects.length == 1 ) {
                    final CustomizerProvider cp = projects[0].getLookup().lookup( CustomizerProvider.class );
                    if ( cp != null ) {
                        if (!DataObject.getRegistry().getModifiedSet().isEmpty()) {
                            // #50992: danger! Project properties dialog may try to write to the same config files.

                            //#92011 - reducing the frequency of the dialog popping up.
                            Set<DataObject> candidates = new HashSet<DataObject>();
                            List<FileObject> metadataFiles = ProjectOperations.getMetadataFiles(projects[0]);

                            for (DataObject dobj : DataObject.getRegistry().getModifiedSet()) {
                                // only consider files from our project
                                if (projects[0] == FileOwnerQuery.getOwner(dobj.getPrimaryFile())) {
                                    // now check if it's metadata or data - not 100% bulletproof, but should reduce the probability significantly
                                    for (FileObject df : metadataFiles) {
                                        if (df.equals(dobj.getPrimaryFile()) || 
                                                (df.isFolder() && FileUtil.isParentOf(df, dobj.getPrimaryFile()))) {
                                            candidates.add(dobj);
                                            break;
                                        }
                                    }
                                }
                            }
                            if (!candidates.isEmpty()) {
                                String saveAll = NbBundle.getMessage(CustomizeProject.class, "CustomizeProject.saveAll");
                                Object ret = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                                        NbBundle.getMessage(CustomizeProject.class, "CustomizeProject.save_modified_files"), 
                                        NbBundle.getMessage(CustomizeProject.class, "CustomizeProject.save_modified_title"),
                                        NotifyDescriptor.OK_CANCEL_OPTION,
                                        NotifyDescriptor.WARNING_MESSAGE,
                                        new Object[] {
                                            saveAll,
                                            NotifyDescriptor.CANCEL_OPTION
                                        }, 
                                        saveAll));
                                if (ret != saveAll) {
                                    return;
                                } else {
                                    for (DataObject dobj : candidates) {
                                        ExitDialog.doSave(dobj);
                                    }
                                }
                            }
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                cp.showCustomizer();
                            }
                        });
                    }
                }
            }
        });
        
    }
    
    @Override
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new CustomizeProject( actionContext );
    }
    
    
    // Implementation of Presenter.Popup ---------------------------------------
    
    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem popupPresenter = new JMenuItem();
        org.openide.awt.Actions.connect(popupPresenter, this, true);
        popupPresenter.setText( namePatternPopup );
        return popupPresenter;
    }
    
}
