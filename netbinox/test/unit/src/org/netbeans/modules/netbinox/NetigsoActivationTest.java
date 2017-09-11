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

import java.util.Enumeration;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.netbeans.core.startup.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.internal.core.AbstractBundle;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Do we correctly call the BundleActivators?
 *
 * @author Jaroslav Tulach
 */
public class NetigsoActivationTest extends SetupHid {
    private static Module m1;
    private static ModuleManager mgr;

    public NetigsoActivationTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        File simpleModule = createTestJAR("activate", null);
    }

    public void testActivation() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        Enumeration en;
        int checks = 0;
        try {
            File simpleModule = new File(jars, "activate.jar");
            m1 = mgr.create(simpleModule, null, false, false, false);
            mgr.enable(m1);

            Class<?> main = m1.getClassLoader().loadClass("org.activate.Main");
            Object s = main.getField("start").get(null);
            assertNotNull("Bundle started, its context provided", s);

            BundleContext bc = (BundleContext)s;
            StringBuilder sb = new StringBuilder();
            for (Bundle b : bc.getBundles()) {
                URL root = b.getEntry("/");
                if (root == null) {
                    sb.append("No root URL for ").append(b.getSymbolicName()).append("\n");
                }
                BundleFile bFile = null;
                if (b instanceof AbstractBundle) {
                    BundleData bd = ((AbstractBundle) b).getBundleData();
                    if (bd instanceof BaseData) {
                        bFile = ((BaseData) bd).getBundleFile();
                    }
                }
                assertNotNull("All our bundles have BundleFile", bFile);
                
                en = b.findEntries("/", null, true);
                if (en == null) {
                    sb.append("No entries for ").append(b.getSymbolicName()).append("\n");
                    continue;
                }
                while (en.hasMoreElements()) {
                    URL u = (URL) en.nextElement();
                    final String ef = u.toExternalForm();
                    int pref = ef.indexOf("/org/");
                    int last = ef.lastIndexOf("/");
                    if (pref != -1 && last != -1) {
                        String entry = ef.substring(pref + 1, last + 1);
                        assertTrue("/ is at the end", entry.endsWith("/"));
                        checks++;
                        final URL found = b.getEntry(entry);
                        assertNotNull("Entry found " + entry + " in " + b.getSymbolicName(), found);
                        assertTrue("Directory found", bFile.containsDir(entry));
                        BundleEntry ee = bFile.getEntry(entry);
                        URL local = ee.getLocalURL();
                        assertEquals("JAR protocol used", "jar", local.getProtocol());
                        if (!local.toExternalForm().contains("!/")) {
                            fail("!/ shall be in the URL: " + local);
                        }
                        
                        final String nonExist = "non/existent/entry/";
                        URL notFound = b.getEntry(nonExist);
                        assertNull("Entries for non-existing entries are not found", notFound);
                        assertFalse("Non-existent not found", bFile.containsDir(nonExist));
                    }
                }
            }
            if (sb.length() > 0) {
                fail(sb.toString());
            }
            if (checks == 0) {
                fail("There shall be some checks for entries");
            }

            assertNotNull("OSGi version available to contexts", bc.getProperty("osgi.framework.version"));
            
            String version = System.getProperty("osgi.framework.version");
            assertNull("Version is not available in system properties by default", version);
            
            mgr.disable(m1);

            Object e = main.getField("stop").get(null);
            assertNotNull("Bundle stopped, its context provided", e);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
}
