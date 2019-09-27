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
package org.netbeans.modules.ide.ergonomics.fod;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/** Feature On Demand Unit Provider.
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service=UpdateProvider.class)
public class FoDUpdateUnitProvider implements UpdateProvider {
    private static final Logger log = Logger.getLogger (FoDUpdateUnitProvider.class.getName ()); // NOI18N

    public FoDUpdateUnitProvider() {
    }

    @Override
    public String getName () {
        return "fod-provider";
    }

    @Override
    public String getDisplayName () {
        return null;
    }


    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public CATEGORY getCategory() {
        return CATEGORY.STANDARD;
    }
    
    /**
     * Known extra modules present during tests. Should contain compile-time
     * modules NB Javac stub
     */
    private static final Collection<String> EXCLUDE_EXTRA_MODULES = new HashSet<>(Arrays.asList(
        "org.netbeans.modules.java.source.nbjavac.test"// NOI18N
    ));

    // protected because of tests obly
    protected boolean acceptsModule(ModuleInfo mi) {
        return !EXCLUDE_EXTRA_MODULES.contains(mi.getCodeNameBase());
    }
    
    @Override
    public Map<String, UpdateItem> getUpdateItems () throws IOException {
        Map<String, UpdateItem> res = new HashMap<String, UpdateItem> ();
        Collection<? extends ModuleInfo> all = Lookup.getDefault().lookupAll(ModuleInfo.class);
        Set<ModuleInfo> notYetProcessed = new HashSet<ModuleInfo>(all);
        FEATURES: for (FeatureInfo fi : FeatureManager.features()) {
            if (!fi.isPresent()) {
                continue;
            }
            Set<String> cnbs = new HashSet<>(fi.getCodeNames());
            SpecificationVersion jdk = new SpecificationVersion(System.getProperty("java.specification.version")); // NOI18N
            // add required extra modules
            // extra module is required iff it provides ONLY getExtraModulesRequiredText (but not recommended one), or
            // the current JDK does not satisfy the minJDK requirement of the extra module.
            for (FeatureInfo.ExtraModuleInfo mi :  fi.getExtraModules()) {
                String m = mi.explicitCodebase();
                if (m == null) {
                    continue;
                }
                boolean required = false;

                if (mi.recMinJDK != null && jdk.compareTo(mi.recMinJDK) < 0) {
                    required = true;
                }
                if (fi.getExtraModulesRequiredText() != null && fi.getExtraModulesRecommendedText() == null) {
                    required = true;
                }
                if (required) {
                    cnbs.add(m);
                }
            }
            if (registerFeature(
                all, cnbs,
                fi.getFeatureCodeNameBase(), null, res, notYetProcessed
            )) {
                continue;
            }
        }
        Set<String> baseIDE = new HashSet<String>();
        Set<String> extra = new HashSet<String>();
        for (ModuleInfo mi : notYetProcessed) {
            if (FeatureManager.showInAU(mi)) {
                if (isPlatformCluster(mi)) {
                    continue;
                }
                if (isIDECluster(mi)) {
                    baseIDE.add(mi.getCodeNameBase());
                } else if (acceptsModule(mi)) {
                    extra.add(mi.getCodeNameBase());
                }
            }
        }
        registerFeature(
            all, baseIDE, "base.ide",
            NbBundle.getMessage(FoDUpdateUnitProvider.class, "FeatureBaseIDE"),
            res, notYetProcessed
        );
        if (!extra.isEmpty()) {
            registerFeature(
                all, extra, "user.installed",
                NbBundle.getMessage(FoDUpdateUnitProvider.class, "FeatureUserInstalled"),
                res, notYetProcessed
            );
        }

        return res;
    }
    
    @Override
    public boolean refresh (boolean force) throws IOException {
        return true;
    }

    private boolean registerFeature(
        Collection<? extends ModuleInfo> allModules,
        Set<String> codeNames, String prefCnb, String displayName, Map<String, UpdateItem> res,
        Set<ModuleInfo> processed
    ) {
        Set<String> justKits = new HashSet<String>();
        justKits.addAll(codeNames);
        String name = "fod." + prefCnb; // NOI18N
        ModuleInfo preferred = null;
        StringBuilder description = new StringBuilder();
        for (ModuleInfo mi : allModules) {
            if (prefCnb != null && prefCnb.equals(mi.getCodeNameBase())) {
                preferred = mi;
            }
            if (codeNames.contains(mi.getCodeNameBase())) {
                processed.remove(mi);
                if (FeatureManager.showInAU(mi)) {
                    StringBuilder sb = new StringBuilder();
                    Object desc = mi.getLocalizedAttribute("OpenIDE-Module-Long-Description"); // NOI18N
                    if (!(desc instanceof String)) {
                        desc = mi.getLocalizedAttribute("OpenIDE-Module-Short-Description"); // NOI18N
                    }
                    if (desc instanceof String) {
                        sb.append("<h5>").append(mi.getDisplayName()).append("</h5>"); // NOI18N
                        sb.append(desc);
                        sb.append("\n"); // NOI18N
                    }
                    if (preferred == mi) {
                        description.insert(0, sb);
                    } else {
                        description.append(sb);
                    }
                    for (Dependency d : mi.getDependencies()) {
                        if (d.getType() != Dependency.TYPE_MODULE) {
                            continue;
                        }
                        FeatureInfo fi = FeatureManager.findInfo(d.getName());
                        if (fi != null) {
                            // assert fi.getFeatureCodeNameBase() != null : "No CNB for " + fi; // unit-test FeatureOffDemand**
                            String cnb = "fod." + fi.getFeatureCodeNameBase();
                            if (!cnb.equals(name)) {
                                justKits.add(cnb);
                            }
                        }
                    }
                    continue;
                }
                justKits.remove(mi.getCodeNameBase());
            }
        }
        Object featureName = displayName;
        String specVersion;
        if (featureName == null) {
            if (preferred == null) {
                return true;
            }
            featureName = preferred.getLocalizedAttribute("OpenIDE-Module-Display-Category"); // NOI18N
            if (!(name instanceof String)) {
                featureName = preferred.getDisplayName();
            }
            specVersion = preferred.getSpecificationVersion().toString();
        } else {
            specVersion = "1.0";
        }
        UpdateItem feature = UpdateItem.createFeature(name, specVersion, justKits, (String) featureName, description.toString(), null);
        res.put(name, feature);
        return false;
    }

    private boolean isIDECluster(ModuleInfo mi) {
        return FeatureManager.isModuleFrom(mi, "ide"); // NOI18N
    }
    private boolean isPlatformCluster(ModuleInfo mi) {
        if ("org.netbeans.modules.ide.branding.kit".equals(mi.getCodeName())) { // NOI18N
            return true;
        }
        return FeatureManager.isModuleFrom(mi, "platform"); // NOI18N
    }
}
