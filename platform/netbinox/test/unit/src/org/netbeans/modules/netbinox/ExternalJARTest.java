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
package org.netbeans.modules.netbinox;

import java.util.Enumeration;
import org.netbeans.core.startup.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Locale;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Do we correctly call the BundleActivators?
 *
 * @author Jaroslav Tulach
 */
public class ExternalJARTest extends SetupHid {
    private static ModuleManager mgr;
    private File simpleModule;
    private File dependsOnSimple;

    public ExternalJARTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());

        data = new File(getDataDir(), "jars");
        File activate = new File(data, "activate");
        assertTrue("Directory exists", activate.isDirectory());
        
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        File activateLib = SetupHid.createTestJAR(data, jars, "activate", null);
        System.setProperty("ext.jar", activateLib.getPath());
        
        String bundleMan = "Bundle-SymbolicName: org.foo\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Bundle-ClassPath: external:$ext.jar$\n" +
                "Import-Package: org.osgi.framework\n" +
                "Export-Package: org.activate\n\n\n";
        simpleModule = NetigsoHid.changeManifest(
            getWorkDir(), 
            SetupHid.createTestJAR(data, jars, "simple-module", null),
            bundleMan
        );
        String depMan = "Manifest-Version: 1.0\n" +
            "OpenIDE-Module: org.bar2/1\n" +
            "OpenIDE-Module-Module-Dependencies: org.foo\n\n\n";
        dependsOnSimple = NetigsoHid.changeManifest(
            getWorkDir(),
            SetupHid.createTestJAR(data, jars, "depends-on-simple-module", null, simpleModule),
            depMan
        );
    }

    public void testActivation() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        
        try {
            Module m1 = mgr.create(simpleModule, null, false, true, false);
            Module m2 = mgr.create(dependsOnSimple, null, false, false, false);
            mgr.enable(m2);

            {
                Class<?> main = m1.getClassLoader().loadClass("org.activate.Main");
                assertNotNull("m1 can load class from external library of m1", main);
            }
            
            {
                Class<?> main = m2.getClassLoader().loadClass("org.activate.Main");
                assertNotNull("m2 can load class from external library of m1", main);
            }

            mgr.disable(m2);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
}
