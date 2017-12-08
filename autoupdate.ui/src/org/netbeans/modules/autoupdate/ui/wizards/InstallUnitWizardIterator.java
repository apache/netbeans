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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public final class InstallUnitWizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {
    
    private int index;
    private final List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>> ();
    private InstallUnitWizardModel installModel;
    private WizardDescriptor.Panel<WizardDescriptor> licenseApprovalStep = null;
    private WizardDescriptor.Panel<WizardDescriptor> customHandleStep = null;
    private WizardDescriptor.Panel<WizardDescriptor> installStep = null;
    private boolean isCompact = false;
    private boolean clearLazyUnits = false;
    private final boolean allowRunInBackground;
    private final boolean runInBackground;
    
    public InstallUnitWizardIterator (InstallUnitWizardModel model) {
        this (model, false);
    }
    
    public InstallUnitWizardIterator (InstallUnitWizardModel model, boolean clearLazyUnits) {
        this(model, clearLazyUnits, true, true);
    }
    
    public InstallUnitWizardIterator (InstallUnitWizardModel model, boolean clearLazyUnits, boolean allowRunInBackground) {
        this(model, clearLazyUnits, allowRunInBackground, false);
    }
    
    public InstallUnitWizardIterator (InstallUnitWizardModel model, boolean clearLazyUnits, boolean allowRunInBackground, boolean runInBackground) {
        this.installModel = model;
        this.clearLazyUnits = clearLazyUnits;
        this.allowRunInBackground = allowRunInBackground;
        this.runInBackground = runInBackground;
        createPanels ();
        index = 0;
    }
    
    public InstallUnitWizardModel getModel () {
        assert installModel != null;
        return installModel;
    }
    
    private void createPanels () {
        assert panels != null && panels.isEmpty() : "Panels are still empty";
        panels.add (new OperationDescriptionStep (installModel));
        licenseApprovalStep = new LicenseApprovalStep (installModel);
        panels.add (licenseApprovalStep);
        customHandleStep = new CustomHandleStep (installModel);
        panels.add (customHandleStep);
        installStep = new InstallStep (installModel, clearLazyUnits, allowRunInBackground, runInBackground);
        panels.add (installStep);
    }
    
    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current () {
        assert panels != null;
        return panels.get (index);
    }
    
    @Override
    public String name () {
        return NbBundle.getMessage (InstallUnitWizardIterator.class, "InstallUnitWizard_Title");
    }
    
    @Override
    public boolean hasNext () {
        compactPanels ();
        return index < panels.size () - 1;
    }
    
    @Override
    public boolean hasPrevious () {
        compactPanels ();
        return index > 0 && ! (current () instanceof InstallStep || current () instanceof CustomHandleStep);
    }
    
    @Override
    public void nextPanel () {
        compactPanels ();
        if (!hasNext ()) {
            throw new NoSuchElementException ();
        }
        index++;
    }
    
    @Override
    public void previousPanel () {
        compactPanels ();
        if (!hasPrevious ()) {
            throw new NoSuchElementException ();
        }
        index--;
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener (ChangeListener l) {}
    @Override
    public void removeChangeListener (ChangeListener l) {}

    private void compactPanels () {
        if (isCompact) {
            return ;
        }

        boolean allLicensesTouched = getModel().allLicensesTouched();
        if (allLicensesTouched && getModel ().allLicensesApproved ()) {            
            panels.remove (licenseApprovalStep);
        }
        if (! getModel ().hasCustomComponents ()) {
            panels.remove (customHandleStep);
        }
        if (! getModel ().hasStandardComponents ()) {
            panels.remove (installStep);
        }
        isCompact = allLicensesTouched;
    }
    
}
