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

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.api.webmodule.ExtenderController;

import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

final class JSFConfigurationWizardPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    private ExtenderController controller;
    private WizardDescriptor wizardDescriptor;
    private JSFConfigurationWizardPanelVisual component;
    private WebModuleExtender wme;
    
    /** Create the wizard panel descriptor. */
    public JSFConfigurationWizardPanel(WebModuleExtender wme) {
        this.wme = wme;
        controller = ExtenderController.create();
    }

    /** Create the wizard panel descriptor with specified ExtenderController. */
    public JSFConfigurationWizardPanel(WebModuleExtender wme, ExtenderController extenderController) {
        this.wme = wme;
        this.controller = extenderController;
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new JSFConfigurationWizardPanelVisual(this, controller, wme);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        HelpCtx helpCtx = null;
        if (component != null && (helpCtx = component.getHelpCtx())!=null)
            return helpCtx;
        return new HelpCtx("org.netbeans.modules.web.jsf.wizards.JSFConfigurationWizardPanel");
    }

    @Override
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    protected void fireChangeEvent() {
        changeSupport.fireChange();
    }

    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
    }

    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
    }
}
