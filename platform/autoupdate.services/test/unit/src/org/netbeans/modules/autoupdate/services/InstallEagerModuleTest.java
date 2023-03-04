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

    public void testInstallRegularModule () throws IOException {
        String eagerModule = "com.sun.testmodule.eager";
        String regularModule1 = "org.yourorghere.independent";
        String regularModule2 = "org.yourorghere.engine";
        String catalog = generateCatalog (
                generateModuleElement (eagerModule, "1.0", null, null, false, true,
                    regularModule1,
                    regularModule2),
                generateModuleElement (regularModule1, "1.0", null, null, true, false),
                generateModuleElement (regularModule2, "1.0", null, null, true, false, regularModule1)

                );

        AutoupdateCatalogProvider p = createUpdateProvider (catalog);
        p.refresh (true);
        Map<String, UpdateItem> updates = p.getUpdateItems ();

        // initial check of updates being and its states
        ModuleItem eagerModuleItem = (ModuleItem) Trampoline.SPI.impl (updates.get (eagerModule + "_1.0"));
        assertFalse (eagerModuleItem.getModuleInfo ().getDependencies () + " are not empty.",
                eagerModuleItem.getModuleInfo ().getDependencies ().isEmpty ());
        ModuleItem regularModule1Item = (ModuleItem) Trampoline.SPI.impl (updates.get (regularModule1 + "_1.0"));
        ModuleItem regularModule2Item = (ModuleItem) Trampoline.SPI.impl (updates.get (regularModule2 + "_1.0"));
        assertTrue (eagerModule + " is eager.", eagerModuleItem.isEager () && ! eagerModuleItem.isAutoload ());
        assertFalse (regularModule1 + " is regular.", regularModule1Item.isEager () || regularModule1Item.isAutoload ());
        assertFalse (regularModule2 + " is regular.", regularModule2Item.isEager () || regularModule2Item.isAutoload ());

        // acquire UpdateUnits for test modules
        UpdateUnitProviderFactory.getDefault ().create ("test-update-provider", "test-update-provider", generateFile (catalog));
        UpdateUnitProviderFactory.getDefault ().refreshProviders (null, true);
        UpdateUnit u1 = UpdateManagerImpl.getInstance ().getUpdateUnit (regularModule1);
        assertTrue (UpdateManager.TYPE.KIT_MODULE.equals (u1.getType ()));

        //keep it to prevent GC-ed cache in UpdateManagerImpl
        List<UpdateUnit> uuu =   UpdateManagerImpl.getInstance ().getUpdateUnits();
        UpdateUnit u2 = UpdateManagerImpl.getInstance ().getUpdateUnit (regularModule2);
        UpdateUnit ea = UpdateManagerImpl.getInstance ().getUpdateUnit (eagerModule);
        assertNotNull ("Unit " + regularModule1 + " found.", u1);
        assertNotNull ("Unit " + regularModule2 + " found.", u2);
        assertNotNull ("Unit " + eagerModule + " found.", ea);

        // install regular module 1
        installUpdateUnit (u1);

        // check states installed regular 1 and others
        assertNotNull (u1 + " is installed.", u1.getInstalled ());
        assertNull (u2 + " is not installed.", u2.getInstalled ());
        assertNull (ea + " is not installed.", ea.getInstalled ());

        // installe regular module 2
        installUpdateUnit (u2);

        // check states installed regular 1 and others
        assertNotNull (u1 + " is installed.", u1.getInstalled ());
        assertNotNull (u2 + " is installed.", u2.getInstalled ());
        assertNotNull (ea + " is must be installed as well because all required modules are on.", ea.getInstalled ());

    }

}
