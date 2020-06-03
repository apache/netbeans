/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.modeldiscovery.provider;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.ResolvedPath;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 */
public class PackageConfigTestCase extends CndBaseTestCase {

    private static final boolean TRACE = false;

    public PackageConfigTestCase(String testName) {
        super(testName);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testLibxmlPackage() {
        Logger logger = Logger.getLogger(NbPreferences.class.getName());
        logger.setLevel(Level.SEVERE);
        PkgConfigImpl pc = (PkgConfigImpl) new PkgConfigManagerImpl().getPkgConfig(ExecutionEnvironmentFactory.getLocal(), null);
        basicTest(pc, "libxml-2.0", "libxml/tree.h");
    }

    @Test
    public void testGtkPackage() {
        // Test requires cygwin on Windows platform
        // Test requires package gtk+-2.0
        if (Utilities.isMac()) {
            // It seems problematic to install gtk on mac
            return;
        }
        Logger logger = Logger.getLogger(NbPreferences.class.getName());
        logger.setLevel(Level.SEVERE);
        PkgConfigImpl pc = (PkgConfigImpl) new PkgConfigManagerImpl().getPkgConfig(ExecutionEnvironmentFactory.getLocal(), null);
        basicTest(pc, "gtk+-2.0", "gtk/gtk.h");
    }

    private void basicTest(PkgConfigImpl pc, String packageName, String include) {
        if (TRACE) {
            pc.traceConfig(packageName, true);
            pc.traceRecursiveConfig(packageName);
        }
        //pc.trace();
        assertNotNull(pc.getPkgConfig(packageName));
        Collection<ResolvedPath> listRP = pc.getResolvedPath(include);
        assertNotNull(listRP);
        assertTrue(!listRP.isEmpty());
        boolean find = false;
        for (ResolvedPath rp : listRP) {
            if (TRACE) {
                System.out.println("Resolved include paths");
            }
            String path = rp.getIncludePath();
            if (TRACE) {
                System.out.println("Include: " + include);
            }
            if (TRACE) {
                System.out.println("Path:    " + path);
            }
            StringBuilder packages = new StringBuilder();
            for (PackageConfiguration pkg : rp.getPackages()) {
                if (TRACE) {
                    System.out.print("Package: " + pkg.getName());
                }
                packages.append(pkg.getName()).append(" ");
                StringBuilder buf = new StringBuilder();
                for (String p : pkg.getIncludePaths()) {
                    if (buf.length() > 0) {
                        buf.append(", ");
                    }
                    buf.append(p);
                }
                StringBuilder buf2 = new StringBuilder();
                for (String p : pkg.getMacros()) {
                    if (buf2.length() > 0) {
                        buf2.append(", ");
                    }
                    buf2.append(p);
                }
                if (TRACE) {
                    System.out.println("\t[" + buf.toString() + "] [" + buf2.toString() + "]");
                }
            }
            if (packages.toString().indexOf(packageName + " ") >= 0) {
                find = true;
            }
        }
        assertTrue(find);
    }

// World is not yet ready for this test...

//    @Test
//    public void testRemoteGtkPackage() {
//        if (canTestRemote()) { //TODO: 10k vs 3
//            PlatformInfo pi = PlatformInfo.getDefault(getHKey());
//            PkgConfigImpl pc = new PkgConfigImpl(pi, null);
//            basicTest(pc);
//        }
//    }
}
