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

package org.netbeans.modules.web.project.ui.wizards;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.api.webmodule.ExtenderController;

import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * Panel asking for web frameworks to use.
 * @author Radko Najman
 */
final class PanelSupportedFrameworks implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    private ExtenderController controller = ExtenderController.create();
    private WizardDescriptor wizardDescriptor;
    private PanelSupportedFrameworksVisual component;
    private NewWebProjectWizardIterator iterator = null;
    
    /** Create the wizard panel descriptor. */
    public PanelSupportedFrameworks(NewWebProjectWizardIterator iterator) {
        this.iterator = iterator;
    }
    
    public boolean isFinishPanel() {
        return true;
    }

    public Component getComponent() {
        if (component == null)
            component = new PanelSupportedFrameworksVisual(this, controller, null, PanelSupportedFrameworksVisual.ALL_FRAMEWORKS, null);

        return component;
    }
    
    public HelpCtx getHelp() {
        HelpCtx helpCtx = null;
        if (component != null && (helpCtx = component.getHelpCtx())!=null)
            return helpCtx;
        return new HelpCtx(PanelSupportedFrameworks.class);
    }
    
    public boolean isValid() {

        if(iterator.isIstantiating == true)
            return false;

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
        component.read(wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
    }
}
