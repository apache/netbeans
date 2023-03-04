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
import java.util.logging.Level;
import junit.framework.Test;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.netigso.NetigsoUtil;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.netbinox.ContextClassLoaderTest.Compile;
import org.osgi.framework.Bundle;
import org.osgi.framework.launch.Framework;

public class SQLTimeTest extends NbTestCase {
    static {
        System.setProperty("java.awt.headless", "true");
    }

    public SQLTimeTest(String name) {
        super(name);
    }

    public static Test suite() {
        System.setProperty("java.awt.headless", "true");
        assertTrue("In headless mode", GraphicsEnvironment.isHeadless());
        NbTestSuite s = new NbTestSuite();
        s.addTest(new Compile("testCompileJAR"));
        s.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(SQLTimeTest.class)
                                                    .failOnException(Level.WARNING)
                                                    .gui(false)));
        return s;
    }

    public void testSQLTime() throws Exception {
        File j1 = new File(System.getProperty("activate.jar"));
        assertTrue("File " + j1 + " exists", j1.exists());
        
        ModuleManager mgr = Main.getModuleSystem().getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        Module m1;
        m1 = mgr.create(j1, null, false, false, false);
        System.setProperty("activated.checkentries", "/org/activate/entry.txt");
        mgr.enable(m1);

        assertTrue("OSGi module is now enabled", m1.isEnabled());
        mgr.mutexPrivileged().exitWriteAccess();
        Framework w = NetigsoUtil.framework(mgr);
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (Bundle b : w.getBundleContext().getBundles()) {
            sb.append("\n").append(b.getSymbolicName());
            if ("org.activate".equals(b.getSymbolicName())) {
                b.loadClass("java.sql.Time");
                found = true;
                break;
            }
        }
        if (!found) {
            fail("Expecting equinox among list of enabled bundles:" + sb);
        }
    }
    
}
