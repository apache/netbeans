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
public class SelectObjectFilesWizard implements WizardDescriptor.Panel, ChangeListener {
    
    private DiscoveryDescriptor wizardDescriptor;
    private SelectObjectFilesPanel component;
    private final String name;

    public SelectObjectFilesWizard(){
	name = NbBundle.getMessage(SelectObjectFilesPanel.class, "SelectObjectRootName"); // NOI18N
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new SelectObjectFilesPanel(this);
	    component.setName(name);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(DiscoveryWizardAction.HELP_CONTEXT_SELECT_OBJECT_FILES);
    }
    
    public boolean isValid() {
    	boolean valid = ((SelectObjectFilesPanel)getComponent()).valid();
        if (valid) {
            wizardDescriptor.setMessage(null);
        }
        return valid;
    }
    
    private final Set<ChangeListener> listeners = new HashSet<>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
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

    public void stateChanged(ChangeEvent e) {
	fireChangeEvent();
    }
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        wizardDescriptor = DiscoveryWizardDescriptor.adaptee(settings);
        component.read(wizardDescriptor);
    }
    
    public void storeSettings(Object settings) {
        component.store(DiscoveryWizardDescriptor.adaptee(settings));
    }
}

