/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Oracle, Inc.
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
