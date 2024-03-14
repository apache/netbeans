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
public class TaskListSettingsTest extends BasicTestForImport {
    public TaskListSettingsTest(String testName) {
        super(testName, "org-netbeans-modules-tasklist-docscan-Settings.settings");
    }

    @Override
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("/org/netbeans/modules/tasklist/docscan");
    }
    
    @Override
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {"Tag<<<<<<<",
                "Tag@todo",
                "TagFIXME",
                "TagPENDING",
                "TagTODO",
                "TagXXX",
                "skipComments",
                "modificationTime",
                "usabilityLimit"
        });
    }
    
    public void testModificationTime() throws Exception {
        assertPropertyType("modificationTime", "java.lang.Long");
        assertProperty("modificationTime", "0");
    }
    public void testSkipComments() throws Exception {
        assertPropertyType("skipComments","java.lang.Boolean");
        assertProperty("skipComments","false");
    }
    public void testUsabilityLimit() throws Exception {
        assertPropertyType("usabilityLimit","java.lang.Integer");
        assertProperty("usabilityLimit","300");
    }
    
    public void testTaskTagsTypes() throws Exception {
        assertProperty("Tag<<<<<<<", "1");
        assertProperty("Tag@todo","3");
        assertProperty("TagFIXME","3");
        assertProperty("TagPENDING","3");
        assertProperty("TagTODO","3");
        assertProperty("TagXXX","3");
    } 
}
