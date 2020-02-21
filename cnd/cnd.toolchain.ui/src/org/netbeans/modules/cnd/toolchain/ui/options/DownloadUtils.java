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

package org.netbeans.modules.cnd.toolchain.ui.options;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateManager.TYPE;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 */
public final class DownloadUtils {

    private DownloadUtils() {
    }

    public static boolean showDownloadConfirmation(CompilerSet cs) {
        String selected = cs.getCompilerFlavor().toString();
        String name = cs.getDisplayName();
        String uc = cs.getCompilerFlavor().getToolchainDescriptor().getUpdateCenterUrl();
        String message = ToolsPanel.getString("ToolsPanel.UpdateCenterMessageConformation", selected, name, uc);
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message,
                ToolsPanel.getString("ToolsPanel.UpdateCenterTitle"), NotifyDescriptor.YES_NO_OPTION);
        Object ret = DialogDisplayer.getDefault().notify(nd);
        if (ret == NotifyDescriptor.YES_OPTION) {
            downloadCompilerSet(cs);
            return true;
        }
        return false;
    }

    private static UpdateUnit findKnownUnit(CompilerSet cs) {
         for(UpdateUnitProvider provider : UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(false)){
            if (provider.getProviderURL() != null) {
                List<UpdateUnit> list = provider.getUpdateUnits(TYPE.MODULE);
                for (UpdateUnit unit : list) {
                    if (cs.getCompilerFlavor().getToolchainDescriptor().getModuleID().equals(unit.getCodeName())) {
                        return unit;
                    }
                }
            }
         }
         return null;
    }

    private static UpdateUnitProvider findKnownProvider(CompilerSet cs) throws MalformedURLException {
         URL url = new URL(cs.getCompilerFlavor().getToolchainDescriptor().getUpdateCenterUrl());
         for(UpdateUnitProvider provider : UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(false)){
            URL u = provider.getProviderURL();
            if (u != null) {
                String f1 = u.getFile();
                String f2 = url.getFile();
                if (f1.equalsIgnoreCase(f2)) {
                    return provider;
                }
            }
         }
         return null;
    }

    static void downloadCompilerSet(CompilerSet cs) {
        String fail = null;
        try {
            UpdateUnit unit = findKnownUnit(cs);
            if (unit == null) {
                UpdateUnitProvider provider = findKnownProvider(cs);
                if (provider == null) {
                    provider = UpdateUnitProviderFactory.getDefault().create(
                            cs.getCompilerFlavor().getToolchainDescriptor().getModuleID(),
                            cs.getCompilerFlavor().getToolchainDescriptor().getUpdateCenterDisplayName(),
                            new URL(cs.getCompilerFlavor().getToolchainDescriptor().getUpdateCenterUrl()),
                            UpdateUnitProvider.CATEGORY.COMMUNITY);
                    try {
                        provider.refresh(null, true);
                        List<UpdateUnit> list = provider.getUpdateUnits(TYPE.MODULE);
                        for (UpdateUnit u : list) {
                            if (cs.getCompilerFlavor().getToolchainDescriptor().getModuleID().equals(u.getCodeName())) {
                                unit = u;
                                break;
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        if (provider != null) {
                            UpdateUnitProviderFactory.getDefault().remove(provider);
                        }
                        fail = ToolsPanel.getString("ToolsPanel.UpdateCenterNotFound",
                                         cs.getCompilerFlavor().getToolchainDescriptor().getUpdateCenterDisplayName(),
                                         cs.getCompilerFlavor().getToolchainDescriptor().getUpdateCenterUrl());
                    }
                }
            }
            if (unit != null) {
                OperationContainer<InstallSupport> installContainer = OperationContainer.createForInstall();
                installContainer.add(unit.getAvailableUpdates());
                InstallSupport support = installContainer.getSupport();
                if (support != null) {
                    PluginManager.openInstallWizard(installContainer);
                    return;
                }
            }
            if (fail == null) {
                fail = ToolsPanel.getString("ToolsPanel.ModuleNotFound", cs.getDisplayName(),
                             cs.getCompilerFlavor().getToolchainDescriptor().getUpdateCenterDisplayName(),
                             cs.getCompilerFlavor().getToolchainDescriptor().getUpdateCenterUrl());
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            fail = ToolsPanel.getString("ToolsPanel.UpdateCenterNotFound",
                             cs.getCompilerFlavor().getToolchainDescriptor().getUpdateCenterDisplayName(),
                             cs.getCompilerFlavor().getToolchainDescriptor().getUpdateCenterUrl());
        }
        if (fail != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(fail,NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }
}
