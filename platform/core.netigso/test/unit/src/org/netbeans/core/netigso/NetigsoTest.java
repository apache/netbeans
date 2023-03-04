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
import org.netbeans.MockModuleInstaller;
import org.netbeans.MockEvents;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.junit.RandomlyFails;

/**
 * Basic tests to verify the basic interaction between NetBeans module
 * system and OSGi.
 *
 * @author Jaroslav Tulach
 */
@RandomlyFails // assert framework != null or OverlappingFileLockException in BundleCache.<init>
public class NetigsoTest extends NetigsoHid {

    public NetigsoTest(String name) {
        super(name);
    }

    public void testFactoryCreatesOurModules() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m2;
        Module m1;
        HashSet<Module> both = null;
        try {
            String mf = "Bundle-SymbolicName: org.foo;singleton:=true\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Export-Package: org.foo";
            String mfBar =
                "OpenIDE-Module: org.bar/1\n" +
                "OpenIDE-Module-Name: Depends on bar test module\n" +
                "OpenIDE-Module-Module-Dependencies: org.foo\n" +
                "some";

            File j1 = changeManifest(new File(jars, "simple-module.jar"), mf);
            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            m1 = mgr.create(j1, null, false, false, false);
            m2 = mgr.create(j2, null, false, false, false);
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(m1, m2));
            mgr.enable(b);
            both = b;

            
            Class<?> clazz = m2.getClassLoader().loadClass("org.bar.SomethingElse");
            Class<?> sprclass = m2.getClassLoader().loadClass("org.foo.Something");

            assertEquals("Correct parent is used", sprclass, clazz.getSuperclass());

        } finally {
            if (both != null) {
                mgr.disable(both);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }

    }

    public void testDashnames() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m2;
        Module m1;
        HashSet<Module> both = null;
        try {
            String mf = "Bundle-SymbolicName: org.foo-bar\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Export-Package: org.foo";
            String mfBar =
                "OpenIDE-Module: org.bar/1\n" +
                "OpenIDE-Module-Name: Depends on bar test module\n" +
                "OpenIDE-Module-Module-Dependencies: org.foo_bar\n" +
                "some";

            File j1 = changeManifest(new File(jars, "simple-module.jar"), mf);
            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            m1 = mgr.create(j1, null, false, false, false);
            m2 = mgr.create(j2, null, false, false, false);
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(m1, m2));
            mgr.enable(b);
            both = b;


            Class<?> clazz = m2.getClassLoader().loadClass("org.bar.SomethingElse");
            Class<?> sprclass = m2.getClassLoader().loadClass("org.foo.Something");

            assertEquals("Correct parent is used", sprclass, clazz.getSuperclass());

        } finally {
            if (both != null) {
                mgr.disable(both);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }

    }

    public void testFactoryCreatesOurModulesWithDeps() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        HashSet<Module> both = null;
        try {
            String mf = "Bundle-SymbolicName: org.foo\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Export-Package: org.foo";
            String mfBar =
                "OpenIDE-Module: org.bar/1\n" +
                "OpenIDE-Module-Name: Depends on bar test module\n" +
                "OpenIDE-Module-Module-Dependencies: org.foo > 1.0\n" +
                "some";

            File j1 = changeManifest(new File(jars, "simple-module.jar"), mf);
            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            Module m1 = mgr.create(j1, null, false, false, false);
            Module m2 = mgr.create(j2, null, false, false, false);
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(m1, m2));
            mgr.enable(b);
            both = b;

            Class<?> clazz = m2.getClassLoader().loadClass("org.bar.SomethingElse");
            Class<?> sprclass = m2.getClassLoader().loadClass("org.foo.Something");

            assertEquals("Correct parent is used", sprclass, clazz.getSuperclass());
        } finally {
            if (both != null) {
                mgr.disable(both);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }


    public void testLongDepsAreShortened() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m2 = null;
        try {
            String mfBar =
                "OpenIDE-Module: org.bar/1\n" +
                "OpenIDE-Module-Specification-Version: 2.3.0.42.2\n" +
                "OpenIDE-Module-Name: Too many dots in version\n" +
                "OpenIDE-Module-Public-Packages: org.bar.*\n" +
                "some";

            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            m2 = mgr.create(j2, null, false, false, false);
            mgr.enable(m2);
        } finally {
            if (m2 != null) {
                mgr.disable(m2);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testNonNumericVersionNumberIsOK() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m2 = null;
        try {
            String mfBar =
                "Bundle-SymbolicName: org.bar\n" +
                "Bundle-Version: 2.3.0.Prelude-rel24\n" +
                "Export-Packages: org.bar.*\n" +
                "some";

            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            m2 = mgr.create(j2, null, false, false, false);
            mgr.enable(m2);
            assertEquals("2.3.0", m2.getSpecificationVersion().toString());
            assertEquals("2.3.0.Prelude-rel24", m2.getImplementationVersion());
        } finally {
            if (m2 != null) {
                mgr.disable(m2);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
}
