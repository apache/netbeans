/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core.netigso;

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
        NetigsoUtil.downgradeJDK();
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
