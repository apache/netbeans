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

import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
final class InstallUnitWizardIterator extends InstallableIteratorBase {
    
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
        super(model, clearLazyUnits, allowRunInBackground, runInBackground);
        createPanels ();
    }
    
    public InstallUnitWizardModel getModel () {
        assert installModel != null;
        return installModel;
    }
    
    private void createPanels () {
        assert panels != null && panels.isEmpty() : "Panels are still empty";
        panels.add (new OperationDescriptionStep (installModel));
        insertPanels(1);
    }
    
    @Override
    public String name () {
        return NbBundle.getMessage (InstallUnitWizardIterator.class, "InstallUnitWizard_Title");
    }
    
    @Override
    public boolean hasPrevious () {
        return super.hasPrevious() && ! (current () instanceof InstallStep || current () instanceof CustomHandleStep);
    }
    
}
