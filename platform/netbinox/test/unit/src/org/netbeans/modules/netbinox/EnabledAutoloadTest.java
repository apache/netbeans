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

import java.io.File;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class EnabledAutoloadTest extends NbTestCase {
    private File j1;

    public EnabledAutoloadTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().addTest(
                EnabledAutoloadTest.class
            ).honorAutoloadEager(true).clusters("platform.*").failOnException(Level.WARNING)/*.failOnMessage(Level.WARNING)*/
            .gui(false)
        );
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        File jars = new File(getWorkDir(), "jars");
        jars.mkdirs();

        j1 = SetupHid.createTestJAR(getDataDir(), jars, "simple-module.jar", null);
    }

    public void testDependOnAutoload() throws Exception {
        ModuleManager mgr = Main.getModuleSystem().getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1;
            String mf = "Bundle-SymbolicName: org.require.autoload\n" +
                "Bundle-Version: 33.0.3\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Require-Bundle: org.openide.compat\n\n";
            /*
            mf = "OpenIDE-Module: org.require.autoload\n" +
                "OpenIDE-Module-Module-Dependencies: org.openide.compat\n\n";
            /**/
            File jj1 = NetigsoHid.changeManifest(getWorkDir(), j1, mf);
            m1 = mgr.create(jj1, null, false, false, false);

            CharSequence log = Log.enable("org.netbeans.core.modules", Level.WARNING);
            mgr.enable(m1);

            assertTrue("OSGi module is now enabled", m1.isEnabled());

            Class<?> wl = m1.getClassLoader().loadClass("org.openide.util.WeakListener");
            assertNotNull("Weak listener found", wl);

            Module compat = mgr.get("org.openide.compat");
            assertTrue("Compat module is turned on too", compat.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
}
