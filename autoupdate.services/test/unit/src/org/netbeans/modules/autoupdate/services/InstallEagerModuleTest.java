/**
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

package org.netbeans.modules.autoupdate.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.netbeans.modules.autoupdate.updateprovider.ModuleItem;
import org.netbeans.spi.autoupdate.UpdateItem;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallEagerModuleTest extends NbmAdvancedTestCase {
    
    public InstallEagerModuleTest (String testName) {
        super (testName);
    }
    
    public void testIfAutoupdateUnderstandsEagerAttribute () throws IOException {
        // Lookup.getDefault ().lookup (ModuleInfo.class);
        String eagerModule = "com.sun.testmodule.eager";
        String regularModule = "com.sun.testmodule.regular";
        String catalog = generateCatalog (
                generateModuleElement (eagerModule, "1.3", null, null, false, true,
                    "org.openide.filesystems > 6.2",
                    "org.openide.util > 6.2",
                    "org.openide.modules > 6.2",
                    "org.openide.nodes > 6.2",
                    "org.openide.loaders",
                    "org.openide.io"),
                generateModuleElement (regularModule, "1.3", null, null, false, false,
                    "org.openide.filesystems > 6.2",
                    "org.openide.util > 6.2",
                    "org.openide.modules > 6.2",
                    "org.openide.nodes > 6.2",
                    "org.openide.loaders",
                    "org.openide.io")
                
                );
        AutoupdateCatalogProvider p = createUpdateProvider (catalog);
        p.refresh (true);
        Map<String, UpdateItem> updates = p.getUpdateItems ();
        assertNotNull ("Some modules are installed.", updates);
        assertFalse ("Some modules are installed.", updates.isEmpty ());
        
        // check being 
        assertTrue (eagerModule + " found in parsed items.", updates.keySet ().contains (eagerModule + "_1.3"));
        assertTrue (regularModule + " found in parsed items.", updates.keySet ().contains (regularModule + "_1.3"));
        
        // check state
        ModuleItem eagerModuleItem = (ModuleItem) Trampoline.SPI.impl (updates.get (eagerModule + "_1.3"));
        ModuleItem regularModuleItem = (ModuleItem) Trampoline.SPI.impl (updates.get (regularModule + "_1.3"));
        assertTrue (eagerModule + " is eager.", eagerModuleItem.isEager () && ! eagerModuleItem.isAutoload ());
        assertFalse (regularModule + " is regular.", regularModuleItem.isEager () || regularModuleItem.isAutoload ());
    }
    
    public void testAquireEargersFromManager () throws IOException {
        String eagerModule = "com.sun.testmodule.eager";
        String regularModule1 = "org.yourorghere.independent";
        String regularModule2 = "org.yourorghere.engine";
        String catalog = generateCatalog (
                generateModuleElement (eagerModule, "1.0", null, null, false, true,
                    regularModule1,
                    regularModule2),
                generateModuleElement (regularModule1, "1.0", null, null, false, false),
                generateModuleElement (regularModule2, "1.0", null, null, false, false, regularModule1)
                
                );

        UpdateUnitProviderFactory.getDefault ().create ("test-update-provider", "test-update-provider", generateFile (catalog));
        UpdateUnitProviderFactory.getDefault ().refreshProviders (null, true);
        
        Set<UpdateElement> eagerElements = UpdateManagerImpl.getInstance ().getAvailableEagers ();
        assertFalse ("Some available eagers are found.", eagerElements.isEmpty ());
        UpdateUnit ea = UpdateManagerImpl.getInstance ().getUpdateUnit (eagerModule);
        UpdateElement foundEaEl = eagerElements.iterator ().next ();
        UpdateUnit foundEaU = foundEaEl.getUpdateUnit ();
        assertEquals ("Same eager UpdateUnit", ea, foundEaU);
        
        // assertFalse ("Some installed eagers are found.", UpdateManagerImpl.getInstance ().getInstalledEagers ().isEmpty ());
    }
}
