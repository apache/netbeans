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
package org.netbeans.modules.hibernate.wizards;

import java.awt.Component;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 *
 * @author gowri
 */
public class HibernateMappingWizardDescriptor implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    private WizardDescriptor wizardDescriptor;
    private HibernateMappingWizardPanel panel;
    private Project project;
    private String title;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /** Create the wizard panel descriptor. */
    public HibernateMappingWizardDescriptor(Project project, String title) {
        this.project = project;
        this.title = title;
    }

    public boolean isFinishPanel() {
        return isValid();
    }

    public Component getComponent() {
        if (panel == null) {
            panel = new HibernateMappingWizardPanel(project);            
        }
        return panel;
    }

    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    public HelpCtx getHelp() {
        return new HelpCtx(HibernateMappingWizardDescriptor.class);
    }

    public boolean isValid() {
        return true;
    }

    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        wizardDescriptor.putProperty("NewFileWizard_Title", title); 
    }

    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        panel = (HibernateMappingWizardPanel) getComponent();
        if (WizardDescriptor.PREVIOUS_OPTION.equals(wizardDescriptor.getValue())) {
            return;
        }       
    }
    
    public String getClassName() {
        return panel.getClassName();
    }
    
    public FileObject getConfigurationFile() {
        return panel.getConfigurationFile();
    }
    
    public String getDatabaseTable() {
        return panel.getDatabaseTable();
    }
}
