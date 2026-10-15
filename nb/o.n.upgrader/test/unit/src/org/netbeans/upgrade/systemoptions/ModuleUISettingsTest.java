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

package org.netbeans.upgrade.systemoptions;

public class ModuleUISettingsTest extends BasicTestForImport {

    public ModuleUISettingsTest(String name) {
        super(name, "org-netbeans-modules-apisupport-project-ui-ModuleUI.settings");
    }

    @Override
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/apisupport/project");
    }

    @Override
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "lastChosenLibraryLocation",
            "lastUsedNbPlatformLocation",
            "newModuleCounter",
            "newSuiteCounter",
            "confirmReloadInIDE",
            "lastUsedPlatformID",
            "harnessesUpgraded",
        });
    }

    public void testPropertyValues() throws Exception {
        assertPropertyTypeAndValue("lastChosenLibraryLocation", "java.lang.String", "/home/jglick");
        assertPropertyTypeAndValue("lastUsedNbPlatformLocation", "java.lang.String", "/home/jglick");
        assertPropertyTypeAndValue("newModuleCounter", "java.lang.Integer", "0");
        assertPropertyTypeAndValue("newSuiteCounter", "java.lang.Integer", "1");
        assertPropertyTypeAndValue("confirmReloadInIDE", "java.lang.Boolean", "true");
        assertPropertyTypeAndValue("lastUsedPlatformID", "java.lang.String", "default");
        assertPropertyTypeAndValue("harnessesUpgraded", "java.lang.Boolean", "true");
    }

}
