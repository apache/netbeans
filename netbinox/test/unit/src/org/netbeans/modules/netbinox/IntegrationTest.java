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
