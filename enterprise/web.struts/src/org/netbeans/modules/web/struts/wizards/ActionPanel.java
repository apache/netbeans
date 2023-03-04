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

package org.netbeans.modules.web.struts.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import org.netbeans.api.project.Project;

/**
 *
 * @author radko
 */
public class ActionPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel, ChangeListener {

    private WizardDescriptor wizardDescriptor;
    private ActionPanelVisual component;
    private Project project;

    /** Creates a new instance of ActionPanel */
    public ActionPanel(Project project, WizardDescriptor wizardDescriptor) {
        this.project=project;
        this.wizardDescriptor = wizardDescriptor;
    }
    
    Project getProject() {
        return project;
    }
    
    public Component getComponent() {
        if (component == null){
            component = new ActionPanelVisual(this);
            component.addChangeListener(this);
        }
        return component;
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor desc = (WizardDescriptor) settings;
        component.store(desc);
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ActionPanel.class);
    }

    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);
    
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
    public boolean isFinishPanel() {
        return isValid();
    }
    
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }
    
     private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }
}
