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

package org.netbeans.core.netigso;

import java.lang.reflect.Method;
import org.netbeans.core.startup.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.netbeans.Util;
import org.netbeans.junit.RandomlyFails;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Are modules turned on by OSGi framework directly marked as enabled?
 * Can we load classes from them?
 *
 * @author Jaroslav Tulach
 */
@RandomlyFails // http://netbeans.org/bugzilla/show_bug.cgi?id=227778
public class NetigsoOSGiActivationVisibleTest extends SetupHid {
    private static Module m1;
    private static Module m2;
    private static ModuleManager mgr;
    private static File simpleBundle;
    private static File activate;
    private static Bundle toEnable;
    private static Class directBundle;
    private static Class someModule;
    private static Method loadClass;

    public NetigsoOSGiActivationVisibleTest(String name) {
        super(name);
    }
    
    protected boolean autoload() {
        return false;
    }

    protected @Override void setUp() throws Exception {
        if (System.getProperty("netbeans.user") != null) {
            return;
        }
        Locale.setDefault(Locale.US);
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        activate = createTestJAR("activate", null);
        File simpleModule = createTestJAR("simple-module", null);
        
        String mf = "Bundle-SymbolicName: org.snd.module\n" +
            "Bundle-Version: 33.0.3\n" +
            "Bundle-ManifestVersion: 2\n";
        simpleBundle = NetigsoHid.changeManifest(getWorkDir(), simpleModule, mf);
        
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        BundleContext bc;
        try {
            m1 = mgr.create(activate, null, false, false, false);
            m2 = mgr.create(simpleBundle, null, false, autoload(), false);
            
            mgr.enable(m1);

            Class<?> main = m1.getClassLoader().loadClass("org.activate.Main");
            Object s = main.getField("start").get(null);
            assertNotNull("Bundle started, its context provided", s);
            bc = (BundleContext) s;
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        toEnable = null;
        for (Bundle b : bc.getBundles()) {
            if (b.getSymbolicName().equals("org.snd.module")) {
                toEnable = b;
                break;
            }
        }
        assertNotNull("org.snd.module bundle found", toEnable);
            
        assertFalse("not started yet", m2.isEnabled());
        toEnable.start();
        
        directBundle = toEnable.loadClass("org.foo.Something");
        someModule = m2.getClassLoader().loadClass("org.foo.Something");
        loadClass = directBundle.getMethod("loadClass", String.class, ClassLoader.class);
    }

    @Override
    protected int timeOut() {
        return 300000;
    }
    
    
    
    public void testClassFromBundle() throws Exception {
        assertNotNull("Bundle knows how to load the class", directBundle);
    }
    public void testClassModuleM2() throws Exception {
        assertNotNull("Something loaded from module CL", someModule);
    }
    
    public void testClassLoaderImplementsUtilModuleProvider() {
        final ClassLoader l = m2.getClassLoader();
        assertTrue("ModuleProvider interface implemented: " + l, l instanceof Util.ModuleProvider);
        Util.ModuleProvider mp = (Util.ModuleProvider)l;
        assertEquals("Module is returned", m2, mp.getModule());
    }
    
    public void testClassFromDirectBundle() throws Exception {
        Class<?> directly = (Class<?>) loadClass.invoke(null, "org.foo.Something", null);
        assertNotNull("Bundle knows how to load the class from itself without problems", directly);
    }
     
    public void testClassFromBundleClassLoader() throws Exception {
        Class<?> someFromBundle = (Class<?>) loadClass.invoke(null, "org.foo.Something", someModule.getClassLoader());
        assertNotNull("Bundle knows how to load the class from its using own classloader", someFromBundle);
    }

    public void testClassFromContextClassLoader() throws Exception {
        Class<?> some = (Class<?>) loadClass.invoke(null, "org.foo.Something", Thread.currentThread().getContextClassLoader());
        assertNotNull("Context class loader loads from disabled module bundles too", some);
    }

    public void testM2RemainsDisabled() throws Exception {
        assertFalse("still disabled from NetBeans view point", m2.isEnabled());
    }
    
    public void testResourceFromBundle() throws Exception {
        URL res = toEnable.getResource("org/foo/Something.txt");
        assertNotNull("Bundle knows how to own resource", res);
    }

    public void testResourceFromModule() throws Exception {
        URL res = m2.getClassLoader().getResource("org/foo/Something.txt");
        assertNotNull("Module knows how to own resource", res);
    }

    public void testResourceDirectFromBundle() throws Exception {
        Method loadResource = directBundle.getMethod("loadResource", String.class, ClassLoader.class);
        URL res = (URL) loadResource.invoke(null, "org/foo/Something.txt", null);
        assertNotNull("Bundle knows how to own resource from its classloader", res);
    }

    public void testResourceDirectViaModuleClassLoader() throws Exception {
        Method loadResource = directBundle.getMethod("loadResource", String.class, ClassLoader.class);
        URL res = (URL) loadResource.invoke(null, "org/foo/Something.txt", someModule.getClassLoader());
        assertNotNull("Module knows how to own resource from its classloader", res);
    }

    public void testResourceFromContextClassLoader() throws Exception {
        Method loadResource = directBundle.getMethod("loadResource", String.class, ClassLoader.class);
        URL res = (URL) loadResource.invoke(null, "org/foo/Something.txt", Thread.currentThread().getContextClassLoader());
        assertNotNull("Contxt class loader loads resource from disabled modules too", res);
    }
    
    private static void assertEnumeration(String msg, Enumeration<?> res) {
        assertNotNull(msg + " enumeration not null", res);
        assertTrue(msg + " has at least one", res.hasMoreElements());
        assertNotNull(msg + " one is not zero", res.nextElement());
        assertFalse(msg + " no more items", res.hasMoreElements());
    }
    
    public void testResourcesFromBundle() throws Exception {
        Enumeration<URL> res = toEnable.getResources("org/foo/Something.txt");
        assertEnumeration("Bundle can get its own resources", res);
    }

    public void testResourcesFromModule() throws Exception {
        Enumeration<URL> res = m2.getClassLoader().getResources("org/foo/Something.txt");
        assertEnumeration("Module knows how to own resource", res);
    }

    public void testResourcesDirectFromBundle() throws Exception {
        Method loadResource = directBundle.getMethod("loadResources", String.class, ClassLoader.class);
        Enumeration<URL> res = (Enumeration<URL>) loadResource.invoke(null, "org/foo/Something.txt", null);
        assertEnumeration("Bundle knows how to own resource from its classloader", res);
    }

    public void testResourcesDirectViaModuleClassLoader() throws Exception {
        Method loadResource = directBundle.getMethod("loadResources", String.class, ClassLoader.class);
        Enumeration<URL> res = (Enumeration<URL>) loadResource.invoke(null, "org/foo/Something.txt", someModule.getClassLoader());
        assertEnumeration("Module knows how to own resource from its classloader", res);
    }

    public void testResourcesFromContextClassLoader() throws Exception {
        Method loadResource = directBundle.getMethod("loadResources", String.class, ClassLoader.class);
        Enumeration<URL> res = (Enumeration<URL>) loadResource.invoke(null, "org/foo/Something.txt", Thread.currentThread().getContextClassLoader());
        assertEnumeration("Contxt class loader loads resource from disabled modules too", res);
    }

    public void testResourceAsStreamFromModule() throws Exception {
        InputStream res = m2.getClassLoader().getResourceAsStream("org/foo/Something.txt");
        assertNotNull("Module knows how to own resource", res);
    }

    public void testResourceAsStreamDirectFromBundle() throws Exception {
        Method loadResource = directBundle.getMethod("loadResourceAsStream", String.class, ClassLoader.class);
        InputStream res = (InputStream) loadResource.invoke(null, "org/foo/Something.txt", null);
        assertNotNull("Bundle knows how to own resource from its classloader", res);
    }

    public void testResourceAsStreamDirectViaModuleClassLoader() throws Exception {
        Method loadResource = directBundle.getMethod("loadResourceAsStream", String.class, ClassLoader.class);
        InputStream res = (InputStream) loadResource.invoke(null, "org/foo/Something.txt", someModule.getClassLoader());
        assertNotNull("Module knows how to own resource from its classloader", res);
    }

    public void testResourceAsStreamFromContextClassLoader() throws Exception {
        Method loadResource = directBundle.getMethod("loadResourceAsStream", String.class, ClassLoader.class);
        InputStream res = (InputStream) loadResource.invoke(null, "org/foo/Something.txt", Thread.currentThread().getContextClassLoader());
        assertNotNull("Contxt class loader loads resource from disabled modules too", res);
    }
    
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
}
