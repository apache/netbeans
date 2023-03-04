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

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NetigsoOSGiFragmentTest extends NetigsoHid {
    public NetigsoOSGiFragmentTest(String name) {
        super(name);
    }


    public void testOSGiFragmentDependency() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        HashSet<Module> toCleanUp = null;
        try {
            String mfFoo = ""
                    + "Bundle-SymbolicName: org.foo\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Export-Package: org.foo,org.bar\n" +
                "\n\n";
            
            String mfBar = "Fragment-Host: org.foo\n"
                + "Bundle-SymbolicName: org.bar\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "\n\n";

            String mfDependency = "OpenIDE-Module: org.test\n"
                    + "OpenIDE-Module-Module-Dependencies: org.bar, org.foo\n" +
                "\n\n";

            File j1 = changeManifest(new File(jars, "simple-module.jar"), mfFoo);
            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            File j3 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfDependency);
            Module m1 = mgr.create(j1, null, false, true, false);
            Module m2 = mgr.create(j2, null, false, true, false);
            Module m3 = mgr.create(j3, null, false, false, false);
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(m3));
            mgr.enable(m3);
            toCleanUp = b;

            Class<?> sprclass = m3.getClassLoader().loadClass("org.foo.Something");
            Class<?> clazz = m3.getClassLoader().loadClass("org.bar.SomethingElse");

            assertEquals("Correct parent is used", sprclass, clazz.getSuperclass());
        } finally {
            if (toCleanUp != null) {
                mgr.disable(toCleanUp);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
