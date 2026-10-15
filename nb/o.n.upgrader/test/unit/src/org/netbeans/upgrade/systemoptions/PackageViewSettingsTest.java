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

public class PackageViewSettingsTest extends BasicTestForImport {

    public PackageViewSettingsTest(String name) {
        super(name, "org-netbeans-modules-java-project-packageViewSettings.settings");
    }

    @Override
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/java/project");
    }

    @Override
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "packageViewType",
        });
    }

    public void testPackageViewType() throws Exception {
        assertPropertyType("packageViewType", "java.lang.Integer");
        assertProperty("packageViewType", "0");
    }

}
