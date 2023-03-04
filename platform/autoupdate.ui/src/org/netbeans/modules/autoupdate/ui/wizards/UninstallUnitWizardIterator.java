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

package org.netbeans.modules.autoupdate.ui.wizards;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public final class UninstallUnitWizardIterator extends InstallableIteratorBase implements ChangeListener {
    
    private UninstallUnitWizardModel uninstallModel;
    private OperationDescriptionStep descStep;
    private boolean installAdded;
    
    public UninstallUnitWizardIterator (UninstallUnitWizardModel model) {
        super(model.createInstallModel(), false, false, false);
        uninstallModel = model;
        createPanels ();
        index = 0;
    }
    
    private void createPanels () {
        assert panels != null && panels.isEmpty() : "Panels are still empty";
        descStep = new OperationDescriptionStep (uninstallModel);
        panels.add (descStep);
        if (uninstallModel.hasCustomComponents ()) {
            panels.add (new CustomHandleStep (uninstallModel));
        }
        panels.add (new UninstallStep (uninstallModel));
        // will react on the change to the final presentation
        descStep.addChangeListener(this);
    }
    
    public WizardDescriptor.Panel<WizardDescriptor> current () {
        assert panels != null;
        return panels.get (index);
    }

    @Override
    public void nextPanel() {
        if (current() instanceof InstallStep) {
            index = 0;
            // must reset the data in descStep
            descStep.reset();
            removeInstallPanels();
        } else {
            super.nextPanel();
        }
    }
    
    public String name () {
        return NbBundle.getMessage (UninstallUnitWizard.class, "UninstallUnitWizard_Title");
    }
    
    public boolean hasPrevious () {
        // disable Back on 'confirmation' screen
        if (current() instanceof UninstallStep) {
            return false;
        }
        // disable Back during installation, no turning back to licenses
        if (current() instanceof InstallStep) {
            return false;
        }
        return super.hasPrevious();
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener (ChangeListener l) {}
    public void removeChangeListener (ChangeListener l) {}

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() != descStep) {
            return;
        }
        // if the model has initialized and says we have components to install,
        // add the intermediate install panels to the wizard
        if (uninstallModel.hasComponentsToInstall() && !installAdded) {
            installAdded = true;
            insertPanels(1);
        }
    }
    
    protected void compactPanels() {
        // block compacting on further panels since it may damage the 
        // current() panel after installation is complete
        if (index == 0) {
            super.compactPanels();
        }
    }
    
}
