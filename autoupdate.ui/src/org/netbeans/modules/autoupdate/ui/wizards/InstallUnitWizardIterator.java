/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
