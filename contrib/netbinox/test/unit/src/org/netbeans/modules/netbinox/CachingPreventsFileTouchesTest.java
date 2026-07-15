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

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.SetupHid;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.Places;
import org.openide.util.Lookup;

/**
 * Read access test
 * see details on http://wiki.netbeans.org/FitnessViaWhiteAndBlackList
 */
public class CachingPreventsFileTouchesTest extends NbTestCase {
    private static final Logger LOG = Logger.getLogger(CachingPreventsFileTouchesTest.class.getName());
    static {
        System.setProperty("java.awt.headless", "true");
    }

    private static void initCheckReadAccess() throws IOException {
        Set<String> allowedFiles = new HashSet<String>();
        CountingSecurityManager.initialize(null, CountingSecurityManager.Mode.CHECK_READ, allowedFiles);
    }
    
    public CachingPreventsFileTouchesTest(String name) {
        super(name);
    }
    
    public static Test suite() throws IOException {
        Locale.setDefault(Locale.US);
        CountingSecurityManager.initialize("none", CountingSecurityManager.Mode.CHECK_READ, null);
        System.setProperty("org.netbeans.Stamps.level", "ALL");
        System.setProperty(NbModuleSuite.class.getName() + ".level", "FINE");
        System.setProperty("org.netbeans.modules.netbinox.level", "FINE");

        NbTestSuite suite = new NbTestSuite();
        Compile compile = new Compile("testCompile");
        suite.addTest(compile);
        NbModuleSuite.Configuration common = NbModuleSuite.emptyConfiguration().clusters("(?!ergonomics).*").enableClasspathModules(false)
                .gui(false).honorAutoloadEager(true);
        {
            NbModuleSuite.Configuration conf = common.reuseUserDir(false).addTest(CachingPreventsFileTouchesTest.class, "testInitUserDir");
            suite.addTest(NbModuleSuite.create(conf));
        }
        {
            NbModuleSuite.Configuration conf = common.reuseUserDir(true).addTest(CachingPreventsFileTouchesTest.class, "testStartAgain");
            suite.addTest(NbModuleSuite.create(conf));
        }
        {
            NbModuleSuite.Configuration conf = common.reuseUserDir(true).addTest(CachingPreventsFileTouchesTest.class, "testStartOnceMore");
            suite.addTest(NbModuleSuite.create(conf));
        }

        suite.addTest(new CachingPreventsFileTouchesTest("testInMiddle"));

        {
            NbModuleSuite.Configuration conf = common.reuseUserDir(true).addTest(CachingPreventsFileTouchesTest.class, "testReadAccess", "testVerifyActivatorExecuted", "testRememberCacheDir");
            suite.addTest(NbModuleSuite.create(conf));
        }
        suite.addTest(new CachingPreventsFileTouchesTest("testCachesDontUseAbsolutePaths"));

        return suite;
    }

    public void testInitUserDir() throws Exception {
        File simpleModule = new File(System.getProperty("activate.jar"));

        File newModule = new File(new File(new File(System.getProperty("netbeans.user")), "modules"), "org-activate.jar");
        newModule.getParentFile().mkdirs();
        simpleModule.renameTo(newModule);
        assertTrue("New module correctly created", newModule.exists());

        class Activate implements FileSystem.AtomicAction {
            public void run() throws IOException {
                FileObject fo = FileUtil.getConfigFile("Modules").createData("org-activate.xml");
                OutputStream os = fo.getOutputStream();
                os.write((
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"\n" +
        "                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">\n" +
        "<module name=\"org.activate\">\n" +
        "    <param name=\"autoload\">false</param>\n" +
        "    <param name=\"eager\">false</param>\n" +
        "    <param name=\"enabled\">true</param>\n" +
        "    <param name=\"jar\">modules/org-activate.jar</param>\n" +
        "    <param name=\"reloadable\">false</param>\n" +
        "</module>\n" +
        "").getBytes());
                os.close();
            }
        }

        FileUtil.runAtomicAction(new Activate());


        Class<?> main = null;
        Object s = null;
        for (int i = 0; i < 360; i++) {
            LOG.log(Level.INFO, "testInitUserDir - waiting for activation {0}", i);
            try {
                main = Thread.currentThread().getContextClassLoader().loadClass("org.activate.Main");
                s = main.getField("start").get(null);
                if (s == null) {
                    Thread.sleep(500);
                    continue;
                }
            } catch (ClassNotFoundException ex) {
                Thread.sleep(500);
                continue;
            }
            break;
        }
        LOG.log(Level.INFO, "testInitUserDir - waiting for activation over. Testing.");
        assertNotNull("Activate module shall start", main);
        LOG.log(Level.INFO, "checking field from {0}", main);
        s = main.getField("start").get(null);
        assertNotNull("Bundle started, its context provided", s);

        CachingAndExternalPathsTest.doNecessarySetup();
        
        LOG.info("testInitUserDir - finished");
    }

