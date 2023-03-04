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
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.osgi.framework.launch.Framework;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class IntegrationTest extends NbTestCase {
    private File j1;
    private Logger LOG;

    public IntegrationTest(String name) {
        super(name);
    }

    public static Test suite() {
        return
            NbModuleSuite.emptyConfiguration().addTest(
                IntegrationTest.class
            ).honorAutoloadEager(true).clusters(
                "platform.*"
            ).failOnException(Level.WARNING)/*.failOnMessage(Level.WARNING)*/
            .gui(false)
        .suite();
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
        clearWorkDir();
        File jars = new File(getWorkDir(), "jars");
        jars.mkdirs();

        j1 = SetupHid.createTestJAR(getDataDir(), jars, "simple-module.jar", null);
    }

    @RandomlyFails // NB-Core-Build #8007: Framework found
    public void testCheckWhichContainerIsRunning() throws Exception {
        ModuleManager mgr = Main.getModuleSystem().getManager();
        Module m1;
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            String mf = "Bundle-SymbolicName: org.foo\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Export-Package: org.foo";

            LOG.info("about to enable module org.foo");
            File jj1 = NetigsoHid.changeManifest(getWorkDir(), j1, mf);
            m1 = mgr.create(jj1, null, false, false, false);
            mgr.enable(m1);
            LOG.info("Enabling is over");

            assertTrue("OSGi module is now enabled", m1.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }

        Framework w = NetigsoUtil.framework(mgr);
        assertNotNull("Framework found", w);
        if (!w.getClass().getName().contains("felix")) {
            fail("By default the OSGi framework is felix: " + w.getClass());
        }


        ClassLoader fwloader = w.getClass().getClassLoader();
        Method addURLMethod = howEclipseFindsMethodToSupportFrameworks(fwloader.getClass());

        assertNotNull("addURL method found", addURLMethod);
    }

    private static Method howEclipseFindsMethodToSupportFrameworks(Class<?> clazz) {

        if (clazz == null) {
            return null;
        }
        try {
            Method result = clazz.getDeclaredMethod("addURL", URL.class);
            result.setAccessible(true);
            return result;
        } catch (NoSuchMethodException ex) {
        } catch (SecurityException ex) {
        }
        return howEclipseFindsMethodToSupportFrameworks(clazz.getSuperclass());
    }
}
