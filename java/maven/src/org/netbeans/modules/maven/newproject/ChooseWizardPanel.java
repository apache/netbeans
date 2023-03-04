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

package org.netbeans.modules.maven.newproject;

import javax.swing.event.ChangeListener;
import static org.netbeans.modules.maven.newproject.Bundle.*;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

public class ChooseWizardPanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {
    
    private WizardDescriptor wizardDescriptor;
    private ChooseArchetypePanel component;
    
    public ChooseWizardPanel() {
    }
    
    @Messages("LBL_CreateProjectStep=Maven Archetype")
    public @Override ChooseArchetypePanel getComponent() {
        if (component == null) {
            component = new ChooseArchetypePanel(this);
            component.setName(LBL_CreateProjectStep());
        }
        return component;
    }
    
    public @Override HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.maven.newproject.ChooseWizardPanel");
    }
    
    public @Override boolean isValid() {
        return getComponent().valid(wizardDescriptor);
    }
    
    private final ChangeSupport cs = new ChangeSupport(this);
    public @Override void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    public @Override void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    protected final void fireChangeEvent() {
        cs.fireChange();
    }
    
    public @Override void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        getComponent().read(wizardDescriptor);
    }
    
    public @Override void storeSettings(WizardDescriptor settings) {
        getComponent().store(settings);
    }
    
    public boolean isFinishPanel() {
        return true;
    }
    
    public @Override void validate() throws WizardValidationException {
        getComponent().validate(wizardDescriptor);
    }
    
}
