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
package org.netbeans.modules.micronaut.newproject;

import java.awt.Component;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Wizard descriptor for Features step in Micronaut project wizard.
 *
 * @author Dusan Balek
 */
public class FeaturesWizardPanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor> {

    WizardDescriptor wizardDescriptor;
    private FeaturesVisual component;

    public FeaturesWizardPanel() {
    }

    @Override
    @NbBundle.Messages("LBL_Features=Features")
    public Component getComponent() {
        if (component == null) {
            component = new FeaturesVisual(this);
            component.setName(Bundle.LBL_Features());
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }

    private final ChangeSupport chgSupport = new ChangeSupport(this);

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (chgSupport) {
            chgSupport.addChangeListener(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (chgSupport) {
            chgSupport.removeChangeListener(l);
        }
    }

    protected final void fireChangeEvent() {
        chgSupport.fireChange();
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        wizardDescriptor = wiz;
        component.read(wizardDescriptor);
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        WizardDescriptor d = wiz;
        component.store(d);
    }

    @Override
    public boolean isFinishPanel() {
        return false;
    }

    @Override
    public void validate() throws WizardValidationException {
        getComponent();
        component.validate(wizardDescriptor);
    }
}
