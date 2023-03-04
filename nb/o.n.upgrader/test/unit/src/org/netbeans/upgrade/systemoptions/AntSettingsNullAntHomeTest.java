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

package org.netbeans.upgrade.systemoptions;

/**
 * Test for issue #92439.
 * @author Jesse Glick
 */
public class AntSettingsNullAntHomeTest extends BasicTestForImport {

    public AntSettingsNullAntHomeTest(String n) {
        super(n, "AntSettings-null-ant-home.settings");
    }

    @Override
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/apache/tools/ant/module");
    }

    @Override
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
            "saveAll",
            "alwaysShowOutput",
            "extraClasspath",
            "antHome",
            "verbosity",
            "autoCloseTabs",
            "properties",
        });
    }

    public void testNullAntHome() throws Exception {
        assertProperty("antHome", null);
    }

}
