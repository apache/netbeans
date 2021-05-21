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

package org.netbeans.modules.javaee.project.api.ant.ui.wizard;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * Panel just asking for basic info.
 */
public final class ProjectLocationWizardPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    public static final String PROJECT_DIR = "projdir"; //NOI18N
    public static final String NAME = "name"; //NOI18N
    public static final String SHARED_LIBRARIES = "sharedLibraries"; // NOI18N
    
    private WizardDescriptor wizardDescriptor;
    private ProjectLocationPanel component;
    private Object j2eeModuleType;
    private String defaultNameFormatter;
    private String name;
    private String title;
    
    /** Create the wizard panel descriptor. */
    public ProjectLocationWizardPanel(Object j2eeModuleType, String name, String title, String defaultNameFormatter) {
        this.j2eeModuleType = j2eeModuleType;
        this.defaultNameFormatter = defaultNameFormatter;
        this.name = name;
        this.title = title;
    }
    
    public boolean isFinishPanel() {
        return false;
    }

    public Component getComponent() {
        if (component == null) {
            component = new ProjectLocationPanel(j2eeModuleType, name, title, this, defaultNameFormatter);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(ProjectImportLocationPanel.generateHelpID(ProjectLocationWizardPanel.class, j2eeModuleType));
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    protected void fireChangeEvent() {
        changeSupport.fireChange();
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read (wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        d.putProperty("NewProjectWizard_Title", null); // NOI18N
    }
}
