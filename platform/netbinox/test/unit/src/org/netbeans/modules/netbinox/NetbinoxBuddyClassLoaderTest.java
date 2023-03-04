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

import org.netbeans.core.startup.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Does context classloader in NetBeans honour the Equinox buddy policy?
 *
 * @author Jaroslav Tulach
 */
public class NetbinoxBuddyClassLoaderTest extends SetupHid {
    private static Module m1;
    private Module m2;
    private static ModuleManager mgr;
    private int cnt;
    private File simpleBundle;
    private File activate;

    public NetbinoxBuddyClassLoaderTest(String name) {
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

        File act = createTestJAR("activate", null); 
        String amf = "Manifest-Version: 1.0\n" +
            "Bundle-SymbolicName: org.activate\n" +
            "Bundle-Version: 1.2\n" +
            "Eclipse-BuddyPolicy: registered\n" + 
            "Import-Package: org.osgi.framework\n" +
            "Bundle-Activator: org.activate.Main\n" +
            "\n";
        activate = NetigsoHid.changeManifest(getWorkDir(), act, amf);
        
        File simpleModule = createTestJAR("simple-module", null);
        String mf = "Bundle-SymbolicName: org.snd.module\n" +
            "Export-Package: org.foo\n" + 
            "Require-Bundle: org.activate\n" +
            "Eclipse-RegisterBuddy: org.activate\n" +
            "Bundle-Version: 33.0.3\n" +
            "Bundle-ManifestVersion: 2\n";
        simpleBundle = NetigsoHid.changeManifest(getWorkDir(), simpleModule, mf);
    }

    public void testActivation() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        BundleContext bc;
        Method loadClass = null;
        try {
            m1 = mgr.create(activate, null, false, false, false);
            m2 = mgr.create(simpleBundle, null, false, false, false);
            
            mgr.enable(m1);

            Class<?> main = m1.getClassLoader().loadClass("org.activate.Main");
            Object s = main.getField("start").get(null);
            assertNotNull("Bundle started, its context provided", s);
            bc = (BundleContext) s;
            loadClass = main.getMethod("loadClass", String.class, ClassLoader.class);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        Bundle toEnable = null;
        for (Bundle b : bc.getBundles()) {
            if (b.getSymbolicName().equals("org.snd.module")) {
                toEnable = b;
                break;
            }
        }
        
        assertNotNull("org.snd.module bundle found", toEnable);
        assertEquals("Just resolved", Bundle.RESOLVED, toEnable.getState());
        assertFalse("not started", m2.isEnabled());

        Class<?> directly = (Class<?>) loadClass.invoke(null, "org.foo.Something", null);
        assertNotNull("Bundle knows how to load the class from its buddy", directly);
        
        Class<?> someFromBundle = (Class<?>) loadClass.invoke(null, "org.foo.Something", m1.getClassLoader());
        assertNotNull("Bundle knows how to load the class from its buddy", someFromBundle);

        Class<?> some = (Class<?>) loadClass.invoke(null, "org.foo.Something", Thread.currentThread().getContextClassLoader());
        assertNotNull("Context class loader deals with buddies too", some);
    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
}
