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

import org.netbeans.modules.apisupport.project.api.Util;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectManagerTest;
import org.netbeans.junit.Log;
import org.netbeans.modules.apisupport.project.queries.ClassPathProviderImplTest;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleProperties;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Test {@link Evaluator} generally (but also see {@link ClassPathProviderImplTest}).
 * @author Jesse Glick
 */
public class EvaluatorTest extends TestBase {

    public EvaluatorTest(String name) {
        super(name);
    }
    
    private NbModuleProject javaProjectProject;
    private NbModuleProject loadersProject;
    private File userPropertiesFile;
    
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        userPropertiesFile = TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
        FileObject dir = nbRoot().getFileObject("java/java.project");
        assertNotNull("have java.project checked out", dir);
        ProjectManagerTest.resetProjectManager(ProjectManager.getDefault());
        Project p = ProjectManager.getDefault().findProject(dir);
        javaProjectProject = (NbModuleProject)p;
        dir = nbRoot().getFileObject("platform/openide.loaders");
        assertNotNull("have openide.loaders checked out", dir);
        p = ProjectManager.getDefault().findProject(dir);
        loadersProject = (NbModuleProject)p;
    }

    public void testEvaluator() throws Exception {
        PropertyEvaluator eval = javaProjectProject.evaluator();
        assertEquals("right basedir", file("java/java.project"),
            javaProjectProject.getHelper().resolveFile(eval.getProperty("basedir")));
        assertEquals("right nb_all", nbRootFile(),
            javaProjectProject.getHelper().resolveFile(eval.getProperty("nb_all")));
        assertEquals(file(nbRootFile(), "nbbuild/build/nbantext.jar"), javaProjectProject.getHelper().resolveFile(eval.getProperty("nbantext.jar")));
        assertEquals("right code.name.base.dashes", "org-netbeans-modules-java-project", eval.getProperty("code.name.base.dashes"));
        assertEquals("right is.autoload", "true", eval.getProperty("is.autoload"));
        assertEquals("right manifest.mf", "manifest.mf", eval.getProperty("manifest.mf"));
        assertEquals("right module JAR", file("nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/modules/org-netbeans-modules-java-project.jar"),
            javaProjectProject.getHelper().resolveFile(eval.evaluate("${cluster}/${module.jar}")));
        eval = loadersProject.evaluator();
        assertEquals("right module JAR", file("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"),
            loadersProject.getHelper().resolveFile(eval.evaluate("${cluster}/${module.jar}")));
    }

    // specifically tests ${cluster} prop. for standalone & suite comp. modules, for NB.org already tested in testEvaluator()
    public void testClusterProperty() throws Exception {
        NbModuleProject p1 = generateStandaloneModule("module1");
        PropertyEvaluator eval = p1.evaluator();
        assertEquals("${build.dir}", file(getWorkDir(), "module1/build"), p1.getHelper().resolveFile(eval.getProperty("build.dir")));
        assertEquals("standalone ${cluster}", file(getWorkDir(), "module1/build/cluster"), p1.getHelper().resolveFile(eval.getProperty("cluster")));
        SuiteProject s1 = generateSuite("suite1");
        NbModuleProject p2 = generateSuiteComponent(s1, "module2");
        eval = p2.evaluator();
        assertEquals("${suite.dir}", file(getWorkDir(), "suite1"), p2.getHelper().resolveFile(eval.getProperty("suite.dir")));
        assertEquals("${suite.build.dir}", file(getWorkDir(), "suite1/build"), p2.getHelper().resolveFile(eval.getProperty("suite.build.dir")));
        assertEquals("suite component ${cluster}", file(getWorkDir(), "suite1/build/cluster"), p2.getHelper().resolveFile(eval.getProperty("cluster")));
    }

    /** @see "#63541" */
    public void testJdkProperties() throws Exception {
        File testjdk = new File(getWorkDir(), "testjdk");
        EditableProperties ep = Util.loadProperties(FileUtil.toFileObject(userPropertiesFile));
        ep.setProperty("platforms.testjdk.home", testjdk.getAbsolutePath());
        Util.storeProperties(FileUtil.toFileObject(userPropertiesFile), ep);
        NbModuleProject p = generateStandaloneModule("module");
        PropertyEvaluator eval = p.evaluator();
        TestBase.TestPCL l = new TestBase.TestPCL();
        eval.addPropertyChangeListener(l);
        String bootcp = eval.getProperty(Evaluator.NBJDK_BOOTCLASSPATH);
        String origbootcp = bootcp;
        assertNotNull(bootcp); // who knows what actual value will be inside a unit test - probably empty
        ep = p.getHelper().getProperties("nbproject/platform.properties");
        ep.setProperty(ModuleProperties.JAVA_PLATFORM_PROPERTY, "testjdk");
        p.getHelper().putProperties("nbproject/platform.properties", ep);
        assertTrue("got a change in bootcp", l.changed.contains(Evaluator.NBJDK_BOOTCLASSPATH));
        l.reset();
        bootcp = eval.getProperty(Evaluator.NBJDK_BOOTCLASSPATH);
        assertEquals("correct bootcp", new File(testjdk, "jre/lib/rt.jar".replace('/', File.separatorChar)).getAbsolutePath(), bootcp);
        ep = p.getHelper().getProperties("nbproject/platform.properties");
        ep.setProperty(ModuleProperties.JAVA_PLATFORM_PROPERTY, "default");
        p.getHelper().putProperties("nbproject/platform.properties", ep);
        assertTrue("got a change in bootcp", l.changed.contains(Evaluator.NBJDK_BOOTCLASSPATH));
        l.reset();
        bootcp = eval.getProperty(Evaluator.NBJDK_BOOTCLASSPATH);
        assertEquals(origbootcp, bootcp);
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

    private class ModuleListLogHandler extends Handler {
        private Set<String> scannedDirs = Collections.synchronizedSet(new HashSet<String>(1000));
        String error;

        @Override
        public synchronized void publish(LogRecord record) {
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

    public void testPropertiesThatTriggerModuleListScan169040() throws Exception {
        ModuleList.refresh();   // disable cache
        Logger logger = Logger.getLogger(ModuleList.class.getName());
        Level origLevel = logger.getLevel();
        final ModuleListLogHandler handler = new ModuleListLogHandler();
        try {
            logger.setLevel(Level.ALL);
            logger.addHandler(handler);

            long start = System.currentTimeMillis();
            PropertyEvaluator eval = javaProjectProject.evaluator();
            assertEquals("No modules scanned yet", 0, handler.scannedDirs.size());
            String js = eval.getProperty("javac.source");      // does not scan ML
            assertTrue("Valid javac.source value", js != null && !js.isEmpty() && !js.equals("javac.source"));
            assertEquals("No modules scanned yet", 0, handler.scannedDirs.size());
            /* No longer calculated due to #172203 optimization:
            String coreStartupDir = eval.getProperty("core.startup.dir");   // does not scan ML after rev #797729b2749e
            assertTrue("Correct core.startup.dir value", Pattern.matches(".*platform[\\d]*$", coreStartupDir));
             */
            assertEquals("No modules scanned yet", 0, handler.scannedDirs.size());
            String cluster = eval.getProperty("cluster");   // still should not scan ML after fix of #169040
            assertEquals("No modules scanned yet", 0, handler.scannedDirs.size());
            assertTrue("java.project in java cluster", cluster.endsWith(TestBase.CLUSTER_JAVA));
            assertTrue("Absolute cluster dir", (new File(cluster)).isAbsolute());
            assertTrue("Existing cluster dir", (new File(cluster)).isDirectory());
            assertNotNull("Normalized cluster dir path", FileUtil.toFileObject(new File(cluster)));
            String testDir = eval.getProperty("test.unit.src.dir");
            assertEquals("No modules scanned yet", 0, handler.scannedDirs.size());
            assertTrue("Existing test src dir", PropertyUtils.resolveFile(javaProjectProject.getProjectDirectoryFile(), testDir).isDirectory());
            String mf = eval.getProperty("manifest.mf");
            assertEquals("No modules scanned yet", 0, handler.scannedDirs.size());
            
            String runCP = eval.getProperty("module.classpath");    // scans ML (or at least transitive deps)
            assertTrue("Some modules scanned", handler.scannedDirs.size() > 0);
            System.out.println("Synchronous scan, " + handler.scannedDirs.size() + " modules scanned.");

            System.out.println("Scan took " + (System.currentTimeMillis() - start) + " msec.");
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(origLevel);
        }
    }

    @Override
    protected int timeOut() {
        return 30000;
    }

    /* Who knows what this used to test. #173109 changes control flow.
    public void testModuleScanNotBlockingEvaluator169040() throws Exception {
        ModuleList.refresh();   // disable cache
        final Logger mlLogger = Logger.getLogger(ModuleList.class.getName());
        Level origLevel = mlLogger.getLevel();
        final ModuleListLogHandler handler = new ModuleListLogHandler();
        try {
            mlLogger.setLevel(Level.ALL);
            mlLogger.addHandler(handler);
            final PropertyEvaluator eval = javaProjectProject.evaluator();

            Logger observer = Logger.getLogger("observer");
            Log.enable("org.netbeans.modules.apisupport.project.universe.ModuleList", Level.ALL);

            String mt = "THREAD: Test Watch Dog: testModuleScanNotBlockingEvaluator169040 MSG:";
            String wt = "THREAD: worker MSG:";
            String order =  // #169040: will pass only if evaluation of "fast" props is not blocked by ML scan
                wt + "before module.classpath property eval" +
                wt + "scanning NetBeans.org stable sources started" +
                mt + "before synch properties eval" +
                mt + "after synch properties eval" +
                wt + "scanning NetBeans.org stable sources finished";

            Log.controlFlow(mlLogger, observer, order, 0);
            Thread t = new Thread("worker") {

                @Override
                public void run() {
                    mlLogger.log(Level.FINE, "before module.classpath property eval");
                    String runCP = eval.getProperty("module.classpath");    // scans ML (or at least transitive deps)
                    System.out.println("Asynchronous scan, " + handler.scannedDirs.size() + " modules scanned.");
                }
            };
            long start = System.currentTimeMillis();
            t.start();
            mlLogger.log(Level.FINE, "before synch properties eval");
            assertEquals("No modules scanned yet", 0, handler.scannedDirs.size());
            String js = eval.getProperty("javac.source");      // does not scan ML
            assertEquals("No modules scanned yet", 0, handler.scannedDirs.size());
            String coreStartupDir = eval.getProperty("core.startup.dir");   // does not scan ML after rev #797729b2749e
            assertEquals("No modules scanned yet", 0, handler.scannedDirs.size());
            mlLogger.log(Level.FINE, "after synch properties eval");
            t.join();
            System.out.println("Scan took " + (System.currentTimeMillis() - start) + " msec.");
        } finally {
            mlLogger.removeHandler(handler);
            mlLogger.setLevel(origLevel);
        }
    }

    public void testConcurrentPropertyEvaluation() throws Exception {
        ModuleList.refresh();   // disable cache
        final Logger mlLogger = Logger.getLogger(ModuleList.class.getName());
        Level origLevel = mlLogger.getLevel();
        final ModuleListLogHandler handler = new ModuleListLogHandler();
        try {
            mlLogger.setLevel(Level.ALL);
            mlLogger.addHandler(handler);
            final PropertyEvaluator eval = javaProjectProject.evaluator();

            Logger observer = Logger.getLogger("observer");
            Log.enable("org.netbeans.modules.apisupport.project.universe.ModuleList", Level.ALL);

            String w1 = "THREAD: worker1 MSG:";
            String w2 = "THREAD: worker2 MSG:";
            String order = 
                w2 + "before module.classpath property eval" +
                w1 + "before module.classpath property eval" +
                w1 + "scanning NetBeans.org stable sources started" +
                w1 + "scanning NetBeans.org stable sources finished" +
                w1 + "after module.classpath property eval" +   // must be here so that w1 jumps out of all locks before waking up w2
                w2 + "after module.classpath property eval";

            Log.controlFlow(mlLogger, observer, order, 0);
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    mlLogger.log(Level.FINE, "before module.classpath property eval");
                    String runCP = eval.getProperty("module.classpath");    // scans ML (or at least transitive deps)
                    mlLogger.log(Level.FINE, "after module.classpath property eval");
                    System.out.println("Concurrent scan, " + handler.scannedDirs.size() + " modules scanned.");
                }
            };
            Thread t1 = new Thread(r, "worker1");
            Thread t2 = new Thread(r, "worker2");
            long start = System.currentTimeMillis();
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            System.out.println("Scan took " + (System.currentTimeMillis() - start) + " msec.");
        } finally {
            mlLogger.removeHandler(handler);
            mlLogger.setLevel(origLevel);
        }
    }
     */

    public void testGetPlatformInPMWriteAccessDeadlock173345() throws Exception {
        final Logger LOG = Logger.getLogger(this.getClass().getName());
        Logger observer = Logger.getLogger("observer");
        Log.enable(LOG.getName(), Level.ALL);

        String mt = "THREAD: Test Watch Dog: testGetPlatformInPMWriteAccessDeadlock173345 MSG:";
        String wt = "THREAD: worker MSG:";
        String order =
            mt + "before NbPlatform.getPlatforms" +
            wt + "got PM write access";
        Log.controlFlow(LOG, observer, order, 0);
        NbPlatform.reset();
        Thread t = new Thread("worker") {

            @Override
            public void run() {
                try {
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                        public @Override Void run() throws Exception {
                            LOG.log(Level.FINE, "got PM write access");
                            NbPlatform.getPlatforms();
                            LOG.log(Level.FINE, "after NbPlatform.getPlatforms");
                            return null;
                        }
                    });
                } catch (MutexException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        t.start();
        LOG.log(Level.FINE, "before NbPlatform.getPlatforms");
        NbPlatform.getPlatforms();
        LOG.log(Level.FINE, "after NbPlatform.getPlatforms");
        t.join();
    }
}
