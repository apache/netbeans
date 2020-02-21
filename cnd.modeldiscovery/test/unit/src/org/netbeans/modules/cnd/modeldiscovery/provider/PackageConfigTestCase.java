/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
