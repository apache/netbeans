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
import java.util.List;
import org.netbeans.MockEvents;
import org.netbeans.MockModuleInstaller;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.openide.modules.Dependency;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NetigsoOSGiCanRequestTest extends NetigsoHid {

    public NetigsoOSGiCanRequestTest(String name) {
        super(name);
    }

    public void testOSGiCanRequireBundleOnNetBeans() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        HashSet<Module> both = null;
        try {
            String mfBar = "Bundle-SymbolicName: org.bar\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Export-Package: org.bar\n" +
                "Require-Bundle: org.foo;bundle-version=\"[100.0,102.0)\"\n" +
                "\n\n";

            File j1 = new File(jars, "simple-module.jar");
            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            Module m1 = mgr.create(j1, null, false, false, false);
            Module m2 = mgr.create(j2, null, false, false, false);
            
            assertProvidesRequires(m2, "org.bar", "org.foo");
            
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(m1, m2));
            mgr.enable(b);
            both = b;

            Class<?> sprclass = m1.getClassLoader().loadClass("org.foo.Something");
            Class<?> clazz = m2.getClassLoader().loadClass("org.bar.SomethingElse");

            assertEquals("Correct parent is used", sprclass, clazz.getSuperclass());
        } finally {
            if (both != null) {
                mgr.disable(both);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    private static void assertProvidesRequires(Module m, String provides, String requires) {
        List<String> p = Arrays.asList(m.getProvides());
        assertTrue("Bundles provide their packages: " + p, p.contains(provides));

        for (Dependency d : m.getDependencies()) {
            if (d.getType() == Dependency.TYPE_RECOMMENDS) {
                if (!d.getName().startsWith("cnb.")) {
                    continue;
                }
                if (requires.equals(d.getName().substring(4))) {
                    return;
                }
            }
        }
        fail("Module " + m + " does not require " + requires);
    }

}
