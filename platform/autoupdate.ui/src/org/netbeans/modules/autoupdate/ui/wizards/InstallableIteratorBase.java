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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;

/**
 * Common base for AU wizard iterators, does basic panel handling.
 * 
 * @author sdedic
 */
abstract class InstallableIteratorBase  implements WizardDescriptor.Iterator<WizardDescriptor> {
    protected final List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>> ();
    protected final InstallUnitWizardModel installModel;
    
    protected int index;
    private WizardDescriptor.Panel<WizardDescriptor> licenseApprovalStep = null;
    private WizardDescriptor.Panel<WizardDescriptor> customHandleStep = null;
    private WizardDescriptor.Panel<WizardDescriptor> installStep = null;
    private boolean isCompact = false;
    private boolean clearLazyUnits = false;
    private final boolean allowRunInBackground;
    private final boolean runInBackground;

    public InstallableIteratorBase (InstallUnitWizardModel model) {
        this (model, false);
    }
    
    public InstallableIteratorBase (InstallUnitWizardModel model, boolean clearLazyUnits) {
        this(model, clearLazyUnits, true, true);
    }
    
    public InstallableIteratorBase (InstallUnitWizardModel model, boolean clearLazyUnits, boolean allowRunInBackground) {
        this(model, clearLazyUnits, allowRunInBackground, false);
    }
    
    public InstallableIteratorBase (InstallUnitWizardModel model, boolean clearLazyUnits, boolean allowRunInBackground, boolean runInBackground) {
        this.installModel = model;
        this.clearLazyUnits = clearLazyUnits;
        this.allowRunInBackground = allowRunInBackground;
        this.runInBackground = runInBackground;
        index = 0;
    }

    public InstallUnitWizardModel getModel () {
        assert installModel != null;
        return installModel;
    }
    
    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current () {
        assert panels != null;
        return panels.get (index);
    }
    
    @Override
    public boolean hasNext () {
        compactPanels ();
        return index < panels.size () - 1;
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
        if (!hasPrevious()) {
            throw new NoSuchElementException ();
        }
        index--;
    }
    
    @Override
    public boolean hasPrevious () {
        compactPanels ();
        return index > 0;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener (ChangeListener l) {}
    @Override
    public void removeChangeListener (ChangeListener l) {}
    
    /**
     * Inserts install-related panels at a specific index
     * @param index 
     */
    protected void insertPanels(int index) {
        licenseApprovalStep = new LicenseApprovalStep (installModel);
        panels.add (index++, licenseApprovalStep);
        customHandleStep = new CustomHandleStep (installModel);
        panels.add (index++, customHandleStep);
        installStep = new InstallStep (installModel, clearLazyUnits, allowRunInBackground, runInBackground);
        panels.add (index++, installStep);
    }
    
    /**
     * Removes previously registered install panels
     */
    protected void removeInstallPanels() {
        panels.remove(licenseApprovalStep);
        panels.remove(installStep);
        panels.remove(customHandleStep);
    }
    
    protected void compactPanels () {
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
