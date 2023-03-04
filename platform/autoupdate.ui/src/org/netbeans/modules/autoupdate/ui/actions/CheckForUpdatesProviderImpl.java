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
