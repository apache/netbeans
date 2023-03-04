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
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.modules.netbinox.ContextClassLoaderTest.Compile;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.netigso.NetigsoUtil;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.MemoryFilter;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.osgi.framework.Bundle;
import org.osgi.framework.launch.Framework;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class BundleURLConnectionTest extends NbTestCase {
    static {
        System.setProperty("java.awt.headless", "true");
    }

    public BundleURLConnectionTest(String name) {
        super(name);
    }

    public static Test suite() {
        System.setProperty("java.awt.headless", "true");
        assertTrue("In headless mode", GraphicsEnvironment.isHeadless());
        NbTestSuite s = new NbTestSuite();
        s.addTest(new Compile("testCompileJAR"));
        s.addTest(NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().addTest(
                BundleURLConnectionTest.class
            ).honorAutoloadEager(true).failOnException(Level.WARNING)/*.failOnMessage(Level.WARNING)*/
            .gui(false)
        ));
        return s;
    }

    public void testVariousURLs() throws Exception {
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
        assertNotNull("Framework found", w);
        assertEquals("Felix is not in its name", -1, w.getClass().getName().indexOf("felix"));
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (Bundle b : w.getBundleContext().getBundles()) {
            sb.append("\n").append(b.getSymbolicName());
            if (b.getSymbolicName().equals("org.eclipse.osgi")) {
                found = true;
                break;
            }
        }
        if (!found) {
            fail("Expecting equinox among list of enabled bundles:" + sb);
        }
        String text = System.getProperty("activated.entry");
        assertEquals("Ahoj", text);
        
        
        String localURL = System.getProperty("activated.entry.local");
        assertNotNull("bundleentry read OK", localURL);
        // <[jar:file:/home/jarda/src/netbeans/netbinox/netbinox/build/test/unit/work/o.a.n.C/testCompileJAR/jars/activate.jar!/org/activate/entry.txt]>
        assertEquals("Ahoj", readLine(localURL));
        
        String fileURL = System.getProperty("activated.entry.file");
        assertNotNull("fileURL found", fileURL);
        // file:/home/jarda/src/netbeans/netbinox/netbinox/build/test/unit/work/userdir0/var/cache/netigso/org.eclipse.osgi/bundles/46/1/.cp/org/activate/entry.txt
        assertEquals("Ahoj", readLine(fileURL));
        
        
        URL u = (URL) System.getProperties().get("activated.entry.url");
        assertNotNull("URL found", u);
        assertNoByteArray(u, "Ahoj");
    }
    
    static String readLine(String url) throws Exception {
        assertNotNull("url cannot be null", url);
        URL u = new URL(url);
        BufferedReader r = new BufferedReader(new InputStreamReader(u.openStream()));
        return r.readLine();
    }
    
    private static void assertNoByteArray(URL u, final String text) {
        final Object[] found = { null };
        class MF implements MemoryFilter {
            @Override
            public boolean reject(Object obj) {
                if (obj instanceof byte[]) {
                    String s = new String((byte[])obj, StandardCharsets.UTF_8);
                    if (s.startsWith(text)) {
                        found[0] = s;
                    }
                }
                return false;
            }
        }
        
        assertSize("Find the array", Collections.singleton(u), 32000000, new MF());
        assertEquals("The array should not be referenced by the URL", null, found[0]);
    }
    
    
}
