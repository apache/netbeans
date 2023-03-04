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

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import static org.netbeans.modules.ant.freeform.ui.Bundle.*;

/**
 *
 * @author Petr Hrebejk, David Konecny
 */
public class ProjectCustomizerProvider implements CustomizerProvider {
    
    private final FreeformProject project;
    
    public static final String CUSTOMIZER_FOLDER_PATH = "Projects/org-netbeans-modules-ant-freeform/Customizer"; //NO18N
    
    private static final Map<Project,Dialog> project2Dialog = new HashMap<Project,Dialog>(); 
    
    public ProjectCustomizerProvider(FreeformProject project) {
        this.project = project;
    }
            
    @Override
    @NbBundle.Messages("MSG_CustomizerForbidden=The customizer is disabled, using it would revert manual changes done to the nbproject/project.xml file.")
    public void showCustomizer() {
        AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        String show = props.get("show.customizer", true);
        if (show != null && "false".equals(show)) {
            String message = props.get("show.customizer.message", true);
            if (message == null) {
                message = MSG_CustomizerForbidden();
            }
            NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        Dialog dialog = project2Dialog.get (project);
        if ( dialog != null ) {            
            dialog.setVisible(true);
        }
        else {
            InstanceContent ic = new InstanceContent();
            Lookup context = new AbstractLookup(ic);
            ic.add(project);
            ic.add(project.getLookup().lookup(ProjectAccessor.class));
            ic.add(project.getLookup().lookup(AuxiliaryConfiguration.class));
            //TODO replace with generic apis..
            ic.add(ic);
            
            OptionListener listener = new OptionListener();
            dialog = ProjectCustomizer.createCustomizerDialog(CUSTOMIZER_FOLDER_PATH, context, null, listener, null );
            dialog.addWindowListener( listener );
            dialog.setTitle( MessageFormat.format(                 
                    NbBundle.getMessage( ProjectCustomizerProvider.class, "LBL_Customizer_Title" ), // NOI18N 
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName() } ) );

            project2Dialog.put(project, dialog);
            dialog.setVisible(true);
        }
    }    
    

    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener {
    
        // Listening to OK button ----------------------------------------------
        
        @Override
        public void actionPerformed( ActionEvent e ) {
//#95952 some users experience this assertion on a fairly random set of changes in 
// the customizer, that leads me to assume that a project can be already marked
// as modified before the project customizer is shown. 
//            assert !ProjectManager.getDefault().isModified(project) : 
//                "Some of the customizer panels has written the changed data before OK Button was pressed. Please file it as bug."; //NOI18N
            
            // Close & dispose the the dialog
            Dialog dialog = project2Dialog.get( project );
            if ( dialog != null ) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }        
        
        // Listening to window events ------------------------------------------
                
        @Override
        public void windowClosed( WindowEvent e) {
            project2Dialog.remove( project );
        }    
        
        @Override
        public void windowClosing (WindowEvent e) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            Dialog dialog = project2Dialog.get( project );
            if ( dialog != null ) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
                            
}
