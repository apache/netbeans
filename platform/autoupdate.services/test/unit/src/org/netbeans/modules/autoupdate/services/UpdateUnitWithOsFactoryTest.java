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
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateUnitWithOsFactoryTest extends NbmAdvancedTestCase {
    
    public UpdateUnitWithOsFactoryTest (String testName) {
        super (testName);
    }
    
    private UpdateProvider p = null;
    private String testModuleName = "org.netbeans.modules.applemenu";
    private String testModuleVersion = "1.111";
    
    @Override
    protected void setUp () throws IOException {
        System.setProperty("netbeans.user", getWorkDirPath());
        Lookup.getDefault ().lookup (ModuleInfo.class);
        clearWorkDir ();
    }
    
    public void testUpdateItemsDoesntContainAlien () throws IOException {
        String os = org.openide.util.Utilities.isUnix () ? "Windows" : "Unix";
        Lookup.getDefault ().lookup (ModuleInfo.class);
        String catalog = generateCatalog (
                generateModuleElementWithRequires ("com.sun.collablet", "1.3", null,
                    "org.openide.filesystems > 6.2",
                    "org.openide.util > 6.2",
                    "org.openide.modules > 6.2",
                    "org.openide.nodes > 6.2",
                    "org.openide.loaders",
                    "org.openide.io"),
                generateModuleElementWithRequires (testModuleName, testModuleVersion, "org.openide.modules.os." + os,
                    "org.netbeans.core.windows/2",
                    "org.netbeans.modules.editor/3",
                    "org.netbeans.modules.java.editor/1 > 1.3",
                    "org.openide.filesystems > 6.2",
                    "org.openide.loaders",
                    "org.openide.modules > 6.2",
                    "org.openide.nodes > 6.2",
                    "org.openide.util > 6.2")
                
                );
        p = createUpdateProvider (catalog);
        p.refresh (true);
        Map<String, UpdateUnit> unitImpls = new HashMap<String, UpdateUnit> ();
        Map<String, UpdateItem> updates = p.getUpdateItems ();
        assertNotNull ("Some modules are installed.", updates);
        assertFalse ("Some modules are installed.", updates.isEmpty ());
        assertTrue (testModuleName + " found in parsed items.", updates.keySet ().contains (testModuleName + "_" + testModuleVersion));
        
        Map<String, UpdateUnit> newImpls = UpdateUnitFactory.getDefault ().appendUpdateItems (unitImpls, p);
        assertNotNull ("Some units found.", newImpls);
        assertFalse ("Some units found.", newImpls.isEmpty ());
        
        assertFalse (testModuleName + " doesn't found in generated UpdateUnits.", newImpls.keySet ().contains (testModuleName));
    }
    
    public void testUpdateItemsContainsMyModule () throws IOException {
        String os = ! org.openide.util.Utilities.isUnix () ? "Windows" : "Unix";
        String catalog = generateCatalog (
                generateModuleElementWithRequires ("com.sun.collablet", "1.3", null,
                    "org.openide.filesystems > 6.2",
                    "org.openide.util > 6.2",
                    "org.openide.modules > 6.2",
                    "org.openide.nodes > 6.2",
                    "org.openide.loaders",
                    "org.openide.io"),
                generateModuleElementWithRequires (testModuleName, testModuleVersion, "org.openide.modules.os." + os,
                    "org.netbeans.core.windows/2",
                    "org.netbeans.modules.editor/3",
                    "org.netbeans.modules.java.editor/1 > 1.3",
                    "org.openide.filesystems > 6.2",
                    "org.openide.loaders",
                    "org.openide.modules > 6.2",
                    "org.openide.nodes > 6.2",
                    "org.openide.util > 6.2")
                
                );
        p = createUpdateProvider (catalog);
        p.refresh (true);
        Map<String, UpdateUnit> unitImpls = new HashMap<String, UpdateUnit> ();
        Map<String, UpdateItem> updates = p.getUpdateItems ();
        assertNotNull ("Some modules are installed.", updates);
        assertFalse ("Some modules are installed.", updates.isEmpty ());
        assertTrue (testModuleName + " found in parsed items.", updates.keySet ().contains (testModuleName + "_" + testModuleVersion));
        
        Map<String, UpdateUnit> newImpls = UpdateUnitFactory.getDefault ().appendUpdateItems (unitImpls, p);
        assertNotNull ("Some units found.", newImpls);
        assertFalse ("Some units found.", newImpls.isEmpty ());
        
        assertTrue (testModuleName + " must found in generated UpdateUnits.", newImpls.keySet ().contains (testModuleName));
    }
    
}
