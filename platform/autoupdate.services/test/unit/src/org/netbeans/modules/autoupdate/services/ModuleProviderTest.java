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

package org.netbeans.modules.autoupdate.services;

import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import java.util.Map;

import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Jiri Rechtacek
 */
public class ModuleProviderTest extends NbTestCase {
    
    public ModuleProviderTest (String testName) {
        super (testName);
    }
    
    public void testGetModules () {
        Map<String, ModuleInfo> map = InstalledModuleProvider.getInstalledModules ();
        assertNotNull ("Some modules found.", map);
        assertFalse ("Some modules, not empty", map.isEmpty ());
    }
    
}
