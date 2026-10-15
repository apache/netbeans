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

/**
 * @author Radek Matous, Jesse Glick
 */
public class AntSettingsTest extends BasicTestForImport {
    public AntSettingsTest(String testName) {
        super(testName, "org-apache-tools-ant-module-AntSettings.settings");
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
            // "customDefs" not imported
        });
    }
    public void testSaveAll() throws Exception {
        assertPropertyType("saveAll","java.lang.Boolean");
        assertProperty("saveAll","true");
    }
    public void testAlwaysShowOutput() throws Exception {
        assertPropertyType("alwaysShowOutput","java.lang.Boolean");
        assertProperty("alwaysShowOutput","false");
    }
    public void testExtraClasspath() throws Exception {
        assertPropertyType("extraClasspath","org.openide.execution.NbClassPath");
        assertProperty("extraClasspath","/home/jglick/NetBeansProjects:/home/jglick/NetBeansProjects/foo/dist/foo.jar");
    }
    public void testAntHome() throws Exception {
        assertPropertyType("antHome","java.io.File");
        assertProperty("antHome","/space/src/ant/dist");
    }
    public void testVerbosity() throws Exception {
        assertPropertyType("verbosity","java.lang.Integer");
        assertProperty("verbosity","4");
    }
    public void testAutoCloseTabs() throws Exception {
        assertPropertyType("autoCloseTabs","java.lang.Boolean");
        assertProperty("autoCloseTabs","true");
    }
    public void testProperties() throws Exception {
        assertPropertyType("properties", "java.util.HashMap");
        assertProperty("properties", "hello=kitty\nmuscular=midget");
    }
}
