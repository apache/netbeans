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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.NetigsoFramework;
import org.netbeans.SetupHid;
import org.netbeans.core.netigso.NetigsoUtil;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.launch.Framework;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class IntegrationTest extends NbTestCase {
    private File j1;

    public IntegrationTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().addTest(
                IntegrationTest.class
            ).honorAutoloadEager(true).clusters(
                ".*"
            ).failOnException(Level.WARNING)/*.failOnMessage(Level.WARNING)*/
            .gui(false)
        );
    }

    @Override
    protected void setUp() throws Exception {
        File jars = new File(getWorkDir(), "jars");
        jars.mkdirs();

        j1 = SetupHid.createTestJAR(getDataDir(), jars, "simple-module.jar", null);
    }


    public void testCheckWhichContainerIsRunning() throws Exception {
        ModuleManager mgr = Main.getModuleSystem().getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        Module m1;
        String mf = "Bundle-SymbolicName: org.foo\n" +
            "Bundle-Version: 1.1.0\n" +
            "Bundle-ManifestVersion: 2\n" +
            "Export-Package: org.foo";

        File jj1 = NetigsoHid.changeManifest(getWorkDir(), j1, mf);
        m1 = mgr.create(jj1, null, false, false, false);
        mgr.enable(m1);

        assertTrue("OSGi module is now enabled", m1.isEnabled());
        mgr.mutexPrivileged().exitWriteAccess();
        Framework w = findFramework();
        assertNotNull("Framework found", w);
        assertEquals("Felix is not in its name", -1, w.getClass().getName().indexOf("felix"));
        StringBuilder sb = new StringBuilder();
        for (Bundle b : w.getBundleContext().getBundles()) {
            sb.append("\n").append(b.getSymbolicName());
            if (b.getSymbolicName().equals("org.eclipse.osgi")) {
                return;
            }
        }
        fail("Expecting equinox among list of enabled bundles:" + sb);
    }

    static Framework findFramework() throws Exception {
        return NetigsoUtil.framework(Main.getModuleSystem().getManager());
    }
}
