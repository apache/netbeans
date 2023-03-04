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
package org.netbeans.core.netigso;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.MockEvents;
import org.netbeans.MockModuleInstaller;
import org.netbeans.Module;
import org.netbeans.ModuleManager;

public class NetigsoExportPackageTest extends NetigsoHid {

    public NetigsoExportPackageTest(String name) {
        super(name);
    }
    
    
    public void testNetigsoExportPackage() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        HashSet<Module> b = null;
        boolean ok = false;
        try {
            String mfBar = "Bundle-SymbolicName: org.bar\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Import-Package: org.foo\n" +
                "\n\n";

            String mfSimple = "OpenIDE-Module: org.foo/1\n"
                    + "OpenIDE-Module-Specification-Version: 1.2\n"
                    + "OpenIDE-Module-Public-Packages: org.wrong.foo.*\n"
                    + "OpenIDE-Module-Friends: few.unknown\n"
                    + "Netigso-Export-Package: org.foo\n"
                    + "\n\n";

            
            File j1 = changeManifest(new File(jars, "simple-module.jar"), mfSimple);
            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            Module m1 = mgr.create(j1, null, false, false, false);
            Module m2 = mgr.create(j2, null, false, false, false);
            b = new HashSet<Module>(Arrays.asList(m1, m2));
            mgr.enable(b);
            ok = true;
            assertTrue("Enabled OK", m2.isEnabled());
        } finally {
            if (ok) {
                mgr.disable(b);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
}
