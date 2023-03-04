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

package org.netbeans.modules.ide.ergonomics.fod;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import junit.framework.Test;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.Dependency;

import org.openide.util.Lookup;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;


/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class EnableJ2EEEnablesJavaTest extends NbTestCase {

    public EnableJ2EEEnablesJavaTest(String name) {
        super(name);
    }

    public static Test suite() {
        Test test = NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(EnableJ2EEEnablesJavaTest.class).
            gui(false).
            clusters("ergonomics.*").
            clusters(".*").
            enableModules("ide[0-9]*", ".*").
            honorAutoloadEager(true)
        );
        return test;
    }

    public void testEnablingJ2EEEnablesJavaViaAutoUpdateManager() throws Exception {
        FeatureInfo j2ee = null;
        FeatureInfo java = null;
        for (FeatureInfo f : FeatureManager.features()) {
            if (f.getCodeNames().contains("org.netbeans.modules.j2ee.kit")) {
                j2ee = f;
            }
            if (f.getCodeNames().contains("org.netbeans.modules.java.kit")) {
                java = f;
            }
        }
        if (j2ee == null) {
            return;
        }
        assertNotNull("java feature found", java);

        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.FEATURE);
        UpdateElement j2eeUE = null;
        StringBuilder sb = new StringBuilder();
        for (UpdateUnit uu : units) {
            sb.append(uu.getCodeName()).append('\n');
            if (uu.getCodeName().equals("fod.org.netbeans.modules.j2ee.kit")) {
                j2eeUE = uu.getInstalled();
            }
        }
        assertNotNull("J2EE found: " + sb, j2eeUE);
        OperationContainer<OperationSupport> cc = OperationContainer.createForEnable();
        OperationInfo<OperationSupport> info = cc.add(j2eeUE);
        cc.add(info.getRequiredElements());
        cc.getSupport().doOperation(null);


        Set<String> expectedNames = new HashSet<String>(java.getCodeNames());
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (mi.isEnabled()) {
                expectedNames.remove(mi.getCodeNameBase());
            } else {
                for (Dependency d : mi.getDependencies()) {
                    if (d.getType() == Dependency.TYPE_JAVA) {
                        SpecificationVersion v1 = new SpecificationVersion(d.getVersion());
                        SpecificationVersion v2 = Dependency.JAVA_SPEC;
                        if (v2.compareTo(v1) < 0) {
                            // test is running insufficient runtime
                            expectedNames.remove(mi.getCodeNameBase());
                        }
                    }
                }
            }
        }
        if (!expectedNames.isEmpty()) {
            fail(
                "java cluster shall be fully enabled, but this was missing:\n" +
                expectedNames.toString().replace(',', '\n')
            );
        }
    }

}
