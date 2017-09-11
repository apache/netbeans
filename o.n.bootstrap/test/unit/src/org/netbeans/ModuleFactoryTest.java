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
 * Software is Nokia. Portions Copyright 2005 Nokia. All Rights Reserved.
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

package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/**
 * These tests verify that the module manager behaves basically the
 * same way with ModuleFactory as without it.
 * @author David Strupl
 */
public class ModuleFactoryTest extends ModuleManagerTest {

    static {
        MockLookup.setInstances(new MyModuleFactory());
    }

    public ModuleFactoryTest(String name) {
        super(name);
    }

    public static int numberOfStandard = 0;
    public static int numberOfFixed = 0;
    public static boolean testingParentClassloaders = false;
    public static boolean testingDummyModule = false;
    
    public void testFactoryCreatesOurModules() throws Exception {
        // clear the counters before the test!
        numberOfStandard = 0;
        numberOfFixed = 0;
        
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            File j1 = new File(jars, "simple-module.jar");
            File j2 = new File(jars, "depends-on-simple-module.jar");
            File j3 = new File(jars, "dep-on-two-modules.jar");
            URLClassLoader l = new URLClassLoader(new URL[] {Utilities.toURI(j1).toURL(), Utilities.toURI(j2).toURL()});
            Manifest mani1, mani2;
            JarFile j = new JarFile(j1);
            try {
                mani1 = j.getManifest();
            } finally {
                j.close();
            }
            j = new JarFile(j2);
            try {
                mani2 = j.getManifest();
            } finally {
                j.close();
            }
            Module m1 = mgr.createFixed(mani1, null, l);
            Module m2 = mgr.createFixed(mani2, null, l);
            Module m3 = mgr.create(j3, null, false, false, false);
            mgr.enable(new HashSet<Module>(Arrays.asList(m1, m2, m3)));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        assertEquals("Number of standard modules created by our factory ", 1, numberOfStandard);
        assertEquals("Number of fixed modules created by our factory ", 2, numberOfFixed);
    }

    public void testDummyModule() throws Exception {
        ModuleManager mgr = null;
        try {
            testingDummyModule = true;
            MockModuleInstaller installer = new MockModuleInstaller();
            MockEvents ev = new MockEvents();
            mgr = new ModuleManager(installer, ev);
            mgr.mutexPrivileged().enterWriteAccess();
            
            File j1 = new File(jars, "simple-module.jar");
            Module m1 = mgr.create(j1, null, false, false, false);
            mgr.enable(Collections.singleton(m1));
            boolean res = false;
            try {
                m1.getClassLoader().loadClass("java.lang.String");
            } catch (ClassNotFoundException x) {
                res = true;
            }
            assertTrue("dummy module with no-op classloader should not find any classes", res);
        } finally {
            testingDummyModule = false;
            if (mgr != null) {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        }
    }

    public void testRemoveBaseClassLoader() throws Exception {
        ModuleManager mgr = null;
        try {
            testingParentClassloaders = true;
            MockModuleInstaller installer = new MockModuleInstaller();
            MockEvents ev = new MockEvents();
            mgr = new ModuleManager(installer, ev);
            mgr.mutexPrivileged().enterWriteAccess();
            File j1 = new File(jars, "simple-module.jar");
            Module m1 = mgr.create(j1, null, false, false, false);
            mgr.enable(Collections.singleton(m1));
            boolean res = false;
            try {
                mgr.getClassLoader().loadClass("java.lang.String");
            } catch (ClassNotFoundException x) {
                res = true;
            }
            assertTrue("When removing the base classloader not even JDK classes should be found", res);
        } finally {
            testingParentClassloaders = false;
            if (mgr != null) {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        }
    }
    
    private static final class MyModuleFactory extends ModuleFactory {
        public @Override Module create(File jar, Object history, boolean reloadable, boolean autoload, boolean eager, ModuleManager mgr, Events ev) throws IOException {
            if (testingDummyModule || testingParentClassloaders) {
                return new DummyModule(mgr, ev, history, reloadable, autoload, eager);
            }
            numberOfStandard++;
            return super.create(jar, history, reloadable, autoload, eager, mgr, ev);
        }
        
        public @Override Module createFixed(Manifest mani, Object history, ClassLoader loader, boolean autoload, boolean eager, ModuleManager mgr, Events ev) throws InvalidException {
            numberOfFixed++;
            return super.createFixed(mani, history, loader, autoload, eager, mgr, ev);
        }
        
        public @Override boolean removeBaseClassLoader() {
            if (testingParentClassloaders) {
                return true;
            }
            return super.removeBaseClassLoader();
        }
        public @Override ClassLoader getClasspathDelegateClassLoader(ModuleManager mgr, ClassLoader del) {
            if (testingParentClassloaders) {
                return new NoOpClassLoader();
            }
            return del;
        }
    }
    
    private static final class DummyModule extends Module {
        private final Manifest manifest;
        public DummyModule(ModuleManager mgr, Events ev, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
            super(mgr, ev, history, reloadable, autoload, eager);
            manifest = new Manifest();
            manifest.getMainAttributes().putValue("OpenIDE-Module", "boom");
            parseManifest();
        }
        @Override
        public List<File> getAllJars() {
            return Collections.emptyList();
        }
        @Override
        public void setReloadable(boolean r) {
        }
        @Override
        public void reload() throws IOException {
        }
        @Override
        protected void classLoaderUp(Set parents) throws IOException {
            classloader = new JarClassLoader(Collections.<File>emptyList(), new ClassLoader[] {new NoOpClassLoader()});
        }
        @Override
        protected void classLoaderDown() {
        }
        @Override
        protected void cleanup() {
        }
        @Override
        protected void destroy() {
        }
        @Override
        public boolean isFixed() {
            return true;
        }
        @Override
        public Object getLocalizedAttribute(String attr) {
            return null;
        }
        public @Override Manifest getManifest() {
            return manifest;
        }
    }
    
    private static final class NoOpClassLoader extends ClassLoader {
        NoOpClassLoader() {
	    super(ClassLoader.getSystemClassLoader());
	}
        protected @Override Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if ("java.lang.String".equals(name)) {
                throw new ClassNotFoundException("NoOpClassLoader cannot load " + name);
            }
            return super.loadClass(name, resolve);
        }
    }
}
