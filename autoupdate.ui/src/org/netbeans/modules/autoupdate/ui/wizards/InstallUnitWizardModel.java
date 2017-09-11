/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
public final class InstallUnitWizardModel extends OperationWizardModel {
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
