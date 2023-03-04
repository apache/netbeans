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
 *
 * @author tomas
 */
public class CvsSettingsTest extends BasicTestForImport {

    public CvsSettingsTest(String testName) {
        super(testName, "org-netbeans-modules-versioning-system-cvss-settings-CvsModuleConfig.settings");
    }

    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/versioning/system/cvss"); 
    }
    
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {"commitExclusions.0", "commitExclusions.1", "commitExclusions.2", "defaultValues", "ignoredFilePatterns", "textAnnotationsFormat"});        
    }

    public void testPropertyTypes() throws Exception {
        assertPropertyType("commitExclusions", "org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig.PersistentHashSet");                
    }
    
    public void testProperties() throws Exception {
        assertProperty("commitExclusions.0", "/home/tomas/JavaApplication2/src/javaapplication2/NewClass.java");                
        assertProperty("commitExclusions.1", "/home/tomas/JavaApplication2/src/javaapplication2/NewClass1.java");                
        assertProperty("commitExclusions.2", "/home/tomas/JavaApplication2/src/javaapplication2/Main.java");                                        
    }
    
}
