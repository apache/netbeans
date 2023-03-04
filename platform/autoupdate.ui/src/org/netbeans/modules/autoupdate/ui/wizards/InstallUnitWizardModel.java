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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.*;
import org.netbeans.modules.autoupdate.ui.Containers;
import org.netbeans.modules.autoupdate.ui.PluginManagerUI;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.openide.util.Exceptions;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallUnitWizardModel extends OperationWizardModel {
    private Installer installer = null;
    private OperationType doOperation;
    private static Set<String> approvedLicences = new HashSet<String> ();
    private OperationContainer<InstallSupport> updateContainer = null;
    private OperationContainer<OperationSupport> customContainer = Containers.forCustomInstall ();
    private PluginManagerUI manager;
    private boolean allLicensesTouched = false;
    
    /** Creates a new instance of InstallUnitWizardModel */
    public InstallUnitWizardModel (OperationType doOperation, OperationContainer<InstallSupport> updateContainer) {
        this.doOperation = doOperation;
        this.updateContainer = updateContainer;
    }
    
    public InstallUnitWizardModel (OperationType doOperation) {
        this.doOperation = doOperation;
        assert getBaseContainer () != null : "The base container for operation " + doOperation + " must exist!";
        updateContainer = getBaseContainer ();
    }
    
    @Override
    public OperationType getOperation () {
        return doOperation;
    }
    
    @Override
    public OperationContainer<InstallSupport> getBaseContainer () {
        OperationContainer c = getBaseContainerImpl();
        assert c.getSupport() != null || c.listAll().isEmpty() : "Non empty container[list: " + c.listAll() +
                ", invalid: " + c.listInvalid() + "]: but support is " + c.getSupport();
        return c;
    }

    @Override
    OperationContainer<InstallSupport> getInstallContainer() {
        return getBaseContainer();
    }
    
    private OperationContainer<InstallSupport> getBaseContainerImpl() {
        if (updateContainer != null) {
            return updateContainer;
        }
        OperationContainer<InstallSupport> c = null;
        switch (getOperation ()) {
        case INSTALL :
            c = Containers.forAvailable ();
            break;
        case UPDATE :
            c = Containers.forUpdate ();
            break;
        case LOCAL_DOWNLOAD :
            OperationContainer<InstallSupport> forUpdateNbms    = Containers.forUpdateNbms ();
            OperationContainer<InstallSupport> forAvailableNbms = Containers.forAvailableNbms();
            if (forUpdateNbms.listAll ().isEmpty ()) {
                c = forAvailableNbms;
            } else {
                c = forUpdateNbms;
                for (OperationInfo i : forAvailableNbms.listAll ()) {
                    c.add (i.getUpdateElement ());
                }
                assert forAvailableNbms.listInvalid ().isEmpty () :
                    "Containers.forAvailableNbms().listInvalid() should be empty but " + forAvailableNbms.listInvalid ();
                forAvailableNbms.removeAll ();
            }
            updateContainer = c;
            break;
        }
        return c;
    }
    
    @Override
    public OperationContainer<OperationSupport> getCustomHandledContainer () {
        return customContainer;
    }
    
    public boolean allLicensesApproved () {
        boolean res = true;
        for (UpdateElement el : getAllUpdateElements ()) {
            if (! OperationType.UPDATE.equals(getOperation()) || ! Utilities.isLicenseIdApproved(el.getLicenseId())) {
                String lic = el.getLicence ();
                if (lic != null && ! approvedLicences.contains (lic)) {
                    res = false;
                    break;
                }
            }
        }
        allLicensesTouched = true;
        return res;
    }
    public boolean allLicensesTouched() {
        return allLicensesTouched;
    }
    
    public void addApprovedLicenses (Collection<String> licences) {
        approvedLicences.addAll (licences);
        allLicensesTouched = false;
    }
    
    public void setInstaller (Installer i) {
        installer = i;
    }
    
    public Installer getInstaller () {
        return installer;
    }
    
    @Override
    public void doCleanup (boolean cancel) throws OperationException {
        try {
            if (cancel) {
                if (getBaseContainerImpl() != null && getBaseContainerImpl().getSupport() != null) {
                    getBaseContainerImpl().getSupport().doCancel();
                }
                if (OperationType.LOCAL_DOWNLOAD == getOperation ()) {
                    InstallSupport asupp = Containers.forAvailableNbms ().getSupport ();
                    if (asupp != null) {
                        asupp.doCancel ();
                    }
                    InstallSupport usupp = Containers.forUpdateNbms ().getSupport ();
                    if (usupp != null) {
                        usupp.doCancel ();
                    }
                    Containers.forAvailableNbms ().removeAll ();
                    Containers.forUpdateNbms ().removeAll ();
                } else {
                    InstallSupport isupp = (InstallSupport) getBaseContainer ().getSupport ();
                    if (isupp != null) {
                        isupp.doCancel ();
                    }
                }
                OperationSupport osupp = getCustomHandledContainer ().getSupport ();
                if (osupp != null) {
                    osupp.doCancel ();
                }
            }
        } catch (Exception x) {
            Exceptions.printStackTrace (x);
        } finally {
            super.doCleanup (false);
        }
    }
    
    public PluginManagerUI getPluginManager () {
        return manager;
    }
    
    public void setPluginManager (PluginManagerUI manager) {
        this.manager = manager;
    }
}