    public void testStartAgain() throws Exception {
        CachingAndExternalPathsTest.doNecessarySetup();
        final String dirs = System.getProperty("netbeans.dirs");
        for (String s : dirs.split(File.pathSeparator)) {
            if (s.endsWith("ergonomics")) {
                fail("There should be no ergonomics cluster in netbeans.dirs: " + dirs);
            }
        }
        
        // will be reset next time the system starts
        System.getProperties().remove("netbeans.dirs");
        // initializes counting, but waits till netbeans.dirs are provided
        // by NbModuleSuite
    }

    public void testStartOnceMore() throws Exception {
        CachingAndExternalPathsTest.doNecessarySetup();
        // will be reset next time the system starts
        System.getProperties().remove("netbeans.dirs");
        // initializes counting, but waits till netbeans.dirs are provided
        // by NbModuleSuite
        LOG.info("testStartAgain - enabling initCheckReadAccess");
        initCheckReadAccess();
        LOG.info("testStartAgain - finished");
    }

    public void testInMiddle() {
        LOG.info("Previous run finished, starting another one");
        System.setProperty("activated.count", "0");
    }

    @RandomlyFails // NB-Core-Build #8003: expected:<0> but was:<2>
    public void testReadAccess() throws Exception {
        LOG.info("Inside testReadAccess");
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        try {
            Class<?> c = Class.forName("javax.help.HelpSet", true, l);
        } catch (ClassNotFoundException ex) {
            // never mind
        }
        try {
            if (CountingSecurityManager.isEnabled()) {
                CountingSecurityManager.assertCounts("No reads during startup", 0);
            } else {
               Logger.getLogger("TEST.testReadAccess").warning("Initialization mode, counting is disabled");
            }
        } catch (Error e) {
            e.printStackTrace(getLog("file-reads-report.txt"));
            throw e;
        }
    }

    public void testVerifyActivatorExecuted() {
        assertEquals("1", System.getProperty("activated.count"));
    }

    public void testRememberCacheDir() {
        File cacheDir = Places.getCacheDirectory();
        assertTrue("It is a directory", cacheDir.isDirectory());
        System.setProperty("mycache", cacheDir.getPath());
        
        File boot = InstalledFileLocator.getDefault().locate("lib/boot.jar", "org.netbeans.bootstrap", false);
        assertNotNull("Boot.jar found", boot);
        System.setProperty("myinstall", boot.getParentFile().getParentFile().getParentFile().getPath());
    }

    public void testCachesDontUseAbsolutePaths() throws Exception {
        String cache = System.getProperty("mycache");
        String install = System.getProperty("myinstall");
        
        assertNotNull("Cache found", cache);
        assertNotNull("Install found", install);
        
        File cacheDir = new File(cache);
        assertTrue("Cache dir is dir", cacheDir.isDirectory());
        int cnt = 0;
        final File[] arr = recursiveFiles(cacheDir, new ArrayList<File>());
        Collections.shuffle(Arrays.asList(arr));
        for (File f : arr) {
            // Same as in o.n.core.startup.layers.CachingPreventsFileTouchesTest
            if (!f.isDirectory() && !f.getName().equals("all-checksum.txt")) {
                cnt++;
                assertFileDoesNotContain(f, install);
            }
        }
        assertTrue("Some cache files found", cnt > 4);
    }
    
    private static File[] recursiveFiles(File dir, List<? super File> collect) {
        File[] arr = dir.listFiles();
        if (arr != null) {
            for (File f : arr) {
                if (f.isDirectory()) {
                    recursiveFiles(f, collect);
                } else {
                    collect.add(f);
                }
            }
        }
        return collect.toArray(new File[0]);
    }

    private static void assertFileDoesNotContain(File file, String text) throws IOException, PropertyVetoException {
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(file.getParentFile());
        FileObject fo = lfs.findResource(file.getName());
        assertNotNull("file object for " + file + " found", fo);
        String content = fo.asText();
        if (content.contains(text)) {
            fail("File " + file + " seems to contain '" + text + "'!");
        }
    }
    
    public static class Compile extends NbTestCase {
        private File simpleModule;

        public Compile(String name) {
            super(name);
        }

        public void testCompile() throws Exception {
            File data = new File(getDataDir(), "jars");
            File jars = new File(getWorkDir(), "jars");
            simpleModule = SetupHid.createTestJAR(data, jars, "activate", "activate");

            System.setProperty("activate.jar", simpleModule.getPath());
        }
    }
}
