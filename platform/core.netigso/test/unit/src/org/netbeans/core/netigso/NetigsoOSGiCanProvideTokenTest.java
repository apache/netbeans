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

import org.netbeans.core.startup.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;

/**
 * Autoload with needs should be properly notified.
 *
 * @author Jaroslav Tulach
 */
public class NetigsoOSGiCanProvideTokenTest extends SetupHid {
    private static Module m1;
    private static Module m2;
    private static ModuleManager mgr;
    private static File needsButDoesNotHave;
    private static File bundleRequires;

    public NetigsoOSGiCanProvideTokenTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() {
    }
    
    public void testBundleRequiresAutoloadWithoutSatisfiedNeeds()
    throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        File simpleModule = createTestJAR("simple-module", null);
        assertNotNull("simpleModule created", simpleModule);
        
        String bundle = "Bundle-SymbolicName: org.my.bundle\n"
                + "Bundle-ManifestVersion: 2\n"
                + "Require-Bundle: org.snd.module\n"
                + "OpenIDE-Module-Provides: token.from.bundle\n"
                + "\n"
                + "\n"
                + "";
        bundleRequires = NetigsoHid.changeManifest(
            getWorkDir(), simpleModule, bundle
        );
        
        String mf = "OpenIDE-Module: org.snd.module\n" +
            "OpenIDE-Module-Specification-Version: 33.0.3\n" +
            "OpenIDE-Module-Needs: token.from.bundle\n"
                + "\n\n";
        needsButDoesNotHave = NetigsoHid.changeManifest(getWorkDir(), simpleModule, mf);
        
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            m1 = mgr.create(bundleRequires, null, false, false, false);
            m2 = mgr.create(needsButDoesNotHave, null, false, true, false);
            mgr.enable(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        assertTrue("module m2 is enabled", m2.isEnabled());
        assertTrue("module m1 is enabled", m1.isEnabled());
    }
    
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
}
