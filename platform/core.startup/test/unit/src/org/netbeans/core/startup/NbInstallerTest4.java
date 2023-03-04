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

package org.netbeans.core.startup;

import org.netbeans.MockEvents;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.Stamps;
import org.openide.filesystems.FileUtil;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest4 extends NbInstallerTestBase {

    public NbInstallerTest4(String name) {
        super(name);
    }

    /** Test #21173/#23609: overriding layers by module dependencies.
     * Version 1: all modules loaded together.
     */
    public void testDependencyLayerOverrides1() throws Exception {
        Main.getModuleSystem (); // init module system
        final MockEvents ev = new MockEvents();
        NbInstaller installer = new NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(installer, ev);
        installer.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "base-layer-mod.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "override-layer-mod.jar"), null, false, false, false);
            assertEquals(Collections.EMPTY_SET, m2.getProblems());
            assertEquals(null, slurp("foo/file1.txt"));
            assertEquals(null, slurp("foo/file3.txt"));
            assertEquals(null, slurp("foo/file4.txt"));
            Set<Module> m1m2 = new HashSet<Module>(Arrays.asList(m1, m2));
            mgr.enable(m1m2);
            
            Stamps.getModulesJARs().flush(0);
            Stamps.getModulesJARs().shutdown();
            
            assertEquals("base contents", slurp("foo/file1.txt"));
            assertEquals("customized contents", slurp("foo/file3.txt"));
            assertEquals(null, slurp("foo/file4.txt"));
            assertEquals("someotherval", FileUtil.getConfigFile("foo/file5.txt").getAttribute("myattr"));
            mgr.disable(m1m2);
            assertEquals(null, slurp("foo/file1.txt"));
            mgr.delete(m2);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
