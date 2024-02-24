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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotEquals;
import static org.netbeans.SetupHid.createTestJAR;
import org.netbeans.junit.RandomlyFails;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;

/** Test the module manager as well as the Module class.
 * This means creating modules from JAR as well as from "classpath"
 * (i.e. rigged-up classloader), and testing that it creates them with
 * the correct stuff; testing that the various pieces of the manifest
 * are correctly parsed and made accessible; that dependencies work
 * when things are done in various orders etc.; that problems (such as
 * missing dependencies) are accurately reported; that the classloaders
 * are capable of getting everything listed; that module installer
 * methods are called at the correct times and with modules in the correct
 * state; that changes are fired correctly; etc.
 * Note that since the design of the module manager makes no direct
 * reference to general IDE classes other than standalone APIs and a couple
 * of standalone core utilities, this entire test can (and ought to be)
 * executed in standalone mode.
 * @author Jesse Glick
 */
public class ModuleManagerTest extends SetupHid {

    static {
        // To match org.netbeans.Main.execute (cf. #44828):
        new URLConnection(ModuleManagerTest.class.getResource("ModuleManagerTest.class")) {
            public @Override void connect() throws IOException {}
        }.setDefaultUseCaches(false);
        ProxyURLStreamHandlerFactory.register();
    }

    public ModuleManagerTest(String name) {
        super(name);
    }
    
