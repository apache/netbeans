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

package org.netbeans.modules.apisupport.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.modules.apisupport.project.ui.ImportantFilesNodeFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.test.MockLookup;
import org.openide.util.test.TestFileUtils;

/**
 * Basic setup for all the tests.
 *
 * @author Jesse Glick, Martin Krauskopf
 */
  public abstract class TestBase extends NbTestCase {

    public static final String CLUSTER_IDE = "ide";
    public static final String CLUSTER_PLATFORM = "platform";
    public static final String CLUSTER_ENTERPRISE = "enterprise";
    public static final String CLUSTER_APISUPPORT = "apisupport";
    public static final String CLUSTER_JAVA = "java";

    protected TestBase(String name) {
        super(name);
    }
    
    private static String EEP = "example-external-projects";
    
    /**
     * Tells whether NB source tree is available (which is not the case with e.g.
     * within binary distribution).
     */
    private boolean sourceAvailable;
    
    /** Represents netbeans.org source tree this test is run in if {@link #sourceAvailable}. */
    private File nbrootF;
    
    /** Represents netbeans.org source tree this test is run in if {@link #sourceAvailable}. */
    private FileObject nbroot;
    
    /** Represents destination directory with NetBeans (always available). */
    protected File destDirF;
    
    protected File apisZip;
    
    /** sample projects doesn't have datadir
     */
    protected static boolean noDataDir = false;
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances();
        sourceAvailable = isSourceAvailable();
        if (sourceAvailable) {
            nbrootF = FileUtil.normalizeFile(getTestNBRoot());
            nbroot = FileUtil.toFileObject(nbrootF);
            assertNotNull("have a file object for nbroot when using " + System.getProperty("java.class.path"), nbroot);
            destDirF = file(nbrootF, "nbbuild/netbeans").getAbsoluteFile();
            File extexamplesF = file(getDataDir(), EEP);
            if (!noDataDir) {
                assertTrue("there is a dir " + extexamplesF, extexamplesF.isDirectory());
                assertNotNull("have a file object for extexamples", FileUtil.toFileObject(extexamplesF));
            }
        } else {
            destDirF = getTestNBDestDir();
        }

        assertTrue("Directory really exists: " + destDirF, destDirF.isDirectory());
        
        // Need to set up private locations in extexamples, as if they were opened in the IDE.
        clearWorkDir();
        
        ErrorManagerImpl.registerCase(this);
        
        // Nonexistent path, just for JavadocForBuiltModuleTest:
        apisZip = new File(getWorkDir(), "apis.zip");
        File userPropertiesFile = initializeBuildProperties(getWorkDir(), noDataDir ? null : getDataDir(), apisZip);
        String[] suites = {
            // Suite projects:
            "suite1",
            "suite2",
            "suite4",
            // Standalone module projects:
            "suite3/dummy-project",
        };
        if (!noDataDir) {
            for (int i = 0; i < suites.length; i++) {
                File platformPrivate = resolveEEPFile(suites[i] + "/nbproject/private/platform-private.properties");
                Properties p = new Properties();
                p.setProperty("user.properties.file", userPropertiesFile.getAbsolutePath());
                platformPrivate.getParentFile().mkdirs();
                OutputStream os = new FileOutputStream(platformPrivate);
                try {
                    p.store(os, null);
                } finally {
                    os.close();
                }
            }
        }
        NbPlatform.reset();
    }
    
    protected @Override void tearDown() throws Exception {
        super.tearDown();
        ErrorManagerImpl.registerCase(null);
    }
    
    /**
     * Sets up global build.properties for the default platform.
     * For {@link PropertyUtils#userBuildProperties()}.
     * Called automatically by {@link #setUp}.
     * @param workDir use getWorkDir()
     * @return resulting properties file
     */
    public static File initializeBuildProperties(File workDir, File dataDir) throws Exception {
        return initializeBuildProperties(workDir, dataDir, null);
    }
    
    private static File initializeBuildProperties(File workDir, File dataDir, File apisZip) throws Exception {
        boolean sourceAvailable = isSourceAvailable();
        File nbrootF = sourceAvailable ? getTestNBRoot() : null;
        System.setProperty("netbeans.user", workDir.getAbsolutePath());
        File userPropertiesFile = new File(workDir, "build.properties");
        Properties p = new Properties();
        File defaultPlatform = sourceAvailable ? file(nbrootF, "nbbuild/netbeans") : getTestNBDestDir();
        assertTrue("default platform available (" + defaultPlatform + ')', defaultPlatform.isDirectory());
        p.setProperty("nbplatform.default.netbeans.dest.dir", defaultPlatform.getAbsolutePath());
        p.setProperty("nbplatform.default.harness.dir", "${nbplatform.default.netbeans.dest.dir}/harness");
        if (dataDir != null) {
            File customPlatform = file(file(dataDir, EEP), "/suite3/nbplatform");
            assertTrue("custom platform available (" + customPlatform + ')', customPlatform.isDirectory());
            p.setProperty("nbplatform.custom.netbeans.dest.dir", customPlatform.getAbsolutePath());
            if (apisZip != null) {
                p.setProperty("nbplatform.default.javadoc", apisZip.getAbsolutePath());
            }
            if (sourceAvailable) {
                // Make source association work to find misc-project from its binary:
                p.setProperty("nbplatform.default.sources", nbrootF.getAbsolutePath() + ":" + file(file(dataDir, EEP), "/suite2").getAbsolutePath());
            }
        }
        OutputStream os = new FileOutputStream(userPropertiesFile);
        try {
            p.store(os, null);
        } finally {
            os.close();
        }
        
        return userPropertiesFile;
    }
    
    /**
     * Just calls <code>File(root, path.replace('/', File.separatorChar));</code>
     */
    protected static File file(File root, String path) {
        return new File(root, path.replace('/', File.separatorChar));
    }
    
    private static boolean isSourceAvailable() {
        String nbroot = System.getProperty("test.nbroot");
        return nbroot != null && new File(nbroot, "nbbuild/netbeans/" + CLUSTER_APISUPPORT
                + "/modules/org-netbeans-modules-apisupport-project.jar").isFile();
    }
    
    protected File nbRootFile() {
        assertTrue("NB source tree is available", sourceAvailable);
        return nbrootF;
    }
    
    protected FileObject nbRoot() {
        assertTrue("NB source tree is available", sourceAvailable);
        return nbroot;
    }
    
    protected File resolveEEPFile(final String relativePath) {
        File eepF = FileUtil.normalizeFile(new File(getDataDir(), EEP));
        assertTrue("has EEP directory (" + eepF + ')', eepF.isDirectory());
        File eepRelF = new File(eepF, relativePath);
//        assertTrue("resolved file exists (" + eepRelF + ')', eepRelF.exists());
        return eepRelF;
    }
    
    protected String resolveEEPPath(final String relativePath) {
        return resolveEEPFile(relativePath).getAbsolutePath();
    }
    
    protected FileObject resolveEEP(final String relativePath) {
        return FileUtil.toFileObject(resolveEEPFile(relativePath));
    }
    
    /**
     * Calls in turn {@link #file(File, String)} with {@link #nbrootF} as the
     * first parameter. So the returned path will be actually relative to the
     * netbeans.org source tree this test is run in.
     */
    protected File file(String path) {
        return file(nbrootF, path);
    }

    @Deprecated
    protected File copyFolder(File d) throws IOException {
        return TestUtil.copyFolder(d, getWorkDir());
    }

    @Deprecated
    public static String slurp(FileObject fileObject) throws IOException {
        return fileObject.asText("UTF-8");
    }

    @Deprecated
    public static void dump(FileObject f, String contents) throws IOException {
        TestUtil.dump(f, contents);
    }

    @Deprecated
    public static String slurp(File file) throws IOException {
        return TestFileUtils.readFile(file);
    }

    @Deprecated
    public static void dump(File f, String contents) throws IOException {
        TestFileUtils.writeFile(f, contents);
    }

    /**
     * Blocking call waiting for change in project metadata to be reflected
     * in nodes.
     */
    public static void waitForNodesUpdate() {
      ImportantFilesNodeFactory.getNodesSyncRP().post(new Runnable() {

          public void run() {
          }
      }).waitFinished();
    }
    
    // XXX copied from TestBase in ant/freeform
    public static final class TestPCL implements PropertyChangeListener {
        
        public final Set<String> changed = new HashSet<String>();
        public final Map<String,String> newvals = new HashMap<String,String>();
        public final Map<String,String> oldvals = new HashMap<String,String>();
        
        public TestPCL() {}
        
        public void reset() {
            changed.clear();
            newvals.clear();
            oldvals.clear();
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();
            String nue = (String)evt.getNewValue();
            String old = (String)evt.getOldValue();
            changed.add(prop);
            if (prop != null) {
                newvals.put(prop, nue);
                oldvals.put(prop, old);
            } else {
                assert nue == null : "null prop name -> null new value";
                assert old == null : "null prop name -> null old value";
            }
        }
        
    }
    
    /**
     * Calls in turn {@link TestBase#generateStandaloneModule(File, String)}
     * with the {@link #getWorkDir()} as a first parameter.
     */
    public NbModuleProject generateStandaloneModule(String prjDir) throws IOException {
        return generateStandaloneModule(getWorkDir(), prjDir);
    }
    
    /**
     * Returns {@link NbModuleProject} created in the {@link
     * #getWorkDir()}/prjDir with code name base default to <em>org.example +
     * dotted prjDir</em> which is also used as the <em>default</em> package so
     * the layer and bundle are generated accordingly. Default module's display
     * name is set to <em>Testing Module</em>. So final set of generated files
     * for <em>module1</em> as the parameter may look like:
     *
     * <ul>
     *   <li>module1/manifest.mf
     *   <li>module1/nbproject/platform.properties
     *   <li>module1/nbproject/project.xml
     *   <li>module1/src/org/example/module1/resources/Bundle.properties
     *   <li>module1/src/org/example/module1/resources/layer.xml
     * </ul>
     *
     * Do not forget to first call {@link #initializeBuildProperties} if you are not a TestBase subclass!
     */
    public static NbModuleProject generateStandaloneModule(File workDir, String prjDir) throws IOException {
        return generateStandaloneModule(workDir, prjDir, false);
    }
    public static NbModuleProject generateStandaloneModule(File workDir, String prjDir, boolean osgi) throws IOException {
        FileObject prjDirFO = generateStandaloneModuleDirectory(workDir, prjDir, osgi);
        return (NbModuleProject) ProjectManager.getDefault().findProject(prjDirFO);
    }
    
    /**
     * The same as {@link #generateStandaloneModule(File, String)} but without
     * <em>opening</em> a generated project.
     */
    public static FileObject generateStandaloneModuleDirectory(File workDir, String prjDir) throws IOException {
        return generateStandaloneModuleDirectory(workDir, prjDir, false);
    }
    public static FileObject generateStandaloneModuleDirectory(File workDir, String prjDir, boolean osgi) throws IOException {
        String prjDirDotted = prjDir.replace('/', '.');
        File prjDirF = file(workDir, prjDir);
        NbModuleProjectGenerator.createStandAloneModule(
                prjDirF,
                "org.example." + prjDirDotted, // cnb
                "Testing Module", // display name
                "org/example/" + prjDir + "/resources/Bundle.properties",
                "org/example/" + prjDir + "/resources/layer.xml",
                NbPlatform.PLATFORM_ID_DEFAULT, osgi, true); // platform id
        return FileUtil.toFileObject(prjDirF);
    }
    
    /**
     * Calls in turn {@link TestBase#generateSuite(File, String)} with the
     * {@link #getWorkDir()} as a first parameter.
     */
    public SuiteProject generateSuite(String prjDir) throws IOException {
        return generateSuite(getWorkDir(), prjDir);
    }
    
    /** Generates an empty suite which has the default platform set. */
    public static SuiteProject generateSuite(File workDir, String prjDir) throws IOException {
        return generateSuite(workDir, prjDir, NbPlatform.PLATFORM_ID_DEFAULT);
    }
    
    /** Generates an empty suite. */
    public static SuiteProject generateSuite(File workDir, String prjDir, String platformID) throws IOException {
        File prjDirF = file(workDir, prjDir);
        SuiteProjectGenerator.createSuiteProject(prjDirF, platformID, false);
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        assert project instanceof SuiteProject : "From " + prjDirF + " got " + project + " (try MockLookup.setLayersAndInstances())";
        return (SuiteProject) project;
    }
    
    /**
     * Generates a suite component module which becomes a part of the given
     * <code>suiteProject</code>. Module will be generated inside of the
     * suite's project directory. <p>
     * See {@link #generateStandaloneModule(File, String)} for details about
     * what is generated.
     */
    public static NbModuleProject generateSuiteComponent(SuiteProject suiteProject, String prjDir) throws Exception {
        File suiteDir = suiteProject.getProjectDirectoryFile();
        return generateSuiteComponent(suiteProject, suiteDir, prjDir);
    }
    
    /**
     * Generates a suite component module which becomes a part of the given
     * <code>suiteProject</code>.
     * <p>
     * See {@link #generateStandaloneModule(File, String)} for details about
     * what is generated.
     */
    public static NbModuleProject generateSuiteComponent(SuiteProject suiteProject, File parentDir, String prjDir) throws Exception {
        FileObject fo = generateSuiteComponentDirectory(suiteProject, parentDir, prjDir);
        return (NbModuleProject) ProjectManager.getDefault().findProject(fo);
    }

    /**
     * The same as {@link #generateSuiteComponent(SuiteProject, File, String)} but without
     * <em>opening</em> a generated project.
     */
    public static FileObject generateSuiteComponentDirectory( SuiteProject suiteProject, File parentDir,String prjDir) throws IOException {
        String prjDirDotted = prjDir.replace('/', '.');
        File suiteDir = suiteProject.getProjectDirectoryFile();
        File prjDirF = file(parentDir, prjDir);
        NbModuleProjectGenerator.createSuiteComponentModule(prjDirF, "org.example." + prjDirDotted, "Testing Module", "org/example/" + prjDir + "/resources/Bundle.properties", "org/example/" + prjDir + "/resources/layer.xml", suiteDir, false, true); // suite directory
        return FileUtil.toFileObject(prjDirF);
    }

    @Deprecated
    public static void createJar(File jar, Map<String,String> contents, Manifest manifest) throws IOException {
        TestUtil.createJar(jar, contents, manifest);
    }
    
    public static void makePlatform(File d) throws IOException {
        makePlatform(d, "1.6.1"); // like 5.0
    }
    
    public static void makePlatform(File d, String harnessSpecVersion) throws IOException {
        // To satisfy NbPlatform.defaultPlatformLocation and NbPlatform.isValid, and make at least one module:
        Manifest mani = new Manifest();
        mani.getMainAttributes().putValue("OpenIDE-Module", "core");
        TestBase.createJar(new File(new File(new File(d, "platform"), "core"), "core.jar"), Collections.<String,String>emptyMap(), mani);
        mani = new Manifest();
        mani.getMainAttributes().putValue("OpenIDE-Module", "org.netbeans.modules.apisupport.harness");
        mani.getMainAttributes().putValue("OpenIDE-Module-Specification-Version", harnessSpecVersion);
        TestBase.createJar(new File(new File(new File(d, "harness"), "modules"), "org-netbeans-modules-apisupport-harness.jar"), Collections.<String,String>emptyMap(), mani);
        FileUtil.refreshFor(d);
    }

    @Deprecated
    public static void delete(File f) throws IOException {
        TestFileUtils.deleteFile(f);
    }
    
    private static File getTestNBRoot() {
        String nbroot = System.getProperty("test.nbroot");
        assertNotNull("test.nbroot property has to be set", nbroot);
        return new File(nbroot);
    }
    
    private static File getTestNBDestDir() {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }
}
