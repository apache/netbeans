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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.netbeans.api.project.Project;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author gowri
 */
public class HibernateConfigurationWizardDescriptor implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel, ChangeListener {

    private HibernateConfigurationWizardPanel panel;
    private WizardDescriptor wizardDescriptor;
    private Project project;    
    private String title;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public HibernateConfigurationWizardDescriptor(Project project, String title) {
        this.project = project;
        this.title= title;
    }

    public HibernateConfigurationWizardPanel getComponent() {
        if (panel == null) {
            panel = new HibernateConfigurationWizardPanel();
            panel.addChangeListener(this);
        }
        return panel;
    }

    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    public boolean isFinishPanel() {
        return isValid();
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public boolean isValid() {
        if(!getComponent().isPanelValid()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(HibernateConfigurationWizardDescriptor.class, "ERR_No_DB_Connection_Exists")); // NOI18N
            return false;
        } else {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
        }
        return true;
    }

    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        panel = (HibernateConfigurationWizardPanel) getComponent();
        if (WizardDescriptor.PREVIOUS_OPTION.equals(wizardDescriptor.getValue())) {
            return;
        }       
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        wizardDescriptor.putProperty("NewFileWizard_Title", title); 
    }

    public HelpCtx getHelp() {
        return new HelpCtx(HibernateConfigurationWizardDescriptor.class);
    }

    Project getProject() {
        return project;
    }
    
    String getDialectName() {
        return panel == null ? null : panel.getSelectedDialect();
    }

    String getDriver() {
        return panel == null ? null : panel.getSelectedDriver();
    }

    String getURL() {
        return panel == null ? null : panel.getSelectedURL();
    }
    
    String getUserName() {
        return panel == null ? null : panel.getUserName();
    }
    
    String getPassword() {
        return panel == null ? null : panel.getPassword();
    }
}
