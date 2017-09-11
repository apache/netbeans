/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.autoupdate.ui.actions;

import java.util.Collection;
import java.util.HashSet;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.modules.autoupdate.ui.PluginManagerUI;
import org.netbeans.modules.autoupdate.ui.wizards.InstallUnitWizard;
import org.netbeans.modules.autoupdate.ui.wizards.LazyInstallUnitWizardIterator;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.CheckForUpdatesProvider;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Rechtacek
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.awt.CheckForUpdatesProvider.class)
public class CheckForUpdatesProviderImpl implements CheckForUpdatesProvider {

    @Override
    public boolean openCheckForUpdatesWizard(boolean reload) {
        boolean wizardFinished = false;
        RequestProcessor.Task t = PluginManagerUI.getRunningTask();
        if (t != null && !t.isFinished()) {
            DialogDisplayer.getDefault().notifyLater(
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(AutoupdateCheckScheduler.class,
                    "AutoupdateCheckScheduler_InstallInProgress"), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return false;
        }
        Collection<LazyInstallUnitWizardIterator.LazyUnit> units = LazyInstallUnitWizardIterator.LazyUnit.loadLazyUnits(OperationWizardModel.OperationType.UPDATE);
        try {
            wizardFinished = new InstallUnitWizard().invokeLazyWizard(units, OperationWizardModel.OperationType.UPDATE, reload);
        } finally {
            if (wizardFinished) {
                PluginManagerUI pluginManagerUI = PluginManagerAction.getPluginManagerUI();
                if (pluginManagerUI != null) {
                    pluginManagerUI.updateUnitsChanged();
                }
            }
        }
        return wizardFinished;
    }

    @Override
    public boolean notifyAvailableUpdates(boolean reload) {
        Collection<UpdateElement> updateElements = new HashSet<UpdateElement> ();
        AutoupdateCheckScheduler.checkUpdateElements(OperationWizardModel.OperationType.UPDATE, null, reload, updateElements);
        if (updateElements != null && ! updateElements.isEmpty()) {
            AutoupdateCheckScheduler.notifyAvailableUpdates(updateElements);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getContentDescription() {
        String res = "";
        for (UpdateUnitProvider p : UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(true)) {
            String desc = p.getContentDescription();
            if (desc != null && ! desc.isEmpty()) {
                res = res.isEmpty() ? desc : ", " + desc; // NOI18N
            }
        }
        return res;
    }
}
