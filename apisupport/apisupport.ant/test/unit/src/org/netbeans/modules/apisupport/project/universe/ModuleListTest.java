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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.api.EditableManifest;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleType;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.Mutex;
import org.openide.util.NbCollections;
import org.openide.util.test.TestFileUtils;

/**
 * Test functionality of ModuleList.
 * @author Jesse Glick
 */
public class ModuleListTest extends TestBase {
    
    public ModuleListTest(String name) {
        super(name);
    }
    
    private File suite1, suite2, standaloneSuite3;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        suite1 = resolveEEPFile("suite1");
        suite2 = resolveEEPFile("suite2");
        standaloneSuite3 = resolveEEPFile("suite3");
    }

    // #150856: CME on system props does happen ... but not when cloned first ...
    public void testConcurrentModificationOfSystemProperties2() throws InterruptedException {
        Thread t = new Thread(new Runnable() {

            public void run() {
                for (int i = 0; i < 20000; i++) {
                    System.setProperty("whatever", "anything" + i);
                }
            }
        });
        t.start();
        for (int i = 0; i < 2000; i++) {
            Map<String, String> props = NbCollections.checkedMapByCopy((Map) System.getProperties().clone(), String.class, String.class, false);
        }
        t.join();
    }

    // #150856: ... or just synchronized
    public void testConcurrentModificationOfSystemProperties3() throws InterruptedException {
        Thread t = new Thread(new Runnable() {

            public void run() {
                for (int i = 0; i < 20000; i++) {
                    System.setProperty("whatever", "anything" + i);
                }
            }
        });
        t.start();
        Properties p = System.getProperties();
        for (int i = 0; i < 2000; i++) {
            synchronized (p) {
                Map<String, String> props = NbCollections.checkedMapByCopy(p, String.class, String.class, false);
            }
        }
        t.join();
    }

    // XXX cannot be run in binary dist, requires sources; test against fake platform
    public void testParseProperties() throws Exception {
        File basedir = file("ant.browsetask");
        PropertyEvaluator eval = ModuleList.parseProperties(basedir, nbRootFile(), NbModuleType.NETBEANS_ORG, "org.netbeans.modules.ant.browsetask");
        String nbdestdir = eval.getProperty(ModuleList.NETBEANS_DEST_DIR);
        assertNotNull(nbdestdir);
        assertEquals(file("nbbuild/netbeans"), PropertyUtils.resolveFile(basedir, nbdestdir));
        assertEquals("modules/org-netbeans-modules-ant-browsetask.jar", eval.getProperty("module.jar"));
        assertEquals(file("nbbuild/netbeans/" + TestBase.CLUSTER_JAVA), PropertyUtils.resolveFile(basedir, eval.getProperty("cluster")));
        assertNull(eval.getProperty("suite.dir"));
        basedir = file("openide.loaders");
        eval = ModuleList.parseProperties(basedir, nbRootFile(), NbModuleType.NETBEANS_ORG, "org.openide.loaders");
        assertEquals("modules/org-openide-loaders.jar", eval.getProperty("module.jar"));
        basedir = new File(suite1, "action-project");
        eval = ModuleList.parseProperties(basedir, suite1, NbModuleType.SUITE_COMPONENT, "org.netbeans.examples.modules.action");
        nbdestdir = eval.getProperty(ModuleList.NETBEANS_DEST_DIR);
        assertNotNull(nbdestdir);
        assertEquals(file("nbbuild/netbeans"), PropertyUtils.resolveFile(basedir, nbdestdir));
        assertEquals(suite1, PropertyUtils.resolveFile(basedir, eval.getProperty("suite.dir")));
        basedir = new File(suite2, "misc-project");
        eval = ModuleList.parseProperties(basedir, suite2, NbModuleType.SUITE_COMPONENT, "org.netbeans.examples.modules.misc");
        nbdestdir = eval.getProperty(ModuleList.NETBEANS_DEST_DIR);
        assertNotNull(nbdestdir);
        assertEquals(file("nbbuild/netbeans"), PropertyUtils.resolveFile(basedir, nbdestdir));
        assertEquals(file(suite2, "build/cluster"), PropertyUtils.resolveFile(basedir, eval.getProperty("cluster")));
        assertEquals(suite2, PropertyUtils.resolveFile(basedir, eval.getProperty("suite.dir")));
        basedir = new File(standaloneSuite3, "dummy-project");
        eval = ModuleList.parseProperties(basedir, standaloneSuite3, NbModuleType.STANDALONE, "org.netbeans.examples.modules.dummy");
        nbdestdir = eval.getProperty(ModuleList.NETBEANS_DEST_DIR);
        assertNotNull(nbdestdir);
        assertEquals(file(standaloneSuite3, "nbplatform"), PropertyUtils.resolveFile(basedir, nbdestdir));
        assertEquals(file(standaloneSuite3, "dummy-project/build/cluster"), PropertyUtils.resolveFile(basedir, eval.getProperty("cluster")));
        assertNull(eval.getProperty("suite.dir"));
    }

    private class ModuleListLogHandler extends Handler {
        private Set<String> scannedDirs = Collections.synchronizedSet(new HashSet<String>(1000));
        String error;

        @Override
        public void publish(LogRecord record) {
            String msg = record.getMessage();
            assertFalse("Duplicate scan of project tree detected: " + msg,
                    msg.startsWith("Warning: two modules found with the same code name base"));
            if (msg.startsWith("scanPossibleProject: ") && msg.endsWith("scanned successfully")
                    && ! scannedDirs.add(msg)) {
                error = "scanPossibleProject already run: " + msg;
            }
            if (msg.startsWith("scanCluster: ") && msg.endsWith(" succeeded.")
                    && ! scannedDirs.add(msg)) {
                error = "scanCluster already run: " + msg;
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }

    public void testConcurrentScanningBinary() throws Exception {
        // now testing scan of binary clusters
        ModuleList.refresh();
        final ModuleList mlref[] = new ModuleList[1];
        Logger logger = Logger.getLogger(ModuleList.class.getName());
        Level origLevel = logger.getLevel();
        ModuleListLogHandler handler = new ModuleListLogHandler();
        assertTrue("NB dest. dir exists: " + destDirF, destDirF.exists());
        try {
            logger.setLevel(Level.ALL);
            logger.addHandler(handler);

            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        mlref[0] = ModuleList.findOrCreateModuleListFromBinaries(destDirF);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
            long start = System.currentTimeMillis();
            t.start();
            ModuleList ml = ModuleList.findOrCreateModuleListFromBinaries(destDirF);
            t.join();
            System.out.println("Concurrent scans took " + (System.currentTimeMillis() - start) + "msec.");
            assertNull(handler.error, handler.error);   // error is non-null when duplicate scan detected
            assertNotNull("Module list for dir " + destDirF + " returned null", ml);
            assertNotNull("Module list for dir " + destDirF + " returned null", mlref[0]);
            assertTrue("No clusters scanned.", handler.scannedDirs.size() > 0);
            System.out.println("Total " + handler.scannedDirs.size() + " clusters scanned.");
            // XXX Some more possible concurrent scans could be tested, not that easy to set up,
            // e.g. ML#findOrCreateModuleListFromSuite, ...FromStandaloneModule, ...
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(origLevel);
        }
    }

    public void testFindModulesInSuite() throws Exception {
        assertEquals("correct modules in suite1", new HashSet<File>(Arrays.asList(
            file(suite1, "action-project"),
            file(suite1, "support/lib-project")
        )), new HashSet<File>(Arrays.asList(ModuleList.findModulesInSuite(suite1))));
        assertEquals("correct modules in suite2", new HashSet<File>(Arrays.asList(
            file(suite2, "misc-project")
        )), new HashSet<File>(Arrays.asList(ModuleList.findModulesInSuite(suite2))));
    }

//    XXX: failing test, fix or delete (based on existing NB.org modules, better delete)
//    public void testNetBeansOrgEntries() throws Exception {
//        long start = System.currentTimeMillis();
//        ModuleList ml = ModuleList.getModuleList(file("ant.browsetask")); // should be arbitrary
//        System.err.println("Time to scan netbeans.org sources: " + (System.currentTimeMillis() - start) + "msec");
//        System.err.println("Directories traversed: " + ModuleList.directoriesChecked);
//        System.err.println("XML files parsed: " + ModuleList.xmlFilesParsed + " in " + ModuleList.timeSpentInXmlParsing + "msec");
//        ModuleEntry e = ml.getEntry("org.netbeans.modules.java.project");
//        assertNotNull("have org.netbeans.modules.java.project", e);
//        assertEquals("right jarLocation", file("nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/modules/org-netbeans-modules-java-project.jar"), e.getJarLocation());
//        assertTrue("in all entries", ml.getAllEntries().contains(e));
//        assertEquals("right path", "java.project", e.getNetBeansOrgPath());
//        assertEquals("right source location", file("java.project"), e.getSourceLocation());
//        assertTrue("same by JAR", ModuleList.getKnownEntries(e.getJarLocation()).contains(e));
//        /* will fail if nbbuild/netbeans/nbproject/private/scan-cache-full.ser exists:
//        assertTrue("same by other random file", ModuleList.getKnownEntries(file("nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/config/Modules/org-netbeans-modules-java-project.xml")).contains(e));
//         */
//        assertEquals("right codeNameBase", "org.netbeans.modules.java.project", e.getCodeNameBase());
//        assertEquals(file("nbbuild/netbeans"), e.getDestDir());
//        assertEquals("", e.getClassPathExtensions());
//        assertNotNull("localized name", e.getLocalizedName());
//        assertNotNull("display category", e.getCategory());
//        assertNotNull("short description", e.getShortDescription());
//        assertNotNull("long description", e.getLongDescription());
//        assertNotNull("release version", e.getReleaseVersion());
//        assertNotNull("specification version", e.getSpecificationVersion());
//        assertEquals("number of public packages for " + e, new Integer(7), new Integer(e.getPublicPackages().length));
//        assertFalse("not deprecated", e.isDeprecated());
//        // Test something in a different cluster and dir:
//        e = ml.getEntry("org.openide.filesystems");
//        assertNotNull("have org.openide.filesystems", e);
//        assertEquals("right jarLocation", file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"), e.getJarLocation());
//        assertEquals("right source location", file("openide.filesystems"), e.getSourceLocation());
//        assertTrue("same by JAR", ModuleList.getKnownEntries(e.getJarLocation()).contains(e));
//        assertEquals("right path", "openide.filesystems", e.getNetBeansOrgPath());
//        // Test class-path extensions:
//        e = ml.getEntry("org.netbeans.libs.xerces");
//        assertNotNull(e);
//        assertEquals("correct CP extensions (using <binary-origin> and relative paths)",
//            ":" + file("libs.xerces/external/xerces-2.8.0.jar"),
//            e.getClassPathExtensions());
//        /* XXX unmaintained:
//        e = ml.getEntry("javax.jmi.model");
//        assertNotNull(e);
//        assertEquals("correct CP extensions (using <binary-origin> and property substitutions #1)",
//            ":" + file("mdr/external/mof.jar"),
//            e.getClassPathExtensions());
//         */
//        /* XXX org.netbeans.modules.css moved to "org.netbeans.modules.languages.css?
//        e = ml.getEntry("org.netbeans.modules.css");
//        assertNotNull(e);
//        assertEquals("correct CP extensions (using <binary-origin> and property substitutions #2)",
//            ":" + file("xml/external/flute.jar") + ":" + file("xml/external/sac.jar"),
//            e.getClassPathExtensions());
//         */
//        e = ml.getEntry("org.netbeans.modules.xml.tax");
//        assertNotNull(e);
//        assertEquals("correct CP extensions (using runtime-relative-path)",
//            ":" + file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/ext/org-netbeans-tax.jar"),
//            e.getClassPathExtensions());
//        e = ml.getEntry("org.openide.util.enumerations");
//        assertNotNull(e);
//        assertTrue("this one is deprecated", e.isDeprecated());
//        e = ml.getEntry("org.netbeans.modules.projectui");
//        assertNotNull(e);
//        assertNotNull(e.getProvidedTokens());
//        assertTrue("There are some provided tokens", e.getProvidedTokens().length > 0);
//        // XXX test that getAllEntries() also includes nonstandard modules, and so does getKnownEntries() if necessary
//    }

    // XXX cannot be run in binary dist, requires sources; test against fake platform
    public void testExternalEntries() throws Exception {
        // Start with suite1 - should find also nb_all.
        long start = System.currentTimeMillis();
        ModuleList ml = ModuleList.getModuleList(file(suite1, "support/lib-project"));
        System.err.println("Time to scan suite + NB binaries: " + (System.currentTimeMillis() - start) + "msec");
        ModuleEntry e = ml.getEntry("org.netbeans.examples.modules.action");
        assertNotNull("action-project found", e);
        File jar = resolveEEPFile("/suite1/build/cluster/modules/org-netbeans-examples-modules-action.jar");
        assertEquals("right JAR location", jar, e.getJarLocation());
        assertTrue("in all entries", ml.getAllEntries().contains(e));
        assertNull("no nb.org path", e.getNetBeansOrgPath());
        assertEquals("right source location", file(suite1, "action-project"), e.getSourceLocation());
        assertTrue("same by JAR", ModuleList.getKnownEntries(e.getJarLocation()).contains(e));
        assertEquals("right codeNameBase", "org.netbeans.examples.modules.action", e.getCodeNameBase());
        e = ml.getEntry("org.netbeans.modules.classfile");
        assertNotNull("can find nb.org sources too (classfile module must be built)", e);
        assertEquals("correct nb.org source location", file("java/classfile"), e.getSourceLocation());
        assertNotNull("localized name", e.getLocalizedName());
        assertNotNull("display category", e.getCategory());
        assertNotNull("short description", e.getShortDescription());
        assertNotNull("long description", e.getLongDescription());
        assertNotNull("release version", e.getReleaseVersion());
        assertNotNull("specification version", e.getSpecificationVersion());
        assertNotNull(e.getProvidedTokens());
        assertEquals("there is just provided cnb token", 1, e.getProvidedTokens().length);
        assertEquals("cnb." + e.getCodeNameBase(), e.getProvidedTokens()[0]);
        /*
        e = ml.getEntry("org.netbeans.examples.modules.misc");
        assertNotNull("can find sources from another suite (misc must have been built first)", e);
        assertEquals("correct source location", file(suite2, "misc-project"), e.getSourceLocation());
        assertEquals("number of public packages for " + e, new Integer(1), new Integer(e.getPublicPackages().length));
         */
        e = ml.getEntry("org.netbeans.libs.xerces");
        assertEquals("correct CP exts for a nb.org module (using Class-Path only)",
            ":" + file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/ext/xerces-2.8.0.jar"),
            e.getClassPathExtensions());
        // From suite2, can only find itself, and netbeans.org modules only available in binary form.
        ml = ModuleList.getModuleList(file(suite2, "misc-project"));
        e = ml.getEntry("org.netbeans.examples.modules.misc");
        assertNotNull("can find module from my own suite", e);
        assertEquals("correct JAR location", resolveEEPFile("/suite2/build/cluster/modules/org-netbeans-examples-modules-misc.jar"), e.getJarLocation());
        assertNotNull("localized name", e.getLocalizedName());
        assertNotNull("display category", e.getCategory());
        assertNotNull("short description", e.getShortDescription());
        assertNotNull("long description", e.getLongDescription());
        assertEquals("right codeNameBase", "org.netbeans.examples.modules.misc", e.getCodeNameBase());
        assertNotNull("release version", e.getReleaseVersion());
        assertNotNull("specification version", e.getSpecificationVersion());
        assertNotNull(e.getProvidedTokens());
        assertEquals("there is just provided cnb token", 1, e.getProvidedTokens().length);
        assertEquals("cnb." + e.getCodeNameBase(), e.getProvidedTokens()[0]);
        assertEquals("number of public packages for " + e, new Integer(1), new Integer(e.getPublicPackages().length));
        e = ml.getEntry("org.netbeans.libs.xerces");
        assertNotNull("can find nb.org binary module too", e);
        assertEquals("have sources for that", file("ide/libs.xerces"), e.getSourceLocation());
        assertEquals("and correct JAR location", file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-libs-xerces.jar"), e.getJarLocation());
        assertEquals("and correct CP exts (using Class-Path only)",
            ":" + file("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/ext/xerces-2.8.0.jar"),
            e.getClassPathExtensions());
        e = ml.getEntry("org.openide.util");
        assertNotNull(e);
        assertFalse("binary API not deprecated", e.isDeprecated());
        e = ml.getEntry("org.openide.util.enumerations");
        assertNotNull(e);
        assertTrue("this one is deprecated", e.isDeprecated());
        // From suite3, can find itself and netbeans.org modules in binary form.
        ml = ModuleList.getModuleList(file(standaloneSuite3, "dummy-project"));
        e = ml.getEntry("org.netbeans.examples.modules.dummy");
        assertNotNull("can find myself", e);
        e = ml.getEntry("org.netbeans.modules.classfile");
        assertNotNull("found (fake) nb.org module", e);
        assertNull("...without sources", e.getSourceLocation());
        assertEquals("and with a special JAR location", file(standaloneSuite3, "nbplatform/random/modules/random.jar"), e.getJarLocation());
        assertEquals("correct CP extensions (using Class-Path only, and ignoring sources completely)",
            ":" + file(standaloneSuite3, "nbplatform/random/modules/ext/stuff.jar"),
            e.getClassPathExtensions());
    }
    
    public void testNewlyAddedModule() throws Exception {
        // XXX make new module, call refresh, check that things work
        // (partially tested already by NbModuleProjectGeneratorTest.testCreateSuiteComponentModule)
    }
    
    public void testFindNetBeansOrg() throws Exception {
        assertEquals(nbRootFile(), ModuleList.findNetBeansOrg(file("ide/xml.tax")));
        assertEquals(nbRootFile(), ModuleList.findNetBeansOrg(file("ide/xml.tax/lib")));
        assertEquals(null, ModuleList.findNetBeansOrg(File.listRoots()[0]));
    }
    
    public void testRefreshSuiteModuleList() throws Exception {
        SuiteProject suite = generateSuite("suite1");
        final NbModuleProject p = TestBase.generateSuiteComponent(suite, "module1a");
        ModuleList ml = ModuleList.getModuleList(
                p.getProjectDirectoryFile(),
                NbPlatform.getDefaultPlatform().getDestDir());
        assertNotNull("module1a is in the suite1's module list", ml.getEntry("org.example.module1a"));
        assertEquals("no public packages in the ModuleEntry", 0, ml.getEntry("org.example.module1a").getPublicPackages().length);
    
        // added package must be reflected in the refreshed list (63561)
        Boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                ProjectXMLManager pxm = new ProjectXMLManager(p);
                pxm.replacePublicPackages(Collections.singleton("org.example.module1a"));
                return true;
            }
        });
        assertTrue("replace public packages", result);
        ProjectManager.getDefault().saveProject(p);
    }

    public void testSpecVersionBaseSourceEntries() throws Exception { // #72463
        SuiteProject suite = generateSuite("suite");
        NbModuleProject p = TestBase.generateSuiteComponent(suite, "module");
        ModuleList ml = ModuleList.getModuleList(p.getProjectDirectoryFile());
        ModuleEntry e = ml.getEntry("org.example.module");
        assertNotNull("have entry", e);
        assertEquals("right initial spec vers from manifest", "1.0", e.getSpecificationVersion());
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(SingleModuleProperties.SPEC_VERSION_BASE, "1.1.0");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        EditableManifest em = Util.loadManifest(p.getManifestFile());
        em.removeAttribute(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, null);
        Util.storeManifest(p.getManifestFile(), em);
        ProjectManager.getDefault().saveProject(p);
        assertEquals("right spec.version.base", "1.1", e.getSpecificationVersion());
        ep.setProperty(SingleModuleProperties.SPEC_VERSION_BASE, "1.2.0");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(p);
        assertEquals("right modified spec.version.base", "1.2", e.getSpecificationVersion());
    }

    public void testNormalizedClassPathExtensions() throws Exception { // #199904
        File cluster = new File(getWorkDir(), "cluster");
        TestFileUtils.writeZipFile(new File(cluster, "modules/m.jar"), "META-INF/MANIFEST.MF:OpenIDE-Module: m\nClass-Path: ../x.jar\n");
        ModuleList ml = ModuleList.scanCluster(cluster, null, false, null);
        ModuleEntry e = ml.getEntry("m");
        assertNotNull(e);
        assertEquals(File.pathSeparator + new File(cluster, "x.jar"), e.getClassPathExtensions());
    }

}
