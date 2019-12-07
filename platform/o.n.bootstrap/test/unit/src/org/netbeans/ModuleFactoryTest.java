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
        protected void classLoaderUp(Set<Module> parents) throws IOException {
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
        protected @Override Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if ("java.lang.String".equals(name)) {
                throw new ClassNotFoundException("NoOpClassLoader cannot load " + name);
            }
            return super.loadClass(name, resolve);
        }
    }
}
