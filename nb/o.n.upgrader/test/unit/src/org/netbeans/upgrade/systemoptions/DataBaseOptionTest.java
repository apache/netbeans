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
 * @author Radek Matous
 */
public class DataBaseOptionTest extends BasicTestForImport {
    public DataBaseOptionTest(String testName) {
        super(testName, "org-netbeans-modules-db-explorer-DatabaseOption.settings");
    }

    @Override
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/db");
    }
        
    @Override
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {
        "autoConn", "debugMode"
        });
    }

    public void testAutoConn() throws Exception {
        assertPropertyType("autoConn", "java.lang.Boolean");
        assertProperty("autoConn", "false");
    }

    public void testDebugMode() throws Exception {
        assertPropertyType("debugMode", "java.lang.Boolean");
        assertProperty("debugMode", "true");
    }        
}
