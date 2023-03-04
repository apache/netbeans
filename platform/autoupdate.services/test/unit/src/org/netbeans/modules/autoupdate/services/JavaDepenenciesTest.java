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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Rechtacek
 */
public class JavaDepenenciesTest extends NbmAdvancedTestCase {
    
    public JavaDepenenciesTest (String testName) {
        super (testName);
    }
    
    private UpdateProvider p = null;
    private String testModuleVersion = "1.111";
    
    @Override
    protected void setUp () throws IOException {
        System.setProperty("netbeans.user", getWorkDirPath());
        Lookup.getDefault ().lookup (ModuleInfo.class);
        clearWorkDir ();
    }
    
    public void testUpdateItemsWithCorrectJava () throws IOException {
        String testModuleName = "org.netbeans.java1.5";
        Collection<String> broken = testUpdateItemsWithJavaDependency ("Java > 1.5", testModuleName);
        assertTrue ("No Java dependency is broken but broken was: " + broken, broken.isEmpty ());
    }
    
    public void testUpdateItemsWithHigherJava () throws IOException {
        String testModuleName = "org.netbeans.java123";
        Collection<String> broken = testUpdateItemsWithJavaDependency ("Java > 123", testModuleName);
        assertFalse ("Java dependency is broken but broken was: " + broken, broken.isEmpty ());
    }
    
    private Collection<String> testUpdateItemsWithJavaDependency (String javaDep, String testModuleName) throws IOException {
        Lookup.getDefault ().lookup (ModuleInfo.class);
        String catalog = generateCatalog (
                generateModuleElementWithRequires ("com.sun.collablet", "1.3", null,
                    "org.openide.filesystems > 6.2",
                    "org.openide.util > 6.2",
                    "org.openide.modules > 6.2",
                    "org.openide.nodes > 6.2",
                    "org.openide.loaders",
                    "org.openide.io"),
                generateModuleElementWithJavaDependency (testModuleName, testModuleVersion, javaDep));
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
        OperationContainer<InstallSupport> oc = OperationContainer.createForInstall ();
        assertTrue (testModuleName + " found in generated UpdateUnits.", newImpls.keySet ().contains (testModuleName));
        
        UpdateUnit uu = newImpls.get (testModuleName);
        OperationInfo<InstallSupport> info = oc.add (uu.getAvailableUpdates ().get (0));
        return info.getBrokenDependencies ();
    }
}
