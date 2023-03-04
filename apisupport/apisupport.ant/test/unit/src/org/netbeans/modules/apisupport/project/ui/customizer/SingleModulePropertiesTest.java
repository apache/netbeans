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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.ModuleDependency;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.api.EditableManifest;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerComponentFactory.PublicPackagesTableModel;
import org.netbeans.modules.apisupport.project.universe.HarnessVersion;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.test.RestrictThreadCreation;
import org.openide.util.test.TestFileUtils;

// XXX mkrauskopf: don't use libs/xerces for testing purposes of apisupport
// since it could fail with a new version of xerces lib! Generate or create some
// testing modules in apisupport testing data section instead.

/**
 * Tests {@link SingleModuleProperties}. Actually also for some classes which
 * SingleModuleProperties utilizes - which doesn't mean they shouldn't be tested
 * individually :)
 *
 * @author Martin Krauskopf
 */
public class SingleModulePropertiesTest extends TestBase {
    
    public SingleModulePropertiesTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        noDataDir = true;
        clearWorkDir();
        super.setUp();
        //RestrictThreadCreation.permitStandard();
        //RestrictThreadCreation.permit("org.netbeans.modules.masterfs.GlobalVisibilityQueryImpl.getIgnoredFiles","org.openide.util.lookup.MetaInfServicesLookup.beforeLookup");
        //RestrictThreadCreation.forbidNewThreads(true);
    }

    @Override protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    /** Tests few basic properties to be sure that loading works. */
    public void testThatBasicPropertiesAreLoaded() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        SingleModuleProperties props = loadProperties(p);
        assertNotNull(props.getActivePlatform());
        assertNotNull("loading bundle info", props.getBundleInfo());
        assertEquals("display name", "Testing Module", props.getBundleInfo().getDisplayName());
        assertEquals("cnb", "org.example.module1", props.getCodeNameBase());
        assertNull("no impl. version", props.getImplementationVersion());
        assertTrue("jar file", props.getJarFile().endsWith("org-example-module1.jar"));
        assertEquals("major release version", null, props.getMajorReleaseVersion());
        assertEquals("spec. version", "1.0", props.getSpecificationVersion());
    }
    
    public void testThatPropertiesAreRefreshed() throws Exception {
        doTestThatPropertiesAreRefreshed(false);
    }

    private SingleModuleProperties doTestThatPropertiesAreRefreshed(boolean osgi) throws Exception {
        NbModuleProject p = generateStandaloneModule(getWorkDir(), "module1", osgi);
        SingleModuleProperties props = loadProperties(p);
        assertEquals(osgi, props.isOSGi());
        assertEquals("spec. version", "1.0", props.getSpecificationVersion());
        assertEquals("display name", "Testing Module", props.getBundleInfo().getDisplayName());
        int deps;
        if (osgi) {
            assertEquals("OSGi modules have one dependency", 1, props.getDependenciesListModel().getSize());
            Object dep = props.getDependenciesListModel().getElementAt(0);
            ModuleDependency me = (ModuleDependency)dep;
            assertEquals("Depends on OSGi libs", "org.netbeans.libs.osgi", me.getModuleEntry().getCodeNameBase());
            assertTrue("Compile dep", me.hasCompileDependency());
            deps = 1;
        } else {
            assertEquals("number of dependencies", 0, props.getDependenciesListModel().getSize());
            deps = 0;
        }
        
        // silently change manifest
        InputStream is = new FileInputStream(props.getManifestFile());
        EditableManifest em = new EditableManifest();
        try {
            em = new EditableManifest(is);
        } finally {
            is.close();
        }
        em.setAttribute(osgi ? ManifestManager.BUNDLE_VERSION : ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, "1.1", null);
        OutputStream os = new FileOutputStream(props.getManifestFile());
        try {
            em.write(os);
        } finally {
            os.close();
        }
        
        // silently change bundle
        EditableProperties ep = new EditableProperties(false);
        is = new FileInputStream(props.getBundleInfo().getPaths()[0]);
        try {
            ep.load(is);
        } finally {
            is.close();
        }
        ep.setProperty(LocalizedBundleInfo.NAME, "Miscellaneous");
        os = new FileOutputStream(props.getBundleInfo().getPaths()[0]);
        try {
            ep.store(os);
        } finally {
            os.close();
        }
        
        // modify project.xml
        ApisupportAntUtils.addDependency(p, "org.netbeans.modules.java.project", "1", null, false, null);
        ProjectManager.getDefault().saveProject(p);
        
        simulatePropertiesOpening(props, p);
        
        // check that manifest and bundle has been reloaded
        assertEquals("spec. version", "1.1", props.getSpecificationVersion());
        assertEquals("display name should be changed", "Miscellaneous", props.getBundleInfo().getDisplayName());
        assertEquals("One dependency added", deps + 1, props.getDependenciesListModel().getSize());
        return props;
    }

    public void testThatPropertiesAreRefreshedInOSGiMode() throws Exception {
        SingleModuleProperties props = doTestThatPropertiesAreRefreshed(true);
        Manifest mf = new Manifest(new FileInputStream(props.getManifestFile()));

        for (Object s : mf.getMainAttributes().keySet()) {
            if (s.toString().equals("OpenIDE-Module-Layer")) {
                continue;
            }
            if (s.toString().startsWith("OpenIDE")) {
                fail("Unexpected OpenIDE attribute in manifest: " + s);
            }
        }
    }

    @RandomlyFails // NB-Core-Build #7732: display name was refreshed in ProjectInformation expected:<[Miscellaneous]> but was:<[Testing Module]>
    public void testThatPropertiesListen() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        SingleModuleProperties props = loadProperties(p);
        assertEquals("display name from ProjectInformation", "Testing Module",
                ProjectUtils.getInformation(p).getDisplayName());
        assertEquals("display name from LocalizedBundleInfo", "Testing Module",
                props.getBundleInfo().getDisplayName());
        
        FileObject bundleFO = FileUtil.toFileObject(props.getBundleInfo().getPaths()[0]);
        EditableProperties bundleEP = Util.loadProperties(bundleFO);
        bundleEP.setProperty(LocalizedBundleInfo.NAME, "Miscellaneous");
        // let's fire a change
        Util.storeProperties(bundleFO, bundleEP);
        
        // display name should be refreshed
        assertEquals("display name was refreshed in ProjectInformation", "Miscellaneous",
                ProjectUtils.getInformation(p).getDisplayName());
        assertEquals("display name was refreshed in LocalizedBundleInfo", "Miscellaneous",
                props.getBundleInfo().getDisplayName());
    }
    
    public void testGetPublicPackages() throws Exception {
        final NbModuleProject p = generateStandaloneModule("module1");
        FileUtil.createData(p.getSourceDirectory(), "org/example/module1/One.java");
        FileUtil.createData(p.getSourceDirectory(), "org/example/module1/resources/Two.java");
        
        // apply and save project
        boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                ProjectXMLManager pxm = new ProjectXMLManager(p);
                pxm.replacePublicPackages(Collections.singleton("org.example.module1"));
                return true;
            }
        });
        assertTrue("replace public packages", result);
        ProjectManager.getDefault().saveProject(p);
        
        SingleModuleProperties props = loadProperties(p);
        PublicPackagesTableModel pptm = props.getPublicPackagesModel();
        assertEquals("number of available public packages", 2, pptm.getRowCount());
        assertEquals("number of selected public packages", 1, pptm.getSelectedPackages().size());
    }

    // cannot be run in binary dist, requires sources; test against fake platform
    public void testGetPublicPackagesForNBOrg() throws Exception {
        // libs.xerces properties
        NbModuleProject libP = (NbModuleProject) ProjectManager.getDefault().findProject(nbRoot().getFileObject("ide/libs.xerces"));
        SingleModuleProperties props = loadProperties(libP);
        PublicPackagesTableModel pptm = props.getPublicPackagesModel();
        assertEquals("number of available public packages", 38, pptm.getRowCount());
    }
    
    public void testThatProjectWithoutBundleDoesNotThrowNPE_61469() throws Exception {
        FileObject pFO = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "module1");
        FileObject propsFO = FileUtil.toFileObject(new File(getWorkDir(),
                "module1/src/org/example/module1/resources/Bundle.properties"));
        propsFO.delete();
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(pFO);
        SingleModuleProperties props = loadProperties(p);
        simulatePropertiesOpening(props, p);
    }

    public void testThatManifestFormattingIsNotMessedUp_61248() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        EditableManifest em = Util.loadManifest(p.getManifestFile());
        em.setAttribute(ManifestManager.OPENIDE_MODULE_REQUIRES, "\n" +
                "  org.openide.execution.ExecutionEngine,\n" +
                "  org.openide.windows.IOProvider", null);
        Util.storeManifest(p.getManifestFile(), em);
        String before = TestBase.slurp(p.getManifestFile());

        SingleModuleProperties props = loadProperties(p);
        // two lines below are ensured by CustomizerVersioning - let's simulate it
        props.setImplementationVersion("");
        props.setProvidedTokens("");
        props.storeProperties();
        ProjectManager.getDefault().saveProject(p);
        String after = TestBase.slurp(p.getManifestFile());

        assertEquals("the same content", before, after);
    }

    public void testNiceFormattingForRequiredTokensInManifest_63516() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        EditableManifest em = Util.loadManifest(p.getManifestFile());
        em.setAttribute(ManifestManager.OPENIDE_MODULE_REQUIRES, "\n" +
                "  org.openide.execution.ExecutionEngine,\n" +
                "  org.openide.windows.IOProvider", null);
        Util.storeManifest(p.getManifestFile(), em);

        SingleModuleProperties props = loadProperties(p);
        props.getRequiredTokenListModel().addToken("org.netbeans.api.javahelp.Help");
        // two lines below are ensured by CustomizerVersioning - let's simulate it
        props.setImplementationVersion("");
        props.setProvidedTokens("");
        props.storeProperties();
        ProjectManager.getDefault().saveProject(p);
        String real = TestBase.slurp(p.getManifestFile());
        String newline = System.getProperty("line.separator");
        String expected = "Manifest-Version: 1.0" + newline +
                "AutoUpdate-Show-In-Client: true" + newline +
                "OpenIDE-Module: org.example.module1" + newline +
                "OpenIDE-Module-Layer: org/example/module1/resources/layer.xml" + newline +
                "OpenIDE-Module-Localizing-Bundle: org/example/module1/resources/Bundle.properties" + newline +
                "OpenIDE-Module-Requires: " + newline +
                "  org.netbeans.api.javahelp.Help," + newline +
                "  org.openide.execution.ExecutionEngine," + newline +
                "  org.openide.windows.IOProvider" + newline +
                "OpenIDE-Module-Specification-Version: 1.0" + newline + newline;

        assertEquals("expected content", expected, real);

        props.getRequiredTokenListModel().removeToken("org.openide.execution.ExecutionEngine");
        props.getRequiredTokenListModel().removeToken("org.netbeans.api.javahelp.Help");
        props.storeProperties();
        ProjectManager.getDefault().saveProject(p);
        real = TestBase.slurp(p.getManifestFile());
        expected = "Manifest-Version: 1.0" + newline +
                "AutoUpdate-Show-In-Client: true" + newline +
                "OpenIDE-Module: org.example.module1" + newline +
                "OpenIDE-Module-Layer: org/example/module1/resources/layer.xml" + newline +
                "OpenIDE-Module-Localizing-Bundle: org/example/module1/resources/Bundle.properties" + newline +
                "OpenIDE-Module-Requires: org.openide.windows.IOProvider" + newline +
                "OpenIDE-Module-Specification-Version: 1.0" + newline + newline;

        assertEquals("expected content", expected, real);
    }
    
    public void testAvailablePublicPackages() throws Exception {
        Map<String,String> contents = new HashMap<String,String>();
        contents.put("lib/pkg/Clazz3.class", "");
        contents.put("lib/pkg2/Clazz4.class", "");
        contents.put("1.0/oldlib/Clazz5.class", ""); // #72669
        File jar = new File(getWorkDir(), "some.jar");
        createJar(jar, contents, new Manifest());
        SuiteProject sweet = generateSuite("sweet");
        File moduleDir = new File(getWorkDir(), "module");
        NbModuleProjectGenerator.createSuiteLibraryModule(
                moduleDir, "module", "Module", "module/Bundle.properties",
                sweet.getProjectDirectoryFile(), null, new File[] {jar});
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(moduleDir));
        FileObject srcDir = p.getProjectDirectory().getFileObject("src");
        FileUtil.createData(srcDir, "pkg1/Clazz1.java");
        FileUtil.createData(srcDir, "pkg1/Clazz2.java");
        FileUtil.createData(srcDir, "pkg2/CVS/#1.20#Clazz1.java");
        FileUtil.createData(srcDir, "pkg2/Clazz1.java");
        FileUtil.createData(srcDir, "pkg2/deeper/Clazz1.java");
        FileUtil.createData(srcDir, "pkg2/deeper/and/deeper/Clazz1.java");
        FileUtil.createData(srcDir, ".broken/Clazz.java"); // #72669
        assertEquals(Arrays.asList("lib.pkg", "lib.pkg2", "pkg1", "pkg2", "pkg2.deeper", "pkg2.deeper.and.deeper"),
                new ArrayList<String>(SingleModuleProperties.getInstance(p).getAvailablePublicPackages()));
    }
    
    public void testPublicPackagesAreUpToDate_63561() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        final NbModuleProject p = TestBase.generateSuiteComponent(suite1, "module1a");
        FileUtil.createData(p.getSourceDirectory(), "org/example/module1a/Dummy.java");
        SingleModuleProperties props = loadProperties(p);
        PublicPackagesTableModel pptm = props.getPublicPackagesModel();
        assertEquals("number of available public packages", 1, pptm.getRowCount());
        assertEquals("number of selected public packages", 0, pptm.getSelectedPackages().size());
        assertEquals("no public packages in the ModuleEntry", 0, props.getModuleList().getEntry("org.example.module1a").getPublicPackages().length);
        
        // apply and save project
        boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws IOException {
                ProjectXMLManager pxm = new ProjectXMLManager(p);
                pxm.replacePublicPackages(Collections.singleton("org.example.module1a"));
                return true;
            }
        });
        assertTrue("replace public packages", result);
        ProjectManager.getDefault().saveProject(p);
        
        simulatePropertiesOpening(props, p);
        
        pptm = props.getPublicPackagesModel();
        assertEquals("number of available public packages", 1, pptm.getRowCount());
        assertEquals("number of selected public packages", 1, pptm.getSelectedPackages().size());
        assertEquals("one public packages in the ModuleEntry", 1, props.getModuleList().getEntry("org.example.module1a").getPublicPackages().length);
    }
    
    
    /** Test that a module doesn't offer itself in its dependency list. */
    /* FIXME: Unable to run this test in EQ
    public void testThatTheModuleDoesNotOfferItself_61232() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        SingleModuleProperties props = loadProperties(p);
        for (ModuleDependency dependency : props.getUniverseDependencies(true)) {
            ModuleEntry me = dependency.getModuleEntry();
            assertFalse("module doesn't offer itself in its dependency list: " + p.getCodeNameBase(),
                    p.getCodeNameBase().equals(me.getCodeNameBase()));
        }
    }
    */
    
    public void testGetAvailableFriends() throws Exception {
        // standalone
        NbModuleProject standAlone = generateStandaloneModule("module1");
        SingleModuleProperties props = loadProperties(standAlone);
        assertEquals("There are no friends for standalone module.", 0, props.getAvailableFriends().length);
        
        // suitecomponent
        SuiteProject suite1 = generateSuite("suite1");
        TestBase.generateSuiteComponent(suite1, "component1");
        NbModuleProject component2 = TestBase.generateSuiteComponent(suite1, "component2");
        TestBase.generateSuiteComponent(suite1, "component3");
        props = loadProperties(component2);
        assertEquals("There are two available friends for component2.", 2, props.getAvailableFriends().length);
    }

    // XXX cannot be run in binary dist, requires sources; test against fake platform
    /* FIXME: Unable to run this test in EQ
    public void testGetAvailableFriendsForNBOrg() throws Exception {
        // netbeans.org
        Project javaProject = ProjectManager.getDefault().findProject(nbRoot().getFileObject("java/java.project"));
        SingleModuleProperties props = loadProperties((NbModuleProject) javaProject);
        assertTrue("There are two available friends for component2.", props.getAvailableFriends().length > 50);
    }
    */

    public void testSimulateLocalizedBundlePackageRefactoring() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        SingleModuleProperties props = loadProperties(p);
        assertEquals("display name from ProjectInformation", "Testing Module",
                ProjectUtils.getInformation(p).getDisplayName());
        assertEquals("display name from LocalizedBundleInfo", "Testing Module",
                props.getBundleInfo().getDisplayName());
        
        // rename package
        FileObject pDir = p.getProjectDirectory();
        FileObject pkg = pDir.getFileObject("src/org/example/module1");
        FileLock lock = pkg.lock();
        pkg.rename(lock, "module1Renamed", null);
        lock.releaseLock();
        System.gc();    // no more random
        FileObject manifestFO = pDir.getFileObject("manifest.mf");

        // change manifest
        EditableManifest mf = Util.loadManifest(manifestFO);
        mf.setAttribute(ManifestManager.OPENIDE_MODULE_LOCALIZING_BUNDLE, "org/example/module1Renamed/resources/Bundle.properties", null);
        Util.storeManifest(manifestFO, mf);
        simulatePropertiesOpening(props, p);
        
        // make sure that properties are not damaged
        assertEquals("display name was refreshed in ProjectInformation", "Testing Module",
                ProjectUtils.getInformation(p).getDisplayName());
        assertEquals("display name was refreshed in LocalizedBundleInfo", "Testing Module",
                props.getBundleInfo().getDisplayName());
    }
    
    public void testSimulateIllLocalizedBundlePackageRefactoring_67961() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        SingleModuleProperties props = loadProperties(p);
        assertEquals("display name from ProjectInformation", "Testing Module",
                ProjectUtils.getInformation(p).getDisplayName());
        assertEquals("display name from LocalizedBundleInfo", "Testing Module",
                props.getBundleInfo().getDisplayName());
        
        // change manifest (will fire a change event before the package is actually renamed)
        FileObject pDir = p.getProjectDirectory();
        FileObject manifestFO = pDir.getFileObject("manifest.mf");
        EditableManifest mf = Util.loadManifest(manifestFO);
        mf.setAttribute(ManifestManager.OPENIDE_MODULE_LOCALIZING_BUNDLE, "org/example/module1Renamed/resources/Bundle.properties", null);
        Util.storeManifest(manifestFO, mf);
        
        // rename package
        FileObject pkg = pDir.getFileObject("src/org/example/module1");
        FileLock lock = pkg.lock();
        pkg.rename(lock, "module1Renamed", null);
        lock.releaseLock();
        
        simulatePropertiesOpening(props, p);
        
        // make sure that properties are not damaged
        assertEquals("display name was refreshed in ProjectInformation", "Testing Module",
                ProjectUtils.getInformation(p).getDisplayName());
        assertEquals("display name was refreshed in LocalizedBundleInfo", "Testing Module",
                props.getBundleInfo().getDisplayName());
    }
    
    public void testResolveFile() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        SingleModuleProperties props = loadProperties(p);
        assertTrue("manifest exist", props.evaluateFile("manifest.mf").exists());
        assertTrue("manifest exist", props.evaluateFile(props.getProjectDirectory() + "/manifest.mf").exists());
        assertTrue("manifest exist", props.evaluateFile("${basedir}/manifest.mf").exists());
        assertFalse("non-existing file", props.evaluateFile("non-existing").exists());
        assertFalse("invalid reference", props.evaluateFile("${invalid-reference}/manifest.mf").exists());
    }
    
    public void testThatFilesAreNotTouched_67249() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        FileUtil.createData(p.getSourceDirectory(), "org/example/module1/One.java");
        FileUtil.createData(p.getSourceDirectory(), "org/example/module1/resources/Two.java");
        SingleModuleProperties props = loadProperties(p);
        
        // times before change
        FileObject bundle = FileUtil.toFileObject(props.getBundleInfo().getPaths()[0]);
        FileObject mf = p.getManifestFile();
        long mfTime = mf.lastModified().getTime();
        long bundleTime = bundle.lastModified().getTime();
        
        // be sure we are not too fast
        Thread.sleep(2000);
        
        // select a package
        props.getPublicPackagesModel().setValueAt(Boolean.TRUE, 0, 0);
        props.storeProperties();
        
        // compare with times after change
        assertEquals("time for manifest has not changed", mfTime, mf.lastModified().getTime());
        assertEquals("time for bundle has not changed", bundleTime, bundle.lastModified().getTime());
    }
    
    /* FIXME: Unable to run this test in EQ
    public void testGetUniverseDependencies() throws Exception {
        SuiteProject suite = generateSuite("suite");
        
        NbModuleProject testPrj = generateSuiteComponent(suite, "testPrj");
        
        NbModuleProject apiPrj = generateSuiteComponent(suite, "apiPrj");
        FileUtil.createData(apiPrj.getProjectDirectory(), "src/api/Util.java");
        SingleModuleProperties apiPrjProps = SingleModulePropertiesTest.loadProperties(apiPrj);
        apiPrjProps.getPublicPackagesModel().setValueAt(Boolean.TRUE, 0, 0);
        apiPrjProps.storeProperties();
        ProjectManager.getDefault().saveProject(apiPrj);
        
        NbModuleProject friendPrj = generateSuiteComponent(suite, "friendPrj");
        FileUtil.createData(friendPrj.getProjectDirectory(), "src/friend/Karel.java");
        SingleModuleProperties friendPrjProps = SingleModulePropertiesTest.loadProperties(friendPrj);
        friendPrjProps.getPublicPackagesModel().setValueAt(Boolean.TRUE, 0, 0);
        friendPrjProps.getFriendListModel().addFriend("org.example.testPrj");
        friendPrjProps.storeProperties();
        ProjectManager.getDefault().saveProject(friendPrj);
        
        generateSuiteComponent(suite, "nonApiPrj");
        ModuleEntry apiPrjME = ModuleList.getModuleList(testPrj.getProjectDirectoryFile()).getEntry("org.example.apiPrj");
        ModuleDependency apiPrjDep = new ModuleDependency(apiPrjME);
        ModuleEntry friendPrjME = ModuleList.getModuleList(testPrj.getProjectDirectoryFile()).getEntry("org.example.friendPrj");
        ModuleDependency friendPrjDep = new ModuleDependency(friendPrjME);
        ModuleEntry nonApiPrjME = ModuleList.getModuleList(testPrj.getProjectDirectoryFile()).getEntry("org.example.nonApiPrj");
        ModuleDependency nonApiPrjDep = new ModuleDependency(nonApiPrjME);
        
        SingleModuleProperties testProps = SingleModulePropertiesTest.loadProperties(testPrj);
        Set allDeps = testProps.getUniverseDependencies(false);
        Set allDepsFilterExcluded = testProps.getUniverseDependencies(true);
        Set apiDeps = testProps.getUniverseDependencies(false, true);
        Set apiDepsFilterExcluded = testProps.getUniverseDependencies(true, true);
        
        assertTrue(allDeps.contains(apiPrjDep));
        assertTrue(allDeps.contains(friendPrjDep));
        assertTrue(allDeps.contains(nonApiPrjDep));
        
        assertTrue(allDepsFilterExcluded.contains(apiPrjDep));
        assertTrue(allDepsFilterExcluded.contains(friendPrjDep));
        assertTrue(allDepsFilterExcluded.contains(nonApiPrjDep));
        
        assertTrue(apiDeps.contains(apiPrjDep));
        assertTrue(apiDeps.contains(friendPrjDep));
        assertFalse(apiDeps.contains(nonApiPrjDep));
        
        assertTrue(apiDepsFilterExcluded.contains(apiPrjDep));
        assertTrue(apiDepsFilterExcluded.contains(friendPrjDep));
        assertFalse(apiDepsFilterExcluded.contains(nonApiPrjDep));
        
        // #72124: check that cluster include/exclude lists do not affect suite components:
        EditableProperties ep = suite.getHelper().getProperties("nbproject/platform.properties");
        ep.setProperty(SuiteProperties.ENABLED_CLUSTERS_PROPERTY, "crazy99"); // should not match any platform modules
        suite.getHelper().putProperties("nbproject/platform.properties", ep);
        ProjectManager.getDefault().saveProject(suite);
        allDepsFilterExcluded = testProps.getUniverseDependencies(true);
        assertTrue(allDepsFilterExcluded.contains(apiPrjDep));
        assertTrue(allDepsFilterExcluded.contains(friendPrjDep));
        assertTrue(allDepsFilterExcluded.contains(nonApiPrjDep));
    }
    */
    
    public void testDefaultPackageIsNotOffered_71532() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        FileUtil.createData(p.getProjectDirectory(), "src/BadInDefault.java");
        FileUtil.createData(p.getProjectDirectory(), "src/org/example/module1/GoodOne.java");
        assertEquals("one non-default valid package", 1, loadProperties(p).getPublicPackagesModel().getRowCount());
    }
    
