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
import java.net.URL;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.TestFileUtils;

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
        return
            NbModuleSuite.emptyConfiguration().addTest(
                EnabledAutoloadTest.class
            ).honorAutoloadEager(true).clusters("platform.*").failOnException(Level.WARNING)/*.failOnMessage(Level.WARNING)*/
            .gui(false)
        .suite();
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        File jars = new File(getWorkDir(), "jars");
        jars.mkdirs();

        j1 = SetupHid.createTestJAR(getDataDir(), jars, "simple-module.jar", null);
        System.setProperty("netbeans.user", getWorkDirPath());
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
            
            // OSGi installs URLStreamHandlers, check http(s) protocol parsing
            new URL("http://localhost:10000");
            new URL("https://localhost:10000");
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testAutoloadBundles() throws Exception {
        CharSequence log;
        
        ModuleManager mgr = Main.getModuleSystem().getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            File jar1 = new File(getWorkDir(), "j1.jar");
            TestFileUtils.writeZipFile(jar1,
                    "META-INF/MANIFEST.MF:Bundle-SymbolicName: m1\nExport-Package: m1\nBundle-Version: 1.0\n",
                    "m1/res:ource");
            Module m1 = mgr.create(jar1, null, false, true, false);
            File jar2 = new File(getWorkDir(), "j2.jar");
            TestFileUtils.writeZipFile(jar2,
                    "META-INF/MANIFEST.MF:Bundle-SymbolicName: m2\nExport-Package: m2\nBundle-Version: 1.0\nRequire-Bundle: m1\n",
                    "m2/res:ource");
            Module m2 = mgr.create(jar2, null, false, true, false);
            File jar3 = new File(getWorkDir(), "j3.jar");
            TestFileUtils.writeZipFile(jar3,
                    "META-INF/MANIFEST.MF:OpenIDE-Module: m3\nOpenIDE-Module-Module-Dependencies: m2\nOpenIDE-Module-Public-Packages: -\n");
            Module m3 = mgr.create(jar3, null, false, false, false);
            
            log = Log.enable("org.netbeans", Level.INFO);
            mgr.enable(m3);
            assertTrue(m3.isEnabled());
            assertTrue(m2.isEnabled());
            assertTrue("After fix to bug #201695 module M1 is now enabled", m1.isEnabled());
            assertNotNull(m3.getClassLoader().getResource("m2/res"));
            assertNull("Can't load from not enabled bundle", m2.getClassLoader().getResource("m1/res"));
            assertNotNull("But can load directly from bundle", NetigsoUtil.bundle(m1).getResource("m1/res"));

        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        assertAsynchronousMessage(log, "m1 [1.0 1.0]");
        if (log.toString().contains("bundle m2")) {
            fail("m2 is turned on as module and listed on its own");
        }
    }
    
    private void assertAsynchronousMessage(CharSequence log, String text) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            if (log.toString().contains(text)) {
                return;
            }
            Thread.sleep(100);
        }
        fail("There should be a message about enabling m1:\n" + log);
    }

}
