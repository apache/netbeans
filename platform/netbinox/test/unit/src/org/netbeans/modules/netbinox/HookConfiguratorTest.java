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

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class HookConfiguratorTest extends NbTestCase {
    static {
        System.setProperty("java.awt.headless", "true");
    }

    public HookConfiguratorTest(String name) {
        super(name);
    }

    public static Test suite() {
        System.setProperty("java.awt.headless", "true");
        assertTrue("In headless mode", GraphicsEnvironment.isHeadless());
        NbTestSuite s = new NbTestSuite();
        s.addTest(new Compile("testCompileJAR"));
        s.addTest(NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().addTest(
                HookConfiguratorTest.class
            ).honorAutoloadEager(true).clusters(
                ".*"
            ).failOnException(Level.WARNING)/*.failOnMessage(Level.WARNING)*/
            .gui(false)
        ));
        return s;
    }

    public void testCheckWhichContainerIsRunning() throws Exception {
        File j1 = new File(System.getProperty("activate.jar"));
        assertTrue("File " + j1 + " exists", j1.exists());
        File j2 = new File(System.getProperty("hook.jar"));
        assertTrue("File " + j2 + " exists", j2.exists());


        ModuleManager mgr = Main.getModuleSystem().getManager();
        try {
            mgr.mutexPrivileged().enterWriteAccess();
            Module m1, m2;

            m1 = mgr.create(j1, null, false, false, false);
            m2 = mgr.create(j2, null, false, false, false);
            mgr.enable(m2);
            mgr.enable(m1);

            assertTrue("OSGi module is now enabled", m1.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }

        assertEquals("true", System.getProperty("main.hook"));

    }

    public static class Compile extends SetupHid {

        public Compile(String name) {
            super(name);
        }
        @Override
        protected void setUp() throws Exception {
            Locale.setDefault(Locale.US);
            clearWorkDir();
        }

        public void testCompileJAR() throws Exception {
            data = new File(getDataDir(), "jars");
            jars = new File(getWorkDir(), "jars");
            jars.mkdirs();

            jars.mkdirs();
            File j1 = createTestJAR("activate", null);
            assertNotNull("file found: " + j1);
            File j2 = createTestJAR("hook", null);
            assertNotNull("file found: " + j2);
            System.setProperty("activate.jar", j1.getPath());
            System.setProperty("hook.jar", j2.getPath());
        }
        
        private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
            return createTestJAR(data, jars, name, srcdir, classpath);
        }
    }
}
