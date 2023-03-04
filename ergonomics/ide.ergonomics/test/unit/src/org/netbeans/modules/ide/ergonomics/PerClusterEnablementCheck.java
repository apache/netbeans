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

package org.netbeans.modules.ide.ergonomics;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.netbeans.modules.ide.ergonomics.fod.FindComponentModules;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class PerClusterEnablementCheck extends NbTestCase {
    public PerClusterEnablementCheck(String n) {
        super(n);
    }
    
    public void testAPISupportTriggersAlsoJavaKit() throws Exception {
        FeatureInfo apisupport = null;
        FeatureInfo java = null;
        for (FeatureInfo f : FeatureManager.features()) {
            if (f.getCodeNames().contains("org.netbeans.modules.apisupport.kit")) {
                apisupport = f;
            }
            if (f.getCodeNames().contains("org.netbeans.modules.java.kit")) {
                java = f;
            }
        }
        assertNotNull("apisupport feature found", apisupport);
        assertNotNull("java feature found", java);

        FindComponentModules find = new FindComponentModules(apisupport);
        Set<String> expectedNames = new HashSet<String>(java.getCodeNames());
        for (UpdateElement updateElement : find.getModulesForEnable()) {
            expectedNames.remove(updateElement.getCodeName());
        }
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (isEager(mi) || isAutoload(mi)) {
                expectedNames.remove(mi.getCodeNameBase());
            }
        }
        if (!expectedNames.isEmpty()) {
            fail(
                "java cluster shall be fully enabled, but this was missing:\n" +
                expectedNames.toString().replace(',', '\n')
            );
        }
    }

    private boolean isEager(ModuleInfo mi) throws Exception {
        Method m = mi.getClass().getMethod("isEager");
        return (Boolean)m.invoke(mi);
    }
    private boolean isAutoload(ModuleInfo mi) throws Exception {
        Method m = mi.getClass().getMethod("isAutoload");
        return (Boolean)m.invoke(mi);
    }
}
