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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.netbeans.modules.apisupport.project.ui.ApisupportAntUIUtils;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.HarnessVersion;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Offers to upgrade old harnesses to the new version.
 * @author Jesse Glick
 * @see "issue #71630"
 */
class HarnessUpgrader {
    
    private HarnessUpgrader() {}
    
    public static void checkForUpgrade() {
        if (ModuleUISettings.getDefault().getHarnessesUpgraded()) {
            return;
        }
        ModuleUISettings.getDefault().setHarnessesUpgraded(true);
        final Set<NbPlatform> toUpgrade = new HashSet<NbPlatform>();
        for (NbPlatform p : NbPlatform.getPlatforms()) {
            if (p.isDefault() && !p.isValid()) {
                continue;
            }
            if (p.getHarnessVersion().compareTo(HarnessVersion.V50u1) >= 0) {
                continue;
            }
            if (!p.getHarnessLocation().equals(p.getBundledHarnessLocation())) {
                // Somehow custom, forget it.
                continue;
            }
            toUpgrade.add(p);
        }
        if (!toUpgrade.isEmpty()) {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    promptForUpgrade(toUpgrade);
                }
            });
        }
    }
    
    private static void promptForUpgrade(Set<NbPlatform> platforms) {
        if (ApisupportAntUIUtils.showAcceptCancelDialog(
                NbBundle.getMessage(HarnessUpgrader.class, "HarnessUpgrader.title"),
                NbBundle.getMessage(HarnessUpgrader.class, "HarnessUpgrader.text"),
                NbBundle.getMessage(HarnessUpgrader.class, "HarnessUpgrader.upgrade"),
                NbBundle.getMessage(HarnessUpgrader.class, "HarnessUpgrader.skip"),
                NotifyDescriptor.QUESTION_MESSAGE)) {
            try {
                doUpgrade(platforms);
            } catch (IOException e) {
                Util.err.notify(e);
            }
        }
    }
    
    private static void doUpgrade(Set<NbPlatform> platforms) throws IOException {
        NbPlatform plaf = NbPlatform.getDefaultPlatform();
        if (plaf == null) {
            return;
        }
        File defaultHarness = plaf.getHarnessLocation();
        for (NbPlatform p : platforms) {
            p.setHarnessLocation(defaultHarness);
        }
    }
    
}
