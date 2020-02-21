/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.remote.projectui.wizard.ide;

import java.text.MessageFormat;
import javax.swing.JComponent;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

public final class NewProjectWizard extends TemplateWizard {

    private FileObject templatesFO;
    private MessageFormat format;
    private final ExecutionEnvironment env;

    public NewProjectWizard (FileObject fo, ExecutionEnvironment env) {
        this.templatesFO = fo;
        this.env = env;
        putProperty (TemplatesPanelGUI.TEMPLATES_FOLDER, templatesFO);
        format = new MessageFormat (NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_MessageFormat"));
        //setTitleFormat( new MessageFormat( "{0}") );
    }
        
    @Override
    public void updateState () {
        super.updateState ();
        String substitute = (String)getProperty ("NewProjectWizard_Title"); // NOI18N
        String title;
        if (substitute == null) {
            title = NbBundle.getMessage(NewProjectWizard.class, "LBL_NewProjectWizard_Title", env); // NOI18N
        } else {
            Object[] args = new Object[] {
                    NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Subtitle"), // NOI18N
                    substitute};
            title = format.format (args);
        }
        super.setTitle (title);
    }
    
    @Override
    public void setTitle (String ignore) {}
    
    @Override
    protected WizardDescriptor.Panel<WizardDescriptor> createTemplateChooser() {
        WizardDescriptor.Panel<WizardDescriptor> panel = new ProjectTemplatePanel();
        JComponent jc = (JComponent)panel.getComponent ();
        jc.setPreferredSize( new java.awt.Dimension (500, 340) );
        jc.setName (NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Name")); // NOI18N
        jc.getAccessibleContext ().setAccessibleName (NbBundle.getBundle (NewProjectWizard.class).getString ("ACSN_NewProjectWizard")); // NOI18N
        jc.getAccessibleContext ().setAccessibleDescription (NbBundle.getBundle (NewProjectWizard.class).getString ("ACSD_NewProjectWizard")); // NOI18N
        jc.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer (0));
        jc.putClientProperty (WizardDescriptor.PROP_CONTENT_DATA, new String[] {
                NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Name"), // NOI18N
                NbBundle.getBundle (NewProjectWizard.class).getString ("LBL_NewProjectWizard_Dots")}); // NOI18N
                
        return panel;
    }          
    
}
