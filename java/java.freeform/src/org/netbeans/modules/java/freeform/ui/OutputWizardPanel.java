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

package org.netbeans.modules.java.freeform.ui;

import java.awt.Component;

import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Milan Kubec
 */
public class OutputWizardPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    private WizardDescriptor wizardDescriptor;
    private final ChangeSupport cs = new ChangeSupport(this);
    private OutputPanel component;
    
    public OutputWizardPanel() {
        getComponent().setName(NbBundle.getMessage (OutputWizardPanel.class, "TXT_OutputWizardPanel_Title"));
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new OutputPanel();
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(OutputWizardPanel.class);
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        ProjectModel pm = (ProjectModel) wizardDescriptor.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL);
        getComponent();
        component.setModel(pm);
        wizardDescriptor.putProperty("NewProjectWizard_Title", 
                component.getClientProperty("NewProjectWizard_Title")); // NOI18N
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
    public boolean isValid() {
        getComponent();
        return true;
    }
    
    public final void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    public final void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    protected final void fireChangeEvent() {
        cs.fireChange();
    }
    
    // always return true, it's last panel and the output is not manadatory in wizard
    public boolean isFinishPanel() {
        return true;
    }
    
}
