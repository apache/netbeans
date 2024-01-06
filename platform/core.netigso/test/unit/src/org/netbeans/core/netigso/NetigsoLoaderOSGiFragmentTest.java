/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.MockEvents;
import org.netbeans.MockModuleInstaller;
import org.netbeans.Module;
import org.netbeans.ModuleManager;

public class NetigsoLoaderOSGiFragmentTest extends NetigsoHid {

    private static final String HOST_MANIFEST = ""
            + "Bundle-SymbolicName: org.foo\n"
            + "Bundle-Version: 1.1.0\n"
            + "Bundle-ManifestVersion: 2\n"
            + "\n\n";

    private static final String FRAGMENT_MANIFEST = ""
            + "Fragment-Host: org.foo\n"
            + "Bundle-SymbolicName: org.bar\n"
            + "Bundle-Version: 1.1.0\n"
            + "Bundle-ManifestVersion: 2\n"
            + "Export-Package: org.bar\n"
            + "\n\n";

    private static final String FRAGMENT_MANIFEST_VERSIONED = ""
            + "Fragment-Host: org.foo;bundle-version=\"[1.1.0,1.2.0)\"\n"
            + "Bundle-SymbolicName: org.bar\n"
            + "Bundle-Version: 1.1.0\n"
            + "Bundle-ManifestVersion: 2\n"
            + "Export-Package: org.bar\n"
            + "\n\n";

    public NetigsoLoaderOSGiFragmentTest(String name) {
        super(name);
    }

    public void testNetigsoLoaderOSGiFragmentBundleSubstitution() throws Exception {

        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Set<Module> modules = new HashSet<Module>();

        try {
            File hostFile = changeManifest(new File(jars, "simple-module.jar"), HOST_MANIFEST);
            File fragmentFile = changeManifest(new File(jars, "depends-on-simple-module.jar"), FRAGMENT_MANIFEST);
            Module hostModule = mgr.create(hostFile, null, false, false, false);
            Module fragmentModule = mgr.create(fragmentFile, null, false, false, false);
            modules.addAll(Arrays.asList(hostModule, fragmentModule));
            mgr.enable(modules);

            NetigsoLoader hostLoader = getNetigsoLoaderForModule(hostModule);
            NetigsoLoader fragmentLoader = getNetigsoLoaderForModule(fragmentModule);

            assertEquals("NetigsoLoader in fragment module should use the host bundle",
                    hostLoader.getBundle(), fragmentLoader.getBundle());

        } finally {
            mgr.disable(modules);
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testNetigsoLoaderOSGiFragmentBundleSubstitutionVersioned() throws Exception {

        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Set<Module> modules = new HashSet<Module>();

        try {
            File hostFile = changeManifest(new File(jars, "simple-module.jar"), HOST_MANIFEST);
            File fragmentFile = changeManifest(new File(jars, "depends-on-simple-module.jar"), FRAGMENT_MANIFEST_VERSIONED);
            Module hostModule = mgr.create(hostFile, null, false, false, false);
            Module fragmentModule = mgr.create(fragmentFile, null, false, false, false);
            modules.addAll(Arrays.asList(hostModule, fragmentModule));
            mgr.enable(modules);

            NetigsoLoader hostLoader = getNetigsoLoaderForModule(hostModule);
            NetigsoLoader fragmentLoader = getNetigsoLoaderForModule(fragmentModule);

            assertEquals("NetigsoLoader in fragment module should use the host bundle",
                    hostLoader.getBundle(), fragmentLoader.getBundle());

        } finally {
            mgr.disable(modules);
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    private NetigsoLoader getNetigsoLoaderForModule(Module module) throws Exception {
        ClassLoader moduleClassLoader = module.getClassLoader();

        // NetigsoModule wraps the class loader inside a private DelegateCL class,
        // which delegates to the NetigsoLoader via delegate() method
        Method delegate = moduleClassLoader.getClass().getDeclaredMethod("delegate");
        delegate.setAccessible(true);
        NetigsoLoader netigsoLoader = (NetigsoLoader) delegate.invoke(moduleClassLoader);
        return netigsoLoader;
    }

}
