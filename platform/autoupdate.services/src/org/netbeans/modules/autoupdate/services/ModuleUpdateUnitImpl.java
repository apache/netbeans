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

package org.netbeans.modules.autoupdate.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateManager.TYPE;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.modules.ModuleInfo;
import static org.netbeans.modules.autoupdate.services.Bundle.*;
import org.openide.util.NbBundle.Messages;

public class ModuleUpdateUnitImpl extends UpdateUnitImpl {
    private UpdateUnit visibleAncestor;

    public ModuleUpdateUnitImpl (String codename) {
        super (codename);
    }

    @Override
    public TYPE getType () {
        return UpdateManager.TYPE.MODULE;
    }
    
    @Override
    public boolean isPending () {
        return UpdateUnitFactory.getDefault().isScheduledForRestart (getUpdateUnit ());
    }
    
    @Messages("broad_category=Libraries, Bridges, Uncategorized")
    private static String BROAD_CATEGORY = broad_category();

    @Override
    public UpdateUnit getVisibleAncestor() {
        if (visibleAncestor == null) {
            assert getInstalled() != null : this + " is installed";
            ModuleUpdateElementImpl installedImpl = (ModuleUpdateElementImpl) Trampoline.API.impl(getInstalled());
            TreeSet<Module> visible = new TreeSet<Module> (new Comparator<Module> () {

                @Override
                public int compare(Module o1, Module o2) {
                    return o1.getCodeNameBase().compareTo(o2.getCodeNameBase());
                }
            });
            Set<Module> seen = new HashSet<Module> ();
            for (ModuleInfo mi : installedImpl.getModuleInfos()) {
                visible.addAll(findVisibleAncestor(Utilities.toModule(mi), seen));
            }
            String cat = installedImpl.getCategory();
            String installationCluster = installedImpl.getInstallationCluster();
            if (BROAD_CATEGORY.contains(cat)) {
                cat = null;
            }
            UpdateUnit shot = null;
            UpdateUnit spare = null;
            UpdateUnit strike = null;
            for (Module visMod : visible) {
                visibleAncestor = Utilities.toUpdateUnit(visMod);
                UpdateElementImpl visibleImpl = Trampoline.API.impl(visibleAncestor.getInstalled());
                String visTargetCluster = null;
                String visCat = null;
                if (visibleImpl != null && visibleImpl instanceof ModuleUpdateElementImpl) {
                    visTargetCluster = ((ModuleUpdateElementImpl) visibleImpl).getInstallationCluster();
                    visCat = visibleImpl.getCategory();
                }
                if (installationCluster != null && installationCluster.equals(visTargetCluster)) {
                    spare = visibleAncestor;
                } else if (visCat != null && visCat.equals(cat)) {
                    strike = visibleAncestor;
                    break;
                } else if (shot == null) {
                    shot = visibleAncestor;
                }
            }
            visibleAncestor = strike != null ? strike : spare != null ? spare : shot;
            
            // if it's still unknown - try visible representative in given cluster
            if (visibleAncestor == null && installationCluster != null) {
                for (UpdateElement visEl : UpdateManagerImpl.getInstance().getInstalledKits(installationCluster)) {
                    visibleAncestor = visEl.getUpdateUnit();
                    if (installedImpl.getRawCategory().equals(visEl.getCategory())) {
                        visibleAncestor = visEl.getUpdateUnit();
                        break;
                    }
                }
            }
        }
        return visibleAncestor;
    }
    
    private static Set<Module> findVisibleAncestor(Module module, Set<Module> seen) {
        if (! seen.add(module)) {
            return Collections.emptySet();
        }
        Set<Module> visible = new HashSet<Module> ();
        ModuleManager manager = module.getManager();
        Set<Module> moduleInterdependencies = manager.getModuleInterdependencies(module, ! module.isEager(), false, true);
        for (Module m : moduleInterdependencies) {
            if (m.isEnabled() && Utilities.isKitModule(m)) {
                visible.add(m);
            }
        }
        if (visible.isEmpty()) {
            for (Module m : moduleInterdependencies) {
                if (! m.isEnabled()) {
                    continue;
                }
                assert ! module.equals(m) : m + " cannot depend on itself.";
                if (module.equals(m)) {
                    continue;
                }
                visible.addAll(findVisibleAncestor(m, seen));
                if (! visible.isEmpty()) {
                    break;
                }
            }
        }
        TreeSet<Module> res = new TreeSet<Module>(new Comparator<Module>() {
            @Override
            public int compare(Module o1, Module o2) {
                return o1.getCodeNameBase().compareTo(o2.getCodeNameBase());
            }
        });
        res.addAll(visible);
        return res;
    }

}
