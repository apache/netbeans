/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
