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

package org.netbeans.modules.j2ee.persistence.wizard;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * @author Pavel Buzek
 */
public final class PersistenceClientEntitySelection implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel, ChangeListener {
    public static final String DISABLENOIDSELECTION = "disableNoIdSelection";//NOI18N, used to control if entities without id can be selected
    
    private WizardDescriptor wizardDescriptor;
    private String panelName;
    private HelpCtx helpCtx;
    private PersistenceClientEntitySelectionVisual component;
    private boolean disableNoIdSelection = false;
    
    
    /** Create the wizard panel descriptor. */
    public PersistenceClientEntitySelection(String panelName, HelpCtx helpCtx, WizardDescriptor wizardDescriptor) {
        this.panelName = panelName;
        this.helpCtx = helpCtx;
        this.wizardDescriptor = wizardDescriptor;
    }
    
    @Override
    public boolean isFinishPanel() {
        return false;
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new PersistenceClientEntitySelectionVisual(panelName, wizardDescriptor);
            component.addChangeListener(this);
        }
        return component;
    }
    
    @Override
    public HelpCtx getHelp() {
        return helpCtx;
    }
    
    @Override
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    protected final void fireChangeEvent(ChangeEvent ev) {
        changeSupport.fireChange();
    }
    
    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null){
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
        }
    }
    
    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        d.putProperty(WizardProperties.CREATE_PERSISTENCE_UNIT, component.getCreatePersistenceUnit());
        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent(e);
    }
    
}
