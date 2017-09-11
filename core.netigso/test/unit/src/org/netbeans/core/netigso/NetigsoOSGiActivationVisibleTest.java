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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
        Enumeration res = toEnable.getResources("org/foo/Something.txt");
        assertEnumeration("Bundle can get its own resources", res);
    }

    public void testResourcesFromModule() throws Exception {
        Enumeration<URL> res = m2.getClassLoader().getResources("org/foo/Something.txt");
        assertEnumeration("Module knows how to own resource", res);
    }

    public void testResourcesDirectFromBundle() throws Exception {
        Method loadResource = directBundle.getMethod("loadResources", String.class, ClassLoader.class);
        Enumeration res = (Enumeration) loadResource.invoke(null, "org/foo/Something.txt", null);
        assertEnumeration("Bundle knows how to own resource from its classloader", res);
    }

    public void testResourcesDirectViaModuleClassLoader() throws Exception {
        Method loadResource = directBundle.getMethod("loadResources", String.class, ClassLoader.class);
        Enumeration res = (Enumeration) loadResource.invoke(null, "org/foo/Something.txt", someModule.getClassLoader());
        assertEnumeration("Module knows how to own resource from its classloader", res);
    }

    public void testResourcesFromContextClassLoader() throws Exception {
        Method loadResource = directBundle.getMethod("loadResources", String.class, ClassLoader.class);
        Enumeration res = (Enumeration) loadResource.invoke(null, "org/foo/Something.txt", Thread.currentThread().getContextClassLoader());
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
