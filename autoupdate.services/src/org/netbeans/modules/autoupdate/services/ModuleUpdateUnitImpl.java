/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
            return Collections.EMPTY_SET;
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
