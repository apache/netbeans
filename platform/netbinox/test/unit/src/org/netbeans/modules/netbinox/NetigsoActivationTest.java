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
                
                Enumeration<URL> en = b.findEntries("/", null, true);
                if (en == null) {
                    sb.append("No entries for ").append(b.getSymbolicName()).append("\n");
                    continue;
                }
                while (en.hasMoreElements()) {
                    URL u = en.nextElement();
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