//    public void testReloadNetBeansModulueListSpeedHid() throws Exception {
//        long startTotal = System.currentTimeMillis();
//        SingleModuleProperties props = loadProperties(nbCVSRoot().getFileObject("apisupport/project"),
//                "src/org/netbeans/modules/apisupport/project/Bundle.properties");
//        long start = System.currentTimeMillis();
//        props.reloadModuleListInfo();
//        System.err.println("Reloading of module list: " + (System.currentTimeMillis() - start) + "msec");
//        System.err.println("Total time: " + (System.currentTimeMillis() - startTotal) + "msec");
//    }
//
//    public void testReloadBinaryModulueListSpeedHid() throws Exception {
//        long startTotal = System.currentTimeMillis();
//        SingleModuleProperties props = loadProperties(suite2FO.getFileObject("misc-project"),
//                "src/org/netbeans/examples/modules/misc/Bundle.properties");
//        long start = System.currentTimeMillis();
//        props.reloadModuleListInfo();
//        System.err.println("Time to reload module list: " + (System.currentTimeMillis() - start) + "msec");
//        System.err.println("Total time: " + (System.currentTimeMillis() - startTotal) + "msec");
//    }

        public void testGetActivePlatform() throws Exception {
        ProjectManager.mutex().writeAccess(new ExceptionAction<Void>() {
            // saving of platform.properties of the project must be done under
            // PM.mutex() lock (read would in fact suffice too), otherwise
            // there is a race condition between storing properties file
            // and updating project evaluator
            public Void run() throws Exception {
                SuiteProject suite = generateSuite("suite");
                NbModuleProject module = generateSuiteComponent(suite, "module");
                File plaf = new File(getWorkDir(), "plaf");
                makePlatform(plaf, "1.13"); // 6.7 harness
                NbPlatform.addPlatform("plaf", plaf, "Test Platform");
                FileObject platformPropertiesFO = suite.getProjectDirectory().getFileObject("nbproject/platform.properties");
                EditableProperties platformProperties = Util.loadProperties(platformPropertiesFO);
                platformProperties.put("suite.dir", "${basedir}");
                platformProperties.put("nbplatform.active", "plaf");
                Util.storeProperties(platformPropertiesFO, platformProperties);
                SingleModuleProperties props = loadProperties(module);
                NbPlatform platform = props.getActivePlatform();
                assertEquals(plaf, platform.getDestDir());
                return null;
            }
        });
    }


    static SingleModuleProperties loadProperties(NbModuleProject project) throws IOException {
        return new SingleModuleProperties(project.getHelper(), project.evaluator(),
                getSuiteProvider(project), project.getModuleType(),
                project.getLookup().lookup(LocalizedBundleInfo.Provider.class));
    }
    
    private static SuiteProvider getSuiteProvider(Project p) {
        return p.getLookup().lookup(SuiteProvider.class);
    }
    
    private static void simulatePropertiesOpening(
            final SingleModuleProperties props, final NbModuleProject p) {
        props.refresh(p.getModuleType(), getSuiteProvider(p));
    }
    
    public void testGetPlatformVersionedLocation() throws Exception {
        File plafdir = new File(getWorkDir(), "plaf");
        TestFileUtils.writeZipFile(new File(plafdir, "platform/core/core.jar"), "j:unk");
        File harnessdir = new File(getWorkDir(), "harness");
        TestFileUtils.writeZipFile(new File(harnessdir, "modules/org-netbeans-modules-apisupport-harness.jar"), "META-INF/MANIFEST.MF:OpenIDE-Module-Specification-Version: 1.23\n");
        File suitedir = new File(getWorkDir(), "suite");
        SuiteProjectGenerator.createSuiteProject(suitedir, "special", false);
        FileObject suitedirFO = FileUtil.toFileObject(suitedir);
        FileObject plafProps = suitedirFO.getFileObject("nbproject/platform.properties");
        EditableProperties ep = Util.loadProperties(plafProps);
        ep.setProperty("suite.dir", "${basedir}");
        ep.setProperty("nbplatform.special.netbeans.dest.dir", "${suite.dir}/../plaf");
        ep.setProperty("nbplatform.special.harness.dir", "${suite.dir}/../harness");
        ep.setProperty("cluster.path", new String[] {"${nbplatform.active.dir}/platform:", "${nbplatform.special.harness.dir}"});
        Util.storeProperties(plafProps, ep);
        File moduledir = new File(getWorkDir(), "suite/m");
        NbModuleProjectGenerator.createSuiteComponentModule(moduledir, "m", "m", "m/Bundle.properties", null, suitedir, false, false);
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(moduledir));
        NbPlatform plaf = SingleModuleProperties.getInstance(p).getActivePlatform();
        assertEquals(plafdir, plaf.getDestDir());
        assertEquals(harnessdir, plaf.getHarnessLocation());
        assertEquals(HarnessVersion.V70, plaf.getHarnessVersion());
    }

}