    private LogHandler logHandler = new LogHandler();

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        Util.err.addHandler(logHandler);
    }

    @Override
    protected void tearDown() throws Exception {
        Util.err.removeHandler(logHandler);
        super.tearDown();
    }
    
    
    
    /** Load simple-module and depends-on-simple-module.
     * Make sure they can be installed and in a sane order.
     * Make sure a class from one can depend on a class from another.
     * Try to disable them too.
     */
    public void testSimpleInstallation() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            assertEquals("org.foo", m1.getCodeNameBase());
            assertEquals("org.bar", m2.getCodeNameBase());
            assertCnb(m1);
            assertCnb(m2);
            assertEquals(Collections.EMPTY_SET, m1.getDependencies());
            assertEquals(Dependency.create(Dependency.TYPE_MODULE, "org.foo/1"), m2.getDependencies());
            Map<String,Module> modulesByName = new HashMap<String,Module>();
            modulesByName.put(m1.getCodeNameBase(), m1);
            modulesByName.put(m2.getCodeNameBase(), m2);
            List<Module> m1m2 = Arrays.asList(m1, m2);
            List<Module> m2m1 = Arrays.asList(m2, m1);
            Map<Module,List<Module>> deps = Util.moduleDependencies(m1m2, modulesByName, Collections.<String,Set<Module>>emptyMap());
            assertNull(deps.get(m1));
            assertEquals(Collections.singletonList(m1), deps.get(m2));
            assertEquals(m2m1, Utilities.topologicalSort(m1m2, deps));
            assertEquals(m2m1, Utilities.topologicalSort(m2m1, deps));
            // Leave commented out since it has a (hopefully clean) mutation effect
            // and could affect results:
            /*
            assertEquals(Collections.EMPTY_SET, m1.getProblems());
            assertEquals(Collections.EMPTY_SET, m2.getProblems());
             */
            Set<Module> m1PlusM2 = new HashSet<Module>();
            m1PlusM2.add(m1);
            m1PlusM2.add(m2);
            List<Module> toEnable = mgr.simulateEnable(m1PlusM2);
            assertEquals("correct result of simulateEnable", Arrays.asList(m1, m2), toEnable);
            mgr.enable(m1PlusM2);
            assertEquals(Arrays.asList(
                "prepare",
                "prepare",
                "load"
            ), installer.actions);
            assertEquals(Arrays.asList(
                m1,
                m2,
                Arrays.asList(m1, m2)
            ), installer.args);
            Class<?> somethingelse = Class.forName("org.bar.SomethingElse", true, m2.getClassLoader());
            Method somemethod = somethingelse.getMethod("message");
            assertEquals("hello", somemethod.invoke(somethingelse.getDeclaredConstructor().newInstance()));
            installer.clear();
            List<Module> toDisable = mgr.simulateDisable(Collections.singleton(m1));
            assertEquals("correct result of simulateDisable", Arrays.asList(m2, m1), toDisable);
            toDisable = mgr.simulateDisable(m1PlusM2);
            assertEquals("correct result of simulateDisable #2", Arrays.asList(m2, m1), toDisable);
            mgr.disable(m1PlusM2);
            assertFalse(m1.isEnabled());
            assertFalse(m2.isEnabled());
            assertEquals(Collections.EMPTY_SET, mgr.getEnabledModules());
            assertEquals(m1PlusM2, mgr.getModules());
            assertEquals(Arrays.asList(
                "unload",
                "dispose",
                "dispose"
            ), installer.actions);
            assertEquals(Arrays.asList(
                Arrays.asList(m2, m1),
                m2,
                m1
            ), installer.args);
            installer.clear();
            mgr.enable(m1);
            mgr.shutDown();
            assertEquals(Arrays.asList(
                "prepare",
                "load",
                "closing",
                "close"
            ), installer.actions);
            assertEquals(Arrays.asList(
                m1,
                Collections.singletonList(m1),
                Collections.singletonList(m1),
                Collections.singletonList(m1)
            ), installer.args);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testInstallAutoload() throws Exception {
        // Cf. #9779, I think.
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            // m1 will be an autoload.
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, true, false);
            try {
                mgr.simulateEnable(new HashSet<Module>(Arrays.asList(m1, m2)));
                assertTrue("Should not permit you to simulate enablement of an autoload", false);
            } catch (IllegalArgumentException iae) {
                // Good. m1 should not have been passed to it.
            }
            assertEquals(Collections.EMPTY_SET, m1.getProblems());
            assertEquals(Collections.EMPTY_SET, m2.getProblems());
            List<Module> toEnable = mgr.simulateEnable(Collections.singleton(m2));
            assertEquals("correct result of simulateEnable", Arrays.asList(m1, m2), toEnable);
            mgr.enable(Collections.singleton(m2));
            assertEquals(Arrays.asList(
                "prepare",
                "prepare",
                "load"
            ), installer.actions);
            assertEquals(Arrays.asList(
                m1,
                m2,
                Arrays.asList(m1, m2)
            ), installer.args);
            Class<?> somethingelse = Class.forName("org.bar.SomethingElse", true, m2.getClassLoader());
            Method somemethod = somethingelse.getMethod("message");
            assertEquals("hello", somemethod.invoke(somethingelse.getDeclaredConstructor().newInstance()));
            // Now try turning off m2 and make sure m1 goes away as well.
            assertEquals("correct result of simulateDisable", Arrays.asList(m2, m1), mgr.simulateDisable(Collections.singleton(m2)));
            installer.clear();
            mgr.disable(Collections.singleton(m2));
            assertEquals(Arrays.asList(
                "unload",
                "dispose",
                "dispose"
            ), installer.actions);
            assertEquals(Arrays.asList(
                Arrays.asList(m2, m1),
                m2,
                m1
            ), installer.args);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testInstallEager() throws Exception {
        // Cf. #17501.
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            // m2 will be eager.
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, true);
            try {
                mgr.simulateEnable(new HashSet<Module>(Arrays.asList(m1, m2)));
                fail("Should not permit you to simulate enablement of an eager module");
            } catch (IllegalArgumentException iae) {
                // Good. m2 should not have been passed to it.
            }
            try {
                mgr.enable(new HashSet<>(Arrays.asList(m1, m2)));
                fail("Should not permit you enablem of an eager module");
            } catch (IllegalModuleException iae) {
                // Good. m2 should not have been passed to it.
                assertNotEquals(iae.getMessage(), iae.getLocalizedMessage());
            }
            assertEquals(Collections.EMPTY_SET, m1.getProblems());
            assertEquals(Collections.EMPTY_SET, m2.getProblems());
            List<Module> toEnable = mgr.simulateEnable(Collections.singleton(m1));
            assertEquals("correct result of simulateEnable", Arrays.asList(m1, m2), toEnable);
            mgr.enable(Collections.singleton(m1));
            assertEquals(Arrays.asList(
                "prepare",
                "prepare",
                "load"
            ), installer.actions);
            assertEquals(Arrays.asList(
                m1,
                m2,
                Arrays.asList(m1, m2)
            ), installer.args);
            Class<?> somethingelse = Class.forName("org.bar.SomethingElse", true, m2.getClassLoader());
            Method somemethod = somethingelse.getMethod("message");
            assertEquals("hello", somemethod.invoke(somethingelse.getDeclaredConstructor().newInstance()));
            // Now try turning off m1 and make sure m2 goes away quietly.
            assertEquals("correct result of simulateDisable", Arrays.asList(m2, m1), mgr.simulateDisable(Collections.singleton(m1)));
            installer.clear();
            mgr.disable(Collections.singleton(m1));
            assertEquals(Arrays.asList(
                "unload",
                "dispose",
                "dispose"
            ), installer.actions);
            assertEquals(Arrays.asList(
                Arrays.asList(m2, m1),
                m2,
                m1
            ), installer.args);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testEagerPlusAutoload() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            // m1 autoload, m2 normal, m3 eager
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, true, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "dep-on-dep-on-simple.jar"), null, false, false, true);
            List<Module> toEnable = mgr.simulateEnable(Collections.singleton(m2));
            assertEquals("correct result of simulateEnable", Arrays.asList(m1, m2, m3), toEnable);
            mgr.enable(Collections.singleton(m2));
            assertEquals(Arrays.asList(
                "prepare",
                "prepare",
                "prepare",
                "load"
            ), installer.actions);
            assertEquals(Arrays.asList(
                m1,
                m2,
                m3,
                Arrays.asList(m1, m2, m3)
            ), installer.args);
            Class<?> somethingelseagain = Class.forName("org.baz.SomethingElseAgain", true, m3.getClassLoader());
            Method somemethod = somethingelseagain.getMethod("doit");
            assertEquals("hello", somemethod.invoke(somethingelseagain.getDeclaredConstructor().newInstance()));
            assertEquals("correct result of simulateDisable", Arrays.asList(m3, m2, m1), mgr.simulateDisable(Collections.singleton(m2)));
            installer.clear();
            mgr.disable(Collections.singleton(m2));
            assertEquals(Arrays.asList(
                "unload",
                "dispose",
                "dispose",
                "dispose"
            ), installer.actions);
            assertEquals(Arrays.asList(
                Arrays.asList(m3, m2, m1),
                m3,
                m2,
                m1
            ), installer.args);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    /** Test scenario from #22536: when a normal module and an eager module
     * both depend on the autoload, the eager & autoload modules should
     * always be on, regardless of the normal module.
     */
    public void testEagerPlusAutoload2() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            // m1 autoload, m2 normal, m3 eager
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, true, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "depends-on-simple-module-2.jar"), null, false, false, true);
            mgr.enable(Collections.<Module>emptySet());
            assertTrue(m1.isEnabled());
            assertFalse(m2.isEnabled());
            assertTrue(m3.isEnabled());
            List<Module> toEnable = mgr.simulateEnable(Collections.singleton(m2));
            assertEquals("correct result of simulateEnable", Collections.singletonList(m2), toEnable);
            mgr.enable(Collections.singleton(m2));
            assertTrue(m1.isEnabled());
            assertTrue(m2.isEnabled());
            assertTrue(m3.isEnabled());
            List<Module> toDisable = mgr.simulateDisable(Collections.singleton(m2));
            assertEquals("correct result of simulateDisable", Collections.singletonList(m2), toDisable);
            mgr.disable(Collections.singleton(m2));
            assertTrue(m1.isEnabled());
            assertFalse(m2.isEnabled());
            assertTrue(m3.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testEagerEnabledImmediately() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, true);
            mgr.enable(Collections.<Module>emptySet());
            assertTrue(m1.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, true);
            mgr.enable(Collections.<Module>emptySet());
            assertFalse(m1.isEnabled());
            assertFalse(m2.isEnabled());
            mgr.enable(m1);
            assertTrue(m1.isEnabled());
            assertTrue(m2.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, true, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, true);
            mgr.enable(Collections.<Module>emptySet());
            assertTrue(m1.isEnabled());
            assertTrue(m2.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testEagerEnablementRobust() throws Exception { // #144005
        File dir = getWorkDir();
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            File jar = new File(dir, "eager1.jar");
            TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:OpenIDE-Module: eager1\nOpenIDE-Module-Module-Dependencies: autoload\n\n");
            Module eager1 = mgr.create(jar, null, false, false, true);
            mgr.enable(Collections.<Module>emptySet());
            assertEquals(Collections.emptySet(), mgr.getEnabledModules());
            jar = new File(dir, "autoload.jar");
            TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:OpenIDE-Module: autoload\n\n");
            Module autoload = mgr.create(jar, null, false, true, false);
            mgr.enable(Collections.<Module>emptySet());
            assertEquals(new HashSet<Module>(Arrays.asList(autoload, eager1)), mgr.getEnabledModules());
            jar = new File(dir, "eager2.jar");
            TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:OpenIDE-Module: eager2\nOpenIDE-Module-Module-Dependencies: missing\n\n");
            mgr.create(jar, null, false, false, true);
            mgr.enable(Collections.<Module>emptySet());
            assertEquals(new HashSet<Module>(Arrays.asList(autoload, eager1)), mgr.getEnabledModules());
            jar = new File(dir, "eager3.jar");
            TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:OpenIDE-Module: eager3\nOpenIDE-Module-Module-Dependencies: autoload\n\n");
            Module eager3 = mgr.create(jar, null, false, false, true);
            mgr.enable(Collections.<Module>emptySet());
            assertEquals(new HashSet<Module>(Arrays.asList(autoload, eager1, eager3)), mgr.getEnabledModules());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testCyclic() throws Exception {
        // Cf. #12014.
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module cyc1 = mgr.create(new File(jars, "cyclic-1.jar"), null, false, false, false);
            Module cyc2 = mgr.create(new File(jars, "cyclic-2.jar"), null, false, false, false);
            Module cycd = mgr.create(new File(jars, "depends-on-cyclic-1.jar"), null, false, false, false);
            Set<Module> circular = new HashSet<Module>(Arrays.asList(cyc1, cyc2, cycd));
            assertEquals("correct result of simulateEnable", Collections.EMPTY_LIST, mgr.simulateEnable(circular));
            assertEquals("cyc1 problems include cyc2", cyc1.getDependencies(), cyc1.getProblems());
            assertEquals("cyc2 problems include cyc1", cyc2.getDependencies(), cyc2.getProblems());
            assertEquals("cycd problems include cyc1", cycd.getDependencies(), cycd.getProblems());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testBuildVersionCanBeReadOrIsDelegated() throws Exception {
        // Cf. #12014.
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module cyc1 = mgr.create(new File(jars, "cyclic-1.jar"), null, false, false, false);
            Module cyc2 = mgr.create(new File(jars, "cyclic-2.jar"), null, false, false, false);

            String impl1 = cyc1.getImplementationVersion ();
            String impl2 = cyc2.getImplementationVersion ();
            String bld1 = cyc1.getBuildVersion ();
            String bld2 = cyc2.getBuildVersion ();

            assertEquals (
                "cyc1 does not define build version and thus it is same as impl",
                impl1, bld1
            );

            assertEquals (
                "cyc2 does define build version",
                "this_line_is_here_due_to_yarda",
                bld2
            );

            assertTrue ("Impl and build versions are not same",
                !bld2.equals (impl2)
            );
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testLookup() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m1, m2;
        try {
            m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        Lookup l = mgr.getModuleLookup();
        assertNull(l.lookup(String.class));
        Object random = l.lookup(ModuleInfo.class);
        assertTrue(random == m1 || random == m2);
        random = l.lookup(Module.class);
        assertTrue(random == m1 || random == m2);
        Lookup.Result<ModuleInfo> resultAll = l.lookupResult(ModuleInfo.class);
        assertEquals("finding all instances works", new HashSet<Module>(Arrays.asList(m1, m2)), new HashSet<ModuleInfo>(resultAll.allInstances()));
        Lookup.Result<Module> resultInstance2 = l.lookup(new Lookup.Template<Module>(null, null, m2));
        assertEquals("finding one specific instance works", Collections.singleton(m2), new HashSet<Module>(resultInstance2.allInstances()));
        Collection<? extends Lookup.Item<Module>> items = resultInstance2.allItems();
        assertTrue(items.size() == 1);
        Lookup.Item<Module> item = items.iterator().next();
        assertEquals(m2, item.getInstance());
        Util.err.log(Level.INFO, "Item ID: {0}", item.getId());
        assertTrue("Item class is OK: " + item.getType(), item.getType().isAssignableFrom(Module.class));
        assertEquals("finding by ID works", Collections.singleton(m2),
                new HashSet<Module>(l.lookup(new Lookup.Template<Module>(null, item.getId(), null)).allInstances()));
        final boolean[] waiter = new boolean[] {false};
        resultAll.addLookupListener(new LookupListener() {
            public @Override void resultChanged(LookupEvent lev) {
                Util.err.log(Level.INFO, "Got event: {0}", lev);
                synchronized (waiter) {
                    waiter[0] = true;
                    waiter.notify();
                }
            }
        });
        Module m3;
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            m3 = mgr.create(new File(jars, "cyclic-1.jar"), null, false, false, false);
            mgr.delete(m2);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        assertEquals("results changed", new HashSet<ModuleInfo>(Arrays.asList(m1, m3)), new HashSet<ModuleInfo>(resultAll.allInstances()));
        synchronized (waiter) {
            if (! waiter[0]) {
                waiter.wait(5000);
            }
        }
        assertTrue("got lookup changes within 5 seconds", waiter[0]);
    }

    /** Test that after deletion of a module, problems cache is cleared. */
    public void test14561() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            Set<Module> m1AndM2 = new HashSet<Module>(Arrays.asList(m1, m2));
            mgr.enable(m1AndM2);
            mgr.disable(m1AndM2);
            assertEquals(Collections.EMPTY_SET, m2.getProblems());
            mgr.delete(m1);
            assertEquals(1, m2.getProblems().size());
            assertEquals(Collections.EMPTY_LIST, mgr.simulateEnable(Collections.singleton(m2)));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    /** Test that PROP_PROBLEMS is fired reliably after unexpected problems. */
    public void test14560() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        LoggedPCListener listener = new LoggedPCListener();
        mgr.addPropertyChangeListener(listener);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m1, m2;
        try {
            m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            m1.addPropertyChangeListener(listener);
            m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            m2.addPropertyChangeListener(listener);
            installer.delinquents.add(m1);
            Set<Module> m1AndM2 = new HashSet<Module>(Arrays.asList(m1, m2));
            try {
                mgr.enable(m1AndM2);
            } catch (InvalidException ie) {
                assertEquals(m1, ie.getModule());
            }
            assertFalse(m1.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        assertTrue("Got PROP_PROBLEMS on m1", listener.waitForChange(m1, Module.PROP_PROBLEMS));
    }

    // #14705: make sure package loading is tested
    public void testPackageLoading() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            // Make sure all of these can be turned on:
            tryEnablingModule(mgr, "depends-on-lib-undecl.jar");
            tryEnablingModule(mgr, "depends-on-lib-unvers.jar");
            tryEnablingModule(mgr, "depends-on-lib-vers.jar");
            tryEnablingModule(mgr, "depends-on-lib-vers-partial.jar");
            // In fact it is OK to depend on pkg.somepkg[Something] even with
            // library-undecl.jar, since the classloader will define a package for you.
            //failToEnableModule(mgr, "fails-on-lib-undecl.jar");
            // These should not work:
            failToEnableModule(mgr, "fails-on-lib-unvers.jar");
            failToEnableModule(mgr, "fails-on-lib-old.jar");
            // Make sure that classloading is OK:
            Module m = mgr.create(new File(jars, "depends-on-lib-undecl.jar"), null, false, false, false);
            mgr.enable(m);
            Class<?> c = m.getClassLoader().loadClass("org.dol.User");
            Object o = c.getDeclaredConstructor().newInstance();
            Field f = c.getField("val");
            assertEquals(42, f.getInt(o));
            mgr.disable(m);
            mgr.delete(m);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    private void tryEnablingModule(ModuleManager mgr, String name) throws Exception {
        Module m = mgr.create(new File(jars, name), null, false, false, false);
        try {
            mgr.enable(m);
            mgr.disable(m);
        } finally {
            mgr.delete(m);
        }
    }

    private void failToEnableModule(ModuleManager mgr, String name) throws Exception {
        try {
            tryEnablingModule(mgr, name);
            fail("Was able to turn on " + name + " without complaint");
        } catch (InvalidException ie) {
            // Fine, expected.
        }
    }

    public void testPackageDependencyMayfail() throws Exception {
        //see #63904:
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Manifest mani;
            JarFile jf = new JarFile(new File(jars, "simple-module.jar"));
            try {
                mani = jf.getManifest();
            } finally {
                jf.close();
            }

            Module toFail = mgr.create(new File(jars, "fails-on-non-existing-package.jar"), null, false, false, false);
            Module fixed  = mgr.createFixed(mani, null, this.getClass().getClassLoader());

            try {
                mgr.enable(new HashSet<Module>(Arrays.asList(toFail, fixed)));
                fail("Was able to turn on fails-on-non-existing-package.jar without complaint");
            } catch (InvalidException e) {
                assertTrue("fails-on-non-existing-package.jar was not enabled", e.getModule() == toFail);
            }

            assertTrue("simple-module.jar was enabled", fixed.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }


    // #12549: check that loading of localized manifest attributes works.
    public void testLocalizedManifestAttributes() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Locale starting = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("en", "US"));
            File locmanijar = new File(jars, "localized-manifest.jar");
            assertTrue("test JAR exists: " + locmanijar, locmanijar.isFile()); // #50891
            Module m = mgr.create(locmanijar, null, false, false, false);
            // These are defined in the bundle:
            assertEquals("en_US display name", "Localized Manifest Module", m.getDisplayName());
            assertEquals("en_US bundle main attr", "value #1", m.getLocalizedAttribute("some-other-key"));
            assertEquals("en_US bundle sub attr", "value #2", m.getLocalizedAttribute("locmani/something.txt/other-key"));
            assertEquals("en_US bundle main attr untrans", "value #7", m.getLocalizedAttribute("other-untrans"));
            assertEquals("en_US bundle sub attr untrans", "value #8", m.getLocalizedAttribute("locmani/something.txt/other-untrans"));
            // These in the manifest itself:
            assertEquals("en_US manifest main attr", "value #3", m.getLocalizedAttribute("some-key"));
            assertEquals("en_US manifest sub attr", "value #4", m.getLocalizedAttribute("locmani/something.txt/key"));
            assertEquals("en_US manifest main attr untrans", "value #5", m.getLocalizedAttribute("untrans"));
            assertEquals("en_US manifest sub attr untrans", "value #6", m.getLocalizedAttribute("locmani/something.txt/untrans"));
            mgr.delete(m);
            // Now try it again, with a different locale this time:
            Locale.setDefault(new Locale("cs", "CZ"));
            m = mgr.create(new File(jars, "localized-manifest.jar"), null, false, false, false);
            // Note Unicode values in the bundle.
            assertEquals("cs_CZ display name", "Modul s lokalizovan\u00FDm manifestem", m.getDisplayName());
            assertEquals("cs_CZ bundle main attr", "v\u00FDznam #1", m.getLocalizedAttribute("some-other-key"));
            assertEquals("cs_CZ bundle sub attr", "v\u00FDznam #2", m.getLocalizedAttribute("locmani/something.txt/other-key"));
            // These are not translated, see that they fall back to "default" locale:
            assertEquals("cs_CZ bundle main attr untrans", "value #7", m.getLocalizedAttribute("other-untrans"));
            assertEquals("cs_CZ bundle sub attr untrans", "value #8", m.getLocalizedAttribute("locmani/something.txt/other-untrans"));
            // The manifest cannot hold non-ASCII characters.
            assertEquals("cs_CZ manifest main attr", "vyznam #3", m.getLocalizedAttribute("some-key"));
            assertEquals("cs_CZ manifest sub attr", "vyznam #4", m.getLocalizedAttribute("locmani/something.txt/key"));
            // Also not translated:
            assertEquals("cs_CZ manifest main attr untrans", "value #5", m.getLocalizedAttribute("untrans"));
            assertEquals("cs_CZ manifest sub attr untrans", "value #6", m.getLocalizedAttribute("locmani/something.txt/untrans"));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
            Locale.setDefault(starting);
        }
    }

    // #19698: check that it also works when the module is enabled (above, module was disabled).
    public void testLocalizedManifestAttributesWhileEnabled() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Locale starting = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("en", "US"));
            Module m = mgr.create(new File(jars, "localized-manifest.jar"), null, false, false, false);
            mgr.enable(m);
            // These are defined in the bundle:
            assertEquals("en_US display name", "Localized Manifest Module", m.getDisplayName());
            assertEquals("en_US bundle main attr", "value #1", m.getLocalizedAttribute("some-other-key"));
            assertEquals("en_US bundle sub attr", "value #2", m.getLocalizedAttribute("locmani/something.txt/other-key"));
            assertEquals("en_US bundle main attr untrans", "value #7", m.getLocalizedAttribute("other-untrans"));
            assertEquals("en_US bundle sub attr untrans", "value #8", m.getLocalizedAttribute("locmani/something.txt/other-untrans"));
            // These in the manifest itself:
            assertEquals("en_US manifest main attr", "value #3", m.getLocalizedAttribute("some-key"));
            assertEquals("en_US manifest sub attr", "value #4", m.getLocalizedAttribute("locmani/something.txt/key"));
            assertEquals("en_US manifest main attr untrans", "value #5", m.getLocalizedAttribute("untrans"));
            assertEquals("en_US manifest sub attr untrans", "value #6", m.getLocalizedAttribute("locmani/something.txt/untrans"));
            mgr.disable(m);
            mgr.delete(m);
            // Now try it again, with a different locale this time:
            Locale.setDefault(new Locale("cs", "CZ"));
            m = mgr.create(new File(jars, "localized-manifest.jar"), null, false, false, false);
            mgr.enable(m);
            // Note Unicode values in the bundle.
            assertEquals("cs_CZ display name", "Modul s lokalizovan\u00FDm manifestem", m.getDisplayName());
            assertEquals("cs_CZ bundle main attr", "v\u00FDznam #1", m.getLocalizedAttribute("some-other-key"));
            assertEquals("cs_CZ bundle sub attr", "v\u00FDznam #2", m.getLocalizedAttribute("locmani/something.txt/other-key"));
            // These are not translated, see that they fall back to "default" locale:
            assertEquals("cs_CZ bundle main attr untrans", "value #7", m.getLocalizedAttribute("other-untrans"));
            assertEquals("cs_CZ bundle sub attr untrans", "value #8", m.getLocalizedAttribute("locmani/something.txt/other-untrans"));
            // The manifest cannot hold non-ASCII characters.
            assertEquals("cs_CZ manifest main attr", "vyznam #3", m.getLocalizedAttribute("some-key"));
            assertEquals("cs_CZ manifest sub attr", "vyznam #4", m.getLocalizedAttribute("locmani/something.txt/key"));
            // Also not translated:
            assertEquals("cs_CZ manifest main attr untrans", "value #5", m.getLocalizedAttribute("untrans"));
            assertEquals("cs_CZ manifest sub attr untrans", "value #6", m.getLocalizedAttribute("locmani/something.txt/untrans"));
            mgr.disable(m);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
            Locale.setDefault(starting);
        }
    }

    // There was also a bug that loc mani attrs were not recognized for classpath modules.
    public void testLocalizedManifestAttributesClasspath() throws Exception {
        File jar = new File(jars, "localized-manifest.jar");
        File ljar = new File(new File(jars, "locale"), "localized-manifest_cs.jar");
        Manifest mani;
        JarFile jf = new JarFile(jar);
        try {
            mani = jf.getManifest();
        } finally {
            jf.close();
        }
        ClassLoader l = new URLClassLoader(new URL[] {
            // Order should be irrelevant:
            Utilities.toURI(jar).toURL(),
            Utilities.toURI(ljar).toURL(),
        });
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        Locale starting = Locale.getDefault();
        try {
            ModuleManager mgr = new ModuleManager(installer, ev);
            mgr.mutexPrivileged().enterWriteAccess();
            try {
                Locale.setDefault(new Locale("en", "US"));
                Module m = mgr.createFixed(mani, null, l);
                // These are defined in the bundle:
                assertEquals("en_US display name", "Localized Manifest Module", m.getDisplayName());
                assertEquals("en_US bundle main attr", "value #1", m.getLocalizedAttribute("some-other-key"));
                assertEquals("en_US bundle sub attr", "value #2", m.getLocalizedAttribute("locmani/something.txt/other-key"));
                assertEquals("en_US bundle main attr untrans", "value #7", m.getLocalizedAttribute("other-untrans"));
                assertEquals("en_US bundle sub attr untrans", "value #8", m.getLocalizedAttribute("locmani/something.txt/other-untrans"));
                // These in the manifest itself:
                assertEquals("en_US manifest main attr", "value #3", m.getLocalizedAttribute("some-key"));
                assertEquals("en_US manifest sub attr", "value #4", m.getLocalizedAttribute("locmani/something.txt/key"));
                assertEquals("en_US manifest main attr untrans", "value #5", m.getLocalizedAttribute("untrans"));
                assertEquals("en_US manifest sub attr untrans", "value #6", m.getLocalizedAttribute("locmani/something.txt/untrans"));
            } finally {
                mgr.mutexPrivileged().exitWriteAccess();
            }
            // Need to start with a new manager: cannot delete classpath modules, would be a dupe
            // if we tried to make it again.
            mgr = new ModuleManager(installer, ev);
            mgr.mutexPrivileged().enterWriteAccess();
            try {
                // Now try it again, with a different locale this time:
                Locale.setDefault(new Locale("cs", "CZ"));
                Module m = mgr.createFixed(mani, null, l);
                // Note Unicode values in the bundle.
                assertEquals("cs_CZ display name", "Modul s lokalizovan\u00FDm manifestem", m.getDisplayName());
                assertEquals("cs_CZ bundle main attr", "v\u00FDznam #1", m.getLocalizedAttribute("some-other-key"));
                assertEquals("cs_CZ bundle sub attr", "v\u00FDznam #2", m.getLocalizedAttribute("locmani/something.txt/other-key"));
                // These are not translated, see that they fall back to "default" locale:
                assertEquals("cs_CZ bundle main attr untrans", "value #7", m.getLocalizedAttribute("other-untrans"));
                assertEquals("cs_CZ bundle sub attr untrans", "value #8", m.getLocalizedAttribute("locmani/something.txt/other-untrans"));
                // The manifest cannot hold non-ASCII characters.
                assertEquals("cs_CZ manifest main attr", "vyznam #3", m.getLocalizedAttribute("some-key"));
                assertEquals("cs_CZ manifest sub attr", "vyznam #4", m.getLocalizedAttribute("locmani/something.txt/key"));
                // Also not translated:
                assertEquals("cs_CZ manifest main attr untrans", "value #5", m.getLocalizedAttribute("untrans"));
                assertEquals("cs_CZ manifest sub attr untrans", "value #6", m.getLocalizedAttribute("locmani/something.txt/untrans"));
            } finally {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        } finally {
            Locale.setDefault(starting);
        }
    }

    // #9273: test that modules/patches/<<code-name-dashes>>/*.jar function as patches
    public void testModulePatches() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m = mgr.create(new File(jars, "patchable.jar"), null, false, false, false);
            mgr.enable(m);
            Class<?> c = m.getClassLoader().loadClass("pkg.subpkg.A");
            Field f = c.getField("val");
            Object o = c.getDeclaredConstructor().newInstance();
            assertEquals(25, f.getInt(o));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testSimpleProvReq() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "req-foo.jar"), null, false, false, false);
            assertEquals(Collections.singletonList("foo"), assertCnb(m1));
            assertEquals(Collections.EMPTY_LIST, assertCnb(m2));
            assertEquals(Collections.EMPTY_SET, m1.getDependencies());
            assertEquals(Dependency.create(Dependency.TYPE_REQUIRES, "foo"), m2.getDependencies());
            Map<String,Module> modulesByName = new HashMap<String,Module>();
            modulesByName.put(m1.getCodeNameBase(), m1);
            modulesByName.put(m2.getCodeNameBase(), m2);
            Map<String,Set<Module>> providersOf = new HashMap<String,Set<Module>>();
            providersOf.put("foo", Collections.singleton(m1));
            List<Module> m1m2 = Arrays.asList(m1, m2);
            List<Module> m2m1 = Arrays.asList(m2, m1);
            Map<Module,List<Module>> deps = Util.moduleDependencies(m1m2, modulesByName, providersOf);
            assertNull(deps.get(m1));
            assertEquals(Collections.singletonList(m1), deps.get(m2));
            assertEquals(m2m1, Utilities.topologicalSort(m1m2, deps));
            assertEquals(m2m1, Utilities.topologicalSort(m2m1, deps));
            Set<Module> m1PlusM2 = new HashSet<Module>();
            m1PlusM2.add(m1);
            m1PlusM2.add(m2);
            List<Module> toEnable = mgr.simulateEnable(m1PlusM2);
            assertEquals("correct result of simulateEnable", Arrays.asList(m1, m2), toEnable);
            toEnable = mgr.simulateEnable(Collections.singleton(m2));
            assertEquals("correct result of simulateEnable #2", Arrays.asList(m1, m2), toEnable);
            mgr.enable(m1PlusM2);
            assertEquals(Arrays.asList(
                "prepare",
                "prepare",
                "load"
            ), installer.actions);
            assertEquals(Arrays.asList(
                m1,
                m2,
                Arrays.asList(m1, m2)
            ), installer.args);
            Class<?> testclazz = Class.forName("org.prov_foo.Clazz", true, m1.getClassLoader());
            try {
                Class.forName("org.prov_foo.Clazz", true, m2.getClassLoader());
                fail("Should not be able to access classes due to prov-req deps only");
            } catch (ClassNotFoundException cnfe) {
                // OK, good.
            }
            installer.clear();
            List<Module> toDisable = mgr.simulateDisable(Collections.singleton(m1));
            assertEquals("correct result of simulateDisable", Arrays.asList(m2, m1), toDisable);
            toDisable = mgr.simulateDisable(m1PlusM2);
            assertEquals("correct result of simulateDisable #2", Arrays.asList(m2, m1), toDisable);
            mgr.disable(m1PlusM2);
            assertFalse(m1.isEnabled());
            assertFalse(m2.isEnabled());
            assertEquals(Arrays.asList(
                "unload",
                "dispose",
                "dispose"
            ), installer.actions);
            assertEquals(Arrays.asList(
                Arrays.asList(m2, m1),
                m2,
                m1
            ), installer.args);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testProvReqAllowsDisable() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "recommends-foo.jar"), null, false, false, false);
            
            Set<Module> m1PlusM2 = new HashSet<Module>();
            m1PlusM2.add(m1);
            m1PlusM2.add(m2);
            mgr.enable(m1PlusM2);
            
            assertTrue("m1 enabled", m1.isEnabled());
            assertTrue("m2 enabled", m2.isEnabled());
            
            mgr.disable(m1);
            
            assertFalse("m1 disabled", m1.isEnabled());
            assertTrue("m2 remains enabled", m2.isEnabled());
            
            mgr.disable(m2);
            assertFalse(m1.isEnabled());
            assertFalse(m2.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testProvReqCycles() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo-req-bar.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "prov-bar-req-foo.jar"), null, false, false, false);
            assertEquals("m1 cannot be installed because of m2",
                Dependency.create(Dependency.TYPE_REQUIRES, "bar"),
                m1.getProblems());
            assertEquals("m2 cannot be installed because of m1",
                Dependency.create(Dependency.TYPE_REQUIRES, "foo"),
                m2.getProblems());
            assertEquals("neither m1 nor m2 can be installed",
                Collections.EMPTY_LIST,
                    mgr.simulateEnable(new HashSet<Module>(Arrays.asList(m1, m2))));
            mgr.delete(m2);
            Module m3 = mgr.create(new File(jars, "prov-bar-dep-cyclic.jar"), null, false, false, false);
            assertEquals("m1 cannot be installed because of m3",
                Dependency.create(Dependency.TYPE_REQUIRES, "bar"),
                m1.getProblems());
            assertEquals("m3 cannot be installed because of m1",
                Dependency.create(Dependency.TYPE_MODULE, "prov_foo_req_bar"),
                m3.getProblems());
            assertEquals("neither m1 nor m3 can be installed",
                Collections.EMPTY_LIST,
                    mgr.simulateEnable(new HashSet<Module>(Arrays.asList(m1, m3))));
            m2 = mgr.create(new File(jars, "prov-bar-req-foo.jar"), null, false, false, false);
            assertEquals("m2 cannot be installed because of m1",
                Dependency.create(Dependency.TYPE_REQUIRES, "foo"),
                m2.getProblems());
            Module m4 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            assertEquals("m2 is OK with m4 here",
                Collections.EMPTY_SET,
                m2.getProblems());
            mgr.delete(m1); // to prevent random failures; see comment in MM.sE
            assertEquals("m2 and m4 can be enabled together",
                Arrays.asList(m4, m2),
                mgr.simulateEnable(Collections.singleton(m2)));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testMultipleProvs() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "prov-foo-bar.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "req-foo.jar"), null, false, false, false);
            Set<Module> m123 = new HashSet<Module>(Arrays.asList(m1, m2, m3));
            List<Module> toEnable = mgr.simulateEnable(Collections.singleton(m3));
            // Note order of first two items in toEnable is indeterminate.
            assertEquals("From start, turn on all providers", m123, new HashSet<Module>(toEnable));
            assertEquals("m3 last", m3, toEnable.get(2));
            assertEquals("Could request them all together too", m123, new HashSet<Module>(mgr.simulateEnable(m123)));
            List<Module> m13 = Arrays.asList(m1, m3);
            assertEquals("Or just m1 + m3", m13, mgr.simulateEnable(new HashSet<Module>(m13)));
            List<Module> m23 = Arrays.asList(m2, m3);
            assertEquals("Or just m2 + m3", m23, mgr.simulateEnable(new HashSet<Module>(m23)));
            mgr.enable(m123);
            assertTrue(m1.isEnabled());
            assertTrue(m2.isEnabled());
            assertTrue(m3.isEnabled());
            assertEquals("Can turn off one provider",
                Collections.singletonList(m1),
                mgr.simulateDisable(Collections.singleton(m1)));
            Set<Module> m12 = new HashSet<Module>(Arrays.asList(m1, m2));
            assertEquals("Can't turn off both providers",
                m123,
                new HashSet<Module>(mgr.simulateDisable(m12)));
            mgr.disable(m1);
            assertFalse(m1.isEnabled());
            assertTrue(m2.isEnabled());
            assertTrue(m3.isEnabled());
            List<Module> m32 = Arrays.asList(m3, m2);
            assertEquals("Can't turn off last provider",
                m32,
                mgr.simulateDisable(Collections.singleton(m2)));
            mgr.disable(new HashSet<Module>(m32));
            assertFalse(m1.isEnabled());
            assertFalse(m2.isEnabled());
            assertFalse(m3.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testProvReqUnsatisfiable() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Module m1 = createModule(mgr, "OpenIDE-Module: m1\nOpenIDE-Module-Needs: tok\n");
        Module m2 = createModule(mgr, "OpenIDE-Module: m2\nOpenIDE-Module-Module-Dependencies: m1\n");
        assertEquals(Collections.emptyList(), mgr.simulateEnable(Collections.singleton(m2)));
        Module m3 = createModule(mgr, "OpenIDE-Module: m3\nOpenIDE-Module-Provides: tok\n");
        assertEquals(new HashSet<Module>(Arrays.asList(m1, m2, m3)), new HashSet<Module>(mgr.simulateEnable(Collections.singleton(m2))));
        mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        m1 = createModule(mgr, "OpenIDE-Module: m1\nOpenIDE-Module-Requires: tok\n");
        m2 = createModule(mgr, "OpenIDE-Module: m2\nOpenIDE-Module-Module-Dependencies: m1\nOpenIDE-Module-Provides: tok\n");
        assertEquals(Collections.emptyList(), mgr.simulateEnable(Collections.singleton(m2)));
    }
    
    public void testSimpleProvNeeds() throws Exception {
        doSimpleProvNeeds(false, false);
    }
    
    public void testSimpleProvNeedsReversed() throws Exception {
        doSimpleProvNeeds(true, false);
    }

    public void testSimpleSatisfiedProvRecommends() throws Exception {
        doSimpleProvNeeds(false, true);
    }
    
    public void testSimpleSatisfiedProvRecommendsReversed() throws Exception {
        doSimpleProvNeeds(true, true);
    }
    
    private void doSimpleProvNeeds(boolean reverseOrder, boolean recommends) throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo-depends-needs_foo.jar"), null, false, false, false);
            Module m2;
            if (recommends) {
                m2 = mgr.create(new File(jars, "recommends-foo.jar"), null, false, false, false);
            } else {
                m2 = mgr.create(new File(jars, "needs-foo.jar"), null, false, false, false);
            }
            assertEquals(Collections.singletonList("foo"), assertCnb(m1));
            assertEquals(Collections.EMPTY_LIST, assertCnb(m2));
            assertEquals(1, m1.getDependencies().size());
            int type = recommends ? Dependency.TYPE_RECOMMENDS : Dependency.TYPE_NEEDS;
            assertEquals(Dependency.create(type, "foo"), m2.getDependencies());
            Map<String,Module> modulesByName = new HashMap<String,Module>();
            modulesByName.put(m1.getCodeNameBase(), m1);
            modulesByName.put(m2.getCodeNameBase(), m2);
            Map<String,Set<Module>> providersOf = new HashMap<String,Set<Module>>();
            providersOf.put("foo", Collections.singleton(m1));
            List<Module> m1m2 = Arrays.asList(m1, m2);
            List<Module> m2m1 = Arrays.asList(m2, m1);
            Map<Module,List<Module>> deps = Util.moduleDependencies(m1m2, modulesByName, providersOf);
            assertEquals(Collections.singletonList(m2), deps.get(m1));
