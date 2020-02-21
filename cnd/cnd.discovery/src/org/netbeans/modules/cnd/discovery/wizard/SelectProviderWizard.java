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
package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class SelectProviderWizard implements WizardDescriptor.Panel, ChangeListener {
    
    private DiscoveryDescriptor wizardDescriptor;
    private SelectProviderPanel component;
    private final String name;
    private boolean inited = false;

    public SelectProviderWizard(){
        name = NbBundle.getMessage(SelectProviderWizard.class, "SelectProviderName"); // NOI18N
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new SelectProviderPanel(this);
      	    component.setName(name);
        }
        return component;
    }
    
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(DiscoveryWizardAction.HELP_CONTEXT_SELECT_PROVIDER);
    }
    
    @Override
    public boolean isValid() {
        return ((SelectProviderPanel)getComponent()).valid(wizardDescriptor);
    }
    
    private final Set<ChangeListener> listeners = new HashSet<>(1);
    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      	fireChangeEvent();
    }
    
    DiscoveryDescriptor getWizardDescriptor(){
        return wizardDescriptor;
    }
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(Object settings) {
        if (!inited) {
            wizardDescriptor = DiscoveryWizardDescriptor.adaptee(settings);
            component.read(wizardDescriptor);
            inited = true;
        }
        component.showAlert(wizardDescriptor);
    }
    
    @Override
    public void storeSettings(Object settings) {
        component.store(DiscoveryWizardDescriptor.adaptee(settings));
        ((WizardDescriptor)wizardDescriptor).putProperty("ShowAlert", Boolean.FALSE); // NOI18N
    }
}

