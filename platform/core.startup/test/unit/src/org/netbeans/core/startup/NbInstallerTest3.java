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
import org.openide.util.NbBundle;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest3 extends NbInstallerTestBase {

    public NbInstallerTest3(String name) {
        super(name);
    }

    /** Test #21173/#23595: overriding layers by branding. */
    public void testBrandingLayerOverrides() throws Exception {
        Main.getModuleSystem ();
        final MockEvents ev = new MockEvents();
        NbInstaller installer = new NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(installer, ev);
        installer.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            String orig = NbBundle.getBranding();
            NbBundle.setBranding("foo");
            try {
                Module m1 = mgr.create(new File(jars, "base-layer-mod.jar"), null, false, false, false);
                assertEquals(Collections.EMPTY_SET, m1.getProblems());
                mgr.enable(m1);
                assertEquals("special contents", slurp("foo/file1.txt"));
                assertEquals(null, slurp("foo/file2.txt"));
                mgr.disable(m1);
                mgr.delete(m1);
            } finally {
                NbBundle.setBranding(orig);
            }
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
}