/*            assertEquals(Collections.singletonList(m1), deps.get(m2));
            
            try {
                Utilities.topologicalSort(m1m2, deps);
            } catch (TopologicalSortException ex) {
                Set[] arr = ex.unsortableSets();
                assertEquals("One unsortable set", 1, arr.length);
                assertEquals("It contains two elements", 2, arr[0].size());
                assertTrue("m1 is there", arr[0].contains(m1));
                assertTrue("m2 is there", arr[0].contains(m2));
            }*/
            Set<Module> m1PlusM2 = new LinkedHashSet<Module>();
            if (reverseOrder) {
                m1PlusM2.add(m2);
                m1PlusM2.add(m1);
            } else {
                m1PlusM2.add(m1);
                m1PlusM2.add(m2);
            }
            List<Module> toEnable = mgr.simulateEnable(m1PlusM2);
            assertEquals("correct result of simulateEnable", Arrays.asList(m2, m1), toEnable);
            toEnable = mgr.simulateEnable(Collections.singleton(m1));
            assertEquals("correct result of simulateEnable #2", Arrays.asList(m2, m1), toEnable);
            toEnable = mgr.simulateEnable(Collections.singleton(m2));
            assertEquals("correct result of simulateEnable #3", Arrays.asList(m2, m1), toEnable);
            mgr.enable(m1PlusM2);
            assertEquals(Arrays.asList(
                "prepare",
                "prepare",
                "load"
            ), installer.actions);
            assertEquals(Arrays.asList(
                m2,
                m1,
                Arrays.asList(m2, m1)
            ), installer.args);
            Class<?> testclazz = Class.forName("org.prov_foo.Clazz", true, m1.getClassLoader());
            try {
                Class.forName("org.prov_foo.Clazz", true, m2.getClassLoader());
                fail("Should not be able to access classes due to prov-req deps only");
            } catch (ClassNotFoundException cnfe) {
                // OK, good.
            }
            installer.clear();
            List<Module> toDisable = mgr.simulateDisable(Collections.singleton(m1));
            if (!recommends) {
                assertEquals("correct result of simulateDisable", Arrays.asList(m1, m2), toDisable);
                toDisable = mgr.simulateDisable(m1PlusM2);
                assertEquals("correct result of simulateDisable #2", Arrays.asList(m1, m2), toDisable);
                mgr.disable(m1PlusM2);
                assertFalse(m1.isEnabled());
                assertFalse(m2.isEnabled());
                assertEquals(Arrays.asList(
                    "unload",
                    "dispose",
                    "dispose"
                ), installer.actions);
                assertEquals(Arrays.asList(
                    Arrays.asList(m1, m2),
                    m1,
                    m2
                ), installer.args);
            } else {
                assertEquals("correct result of simulateDisable", Collections.singletonList(m1 ), toDisable);
                toDisable = mgr.simulateDisable(m1PlusM2);
                assertEquals("correct result of simulateDisable #2", Arrays.asList(m1, m2), toDisable);
                mgr.disable(m1);
                assertFalse(m1.isEnabled());
                assertTrue(m2.isEnabled());
                mgr.disable(m2);
                assertFalse(m2.isEnabled());
                assertEquals(Arrays.asList(
                    "unload",
                    "dispose",
                    "unload",
                    "dispose"
                ), installer.actions);
                assertEquals(Arrays.asList(
                    Collections.singletonList(m1),
                    m1,
                    Collections.singletonList(m2),
                    m2
                ), installer.args);
            }
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testProvNeedsWithEager() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            // m1 is regular (disabled) module, providing foo
            Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            // m2 is autoload module, which needs foo
            Module m2 = mgr.create(new File(jars, "needs-foo.jar"), null, false, true, false);
            // m3 is eager module, which depends on m2
            Module m3 = mgr.create(new File(jars, "dep-on-needs_foo-simple.jar"), null, false, false, true);
            
            mgr.enable(Collections.emptySet());
            // since m1 is disabled, eager module m3 should be still disabled
            assertFalse("Incorrectly enabled m1",m1.isEnabled());
            assertFalse("Incorrectly enabled m2",m2.isEnabled());
            assertFalse("Incorrectly enabled m3",m3.isEnabled());
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testComplexProvNeeds() throws Exception {
        doComplexProvNeeds(false, false, false);
    }
    
    public void testComplexProvNeedsReversed() throws Exception {
        doComplexProvNeeds(true, false, false);
    }

    public void testComplexSatisfiedProvRecommends() throws Exception {
        doComplexProvNeeds(false, true, false);
    }
    
    public void testComplexSatisfiedProvRecommendsReversed() throws Exception {
        doComplexProvNeeds(true, true, true);
    }

    public void testComplexProvNeeds2() throws Exception {
        doComplexProvNeeds(false, false, true);
    }
    
    public void testComplexProvNeedsReversed2() throws Exception {
        doComplexProvNeeds(true, false, true);
    }

    public void testComplexSatisfiedProvRecommends2() throws Exception {
        doComplexProvNeeds(false, true, true);
    }
    
    public void testComplexSatisfiedProvRecommendsReversed2() throws Exception {
        doComplexProvNeeds(true, true, true);
    }
    
    private void doComplexProvNeeds(boolean reverseOrder, boolean recommends, boolean sndRec) throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo-depends-needs_foo.jar"), null, false, true, false);
            Module m2;
            if (recommends) {
                m2 = mgr.create(new File(jars, "recommends-foo.jar"), null, false, false, false);
            } else {
                m2 = mgr.create(new File(jars, "needs-foo.jar"), null, false, false, false);
            }
            Module m3 = null;
            if (sndRec) {
                String manifest = "Manifest-Version: 1.0\n" +
"OpenIDE-Module: snd.needs_foo\n" +
"OpenIDE-Module-Name: 2nd Needs foo\n" +
"OpenIDE-Module-Needs: foo\n";
                m3 = mgr.create(copyJar(m2.getJarFile(), manifest), null, false, false, false);
            } else {
                String manifest = "Manifest-Version: 1.0\n" +
"OpenIDE-Module: snd.needs_foo\n" +
"OpenIDE-Module-Name: 2nd Needs foo\n" +
"OpenIDE-Module-Recommends: foo\n";
                m3 = mgr.create(copyJar(m2.getJarFile(), manifest), null, false, false, false);
            }
            assertEquals(Collections.singletonList("foo"), assertCnb(m1));
            assertEquals(Collections.EMPTY_LIST, assertCnb(m2));
            assertEquals(1, m1.getDependencies().size());
            int type = recommends ? Dependency.TYPE_RECOMMENDS : Dependency.TYPE_NEEDS;
            assertEquals(Dependency.create(type, "foo"), m2.getDependencies());
            Map<String,Module> modulesByName = new HashMap<String,Module>();
            modulesByName.put(m1.getCodeNameBase(), m1);
            modulesByName.put(m2.getCodeNameBase(), m2);
            Map<String,Set<Module>> providersOf = new HashMap<String,Set<Module>>();
            providersOf.put("foo", Collections.singleton(m1));
            List<Module> m1m2 = Arrays.asList(m1, m2);
            List<Module> m2m1 = Arrays.asList(m2, m1);
            Map<Module,List<Module>> deps = Util.moduleDependencies(m1m2, modulesByName, providersOf);
            assertEquals(Collections.singletonList(m2), deps.get(m1));
            List<Module> toEnable = mgr.simulateEnable(Collections.singleton(m2));
            assertEquals("correct result of simulateEnable", Arrays.asList(m2, m1), toEnable);

            mgr.enable(m2);
            assertEquals(Arrays.asList(
                "prepare",
                "prepare",
                "load"
            ), installer.actions);
            assertEquals(Arrays.asList(
                m2,
                m1,
                Arrays.asList(m2, m1)
            ), installer.args);
            Class<?> testclazz = Class.forName("org.prov_foo.Clazz", true, m1.getClassLoader());
            try {
                Class.forName("org.prov_foo.Clazz", true, m2.getClassLoader());
                fail("Should not be able to access classes due to prov-req deps only");
            } catch (ClassNotFoundException cnfe) {
                // OK, good.
            }
            
            mgr.enable(m3);
            assertTrue("m3 enabled1", m3.isEnabled());
            
            installer.clear();
            List<Module> toDisable = mgr.simulateDisable(Collections.singleton(m3));
            if (!recommends) {
                mgr.disable(m3);
                assertFalse("M3 enabled", m3.isEnabled());
                assertTrue("Provider enabled", m1.isEnabled());
                assertTrue(m2.isEnabled());
                assertEquals(Arrays.asList(
                    "unload",
                    "dispose"
                ), installer.actions);
                assertEquals(Arrays.asList(
                    Collections.singletonList( m3 ),
                    m3
                ), installer.args);
            } else {
                mgr.disable(m3);
                assertFalse(m3.isEnabled());
                assertTrue(m2.isEnabled());
                assertTrue(m1.isEnabled());
                assertEquals(Arrays.asList(
                    "unload",
                    "dispose"
                ), installer.actions);
                assertEquals(Arrays.asList(
                    Collections.singletonList(m3),
                    m3
                ), installer.args);
            }
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testRecommendsWithoutAProvider() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m2 = mgr.create(new File(jars, "recommends-foo.jar"), null, false, false, false);
            assertEquals(Collections.EMPTY_LIST, assertCnb(m2));
            assertEquals(Dependency.create(Dependency.TYPE_RECOMMENDS, "foo"), m2.getDependencies());
            Map<String,Module> modulesByName = new HashMap<String,Module>();
            modulesByName.put(m2.getCodeNameBase(), m2);
            Map<String,Set<Module>> providersOf = new HashMap<String,Set<Module>>();
            List<Module> m2List = Collections.singletonList( m2 );
            Map<Module,List<Module>> deps = Util.moduleDependencies(m2List, modulesByName, providersOf);
            assertEquals(null, deps.get(m2));

            List<Module> toEnable = mgr.simulateEnable(new HashSet<Module>(m2List));
            assertEquals("correct result of simulateEnable", Collections.singletonList(m2), toEnable);
            mgr.enable(new HashSet<Module>(m2List));
            assertEquals(Arrays.asList(
                "prepare",
                "load"
            ), installer.actions);
            assertEquals(Arrays.asList(
                m2,
//                m1,
                Collections.singletonList(m2)
            ), installer.args);
            installer.clear();
            List<Module> toDisable = mgr.simulateDisable(Collections.singleton(m2));
            assertEquals("correct result of simulateDisable", Collections.singletonList(m2), toDisable);
            mgr.disable(m2);
            assertFalse(m2.isEnabled());
            assertEquals(Arrays.asList(
                "unload",
                "dispose"
            ), installer.actions);
            assertEquals(Arrays.asList(
                Collections.singletonList(m2),
//                m1,
                m2
            ), installer.args);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testNeedsWithAProviderWithoutAProvider() throws Exception {
        doRecommendsWithAProviderWithoutAProvider(false);
    }
    
    public void testRecommendsWithAProviderWithoutAProvider() throws Exception {
        doRecommendsWithAProviderWithoutAProvider(true);
    }

    private void doRecommendsWithAProviderWithoutAProvider(boolean recommends) throws Exception {
        // ========= XXX recommends parameter is unused! ===========
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m2 = mgr.create(new File(jars, "recommends-foo.jar"), null, false, false, false);
            assertEquals(Collections.EMPTY_LIST, assertCnb(m2));
            
            Module m1;
            {
                String manifest = "Manifest-Version: 1.0\n" +
"OpenIDE-Module: snd.provides.foo\n" +
"OpenIDE-Module-Name: Provides foo\n" +
"OpenIDE-Module-Provides: foo\n" +
"OpenIDE-Module-Needs: bla\n";
                m1 = mgr.create(copyJar(m2.getJarFile(), manifest), null, false, true, false);
                
            }
            assertEquals(Dependency.create(Dependency.TYPE_RECOMMENDS, "foo"), m2.getDependencies());
            Map<String,Module> modulesByName = new HashMap<String,Module>();
            modulesByName.put(m2.getCodeNameBase(), m2);
            Map<String,Set<Module>> providersOf = new HashMap<String,Set<Module>>();
            List<Module> m2List = Collections.singletonList(m2);
            Map<Module,List<Module>> deps = Util.moduleDependencies(m2List, modulesByName, providersOf);
            assertEquals(null, deps.get(m2));

            List<Module> toEnable = mgr.simulateEnable(new HashSet<Module>(m2List));
            assertEquals("cannot enable while provider of bla is missing", Collections.singletonList(m2), toEnable);


//            try {
//                mgr.enable(new HashSet<Module>(m2List));
//                fail("Shall not allow enablement as 'bar' is missing");
//            } catch (IllegalArgumentException ex) {
//                // this cannot be enabled
//            }
            
            
            Module m3;
            {
                String manifest = "Manifest-Version: 1.0\n" +
"OpenIDE-Module: snd.provides.bar\n" +
"OpenIDE-Module-Name: Provides bar\n" +
"OpenIDE-Module-Provides: bla\n";
                m3 = mgr.create(copyJar(m2.getJarFile(), manifest), null, false, true, false);
            }
            
            Set<Module> allThreeModules = new HashSet<>(Arrays.asList(m1, m3, m2));
            
            toEnable = mgr.simulateEnable(new HashSet<Module>(m2List));
            assertEquals("all 3 need to be enabled", allThreeModules, new HashSet<Module>(toEnable));
            
            mgr.enable(new HashSet<Module>(m2List));
            assertEquals(Arrays.asList(
                "prepare",
                "prepare",
                "prepare",
                "load"
            ), installer.actions);
            installer.clear();
            List<Module> toDisable = mgr.simulateDisable(Collections.singleton(m2));
            assertEquals("correct result of simulateDisable", allThreeModules, new HashSet<Module>(toDisable));
            mgr.disable(m2);
            assertFalse(m2.isEnabled());
            assertEquals(Arrays.asList(
                "unload",
                "dispose",
                "dispose",
                "dispose"
            ), installer.actions);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testMultipleReqs() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "prov-baz.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "req-foo-baz.jar"), null, false, false, false);
            Set<Module> m123 = new HashSet<Module>(Arrays.asList(m1, m2, m3));
            assertEquals(m123, new HashSet<Module>(mgr.simulateEnable(Collections.singleton(m3))));
            mgr.enable(m123);
            assertEquals(Arrays.asList(m3, m1), mgr.simulateDisable(Collections.singleton(m1)));
            assertEquals(Arrays.asList(m3, m2), mgr.simulateDisable(Collections.singleton(m2)));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testEagerReq() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "prov-baz.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "req-foo-baz.jar"), null, false, false, true);
            assertEquals(Collections.singletonList(m1),
                mgr.simulateEnable(Collections.singleton(m1)));
            assertEquals(Collections.singletonList(m2),
                mgr.simulateEnable(Collections.singleton(m2)));
            Set<Module> m12 = new HashSet<Module>(Arrays.asList(m1, m2));
            Set<Module> m123 = new HashSet<Module>(Arrays.asList(m1, m2, m3));
            assertEquals(m123, new HashSet<Module>(mgr.simulateEnable(m12)));
            mgr.enable(m12);
            assertTrue(m3.isEnabled());
            assertEquals(Arrays.asList(m3, m1),
                mgr.simulateDisable(Collections.singleton(m1)));
            assertEquals(Arrays.asList(m3, m2),
                mgr.simulateDisable(Collections.singleton(m2)));
            assertEquals(m123,
                new HashSet<Module>(mgr.simulateDisable(m12)));
            mgr.disable(m12);
            assertFalse(m3.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testAutoloadProv() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, true, false);
            Module m2 = mgr.create(new File(jars, "req-foo.jar"), null, false, false, false);
            assertEquals(Arrays.asList(m1, m2),
                mgr.simulateEnable(Collections.singleton(m2)));
            mgr.enable(m2);
            assertTrue(m1.isEnabled());
            assertEquals(Arrays.asList(m2, m1),
                mgr.simulateDisable(Collections.singleton(m2)));
            mgr.disable(m2);
            assertFalse(m1.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testWeirdRecursion() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            //Module m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "prov-bar-req-foo.jar"), null, false, true, false);
            Module m3 = mgr.create(new File(jars, "prov-foo-bar.jar"), null, false, false, false);
            Module m4 = mgr.create(new File(jars, "prov-foo-req-bar.jar"), null, false, false, true);
            assertEquals("m2 should not be enabled - m4 might ask for it but m3 already has bar",
                new HashSet<Module>(Arrays.asList(m3, m4)),
                new HashSet<Module>(mgr.simulateEnable(Collections.singleton(m3))));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testLackOfOrderSensitivity() throws Exception {
        String[] moduleNames = new String[] {
            "simple-module.jar",
            "depends-on-simple-module.jar",
            "dep-on-dep-on-simple.jar",
            "prov-foo.jar",
            "prov-baz.jar",
            "prov-foo-bar.jar",
            "req-foo.jar",
            "req-foo-baz.jar",
            "prov-bar-req-foo.jar",
            "prov-foo-req-bar.jar",
        };
        // Never make any of the following eager:
        Set<String> noDepsNames = new HashSet<String>(Arrays.asList(
            "simple-module.jar",
            "prov-foo.jar",
            "prov-baz.jar",
            "prov-foo-bar.jar"
        ));
        List<String> freeModules = new ArrayList<String>(Arrays.asList(moduleNames));
        int count = 100; // # of things to do in order
        Random r = new Random(count * 17 + 113);
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            int i = 0;
            while (i < count) {
                Util.err.log(Level.INFO, "testLackOfOrderSensitivity round #{0}", i);
                switch (r.nextInt(11)) {
                case 0:
                case 1:
                case 2:
                    Util.err.info("Add a regular module");
                    if (!freeModules.isEmpty()) {
                        String name = freeModules.remove(r.nextInt(freeModules.size()));
                        mgr.create(new File(jars, name), null, false, false, false);
                        i++;
                    }
                    break;
                case 3:
                    Util.err.info("Add an autoload");
                    if (!freeModules.isEmpty()) {
                        String name = freeModules.remove(r.nextInt(freeModules.size()));
                        mgr.create(new File(jars, name), null, false, true, false);
                        i++;
                    }
                    break;
                case 4:
                    Util.err.info("Add an eager module");
                    if (!freeModules.isEmpty()) {
                        String name = freeModules.remove(r.nextInt(freeModules.size()));
                        if (!noDepsNames.contains(name)) {
                            Module m = mgr.create(new File(jars, name), null, false, false, true);
                            i++;
                        }
                    }
                    break;
                case 5:
                case 6:
                    Util.err.info("Remove a disabled module");
                    List<Module> disabled = new ArrayList<Module>(moduleNames.length);
                    for (Module m : mgr.getModules()) {
                        if (!m.isEnabled()) {
                            disabled.add(m);
                        }
                    }
                    if (!disabled.isEmpty()) {
                        Module m = disabled.get(r.nextInt(disabled.size()));
                        mgr.delete(m);
                        freeModules.add(m.getJarFile().getName());
                        i++;
                    }
                    break;
                case 7:
                case 8:
                    Util.err.info("Enable some set of modules");
                    List<Module> candidates = new ArrayList<Module>(moduleNames.length);
                    for (Module m : mgr.getModules()) {
                        if (!m.isEnabled() && !m.isAutoload() && !m.isEager() && r.nextBoolean()) {
                            candidates.add(m);
                        }
                    }
                    if (!candidates.isEmpty()) {
                        Collections.shuffle(candidates, r);
                        Set<Module> candidatesSet = new LinkedHashSet<Module>(candidates);
                        assertEquals("OrderPreservingSet works", candidates, new ArrayList<Module>(candidatesSet));
                        //dumpState(mgr);
                        //System.err.println("will try to enable: " + candidates);
                        List<Module> toEnable1 = mgr.simulateEnable(candidatesSet);
                        //System.err.println("Enabling  " + candidates + " ->\n          " + toEnable1);
                        Collections.shuffle(candidates, r);
                        List<Module> toEnable2 = mgr.simulateEnable(new LinkedHashSet<Module>(candidates));
                        Set<Module> s1 = new HashSet<Module>(toEnable1);
                        Set<Module> s2 = new HashSet<Module>(toEnable2);
                        assertEquals("Order preserved", s1, s2);
                        Iterator<Module> it = s1.iterator();
                        while (it.hasNext()) {
                            Module m = it.next();
                            if (m.isAutoload() || m.isEager()) {
                                it.remove();
                            }
                        }
                        mgr.enable(s1);
                        i++;
                    }
                    break;
                case 9:
                case 10:
                    Util.err.info("Disable some set of modules");
                    candidates = new ArrayList<Module>(moduleNames.length);
                    for (Module m : mgr.getModules()) {
                        if (m.isEnabled() && !m.isAutoload() && !m.isEager() && r.nextBoolean()) {
                            candidates.add(m);
                        }
                    }
                    if (!candidates.isEmpty()) {
                        Collections.shuffle(candidates, r);
                        //dumpState(mgr);
                        List<Module> toDisable1 = mgr.simulateDisable(new LinkedHashSet<Module>(candidates));
                        //System.err.println("Disabling " + candidates + " ->\n          " + toDisable1);
                        Collections.shuffle(candidates, r);
                        //System.err.println("candidates #2: " + candidates);
                        List<Module> toDisable2 = mgr.simulateDisable(new LinkedHashSet<Module>(candidates));
                        Set<Module> s1 = new HashSet<Module>(toDisable1);
                        Set<Module> s2 = new HashSet<Module>(toDisable2);
                        assertEquals("Order preserved", s1, s2);
                        Iterator<Module> it = s1.iterator();
                        while (it.hasNext()) {
                            Module m = it.next();
                            if (m.isAutoload() || m.isEager()) {
                                it.remove();
                            }
                        }
                        mgr.disable(s1);
                        i++;
                    }
                    break;
                default:
                    throw new IllegalStateException();
                }
            }
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    /*
    private static void dumpState(ModuleManager mgr) {
        SortedSet modules = new TreeSet(Util.displayNameComparator());
        modules.addAll(mgr.getModules());
        System.err.print("State:");
        Iterator it = modules.iterator();
        while (it.hasNext()) {
            Module m = (Module)it.next();
            System.err.print(" " + m.getCodeNameBase());
            if (m.isAutoload()) {
                System.err.print(" (autoload, ");
            } else if (m.isEager()) {
                System.err.print(" (eager, ");
            } else {
                System.err.print(" (normal, ");
            }
            if (m.isEnabled()) {
                System.err.print("on)");
            } else {
                System.err.print("off)");
            }
        }
        System.err.println();
    }
     */

    public void testRelVersRanges() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module base = mgr.create(new File(jars, "rel-ver-2.jar"), null, false, false, false);
            String[] depNames = new String[] {
                "dep-on-relvertest-1.jar", // 0
                "dep-on-relvertest-1-2.jar", // 1
                "dep-on-relvertest-2.jar", // 2
                "dep-on-relvertest-2-3.jar", // 3
                "dep-on-relvertest-2-3-late.jar", // 4
                "dep-on-relvertest-2-impl.jar", // 5
                "dep-on-relvertest-2-impl-wrong.jar", // 6
                "dep-on-relvertest-2-late.jar", // 7
                "dep-on-relvertest-3-4.jar", // 8
                "dep-on-relvertest-some.jar", // 9
            };
            Module[] deps = new Module[depNames.length];
            for (int i = 0; i < deps.length; i++) {
                deps[i] = mgr.create(new File(jars, depNames[i]), null, false, false, false);
            }
            Set<Module> all = new HashSet<Module>();
            all.add(base);
            all.addAll(Arrays.asList(deps));
            Set<Module> ok = new HashSet<Module>();
            ok.add(base);
            // 0 - too early
            ok.add(deps[1]);
            ok.add(deps[2]);
            ok.add(deps[3]);
            // 4 - too late
            ok.add(deps[5]);
            // 6 - wrong impl version
            // 7 - too late
            // 8 - too late
            // 9 - must give some rel vers, else ~ -1
            assertEquals(ok, new HashSet<Module>(mgr.simulateEnable(all)));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testDisableAgainstRelVersRange() throws Exception {
        // #41449: org.openidex.util/3 disabled improperly when disable module w/ dep on org.openide.util/2-3
        // related to testDisableWithAutoloadMajorRange but this probably has the test backwards
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module base = mgr.create(new File(jars, "rel-ver-2.jar"), null, false, true, false);
            Module dep1 = mgr.create(new File(jars, "dep-on-relvertest-2.jar"), null, false, false, false);
            Module dep2 = mgr.create(new File(jars, "dep-on-relvertest-1-2-nospec.jar"), null, false, false, false);
            Set<Module> all = new HashSet<Module>();
            all.add(dep1);
            all.add(dep2);
            mgr.enable(all);
            all.add(base);
            assertEquals("turn on autoload w/ both deps OK", all, mgr.getEnabledModules());
            Set<Module> dep2only = Collections.singleton(dep2);
            assertEquals("intend to disable only dep2", dep2only, new HashSet<Module>(mgr.simulateDisable(dep2only)));
            mgr.disable(dep2only);
            all.remove(dep2);
            assertEquals("removed just dep2, not autoload used by dep1", all, mgr.getEnabledModules());
            mgr.disable(Collections.singleton(dep1));
            assertEquals("now all gone", Collections.EMPTY_SET, mgr.getEnabledModules());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    /** Test #21114: after deleting a module, its JARs are released.
     * Would probably always pass on Unix, but on Windows it matters.
     */
    @RandomlyFails // NB-Core-Build #2081 in ModuleFactoryTest
    public void testModuleDeletion() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);

        File jar = new File(getWorkDir(), "copy-of-simple-module.jar");
        copy(new File(jars, "simple-module.jar"), jar);

        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m = mgr.create(jar, null, false, false, false);
            mgr.enable(m);
            Class<?> c = m.getClassLoader().loadClass("org.foo.Something");
            URL u = m.getClassLoader().getResource("org/foo/Something.class");
            URLConnection uc = u.openConnection();
            assertNotNull("connetion", uc);
            assertTrue("using JarURLConnection or JarClassLoader's one: " + uc, uc.getClass().getName().indexOf("JarClassLoader") >= 0);
            uc.connect();
            mgr.disable(m);
            mgr.delete(m);

            WeakReference<Class<?>> refC = new WeakReference<>(c);
            WeakReference<URL> refU = new WeakReference<>(u);
            WeakReference<URLConnection> refUC = new WeakReference<>(uc);

            c = null;
            u = null;
            uc = null;

            assertGC ("Module class can go away", refC);
            assertGC ("Module url", refU);
            assertGC ("Module connection ", refUC);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }

        assertTrue("could delete JAR file", jar.delete());
    }

    /** Test #20663: the context classloader is set on all threads
     * according to the system classloader.
     */
    public void testContextClassLoader() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        final ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        // Make sure created threads do not die.
        final Object sleepForever = "sleepForever";
        try {
            final Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            ClassLoader l1 = mgr.getClassLoader();
            assertEquals(l1, Thread.currentThread().getContextClassLoader());
            mgr.enable(m1);
            ClassLoader l2 = mgr.getClassLoader();
            assertTrue(l1 == l2);
            assertEquals(l2, Thread.currentThread().getContextClassLoader());
            mgr.enable(m2);
            ClassLoader l3 = mgr.getClassLoader();
            assertTrue(l1 == l3);
            assertEquals(l3, Thread.currentThread().getContextClassLoader());
            mgr.disable(m2);
            ClassLoader l4 = mgr.getClassLoader();
            assertTrue(l1 != l4);
            assertEquals(l4, Thread.currentThread().getContextClassLoader());
            final Thread[] t23 = new Thread[2];
            final ClassLoader[] lx = new ClassLoader[] {new URLClassLoader(new URL[0])};
            // Make sure t1 runs to completion, though.
            final Object finishT1 = "finishT1";
            Thread t1 = new Thread("custom thread #1") {
                public @Override void run() {
                    synchronized (finishT1) {
                        t23[0] = new Thread("custom thread #2") {
                            public @Override void run() {
                                synchronized (sleepForever) {
                                    try {
                                        sleepForever.wait();
                                    } catch (InterruptedException ie) {
                                        throw new Error(ie.toString());
                                    }
                                }
                            }
                        };
                        t23[0].start();
                        Thread.currentThread().setContextClassLoader(lx[0]);
                        mgr.disable(m1);
                        t23[1] = new Thread("custom thread #3") {
                            public @Override void run() {
                                synchronized (sleepForever) {
                                    try {
                                        sleepForever.wait();
                                    } catch (InterruptedException ie) {
                                        throw new Error(ie.toString());
                                    }
                                }
                            }
                        };
                        t23[1].start();
                        finishT1.notify();
                    }
                    synchronized (sleepForever) {
                        try {
                            sleepForever.wait();
                        } catch (InterruptedException ie) {
                            throw new Error(ie.toString());
                        }
                    }
                }
            };
            t1.start();
            synchronized (finishT1) {
                if (t23[1] == null) {
                    finishT1.wait();
                    assertNotNull(t23[1]);
                }
            }
            assertFalse(m1.isEnabled());
            ClassLoader l5 = mgr.getClassLoader();
            assertTrue(l1 != l5);
            assertTrue(l4 != l5);
            assertEquals(l5, Thread.currentThread().getContextClassLoader());
            // It had a special classloader when we changed modules.
            assertTrue(t1.isAlive());
            assertEquals(lx[0], t1.getContextClassLoader());
            // It was created before the special classloader.
            assertTrue(t23[0].isAlive());
            assertEquals(l5, t23[0].getContextClassLoader());
            // It was created after and should have inherited the special classloader.
            assertTrue(t23[1].isAlive());
            assertEquals(lx[0], t23[1].getContextClassLoader());
            mgr.enable(m1);
            mgr.disable(m1);
            ClassLoader l6 = mgr.getClassLoader();
            assertTrue(l1 != l6);
            assertTrue(l4 != l6);
            assertTrue(l5 != l6);
            assertEquals(l6, Thread.currentThread().getContextClassLoader());
            assertEquals(lx[0], t1.getContextClassLoader());
            assertEquals(l6, t23[0].getContextClassLoader());
            assertEquals(lx[0], t23[1].getContextClassLoader());
        } finally {
            synchronized (sleepForever) {
                sleepForever.notifyAll();
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    /** Make sure classloaders do not overlap.
     * @see "#24996"
     */
    public void testDependOnTwoFixedModules() throws Exception {
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
    }

    /** Test exporting selected packages to clients.
     * @see "#19621"
     */
    public void testPackageExports() throws Exception {
        ModuleManager mgr = new ModuleManager(new MockModuleInstaller(), new MockEvents());
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "api-mod-export-all.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "uses-api-simple-dep.jar"), null, false, false, false);
            mgr.enable(m1);
            mgr.enable(m2);
            m2.getClassLoader().loadClass("usesapi.UsesPublicClass").getDeclaredConstructor().newInstance();
            m2.getClassLoader().loadClass("usesapi.UsesImplClass").getDeclaredConstructor().newInstance();
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            m1 = mgr.create(new File(jars, "api-mod-export-none.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "uses-api-simple-dep.jar"), null, false, false, false);
            mgr.enable(m1);
            mgr.enable(m2);
            try {
                m2.getClassLoader().loadClass("usesapi.UsesPublicClass").getDeclaredConstructor().newInstance();
                fail();
            } catch (InvocationTargetException e) {
                assertTrue(e.getCause() instanceof NoClassDefFoundError);
            }
            try {
                m2.getClassLoader().loadClass("usesapi.UsesImplClass").getDeclaredConstructor().newInstance();
                fail();
            } catch (InvocationTargetException e) {
                assertTrue(e.getCause() instanceof NoClassDefFoundError);
            }
            assertNotNull(mgr.getClassLoader().getResource("usesapi/UsesImplClass.class"));
            assertNotNull(mgr.getClassLoader().getResource("org/netbeans/api/foo/PublicClass.class"));
            assertNotNull(mgr.getClassLoader().getResource("org/netbeans/modules/foo/ImplClass.class"));
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            m1 = mgr.create(new File(jars, "api-mod-export-none.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "uses-api-spec-dep.jar"), null, false, false, false);
            mgr.enable(m1);
            mgr.enable(m2);
            try {
                m2.getClassLoader().loadClass("usesapi.UsesPublicClass").getDeclaredConstructor().newInstance();
                fail();
            } catch (InvocationTargetException e) {
                assertTrue(e.getCause() instanceof NoClassDefFoundError);
            }
            try {
                m2.getClassLoader().loadClass("usesapi.UsesImplClass").getDeclaredConstructor().newInstance();
                fail();
            } catch (InvocationTargetException e) {
                assertTrue(e.getCause() instanceof NoClassDefFoundError);
            }
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            m1 = mgr.create(new File(jars, "api-mod-export-none.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "uses-api-impl-dep.jar"), null, false, false, false);
            mgr.enable(m1);
            mgr.enable(m2);
            m2.getClassLoader().loadClass("usesapi.UsesPublicClass").getDeclaredConstructor().newInstance();
            m2.getClassLoader().loadClass("usesapi.UsesImplClass").getDeclaredConstructor().newInstance();
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            m1 = mgr.create(new File(jars, "api-mod-export-api.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "uses-api-simple-dep.jar"), null, false, false, false);
            assertEquals("api-mod-export-api.jar can be enabled", Collections.EMPTY_SET, m1.getProblems());
            mgr.enable(m1);
            assertEquals("uses-api-simple-dep.jar can be enabled", Collections.EMPTY_SET, m2.getProblems());
            mgr.enable(m2);
            m2.getClassLoader().loadClass("usesapi.UsesPublicClass").getDeclaredConstructor().newInstance();
            try {
                m2.getClassLoader().loadClass("usesapi.UsesImplClass").getDeclaredConstructor().newInstance();
                fail();
            } catch (InvocationTargetException e) {
                assertTrue(e.getCause() instanceof NoClassDefFoundError);
            }
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            m1 = mgr.create(new File(jars, "api-mod-export-api.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "uses-api-spec-dep.jar"), null, false, false, false);
            mgr.enable(m1);
            mgr.enable(m2);
            m2.getClassLoader().loadClass("usesapi.UsesPublicClass").getDeclaredConstructor().newInstance();
            try {
                m2.getClassLoader().loadClass("usesapi.UsesImplClass").getDeclaredConstructor().newInstance();
                fail();
            } catch (InvocationTargetException e) {
                assertTrue(e.getCause() instanceof NoClassDefFoundError);
            }
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            m1 = mgr.create(new File(jars, "api-mod-export-api.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "uses-api-impl-dep.jar"), null, false, false, false);
            mgr.enable(m1);
            mgr.enable(m2);
            m2.getClassLoader().loadClass("usesapi.UsesPublicClass").getDeclaredConstructor().newInstance();
            m2.getClassLoader().loadClass("usesapi.UsesImplClass").getDeclaredConstructor().newInstance();
            mgr.disable(m2);
            mgr.disable(m1);
            mgr.delete(m2);
            mgr.delete(m1);
            // XXX test use of .** to export packages recursively
            // XXX test misparsing of malformed export lines
            // XXX test exporting of >1 package from one module (comma-separated)
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    /** Test that package exports, and package/classloader use generally, is not
     * transitively exported from modules - that you need to declare an explicit
     * module dependency on every module from which you expect to load classes
     * or resources, even if you are already declaring a dependency on an inter-
     * mediate module which has such a dependency.
     * @see "#27853"
     */
    public void testIndirectPackageExports() throws Exception {
        ModuleManager mgr = new ModuleManager(new MockModuleInstaller(), new MockEvents());
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "api-mod-export-api.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "uses-and-exports-api.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "uses-api-transitively.jar"), null, false, false, false);
            Module m4 = mgr.create(new File(jars, "uses-api-directly.jar"), null, false, false, false);
            assertEquals("api-mod-export-api.jar had no problems", Collections.EMPTY_SET, m1.getProblems());
            assertEquals("uses-and-exports-api.jar had no problems", Collections.EMPTY_SET, m2.getProblems());
            assertEquals("uses-api-transitively.jar had no problems", Collections.EMPTY_SET, m3.getProblems());
            assertEquals("uses-api-directly.jar had no problems", Collections.EMPTY_SET, m4.getProblems());
            mgr.enable(new HashSet<>(Arrays.asList(m1, m2, m3, m4)));
            m4.getClassLoader().loadClass("usesapitrans.UsesDirectAPI").getDeclaredConstructor().newInstance();
            m4.getClassLoader().loadClass("usesapitrans.UsesIndirectAPI").getDeclaredConstructor().newInstance();
            m3.getClassLoader().loadClass("usesapitrans.UsesDirectAPI").getDeclaredConstructor().newInstance();
            try {
                m3.getClassLoader().loadClass("usesapitrans.UsesIndirectAPI").getDeclaredConstructor().newInstance();
                fail("Should not be able to use a transitive API class with no direct dependency");
            } catch (InvocationTargetException e) {
                assertTrue(e.getCause() instanceof NoClassDefFoundError);
            }
            mgr.disable(new HashSet<>(Arrays.asList(m1, m2, m3, m4)));
            mgr.delete(m4);
            mgr.delete(m3);
            mgr.delete(m2);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testPublicPackagesCanBeExportedToSelectedFriendsOnlyIssue54123 () throws Exception {
        ModuleManager mgr = new ModuleManager(new MockModuleInstaller(), new MockEvents());
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "api-mod-export-friend.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "uses-api-friend.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "uses-and-exports-api.jar"), null, false, false, false);
            Module m4 = mgr.create(new File(jars, "uses-api-directly.jar"), null, false, false, false);
            Module m5 = mgr.create(new File(jars, "uses-api-impl-dep-for-friends.jar"), null, false, false, false);
            assertEquals("api-mod-export-api.jar had no problems", Collections.EMPTY_SET, m1.getProblems());
            assertEquals("uses-api-friend.jar had no problems", Collections.EMPTY_SET, m2.getProblems());
            assertEquals("uses-and-exports-api.jar had no problems", Collections.EMPTY_SET, m3.getProblems());
            assertEquals("uses-api-directly.jar had no problems", Collections.EMPTY_SET, m4.getProblems());
            assertEquals("uses-api-impl-dep-for-friends.jar had no problems", Collections.EMPTY_SET, m5.getProblems());
            mgr.enable(new HashSet<Module>(Arrays.asList(m1, m2, m3, m4, m5)));
            m2.getClassLoader().loadClass("usesapi.UsesPublicClass").getDeclaredConstructor().newInstance();
            try {
                m2.getClassLoader().loadClass("usesapi.UsesImplClass").getDeclaredConstructor().newInstance();
                fail ("Even friends modules cannot access implementation classes");
            } catch (InvocationTargetException ex) {
                assertTrue(ex.getCause() instanceof NoClassDefFoundError);
            }

            try {
                m4.getClassLoader().loadClass("usesapi.UsesPublicClass").getDeclaredConstructor().newInstance();
                fail ("m4 is not friend and should not be allowed to load the class");
            } catch (InvocationTargetException ex) {
                assertTrue(ex.getCause() instanceof NoClassDefFoundError);
            }
            try {
                m4.getClassLoader().loadClass("usesapi.UsesImplClass").getDeclaredConstructor().newInstance();
                fail ("m4 is not friend and should not be allowed to load the implementation either");
            } catch (InvocationTargetException ex) {
                assertTrue(ex.getCause() instanceof NoClassDefFoundError);
            }
            try {
                m5.getClassLoader().loadClass("usesapi.UsesPublicClass").getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                fail("m5 has an implementation dependency and has not been allowed to load the public class");
            }
            try {
                m5.getClassLoader().loadClass("usesapi.UsesImplClass").getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                fail("m5 has an implementation dependency and has not been allowed to load the imlpementation class");
            }

            mgr.disable(new HashSet<Module>(Arrays.asList(m1, m2, m3, m4, m5)));
            mgr.delete(m5);
            mgr.delete(m4);
            mgr.delete(m3);
            mgr.delete(m2);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testModuleInterdependencies() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "depends-on-simple-module.jar"), null, false, false, false);
            Module m3 = mgr.create(new File(jars, "dep-on-dep-on-simple.jar"), null, false, false, false);
            Set<Module> m1m2 = new HashSet<>(Arrays.asList(m1, m2));
            Set<Module> m2m3 = new HashSet<>(Arrays.asList(m2, m3));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m1, false, false, true));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m1, false, true, true));
            assertEquals(Collections.singleton(m2), mgr.getModuleInterdependencies(m1, true, false, true));
            assertEquals(m2m3, mgr.getModuleInterdependencies(m1, true, true, true));
            assertEquals(Collections.singleton(m1), mgr.getModuleInterdependencies(m2, false, false, true));
            assertEquals(Collections.singleton(m1), mgr.getModuleInterdependencies(m2, false, true, true));
            assertEquals(Collections.singleton(m3), mgr.getModuleInterdependencies(m2, true, false, true));
            assertEquals(Collections.singleton(m3), mgr.getModuleInterdependencies(m2, true, true, true));
            assertEquals(Collections.singleton(m2), mgr.getModuleInterdependencies(m3, false, false, true));
            assertEquals(m1m2, mgr.getModuleInterdependencies(m3, false, true, true));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m3, true, false, true));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m3, true, true, true));
            m1 = mgr.create(new File(jars, "prov-foo.jar"), null, false, false, false);
            m2 = mgr.create(new File(jars, "prov-foo-bar.jar"), null, false, false, false);
            m3 = mgr.create(new File(jars, "req-foo.jar"), null, false, false, false);
            Module m4 = mgr.create(new File(jars, "prov-baz.jar"), null, false, false, false);
            Module m5 = mgr.create(new File(jars, "req-foo-baz.jar"), null, false, false, false);
            m1m2 = new HashSet<>(Arrays.asList(m1, m2));
            assertEquals(m1m2, mgr.getModuleInterdependencies(m3, false, true, true));
            Set<Module> m1m2m4 = new HashSet<>(Arrays.asList(m1, m2, m4));
            assertEquals(m1m2m4, mgr.getModuleInterdependencies(m5, false, true, true));
            Set<Module> m3m5 = new HashSet<>(Arrays.asList(m3, m5));
            assertEquals(m3m5, mgr.getModuleInterdependencies(m1, true, true, true));
            // XXX could do more...
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    public void testModuleInterdependenciesOSGi() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            final File f1 = changeManifest(new File(jars, "simple-module.jar"), 
                "Bundle-SymbolicName: org.simple\n"
                + "\n"
                + "\n"
                + "\n"
            );
            final File f2 = changeManifest(new File(jars, "depends-on-simple-module.jar"),
                "Bundle-SymbolicName: org.depsonsimple\n"
                + "Require-Bundle: org.simple\n"
                + "\n"
                + "\n"
            );
            final File f3 = changeManifest(new File(jars, "dep-on-dep-on-simple.jar"),
                "Bundle-SymbolicName: org.deps.depsonsimple\n"
                + "Require-Bundle: org.depsonsimple\n"
                + "\n"
                + "\n"
            );
            Module m1 = mgr.create(f1, null, false, false, false);
            Module m2 = mgr.create(f2, null, false, false, false);
            Module m3 = mgr.create(f3, null, false, false, false);
            Set<Module> m1m2 = new HashSet<>(Arrays.asList(m1, m2));
            Set<Module> m2m3 = new HashSet<>(Arrays.asList(m2, m3));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m1, false, false, true));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m1, false, true, true));
            assertEquals(Collections.singleton(m2), mgr.getModuleInterdependencies(m1, true, false, true));
            assertEquals(m2m3, mgr.getModuleInterdependencies(m1, true, true, true));
            assertEquals(Collections.singleton(m1), mgr.getModuleInterdependencies(m2, false, false, true));
            assertEquals(Collections.singleton(m1), mgr.getModuleInterdependencies(m2, false, true, true));
            assertEquals(Collections.singleton(m3), mgr.getModuleInterdependencies(m2, true, false, true));
            assertEquals(Collections.singleton(m3), mgr.getModuleInterdependencies(m2, true, true, true));
            assertEquals(Collections.singleton(m2), mgr.getModuleInterdependencies(m3, false, false, true));
            assertEquals(m1m2, mgr.getModuleInterdependencies(m3, false, true, true));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m3, true, false, true));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m3, true, true, true));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    public void testModuleImportOSGi() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            final File f1 = changeManifest(new File(jars, "simple-module.jar"), 
                "Bundle-SymbolicName: org.simple\n"
                + "Export-Package: org.simple.util\n"
                + "\n"
                + "\n"
            );
            final File f2 = changeManifest(new File(jars, "depends-on-simple-module.jar"),
                "Bundle-SymbolicName: org.depsonsimple\n"
                + "Export-Package: org.depsonsimple.test\n"
                + "Import-Package: org.simple.util\n"
                + "\n"
                + "\n"
            );
            final File f3 = changeManifest(new File(jars, "dep-on-dep-on-simple.jar"),
                "Bundle-SymbolicName: org.deps.depsonsimple\n"
                + "Import-Package: org.depsonsimple.test\n"
                + "\n"
                + "\n"
            );
            Module m1 = mgr.create(f1, null, false, false, false);
            Module m2 = mgr.create(f2, null, false, false, false);
            Module m3 = mgr.create(f3, null, false, false, false);
            Set<Module> m1m2 = new HashSet<>(Arrays.asList(m1, m2));
            Set<Module> m2m3 = new HashSet<>(Arrays.asList(m2, m3));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m1, false, false, true));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m1, false, true, true));
            assertEquals(Collections.singleton(m2), mgr.getModuleInterdependencies(m1, true, false, true));
            assertEquals(m2m3, mgr.getModuleInterdependencies(m1, true, true, true));
            assertEquals(Collections.singleton(m1), mgr.getModuleInterdependencies(m2, false, false, true));
            assertEquals(Collections.singleton(m1), mgr.getModuleInterdependencies(m2, false, true, true));
            assertEquals(Collections.singleton(m3), mgr.getModuleInterdependencies(m2, true, false, true));
            assertEquals(Collections.singleton(m3), mgr.getModuleInterdependencies(m2, true, true, true));
            assertEquals(Collections.singleton(m2), mgr.getModuleInterdependencies(m3, false, false, true));
            assertEquals(m1m2, mgr.getModuleInterdependencies(m3, false, true, true));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m3, true, false, true));
            assertEquals(Collections.EMPTY_SET, mgr.getModuleInterdependencies(m3, true, true, true));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testModuleInterdependenciesNeeds() throws Exception { // #114896
        File dir = getWorkDir();
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            File jar = new File(dir, "api.jar");
            TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:OpenIDE-Module: api\nOpenIDE-Module-Needs: provider\n\n");
            Module api = mgr.create(jar, null, false, false, false);
            jar = new File(dir, "impl.jar");
            TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:OpenIDE-Module: impl\nOpenIDE-Module-Provides: provider\n" +
                    "OpenIDE-Module-Module-Dependencies: api\n\n");
            Module impl = mgr.create(jar, null, false, false, false);
            jar = new File(dir, "client.jar");
            TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:OpenIDE-Module: client\nOpenIDE-Module-Module-Dependencies: api\n\n");
            Module client = mgr.create(jar, null, false, false, false);
            assertEquals(Collections.singleton(api), mgr.getModuleInterdependencies(impl, false, false, true));
            assertEquals(Collections.singleton(api), mgr.getModuleInterdependencies(impl, false, true, true));
            assertEquals(Collections.singleton(api), mgr.getModuleInterdependencies(impl, true, false, true));
            assertEquals(new HashSet<Module>(Arrays.asList(api, client)), mgr.getModuleInterdependencies(impl, true, true, true));
            assertEquals(Collections.singleton(api), mgr.getModuleInterdependencies(client, false, false, true));
            assertEquals(new HashSet<Module>(Arrays.asList(api, impl)), mgr.getModuleInterdependencies(client, false, true, true));
            assertEquals(Collections.emptySet(), mgr.getModuleInterdependencies(client, true, false, true));
            assertEquals(Collections.emptySet(), mgr.getModuleInterdependencies(client, true, true, true));
            assertEquals(Collections.singleton(impl), mgr.getModuleInterdependencies(api, false, false, true));
            assertEquals(Collections.singleton(impl), mgr.getModuleInterdependencies(api, false, true, true));
            assertEquals(new HashSet<Module>(Arrays.asList(impl, client)), mgr.getModuleInterdependencies(api, true, false, true));
            assertEquals(new HashSet<Module>(Arrays.asList(impl, client)), mgr.getModuleInterdependencies(api, true, true, true));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testCyclicNeeds() throws Exception { // #161917
        File dir = getWorkDir();
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            File jar = new File(dir, "a.jar");
            TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:OpenIDE-Module: a\nOpenIDE-Module-Needs: T1\nOpenIDE-Module-Provides: T2\n\n");
            Module a = mgr.create(jar, null, false, false, false);
            jar = new File(dir, "b.jar");
            TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:OpenIDE-Module: b\nOpenIDE-Module-Needs: T2\nOpenIDE-Module-Provides: T1\n\n");
            Module b = mgr.create(jar, null, false, false, false);
            assertEquals(Collections.singleton(a), mgr.getModuleInterdependencies(b, false, false, true));
            assertEquals(Collections.singleton(a), mgr.getModuleInterdependencies(b, false, true, true));
            assertEquals(Collections.singleton(a), mgr.getModuleInterdependencies(b, true, false, true));
            assertEquals(Collections.singleton(a), mgr.getModuleInterdependencies(b, true, true, true));
            assertEquals(Collections.singleton(b), mgr.getModuleInterdependencies(a, false, false, true));
            assertEquals(Collections.singleton(b), mgr.getModuleInterdependencies(a, false, true, true));
            assertEquals(Collections.singleton(b), mgr.getModuleInterdependencies(a, true, false, true));
            assertEquals(Collections.singleton(b), mgr.getModuleInterdependencies(a, true, true, true));
            Set<Module> both = new HashSet<>(Arrays.asList(a, b));
            assertEquals(both, new HashSet<Module>(mgr.simulateEnable(Collections.singleton(a))));
            assertEquals(both, new HashSet<Module>(mgr.simulateEnable(Collections.singleton(b))));
            mgr.enable(both);
            assertEquals(both, mgr.getEnabledModules());
            mgr.disable(both);
            assertEquals(Collections.emptySet(), mgr.getEnabledModules());
            mgr.delete(a);
            mgr.delete(b);
            jar = new File(dir, "a.jar");
            TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:OpenIDE-Module: a\nOpenIDE-Module-Needs: T1\nOpenIDE-Module-Provides: T2\n" +
                    "OpenIDE-Module-Module-Dependencies: b\n");
            a = mgr.create(jar, null, false, false, false);
            jar = new File(dir, "b.jar");
            TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:OpenIDE-Module: b\nOpenIDE-Module-Needs: T2\nOpenIDE-Module-Provides: T1\n\n");
            b = mgr.create(jar, null, false, false, false);
            assertEquals(Collections.singleton(a), mgr.getModuleInterdependencies(b, false, false, true));
            assertEquals(Collections.singleton(a), mgr.getModuleInterdependencies(b, false, true, true));
            assertEquals(Collections.singleton(a), mgr.getModuleInterdependencies(b, true, false, true));
            assertEquals(Collections.singleton(a), mgr.getModuleInterdependencies(b, true, true, true));
            assertEquals(Collections.singleton(b), mgr.getModuleInterdependencies(a, false, false, true));
            assertEquals(Collections.singleton(b), mgr.getModuleInterdependencies(a, false, true, true));
            assertEquals(Collections.singleton(b), mgr.getModuleInterdependencies(a, true, false, true));
            assertEquals(Collections.singleton(b), mgr.getModuleInterdependencies(a, true, true, true));
            both = new HashSet<>(Arrays.asList(a, b));
            assertEquals(both, new HashSet<Module>(mgr.simulateEnable(Collections.singleton(a))));
            assertEquals(both, new HashSet<Module>(mgr.simulateEnable(Collections.singleton(b))));
            mgr.enable(both);
            assertEquals(both, mgr.getEnabledModules());
            mgr.disable(both);
            assertEquals(Collections.emptySet(), mgr.getEnabledModules());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testModuleClassLoaderWasNotReadyWhenTheChangeWasFiredIssue() throws Exception {
        doModuleClassLoaderWasNotReadyWhenTheChangeWasFiredIssue (1);
    }
    public void testGlobalClassLoaderWasNotReadyWhenTheChangeWasFiredIssue() throws Exception {
        doModuleClassLoaderWasNotReadyWhenTheChangeWasFiredIssue (2);
    }
    public void testModuleManagerClassLoaderWasNotReadyWhenTheChangeWasFiredIssue() throws Exception {
        doModuleClassLoaderWasNotReadyWhenTheChangeWasFiredIssue (3);
    }

    private void doModuleClassLoaderWasNotReadyWhenTheChangeWasFiredIssue (final int typeOfClassLoader) throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        final ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            final Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);

            class L implements java.beans.PropertyChangeListener {
                ClassLoader l;
                IllegalStateException ex;

                public @Override void propertyChange(java.beans.PropertyChangeEvent event) {
                    if (Module.PROP_ENABLED.equals (event.getPropertyName ())) {
                        try {
                            l = get();
                        } catch (IllegalStateException x) {
                            ex = x;
                        }
                    }
                }

                public ClassLoader get () {
                    switch (typeOfClassLoader) {
                        case 1: return m1.getClassLoader ();
                        case 2: return Thread.currentThread ().getContextClassLoader ();
                        case 3: return mgr.getClassLoader ();
                    }
                    fail ("Wrong type: " + typeOfClassLoader);
                    return null;
                }
            }
            L l = new L ();
            m1.addPropertyChangeListener (l);

            mgr.enable (m1);

            assertTrue ("Successfully enabled", m1.isEnabled ());
            assertEquals ("Classloader at the time of PROP_ENABLED is the same as now", l.get (), l.l);
            assertNull ("No exception thrown", l.ex);
            //System.out.println("L: " + l.l);
            m1.removePropertyChangeListener (l);

            mgr.disable (m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    /** @see "#76917" */
    public void testProblemsStillCorrectWithHardAndSoftMixture() throws Exception {
        File m1j = new File(getWorkDir(), "m1.jar");
        createJar(m1j, Collections.<String,String>emptyMap(), Collections.singletonMap("OpenIDE-Module", "m1"));
        File m2j = new File(getWorkDir(), "m2.jar");
        Map<String,String> mani = new HashMap<String,String>();
        mani.put("OpenIDE-Module", "m2");
        mani.put("OpenIDE-Module-Module-Dependencies", "m1");
        mani.put("OpenIDE-Module-Java-Dependencies", "Java > 2046");
        createJar(m2j, Collections.<String,String>emptyMap(), mani);
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m2 = mgr.create(m2j, null, false, false, false);
            assertEquals("initially m2 has two problems: Java and m1", 2, m2.getProblems().size());
            Module m1 = mgr.create(m1j, null, false, false, false);
            assertEquals("m1 has no problems", Collections.emptySet(), m1.getProblems());
            assertEquals("now m2 should have just one problem: Java", 1, m2.getProblems().size());
            Dependency d = (Dependency) m2.getProblems().iterator().next();
            assertEquals(Dependency.TYPE_JAVA, d.getType());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testShouldDelegateResource() throws Exception {
        File m1j = new File(getWorkDir(), "m1.jar");
        Map<String,String> contents = new HashMap<String,String>();
        contents.put("javax/swing/JPanel.class", "overrides");
        contents.put("javax/xml/parsers/DocumentBuilder.class", "ignored");
        createJar(m1j, contents, Collections.singletonMap("OpenIDE-Module", "m1"));
        File m2j = new File(getWorkDir(), "m2.jar");
        Map<String,String> mani = new HashMap<String,String>();
        mani.put("OpenIDE-Module", "m2");
        mani.put("OpenIDE-Module-Module-Dependencies", "m1");
        createJar(m2j, Collections.<String,String>emptyMap(), mani);
        File m3j = new File(getWorkDir(), "m3.jar");
        createJar(m3j, Collections.<String,String>emptyMap(), Collections.singletonMap("OpenIDE-Module", "m3"));
        MockModuleInstaller installer = new MockModuleInstaller() {
            public @Override boolean shouldDelegateResource(Module m, Module parent, String pkg) {
                if (parent == null && pkg.equals("javax/swing/") && m.getCodeNameBase().matches("m[12]")) {
                    return false;
                } else {
                    return true;
                }
            }
        };
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(m1j, null, false, false, false);
            Module m2 = mgr.create(m2j, null, false, false, false);
            Module m3 = mgr.create(m3j, null, false, false, false);
            mgr.enable(new HashSet<Module>(Arrays.asList(m1, m2, m3)));
            assertOverrides(m1, "javax.swing.JPanel");
            assertOverrides(m2, "javax.swing.JPanel");
            assertDoesNotOverride(m3, "javax.swing.JPanel");
            assertDoesNotOverride(m1, "javax.xml.parsers.DocumentBuilder");
            assertDoesNotOverride(m2, "javax.xml.parsers.DocumentBuilder");
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    public static void assertOverrides(ClassLoader l, String name, String clazz) throws Exception {
        try {
            assertFalse(name + " did not override " + clazz, Class.forName(clazz) == l.loadClass(clazz));
        } catch (LinkageError e) {
            // right: we don't provide legal class bodies here, so it would fail to even load
        }
        String rsrc = clazz.replace('.', '/') + ".class";
        URL cpResource = ModuleManagerTest.class.getResource("/" + rsrc);
        assertNotNull("found " + rsrc, cpResource);
        URL modResource = l.getResource(rsrc);
        assertNotNull("found " + rsrc, modResource);
        assertFalse(name + " did not override " + rsrc, cpResource.equals(modResource));
    }
    public static void assertOverrides(Module m, String clazz) throws Exception {
        assertOverrides(m.getClassLoader(), "module " + m.getCodeNameBase(), clazz);
    }
    public static void assertDoesNotOverride(ClassLoader l, String clazz) throws Exception {
        assertEquals(Class.forName(clazz), l.loadClass(clazz));
        String rsrc = clazz.replace('.', '/') + ".class";
        URL cpResource = ModuleManagerTest.class.getResource("/" + rsrc);
        assertNotNull("found " + rsrc, cpResource);
        URL modResource = l.getResource(rsrc);
        assertNotNull("found " + rsrc, modResource);
        assertEquals(cpResource, modResource);
    }
    public static void assertDoesNotOverride(Module m, String clazz) throws Exception {
        assertDoesNotOverride(m.getClassLoader(), clazz);
    }

    public void testDisableWithAutoloadMajorRange() throws Exception { // #127720; also see testDisableAgainstRelVersRange
        File m1j = new File(getWorkDir(), "m1.jar");
        createJar(m1j, Collections.<String,String>emptyMap(), Collections.singletonMap("OpenIDE-Module", "m1/0"));
        File m2j = new File(getWorkDir(), "m2.jar");
        Map<String,String> mani = new HashMap<String,String>();
        mani.put("OpenIDE-Module", "m2");
        mani.put("OpenIDE-Module-Module-Dependencies", "m1/0-1");
        createJar(m2j, Collections.<String,String>emptyMap(), mani);
        File m3j = new File(getWorkDir(), "m3.jar");
        mani = new HashMap<String,String>();
        mani.put("OpenIDE-Module", "m3");
        mani.put("OpenIDE-Module-Module-Dependencies", "m1/0");
        createJar(m3j, Collections.<String,String>emptyMap(), mani);
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(m1j, null, false, true, false);
            Module m2 = mgr.create(m2j, null, false, false, false);
            Module m3 = mgr.create(m3j, null, false, false, false);
            mgr.enable(m2);
            mgr.enable(m3);
            assertTrue(m1.isEnabled());
            assertTrue(m2.isEnabled());
            assertTrue(m3.isEnabled());
            assertEquals(Collections.singletonList(m3), mgr.simulateDisable(Collections.singleton(m3)));
            mgr.disable(m3);
            assertTrue(m1.isEnabled());
            assertTrue(m2.isEnabled());
            assertFalse(m3.isEnabled());
            assertEquals(Arrays.asList(m2, m1), mgr.simulateDisable(Collections.singleton(m2)));
            mgr.disable(m2);
            assertFalse(m1.isEnabled());
            assertFalse(m2.isEnabled());
            assertFalse(m3.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testDisableWithRegularMajorRange() throws Exception { // #197718
        File m1j = new File(getWorkDir(), "m1.jar");
        createJar(m1j, Collections.<String,String>emptyMap(), Collections.singletonMap("OpenIDE-Module", "m1/0"));
        File m2j = new File(getWorkDir(), "m2.jar");
        Map<String,String> mani = new HashMap<String,String>();
        mani.put("OpenIDE-Module", "m2");
        mani.put("OpenIDE-Module-Module-Dependencies", "m1/0-1");
        createJar(m2j, Collections.<String,String>emptyMap(), mani);
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(m1j, null, false, false, false);
            Module m2 = mgr.create(m2j, null, false, false, false);
            mgr.enable(new HashSet<Module>(Arrays.asList(m1, m2)));
            assertTrue(m1.isEnabled());
            assertTrue(m2.isEnabled());
            assertEquals(Arrays.asList(m2, m1), mgr.simulateDisable(Collections.singleton(m1)));
            mgr.disable(new HashSet<Module>(Arrays.asList(m1, m2)));
            assertFalse(m1.isEnabled());
            assertFalse(m2.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testModuleOwnsClass() throws Exception { // #157798
        clearWorkDir();
        data = new File(getWorkDir(), "data");
        jars = new File(getWorkDir(), "jars");
        TestFileUtils.writeFile(new File(data, "mod1.mf"), "OpenIDE-Module: mod1/1\n\n");
        TestFileUtils.writeFile(new File(data, "mod1/pkg/C1.java"), "package pkg; class C1 {}");
        TestFileUtils.writeFile(new File(data, "mod1/pkg/C2.java"), "package pkg; class C2 {}");
        File mod1JAR = createTestJAR(data, jars, "mod1", null);
        TestFileUtils.writeFile(new File(data, "mod2.mf"), "OpenIDE-Module: mod2/1\n\n");
        TestFileUtils.writeFile(new File(data, "mod2/pkg/C3.java"), "package pkg; class C3 {}");
        File mod2JAR = createTestJAR(data, jars, "mod2", null);
        ModuleManager mgr = new ModuleManager(new MockModuleInstaller(), new MockEvents());
        Modules modules = mgr.getModuleLookup().lookup(Modules.class);
        assertNotNull(modules);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module mod1 = mgr.create(mod1JAR, null, false, false, false);
            mgr.enable(mod1);
            Module mod2 = mgr.create(mod2JAR, null, false, false, false);
            mgr.enable(mod2);
            Class<?> c1 = mod1.getClassLoader().loadClass("pkg.C1");
            Class<?> c2 = mod1.getClassLoader().loadClass("pkg.C2");
            Class<?> c3 = mod2.getClassLoader().loadClass("pkg.C3");
            assertTrue(mod1.owns(c1));
            assertTrue(mod1.owns(c2));
            assertFalse(mod1.owns(c3));
            assertFalse(mod2.owns(c1));
            assertFalse(mod2.owns(c2));
            assertTrue(mod2.owns(c3));
            assertEquals(mod1, modules.ownerOf(c1));
            assertEquals(mod1, modules.ownerOf(c2));
            assertEquals(mod2, modules.ownerOf(c3));
            assertNull(modules.ownerOf(String.class));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        mgr = new ModuleManager(new MockModuleInstaller(), new MockEvents());
        modules = mgr.getModuleLookup().lookup(Modules.class);
        assertNotNull(modules);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            ClassLoader l = new URLClassLoader(new URL[] {Utilities.toURI(mod1JAR).toURL(), Utilities.toURI(mod2JAR).toURL()});
            Module mod1 = mgr.createFixed(loadManifest(mod1JAR), null, l);
            mgr.enable(mod1);
            assertEquals(l, mod1.getClassLoader());
            Module mod2 = mgr.createFixed(loadManifest(mod2JAR), null, l);
            mgr.enable(mod2);
            Class<?> c1 = l.loadClass("pkg.C1");
            assertEquals(l, c1.getClassLoader());
            Class<?> c2 = l.loadClass("pkg.C2");
            Class<?> c3 = l.loadClass("pkg.C3");
            assertTrue(mod1.owns(c1));
            assertTrue(mod1.owns(c2));
            assertFalse(mod1.owns(c3));
            assertFalse(mod2.owns(c1));
            assertFalse(mod2.owns(c2));
            assertTrue(mod2.owns(c3));
            assertEquals(mod1, modules.ownerOf(c1));
            assertEquals(mod1, modules.ownerOf(c2));
            assertEquals(mod2, modules.ownerOf(c3));
            assertNull(modules.ownerOf(String.class));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    private static Manifest loadManifest(File jar) throws IOException {
        JarFile j = new JarFile(jar);
        try {
            return j.getManifest();
        } finally {
            j.close();
        }
    }

    public void testMissingSpecVersion() throws Exception {
        File dir = getWorkDir();
        ModuleManager mgr = new ModuleManager(new MockModuleInstaller(), new MockEvents());
        mgr.mutexPrivileged().enterWriteAccess();
        File jar = new File(dir, "api.jar");
        TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:OpenIDE-Module: api\n\n");
        mgr.create(jar, null, false, false, false);
        jar = new File(dir, "client.jar");
        TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:OpenIDE-Module: client\nOpenIDE-Module-Module-Dependencies: api > 1.0\n\n");
        Module client = mgr.create(jar, null, false, false, false);
        assertEquals(1, client.getProblems().size());
    }

    public void testEnableHostWithEagerFragment() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();

        Module host = mgr.create(new File(jars, "host-module.jar"), null, false, false, false);
        Module fragment = mgr.create(new File(jars, "fragment-module.jar"), null, false, false, true);

        assertTrue("Host is known", mgr.getModules().contains(host));
        assertTrue("Fragment is known", mgr.getModules().contains(fragment));

        mgr.enable(host);

        assertTrue("Host must be enabled", mgr.getEnabledModules().contains(host));
        assertTrue("Fragment must be enabled", mgr.getEnabledModules().contains(fragment));
    }

    public void testEnableHostWithEagerFragmentUnsatisfied() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();

        createTestJAR(data, jars, "fragment-module-missing-token", null);

        Module host = mgr.create(new File(jars, "host-module.jar"), null, false, false, false);
        Module fragment = mgr.create(new File(jars, "fragment-module-missing-token.jar"), null, false, false, true);

        assertTrue("Host is known", mgr.getModules().contains(host));
        assertTrue("Fragment is known", mgr.getModules().contains(fragment));

        mgr.enable(host);

        assertTrue("Host must be enabled", mgr.getEnabledModules().contains(host));
        assertTrue("Fragment must not be enabled", !mgr.getEnabledModules().contains(fragment));
        
        assertTrue(logHandler.warnings.isEmpty());
    }
    
    public void testEnableFragmentBeforeItsHost() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        
        createTestJAR(data, jars, "fragment-module-reg", "fragment-module");

        // m1 autoload, m2 normal, m3 eager
        Module m1 = mgr.create(new File(jars, "host-module.jar"), null, false, false, false);
        Module m2 = mgr.create(new File(jars, "fragment-module-reg.jar"), null, false, false, false);
        Module m3 = mgr.create(new File(jars, "fragment-module.jar"), null, false, false, true);
        List<Module> toEnable = mgr.simulateEnable(Collections.singleton(m2));
        
        assertTrue("Host will be enabled", toEnable.contains(m1));
        assertTrue("Known fragment must be merged in", toEnable.contains(m2));
        assertTrue("Eager fragment must be merged in", toEnable.contains(m3));
        
        // cannot explicitly enable eager module:
        toEnable.remove(m3);
        mgr.enable(new HashSet<>(toEnable));
    }
    
    public void testEnableHostWithoutFragment() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();

        // m1 autoload, m2 normal, m3 eager
        Module m1 = mgr.create(new File(jars, "host-module.jar"), null, false, false, false);
        Module m2 = mgr.create(new File(jars, "fragment-module.jar"), null, false, false, false);
        List<Module> toEnable = mgr.simulateEnable(Collections.singleton(m2));
        
        assertTrue("Host will be enabled", toEnable.contains(m1));
        assertTrue("Known fragment must be merged in", toEnable.contains(m2));
        mgr.enable(new HashSet<>(toEnable));
    }
    
    public void testInstallFragmentAfterHostEnabled() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();

        // m1 autoload, m2 normal, m3 eager
        Module m1 = mgr.create(new File(jars, "host-module.jar"), null, false, false, false);
        mgr.enable(m1);

        Module m2 = mgr.create(new File(jars, "fragment-module.jar"), null, false, false, false);

        try {
            mgr.enable(Collections.singleton(m2));
            fail("Enabling fragment must fail if host is already live");
        } catch (IllegalStateException ex) {
            // ok
        }
    }

    public void testAutoloadHostWithFragmentsDoesNotRun() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();

        createTestJAR(data, jars, "client-module", null);

        Module host = mgr.create(new File(jars, "host-module.jar"), null, false, true, false);
        Module fragment = mgr.create(new File(jars, "fragment-module.jar"), null, false, true, false);
        
        Module client = mgr.create(new File(jars, "client-module.jar"), null, false, false, false);
        
        mgr.enable(client);

        assertFalse("Host can't enable always", mgr.getEnabledModules().contains(host));
        assertFalse("Fragment must not be enabled", mgr.getEnabledModules().contains(fragment));
    }
    
    
    public void testAutoloadFragmentEnablesHostAndPeers() throws Exception {
        ModsCreator c = new ModsCreator();
        createTestJAR(data, jars, "client-module-depend-frag", "client-module");
        c.loadModules();
        
        Module client = c.mgr.create(new File(jars, "client-module-depend-frag.jar"), null, false, false, false);
        c.mgr.enable(client);

        c.checkHostAndOtherFragmentsLoaded(c.fragmentAutoload);
    }
    
    public void testAutoloadHostEnablesEagerFragments() throws Exception {
        ModsCreator c = new ModsCreator();
        createTestJAR(data, jars, "client-module-depend-host", "client-module");
        c.loadModules();
        
        Module client = c.mgr.create(new File(jars, "client-module-depend-host.jar"), null, false, false, false);
        c.mgr.enable(client);

        c.checkHostAndOtherFragmentsLoaded();
    }
    
    public void testBrokenAutoloadFragmentDepend() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();

        createTestJAR(data, jars, "client-module-depend-broken", "client-module");
        createTestJAR(data, jars, "fragment-module-missing-token", null);

        Module host = mgr.create(new File(jars, "host-module.jar"), null, false, false, false);
        Module fragment = mgr.create(new File(jars, "fragment-module-missing-token.jar"), null, false, false, true);
        Module client = mgr.create(new File(jars, "client-module-depend-broken.jar"), null, false, false, false);
        
        try {
            mgr.enable(client);
        } catch (IllegalModuleException ex) {
            assertTrue(ex.getMessage().contains("org.foo.client"));
        }
        
        assertFalse(mgr.getEnabledModules().contains(host));
        assertFalse(mgr.getEnabledModules().contains(fragment));
        assertFalse(mgr.getEnabledModules().contains(client));
    }

    public void testBrokenAutoloadFragmentNeeds() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();

        createTestJAR(data, jars, "client-module-needs-broken", "client-module");
        createTestJAR(data, jars, "fragment-module-missing-token", null);

        Module host = mgr.create(new File(jars, "host-module.jar"), null, false, false, false);
        Module fragment = mgr.create(new File(jars, "fragment-module-missing-token.jar"), null, false, false, true);
        Module client = mgr.create(new File(jars, "client-module-needs-broken.jar"), null, false, false, false);
        
        try {
            mgr.enable(client);
        } catch (IllegalModuleException ex) {
            assertTrue(ex.getMessage().contains("org.foo.client"));
        }
        
        assertFalse(mgr.getEnabledModules().contains(host));
        assertFalse(mgr.getEnabledModules().contains(fragment));
        assertFalse(mgr.getEnabledModules().contains(client));
    }
    
    /**
     * Tests the situation with JavaFX support:
     * - client depends on core module
     * - core module NEEDS or REQUIRES a platform module
     * - platform modules are AUTOLOADS and are fragments
     * - noone depends on platform modules directly
     * - each platform module REQUIRES a token
     * - one token is provided
     * Under normal circumstances, the autoload fragments would not load since noone depends
     * on them. But they are providers, and SOME provider is needed. Hence the matching one(s)
     * will load.
     * 
     * @throws Exception 
     */
    public void testPickFromAutoloadFragmentsByToken() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        installer.provides.put("org.foo.javafx", new String[] { "org.openide.modules.os.Linux", "org.openide.modules.os.Unix" });
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();

        createTestJAR(data, jars, "foo-javafx-core", "javafx-core");
        createTestJAR(data, jars, "foo-javafx-linux", "javafx-linux");
        createTestJAR(data, jars, "foo-javafx-windows", "javafx-windows");
        createTestJAR(data, jars, "foo-javafx-client", "client-module");
        createTestJAR(data, jars, "foo-javafx-linux-eager", "javafx-linux-eager");

        Module host = mgr.create(new File(jars, "foo-javafx-core.jar"), null, false, true, false);
        Module linuxFrag = mgr.create(new File(jars, "foo-javafx-linux.jar"), null, false, true, false);
        Module linuxEager = mgr.create(new File(jars, "foo-javafx-linux-eager.jar"), null, false, false, true);
        Module winFrag = mgr.create(new File(jars, "foo-javafx-windows.jar"), null, false, true, false);
        Module client = mgr.create(new File(jars, "foo-javafx-client.jar"), null, false, false, false);
        
        mgr.enable(client);
        
        assertTrue(mgr.getEnabledModules().contains(host));
        assertTrue(mgr.getEnabledModules().contains(linuxFrag));
        assertTrue(mgr.getEnabledModules().contains(linuxEager));
        
        assertFalse(mgr.getEnabledModules().contains(winFrag));
        assertTrue(mgr.getEnabledModules().contains(client));
        
        assertNotNull(client.getClassLoader().getResource("org/foo/javafx/Bundle.properties"));
        assertNotNull(client.getClassLoader().getResource("org/foo/javafx/Linux.properties"));
        assertNull(client.getClassLoader().getResource("org/foo/javafx/Windows.properties"));
        assertNotNull(client.getClassLoader().getResource("org/foo/javafx/Eager.properties"));
    }
    
    /**
     * Checks that dependencies introduced by the fragment are injected into the 
     * host module.
     * @throws Exception 
     */
    public void testFragmentDependenciesInjectedIntoMain() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        installer.provides.put("org.foo.javafx", new String[] { "org.openide.modules.os.Windows" });
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();

        createTestJAR(data, jars, "foo-javafx-core", "javafx-core");
        createTestJAR(data, jars, "foo-javafx-linux", "javafx-linux");
        createTestJAR(data, jars, "foo-javafx-windows", "javafx-windows");
        createTestJAR(data, jars, "foo-javafx-client", "client-module");
        createTestJAR(data, jars, "foo-javafx-linux-eager", "javafx-linux-eager");
        createTestJAR(data, jars, "agent", "agent");

        Module host = mgr.create(new File(jars, "foo-javafx-core.jar"), null, false, true, false);
        Module linuxFrag = mgr.create(new File(jars, "foo-javafx-linux.jar"), null, false, true, false);
        Module linuxEager = mgr.create(new File(jars, "foo-javafx-linux-eager.jar"), null, false, false, true);
        Module winFrag = mgr.create(new File(jars, "foo-javafx-windows.jar"), null, false, true, false);
        Module client = mgr.create(new File(jars, "foo-javafx-client.jar"), null, false, false, false);
        Module agent = mgr.create(new File(jars, "agent.jar"), null, false, true, false);
        
        mgr.enable(client);
        
        assertTrue(mgr.getEnabledModules().contains(host));
        assertFalse(mgr.getEnabledModules().contains(linuxFrag));
        assertFalse(mgr.getEnabledModules().contains(linuxEager));
        assertTrue(mgr.getEnabledModules().contains(winFrag));
        assertTrue(mgr.getEnabledModules().contains(client));
        
        assertNotNull(host.getClassLoader().getResource("org/agent/HelloWorld.class"));
        assertNull(client.getClassLoader().getResource("org/agent/HelloWorld.class"));
    }

    private class LogHandler extends Handler {
        private List<LogRecord> warnings = new ArrayList<>();
        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                warnings.add(record);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
    
    private class ModsCreator {
        private final ModuleManager mgr;
        
        private Module host;
        private Module fragmentService;
        private Module fragmentBroken;
        private Module fragmentAutoload;
        private Module fragmentRegular;
        
        private List<Module> liveFragments = new ArrayList<>();

        public ModsCreator() {
            MockModuleInstaller installer = new MockModuleInstaller();
            MockEvents ev = new MockEvents();
            mgr = new ModuleManager(installer, ev);
            mgr.mutexPrivileged().enterWriteAccess();
        }
        
        void loadModules() throws Exception {
            
            createTestJAR(data, jars, "fragment-module-reg", "fragment-module");
            createTestJAR(data, jars, "fragment-module-auto", "fragment-module");
            createTestJAR(data, jars, "fragment-module-missing-token", "fragment-module");

            if (host == null) {
                host = mgr.create(new File(jars, "host-module.jar"), null, false, true, false);
            }
            if (fragmentService == null) {
                fragmentService = mgr.create(new File(jars, "fragment-module.jar"), null, false, false, true);
            }
            if (fragmentRegular == null) {
                fragmentRegular = mgr.create(new File(jars, "fragment-module-reg.jar"), null, false, false, false);
            }
            if (fragmentAutoload == null) {
                fragmentAutoload = mgr.create(new File(jars, "fragment-module-auto.jar"), null, false, true, false);
            }
            if (fragmentBroken == null) {
                fragmentBroken = mgr.create(new File(jars, "fragment-module-missing-token.jar"), null, false, true, false);
            }
            liveFragments.add(fragmentService);
            liveFragments.add(fragmentRegular);
            liveFragments.add(fragmentAutoload);
        }
        
        void checkHostAndOtherFragmentsLoaded(Module... pickedDeps) {
            assertTrue("Fragment host must enable", mgr.getEnabledModules().contains(host));
            assertTrue("Eager fragment must load with host", mgr.getEnabledModules().contains(fragmentService));
            Set<Module> picked = new HashSet<>(pickedDeps == null ? Collections.emptyList() : Arrays.asList(pickedDeps));
            for (Module m : liveFragments) {
                if (picked.contains(m)) {
                    assertTrue("Peer fragment must be loaded: " + m.getCodeNameBase(), mgr.getEnabledModules().contains(m));
                } else if (m != fragmentService) {
                    assertFalse("Fragment must not activate: " + m.getCodeNameBase(), mgr.getEnabledModules().contains(m));
                }
            }            
            // the fragment with unsatisfied "needs" is not reported, as it is autoload being triggered by host module.
            assertTrue(logHandler.warnings.isEmpty());
        }
    }
    
    private File copyJar(File file, String manifest) throws IOException {
        File ret = File.createTempFile(file.getName(), "2ndcopy", file.getParentFile());
        JarFile jar = new JarFile(file);
        JarOutputStream os = new JarOutputStream(new FileOutputStream(ret), new Manifest(
            new ByteArrayInputStream(manifest.getBytes())
        ));
        Enumeration<JarEntry> en = jar.entries();
        while (en.hasMoreElements()) {
            JarEntry elem = en.nextElement();
            if (elem.getName().equals("META-INF/MANIFEST.MF")) {
                continue;
            }
            os.putNextEntry(elem);
            InputStream is = jar.getInputStream(elem);
            copyStreams(is, os);
            is.close();
        }
        os.close();
        return ret;
    }

    private static Module createModule(ModuleManager mgr, String manifest) throws Exception {
        return mgr.createFixed(new Manifest(new ByteArrayInputStream(manifest.getBytes())), null, ModuleManagerTest.class.getClassLoader());
    }

    private static Collection<String> assertCnb(Module m) {
        String token = "cnb." + m.getCodeNameBase();
        List<String> arr = new ArrayList<String>();
        boolean ok = false;
        for (String t : m.getProvides()) {
            if (token.equals(t)) {
                ok = true;
            } else {
                arr.add(t);
            }
        }
        assertTrue(token + " is not among the list of provides of module " + m + " which is " + arr, ok);
        return arr;
    }
    }
