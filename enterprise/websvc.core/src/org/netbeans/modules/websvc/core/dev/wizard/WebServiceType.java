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

package org.netbeans.modules.websvc.core.dev.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 *
 * @author radko
 */
public class WebServiceType implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor>, WizardDescriptor.ValidatingPanel<WizardDescriptor>, ChangeListener {

    private WebServiceTypePanel component;
    private WizardDescriptor wizardDescriptor;
    private Project project;
    
    /** Creates a new instance of WebServiceType */
    public WebServiceType(Project project, WizardDescriptor wizardDescriptor) {
        this.project = project;
        this.wizardDescriptor = wizardDescriptor;
    }

    public Component getComponent() {
        if (component == null) {
            component = new WebServiceTypePanel(project);
            component.addChangeListener(this);
        }
        
        return component;
    }

    public HelpCtx getHelp() {
        HelpCtx helpCtx = null;
        if (getComponent() != null && (helpCtx = component.getHelpCtx()) != null)
            return helpCtx;
        
        return new HelpCtx(WebServiceType.class);
    }

    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        component.read (wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
    }

    public void storeSettings(WizardDescriptor settings) {
        component.store(settings);
        settings.putProperty("NewProjectWizard_Title", null); // NOI18N
    }

    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public boolean isFinishPanel() {
        return isValid();
    }

    public void validate() throws WizardValidationException {
        component.validate(wizardDescriptor);
    }

    public void stateChanged(ChangeEvent e) {
        fireChange();
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator<ChangeListener> it = listeners.iterator();
        while (it.hasNext()) {
            it.next().stateChanged(e);
        }
    }

}
