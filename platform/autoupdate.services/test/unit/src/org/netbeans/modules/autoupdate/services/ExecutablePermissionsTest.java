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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;

/**
 *
 * @author Dmitry Lipin
 */
public class ExecutablePermissionsTest extends NbmAdvancedTestCase {

    private UpdateProvider p = null;
    private String testModuleVersion = "1.0";
    private String testModuleName = "org.yourorghere.executable.permissions";

    public ExecutablePermissionsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws IOException, Exception {
        super.setUp();
    }

    @Override
    public boolean canRun() {
        return super.canRun() && !System.getProperty("os.name").startsWith("Windows");
    }

    private String generateExecutablePermissionsModuleElement() {
        String res = "\n<module codenamebase=\"" + testModuleName + "\" " +
                "homepage=\"\" distribution=\"nbresloc:/org/netbeans/api/autoupdate/data/org-yourorghere-executable-permissions.nbm\" " +
                "license=\"AD9FBBC9\" downloadsize=\"3661\" " +
                "needsrestart=\"false\" moduleauthor=\"Steffen Dietz\" " +
                "eager=\"false\" " +
                "releasedate=\"2017/11/24\">\n";
        res += "<manifest OpenIDE-Module=\"" + testModuleName + "\" " +
                "OpenIDE-Module-Name=\"" + testModuleName + "\" " +
                "AutoUpdate-Show-In-Client=\"true\" " +
                "OpenIDE-Module-Specification-Version=\"" + testModuleVersion + "\"/>\n";
        res += "</module>";
        return res;
    }

    public void testExecutablePermissionsModule() throws IOException {
        String catalog = generateCatalog(generateExecutablePermissionsModuleElement());

        p = createUpdateProvider(catalog);
        p.refresh(true);

        Map<String, UpdateUnit> unitImpls = new HashMap<String, UpdateUnit>();
        Map<String, UpdateItem> updates = p.getUpdateItems();
        assertNotNull("Some modules are installed.", updates);
        assertFalse("Some modules are installed.", updates.isEmpty());
        assertTrue(testModuleName + " found in parsed items.", updates.keySet().contains(testModuleName + "_" + testModuleVersion));

        Map<String, UpdateUnit> newImpls = UpdateUnitFactory.getDefault().appendUpdateItems(unitImpls, p);
        assertNotNull("Some units found.", newImpls);
        assertFalse("Some units found.", newImpls.isEmpty());

        UpdateUnit u1 = newImpls.get(testModuleName);
        installUpdateUnit(u1);
        File f = new File(userDir, "bin/start.sh");
        assertTrue("File " + f + " should exist after module installation", f.exists());
        assertTrue("File " + f + " is not executable after module installation", f.canExecute());
    }
}
