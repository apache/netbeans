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

package org.netbeans.modules.apisupport.project.universe;

import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;

/**
 * Test functionality of LocalizedBundleInfo.
 *
 * @author Martin Krauskopf
 */
public class LocalizedBundleInfoTest extends TestBase {

    public LocalizedBundleInfoTest(String name) {
        super(name);
    }

    public void testIsModified() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        LocalizedBundleInfo.Provider provider = p.getLookup().lookup(LocalizedBundleInfo.Provider.class);
        LocalizedBundleInfo info = provider.getLocalizedBundleInfo();
        assertFalse("just loaded", info.isModified());
        info.setCategory("my new category");
        assertTrue("modified", info.isModified());
        info.setCategory("mistyped category");
        assertTrue("modified", info.isModified());
        info.reload();
        assertFalse("reloaded", info.isModified());
    }
    
}
