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

package org.netbeans.core.startup;

import org.netbeans.SetupHid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.LoggedPCListener;
import org.netbeans.MockModuleInstaller;
import org.netbeans.MockEvents;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import java.util.logging.Level;
import org.netbeans.Stamps;
import org.netbeans.junit.Log;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Places;
import org.openide.modules.api.PlacesTestUtils;
import org.openide.util.test.MockLookup;

/** Test the functions of the module list, i.e. finding modules on
 * disk and installing them, and writing out state as needed.
 * @author Jesse Glick
 */
public class ModuleListTest extends SetupHid {
    
    private static final String PREFIX = "wherever/";
    private LocalFileSystem fs;
    
    private final class IFL extends InstalledFileLocator {
        public IFL() {}
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.startsWith(PREFIX)) {
                File f = new File(jars, relativePath.substring(PREFIX.length()).replace('/', File.separatorChar));
                if (f.exists()) {
                    return f;
                }
            }
            return null;
        }
    }
    
    public ModuleListTest(String name) {
        super(name);
    }
    
    private ModuleManager mgr;
    private org.netbeans.core.startup.ModuleList list;
    private FileObject modulesfolder;
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockLookup.setInstances(new IFL());

        File ud = new File(getWorkDir(), "ud");
        PlacesTestUtils.setUserDirectory(ud);
        
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        mgr = new ModuleManager(installer, ev);
        File dir = new File(ud, "config");
        File modulesdir = new File(dir, "Modules");
        if (! modulesdir.mkdirs()) throw new IOException("Making " + modulesdir);
        fs = new LocalFileSystem();
        fs.setRootDirectory(dir);
        modulesfolder = fs.findResource("Modules");
        assertNotNull(modulesfolder);
        list = new ModuleList(mgr, modulesfolder, ev);
    }
    
    private Module makeModule(String jarName) throws Exception {
        File f = new File(jars, jarName);
        Module m = mgr.create(f, new ModuleHistory(PREFIX + jarName), false, false, false);
        return m;
    }
    
    /** Load simple-module and depends-on-simple-module.
     * Make sure they can be installed and in a sane order.
     * Make sure a class from one can depend on a class from another.
     */
    public void testScanAndTwiddle() throws Exception {
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            // XXX try to read an actual initial list too...
            assertEquals(Collections.emptySet(), list.readInitial());
            Set<Module> modules = new HashSet<Module>();
            modules.add(makeModule("simple-module.jar"));
            modules.add(makeModule("depends-on-simple-module.jar"));
            list.trigger(modules);
            assertEquals(modules, mgr.getEnabledModules());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        FileObject[] xml = modulesfolder.getChildren();
        assertEquals(2, xml.length);
        FileObject foo, bar;
        if (xml[0].getPath().equals("Modules/org-foo.xml")) {
            assertEquals("Modules/org-bar.xml", xml[1].getPath());
            foo = xml[0];
            bar = xml[1];
        } else {
            assertEquals("Modules/org-bar.xml", xml[0].getPath());
            assertEquals("Modules/org-foo.xml", xml[1].getPath());
            foo = xml[1];
            bar = xml[0];
        }
        assertFile(FileUtil.toFile(foo), new File(data, "org-foo.xml"));
        assertFile(FileUtil.toFile(bar), new File(data, "org-bar.xml"));
        // Checking that changes in memory will rewrite XML:
        LoggedFileListener listener = new LoggedFileListener();
        modulesfolder.addFileChangeListener(listener);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.get("org.foo");
            assertNotNull(m1);
            Module m2 = mgr.get("org.bar");
            assertNotNull(m2);
            mgr.disable(new HashSet<Module>(Arrays.asList(m1, m2)));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        // We expect it to have marked both as disabled now:
        listener.waitForChange("Modules/org-foo.xml");
        listener.waitForChange("Modules/org-bar.xml");
        assertFile(new File(data, "org-foo_disabled.xml"), FileUtil.toFile(foo));
        assertFile(new File(data, "org-bar_disabled.xml"), FileUtil.toFile(bar));
        // Check that changes in disk are parsed and applied (#13921)
        LoggedPCListener listener2 = new LoggedPCListener();
        ModuleInfo m1 = mgr.findCodeNameBase("org.foo");
        m1.addPropertyChangeListener(listener2);
        copy(new File(data, "org-foo.xml"), foo);
        /* Does not seem to refresh reliably enough:
        copy(new File(data, "org-foo.xml"), FileUtil.toFile(foo));
        foo.refresh();
         */
        // The change ought to be noticed by filesystems, picked up by
        // ModuleList, parsed, and result in org.foo being turned back on.
        listener2.waitForChange(m1, Module.PROP_ENABLED);
        assertTrue("m1 is enabled now", m1.isEnabled());

        /* XXX fails if testAddNewModuleViaXML was run first:
        assertCache();
        */
    }
    
    private void assertCache() throws Exception {
        Stamps.getModulesJARs().flush(0);
        Stamps.getModulesJARs().shutdown();
        
        File f = Places.getCacheSubfile("all-modules.dat");
        assertTrue("Cache exists", f.exists());

        FileObject mf = fs.findResource(modulesfolder.getPath());
        assertNotNull("config folder exits", mf);
        
        CountingSecurityManager.initialize(new File(fs.getRootDirectory(), "Modules").getPath());
        
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr2 = new ModuleManager(installer, ev);
        assertNotNull(mf);
        ModuleList list2 = new ModuleList(mgr2, mf, ev);
        mgr2.mutexPrivileged().enterWriteAccess();
        CharSequence log = Log.enable("org.netbeans.core.startup.ModuleList", Level.FINEST);
        try {
            list2.readInitial();
        } finally {
            mgr2.mutexPrivileged().exitWriteAccess();
        }
        if (log.toString().indexOf("no cache") >= 0) {
            fail("Everything shall be read from cache:\n" + log);
        }
        if (log.toString().indexOf("Reading cache") < 0) {
            fail("Cache shall be read:\n" + log);
        }

        Set<String> moduleNew = cnbs(mgr2.getModules());
        Set<String> moduleOld = cnbs(mgr.getModules());
        
        assertEquals("Same set of modules:", moduleOld, moduleNew);
        
        CountingSecurityManager.assertCounts("Do not access the module config files", 0);
    }
    private static Set<String> cnbs(Set<Module> modules) {
        TreeSet<String> set = new TreeSet<String>();
        for (Module m : modules) {
            set.add(m.getCodeNameBase());
        }
        return set;
    }
    
    /** Check that adding a new module via XML, as Auto Update does, works.
     * Written to help test #27106.
     */
    public void testAddNewModuleViaXML() throws Exception {
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            assertEquals(Collections.emptySet(), list.readInitial());
            assertEquals(Collections.emptySet(), mgr.getModules());
            list.trigger(Collections.<Module>emptySet());
            assertEquals(Collections.emptySet(), mgr.getModules());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        LoggedPCListener listener = new LoggedPCListener();
        mgr.addPropertyChangeListener(listener);
        modulesfolder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                // XXX this will require that there be an appropriate InstalledFileLocator in Lookup
                FileObject fooxml = modulesfolder.createData("org-foo", "xml");
                copy(new File(data, "org-foo.xml"), fooxml);
            }
        });
        assertTrue("PROP_MODULES fired", listener.waitForChange(mgr, ModuleManager.PROP_MODULES));
        mgr.mutexPrivileged().enterReadAccess();
        try {
            Set modules = mgr.getEnabledModules();
            assertEquals(1, modules.size());
            Module m = (Module)modules.iterator().next();
            assertEquals("org.foo", m.getCodeNameBase());
            assertTrue(m.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitReadAccess();
        }
    }
    
    // XXX try to read a nonempty initial list
    
    // XXX would be nice to also have test which uses a layer
    // to install and remove the Modules/ entries in a MFS
    // and check that layer-driven events are enough to cause
    // complex installations & uninstallations
    
    private static void copy(File a, FileObject b) throws IOException {
        OutputStream os = b.getOutputStream();
        try {
            copyStreams(new FileInputStream(a), os);
        } finally {
            os.close();
        }
    }

    private static class LoggedFileListener implements FileChangeListener {

        /** names of files that have changed: */
        private final Set<String> files = new HashSet<String>(100);

        private synchronized void change(FileEvent ev) {
            files.add(ev.getFile().getPath());
            notify();
        }

        public synchronized void waitForChanges() throws InterruptedException {
            wait(5000);
        }

        public synchronized boolean hasChange(String fname) {
            return files.contains(fname);
        }

        public synchronized boolean waitForChange(String fname) throws InterruptedException {
            while (!hasChange(fname)) {
                long start = System.currentTimeMillis();
                waitForChanges();
                if (System.currentTimeMillis() - start > 4000) {
                    //System.err.println("changes=" + changes);
                    return false;
                }
            }
            return true;
        }

        public void fileDeleted(FileEvent fe) {
            change(fe);
        }

        public void fileFolderCreated(FileEvent fe) {
            change(fe);
        }

        public void fileDataCreated(FileEvent fe) {
            change(fe);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore?
        }

        public void fileRenamed(FileRenameEvent fe) {
            change(fe);
        }

        public void fileChanged(FileEvent fe) {
            change(fe);
        }
    }

}
