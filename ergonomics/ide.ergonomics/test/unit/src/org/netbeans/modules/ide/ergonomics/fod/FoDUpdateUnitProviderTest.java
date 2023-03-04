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

package org.netbeans.modules.ide.ergonomics.fod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import junit.framework.Test;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.autoupdate.UpdateItem;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FoDUpdateUnitProviderTest extends NbTestCase {

    public FoDUpdateUnitProviderTest(String n) {
        super(n);
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(FoDUpdateUnitProviderTest.class).
            gui(false).
            clusters(".*")
        );
    }

    public void testGetDisplayName() {
        FoDUpdateUnitProvider instance = new FoDUpdateUnitProvider();
        String result = instance.getDisplayName();
        assertEquals("No name as this provider is hidden", null, result);
    }

    public void testManuallyInstalledModules() throws Exception {
        FoDUpdateUnitProvider instance = new FoDUpdateUnitProvider();
        Map<String, UpdateItem> items = instance.getUpdateItems();

        assertNull("No user installed modules yet", items.get("fod.user.installed"));
        UpdateItem ideKit = items.get("fod.base.ide");
        assertNotNull("Item for FoD found: " + items, ideKit);

        File module = createModule("empty-user-install.jar",
"Manifest-Version", "1.0",
"OpenIDE-Module", "org.netbeans.empty.user.install",
"OpenIDE-Module-Specification-Version", "1.0",
"AutoUpdate-Show-In-Client", "true"
        );
        ModuleManager man = org.netbeans.core.startup.Main.getModuleSystem().getManager();
        try {
            man.mutexPrivileged().enterWriteAccess();
            Module m = man.create(module, this, false, false, false);
            man.enable(m);
            assertTrue("Module is active", m.isEnabled());
        } finally {
            man.mutexPrivileged().exitWriteAccess();
        }

        items = instance.getUpdateItems();
        final UpdateItem userInstalled = items.get("fod.user.installed");
        assertNotNull("No user installed modules yet", userInstalled);
    }

    private File createModule(String fileName, String... attribs) throws IOException {
        File d = new File(getWorkDir(), "modules");
        d.mkdirs();
        File m = new File(d, fileName);
        FileOutputStream out = new FileOutputStream(m);
        Manifest man = new Manifest();
        for (int i = 0; i < attribs.length; i += 2) {
            man.getMainAttributes().putValue(attribs[i], attribs[i + 1]);
        }
        JarOutputStream os = new JarOutputStream(out, man);
        os.close();
        return m;
    }

}
