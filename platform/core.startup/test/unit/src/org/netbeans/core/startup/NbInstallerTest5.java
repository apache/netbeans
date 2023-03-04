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
import java.util.Collections;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.openide.filesystems.FileUtil;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest5 extends NbInstallerTestBase {

    public NbInstallerTest5(String name) {
        super(name);
    }

    /** Test #21173/#23609: overriding layers by module dependencies.
     * Version 2: modules loaded piece by piece.
     * Exercises different logic in XMLFileSystem as well as ModuleLayeredFileSystem.
     */
    public void testDependencyLayerOverrides2() throws Exception {
        Main.getModuleSystem (); // init module system
        System.err.println("Module Info->"+org.openide.util.Lookup.getDefault()
                .lookup(org.openide.modules.ModuleInfo.class)); // TEMP
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
            mgr.enable(m1);
            assertEquals("base contents", slurp("foo/file1.txt"));
            assertEquals("base contents", slurp("foo/file3.txt"));
            assertEquals("base contents", slurp("foo/file4.txt"));
            assertEquals("someval", FileUtil.getConfigFile("foo/file5.txt").getAttribute("myattr"));
            mgr.enable(m2);
            assertEquals("base contents", slurp("foo/file1.txt"));
            assertEquals(null, slurp("foo/file4.txt"));
            assertEquals("customized contents", slurp("foo/file3.txt"));
            assertEquals("someotherval", FileUtil.getConfigFile("foo/file5.txt").getAttribute("myattr"));
            mgr.disable(m2);
            assertEquals("base contents", slurp("foo/file3.txt"));
            mgr.disable(m1);
            assertEquals(null, slurp("foo/file3.txt"));
            mgr.delete(m2);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
