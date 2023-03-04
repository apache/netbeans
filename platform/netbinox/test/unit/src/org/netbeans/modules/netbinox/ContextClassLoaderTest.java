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
public class ContextClassLoaderTest extends NbTestCase {
    static {
        System.setProperty("java.awt.headless", "true");
    }

    public ContextClassLoaderTest(String name) {
        super(name);
    }

    public static Test suite() {
        System.setProperty("java.awt.headless", "true");
        assertTrue("In headless mode", GraphicsEnvironment.isHeadless());
        NbTestSuite s = new NbTestSuite();
        s.addTest(new Compile("testCompileJAR"));
        s.addTest(NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().addTest(
                ContextClassLoaderTest.class
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


        ModuleManager mgr = Main.getModuleSystem().getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        Module m1;

        m1 = mgr.create(j1, null, false, false, false);
        mgr.enable(m1);

        assertTrue("OSGi module is now enabled", m1.isEnabled());
        mgr.mutexPrivileged().exitWriteAccess();


        Class<?> main = m1.getClassLoader().loadClass("org.activate.Main");
        Object s = main.getField("start").get(null);
        assertNotNull("Bundle started, its context provided", s);

        ClassLoader l = (ClassLoader) main.getField("loader").get(null);
        assertNotNull("Classloader is provided", l);
        Class<?> c = l.loadClass("org.netbeans.modules.favorites.Tab");
        assertNotNull("Class from favorites module found in " + l, c);

        Class<?> main2 = Thread.currentThread().getContextClassLoader().loadClass(main.getName());
        assertSame("Context classloader loads the same class", main, main2);

        Class<?> main3 = l.loadClass(main.getName());
        assertSame("Bundle's context classloader loads the same class", main, main3);
    }

    public void testContextClassLoaderIsOK() throws ClassNotFoundException {
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        assertNotNull("Context class loader is found", l);

        Class<?> c = l.loadClass("org.netbeans.modules.favorites.Tab");
        assertNotNull("Class from favorites module found in " + l, c);
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
            System.setProperty("activate.jar", j1.getPath());
        }
        
        private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
            return createTestJAR(data, jars, name, srcdir, classpath);
        }
    }
}
