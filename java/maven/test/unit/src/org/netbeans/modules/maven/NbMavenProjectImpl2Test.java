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

package org.netbeans.modules.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import org.junit.Assume;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.execute.MavenExecMonitor;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.openide.windows.IOProvider;

public class NbMavenProjectImpl2Test extends NbTestCase {
    public NbMavenProjectImpl2Test(String name) {
        super(name);
        
    }

    private FileObject wd;
    private File repo;
    private FileObject repoFO;
    private FileObject dataFO;

    private static File getTestNBDestDir() {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }

    protected @Override void setUp() throws Exception {
        // this property could be eventually initialized by NB module system, as MavenCacheDisabler i @OnStart, but that's unreliable.
        System.setProperty("maven.defaultProjectBuilder.disableGlobalModelCache", "true");
        
        clearWorkDir();

        wd = FileUtil.toFileObject(getWorkDir());
        //synchronous reload of maven project asserts sanoty in some tests..
        System.setProperty("test.reload.sync", "false");

        // This is needed, otherwose the core window's startup code will redirect
        // System.out/err to the IOProvider, and its Trivial implementation will redirect
        // it back to System.err - loop is formed. Initialize IOProvider first, it gets
        // the real System.err/out references.
        IOProvider p = IOProvider.getDefault();

        repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        repoFO = FileUtil.toFileObject(repo);
        dataFO = FileUtil.toFileObject(getDataDir());
        
        // Configure the DummyFilesLocator with NB harness dir
        File destDirF = getTestNBDestDir();
        DummyInstalledFileLocator.registerDestDir(destDirF);

        useTestJdk = true;
    }

    @Override
    protected void tearDown() throws Exception {
        useTestJdk = false;
        super.tearDown();
    }
    
    

    protected @Override Level logLevel() {
        return Level.FINE;
    }

    protected @Override String logRoot() {
        return "org.netbeans.modules.maven";
    }
    
    volatile static boolean useTestJdk;
    
    /**
     * Forces Maven execution on the test JDK. Should not affect other classes. This is significant
     * on the developer's machine with multiple JDKs installed. Not so interesting on CI.
     */
    @ProjectServiceProvider(projectType = NbMavenProject.TYPE, service = LateBoundPrerequisitesChecker.class)
    public static class JdkChecker implements LateBoundPrerequisitesChecker {
        @Override
        public boolean checkRunConfig(RunConfig config, ExecutionContext con) {
            if (useTestJdk) {
                config.setProperty("Env.JAVA_HOME", System.getProperty("java.home"));
            }
            return true;
        }
    }
   
    private void cleanMavenRepository() throws IOException {
        Path path = Paths.get(System.getProperty("user.home"), ".m2", "repository");
        if (!Files.isDirectory(path)) {
            return;
        }
        FileUtil.toFileObject(path.toFile()).delete();
    }
    
    /**
     * Primes the project including dependency fetch, waits for the operation to complete.
     * @throws Exception 
     */
    void primeProject(Project p) throws Exception {
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        if (ap == null) {
            throw new IllegalStateException("No action provider");
        }
        assertTrue(Arrays.asList(ap.getSupportedActions()).contains(ActionProvider.COMMAND_PRIME));
        
        CountDownLatch primeLatch = new CountDownLatch(1);
        ActionProgress prg = new ActionProgress() {
            @Override
            protected void started() {
            }

            @Override
            public void finished(boolean success) {
                primeLatch.countDown();
            }
        };
        ap.invokeAction(ActionProvider.COMMAND_PRIME, Lookups.fixed(prg));
        primeLatch.await(300, TimeUnit.SECONDS);
    }
    
    MavenExecMonitor mme;
    
    Project reloadProject;
    NbMavenProjectImpl reloadProjectImpl;
    
    AtomicInteger projectEventCount = new AtomicInteger();
    CompletableFuture<Void> event = new CompletableFuture<>();
    CompletableFuture<org.openide.util.Task> task = new CompletableFuture<>();
    
    
    private void reloadProjectSetup() throws Exception {
        clearWorkDir();
        
        FileObject testApp = dataFO.getFileObject("parent-projects/single");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        assertNotNull(p);
        NbMavenProjectImpl impl = p.getLookup().lookup(NbMavenProjectImpl.class);
        
        Pair<List<Task>, Task> blockState = impl.reloadBlockingState();
        assertTrue(blockState.first().isEmpty());
        assertNull(blockState.second());
        
        reloadProject = p;
        reloadProjectImpl = impl;

        NbMavenProject.addPropertyChangeListener(reloadProject, (e) -> {
            if (NbMavenProject.PROP_PROJECT.equals(e.getPropertyName())) {
                projectEventCount.incrementAndGet();
                event.complete(null);
            }
        });
    }
    
    // Let's have per-testcase RP, so an eventually dangling task does not ruin other testcases.
    private final RequestProcessor processor = new RequestProcessor(NbMavenProjectImplTest.class);
    
    private List<Throwable> asyncThrowable = Collections.synchronizedList(new ArrayList<>());
    
    private void recordAsync(Throwable t) {
        asyncThrowable.add(t);
    }
    
    public void testProjectReloadsSchedulesTask() throws Exception {
        reloadProjectSetup();
        AtomicBoolean firedAtCompletion = new AtomicBoolean();
        
        // blocks the reload task from completing
        synchronized (reloadProjectImpl) {
            assertTrue("Project must not be reloading", reloadProjectImpl.getReloadTask().isFinished());
            
            Task t = reloadProjectImpl.fireProjectReload(true);
            t.addTaskListener((e) -> {
                firedAtCompletion.set(event.isDone());
                task.complete(e);
            });
            
            assertFalse("Project reload started", reloadProjectImpl.getReloadTask().isFinished());
        }
        
        CompletableFuture.allOf(event, task).get(10, TimeUnit.SECONDS);
        assertTrue("Project event fired before task completion", firedAtCompletion.get());
    }
    
    /**
     * Checks that the project reload is NOT planned after fireProjectReload(true), if there's a blocking task. Then it checks,
     * that completing the blocker, the reload task gets scheduled and completes.
     */
    public void testProjectOperationDelaysReload() throws Exception {
        reloadProjectSetup();
        
        AtomicBoolean firedAtCompletion = new AtomicBoolean();
        CountDownLatch blocker = new CountDownLatch(1);
        RequestProcessor.Task blockerTask = reloadProjectImpl.scheduleProjectOperation(processor, () -> {
            try {
                blocker.await();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }, 0);

        
        Task reloadTask;
        synchronized (reloadProjectImpl) {
            assertTrue("Project must not be reloading", reloadProjectImpl.getReloadTask().isFinished());
            
            reloadTask = reloadProjectImpl.fireProjectReload(true);
            assertTrue("Reload task must not start with active blockers", reloadProjectImpl.getReloadTask().isFinished());
        }
        
        Thread.sleep(1000);
        assertTrue("Reload task must not start with active blockers", reloadProjectImpl.getReloadTask().isFinished());
        
        CountDownLatch delay1 = new CountDownLatch(1);

        // the blocker tasks's listener already present should eventually release / schedule reload task, so 
        // this listener should get an already scheduled task.
        blockerTask.addTaskListener((e) -> {
            reloadTask.addTaskListener((e2) -> {
                firedAtCompletion.set(event.isDone());
                task.complete(e);
            });
            delay1.countDown();
        });
        
        // unblock the task
        blocker.countDown();
        
        delay1.await();
        
        try {
            task.get(10, TimeUnit.SECONDS); 
        } catch (TimeoutException e) {
            fail("Reload was not scheduled");
        }
        
        assertTrue("Project event fired before task completion", firedAtCompletion.get());
    }
    
    /**
     * Checks that reload that ignores blockers completes regardless of blockers still unfinished.
     */
    public void testForcedReloadBypassesBlockers() throws Exception {
        reloadProjectSetup();
        
        CountDownLatch blocker = new CountDownLatch(1);
        
        AtomicReference<Task> reloadTask = new AtomicReference<>();
        
        RequestProcessor.Task blockerTask = reloadProjectImpl.scheduleProjectOperation(processor, () -> {
            try {
                blocker.await();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }, 0);
        
        synchronized (reloadProjectImpl) {
            assertTrue("Project must not be reloading", reloadProjectImpl.getReloadTask().isFinished());
            reloadTask.set(reloadProjectImpl.fireProjectReload(false));
            assertFalse("Forced reload should be scheduled immediately", reloadProjectImpl.getReloadTask().isFinished());
        }

        assertTrue("Reload finished despite blockers", reloadTask.get().waitFinished(10 * 1000));
        assertFalse(blockerTask.isFinished());
        
        blockerTask.cancel();
    }
    
    /**
     * Force-reload should not clean the blocker list.
     */
    public void testForcedReloadKeepsWaiters() throws Exception {
        reloadProjectSetup();
        
        CountDownLatch blocker = new CountDownLatch(1);
        AtomicReference<Task> reloadTask = new AtomicReference<>();
        reloadProjectImpl.scheduleProjectOperation(processor, () -> {
            try {
                blocker.await();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }, 0);
        
        synchronized (reloadProjectImpl) {
            assertTrue("Project must not be reloading", reloadProjectImpl.getReloadTask().isFinished());
            reloadTask.set(reloadProjectImpl.fireProjectReload(false));
            assertFalse("Forced reload should be scheduled immediately", reloadProjectImpl.getReloadTask().isFinished());
        }

        
        Pair<List<Task>, Task> blockState = reloadProjectImpl.reloadBlockingState();
        assertFalse(blockState.first().isEmpty());
        assertNull("No soft-reload was requested", blockState.second());
        reloadProjectImpl.fireProjectReload(true);
        blockState = reloadProjectImpl.reloadBlockingState();
        assertNotNull("Soft-reload requested, completion task must have been created", blockState.second());
        blocker.countDown();
    }
    
    /**
     * Reload is called several times while blocked. Should happen and fire just once after unblocking.
     */
    public void testDelayedReloadTwiceHappensOnce() throws Exception {
        reloadProjectSetup();
        
        CountDownLatch blocker = new CountDownLatch(1);
        Task reloadTask1;
        Task reloadTask2;
        reloadProjectImpl.scheduleProjectOperation(processor, () -> {
            try {
                blocker.await();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }, 0);
        
        assertTrue("Project must not be reloading", reloadProjectImpl.getReloadTask().isFinished());
        reloadTask1 = reloadProjectImpl.fireProjectReload(true);
        reloadTask2 = reloadProjectImpl.fireProjectReload(true);
        
        blocker.countDown();
        
        reloadTask1.waitFinished(10 * 1000);
        reloadTask2.waitFinished(10 * 1000);
        
        assertEquals("Reload must complete exactly once", 1, this.projectEventCount.get());
    }
    
    public void testForcedReloadReloadsAgainAfterBlockersComplete() throws Exception {
        reloadProjectSetup();
        CountDownLatch blocker = new CountDownLatch(1);
        
        reloadProjectImpl.scheduleProjectOperation(processor, () -> {
            try {
                blocker.await();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }, 0);

        Task reloadTask1;
        Task reloadTask2;
        reloadTask1 = reloadProjectImpl.fireProjectReload(true);
        // now force-reload
        reloadTask2 = reloadProjectImpl.fireProjectReload(false);
        
        reloadTask2.waitFinished();
        
        assertEquals("The force-reload must complete and fire", 1, projectEventCount.get());
        blocker.countDown();

        reloadTask1.waitFinished();
        assertEquals("The reload scheduled during blocker must run separately", 2, projectEventCount.get());
    }
    
    public void testDoubleBlockersReleaseAtEnd() throws Exception {
        reloadProjectSetup();
        
        CountDownLatch blocker = new CountDownLatch(1);
        CountDownLatch blocker2 = new CountDownLatch(1);
        
        Task b1 = reloadProjectImpl.scheduleProjectOperation(processor, () -> {
            try {
                blocker.await();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }, 0);

        Task b2 = reloadProjectImpl.scheduleProjectOperation(processor, () -> {
            try {
                blocker2.await();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }, 0);
        
         
        Pair<List<Task>, Task> blockState = reloadProjectImpl.reloadBlockingState();
        assertEquals("Two blockers are registered", 2, blockState.first().size());
        
        RequestProcessor.Task t;
        synchronized (reloadProjectImpl) {
            t = reloadProjectImpl.fireProjectReload(true);
            assertFalse(t.isFinished());
        }
        
        blocker.countDown();
        assertFalse("Blocked reload should be still unscheduled", t.waitFinished(300));
        blocker2.countDown();
        t.waitFinished(10 * 1000);
        assertTrue(b1.isFinished());
        assertTrue(b2.isFinished());
        assertEquals("Project events must fire once", 1, projectEventCount.get());
    }
    
    public void testCompleteBlockersWithoutReloadDoesNotReload() throws Exception {
        reloadProjectSetup();
        
        CountDownLatch blocker = new CountDownLatch(1);
        CountDownLatch blocker2 = new CountDownLatch(1);
        
        Task b1 = reloadProjectImpl.scheduleProjectOperation(processor, () -> {
            try {
                blocker.await();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }, 0);

        Task b2 = reloadProjectImpl.scheduleProjectOperation(processor, () -> {
            try {
                blocker2.await();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }, 0);
        
         
        Pair<List<Task>, Task> blockState = reloadProjectImpl.reloadBlockingState();
        assertEquals(2, blockState.first().size());
        
        blocker.countDown();
        blocker2.countDown();
        b1.waitFinished(10 * 1000);
        b2.waitFinished(10 * 1000);
        
        blockState = reloadProjectImpl.reloadBlockingState();
        assertEquals(0, blockState.first().size());
        assertEquals(0, projectEventCount.get());
    }
    
    /**
     * Checks that subproject reload after the subproject primes.
     */
    public void testSubprojectsReloadAfterPriming() throws Exception {
        cleanMavenRepository();
        clearWorkDir();
        
        FileUtil.toFileObject(getWorkDir()).refresh();

        FileObject testApp = dataFO.getFileObject("projects/multiproject/democa");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        assertNotNull(p);

        Project sub = ProjectManager.getDefault().findProject(prjCopy.getFileObject("lib"));
        assertNotNull(sub);
        
        // check the project's validity:
        NbMavenProject subMaven = sub.getLookup().lookup(NbMavenProject.class);
        assertTrue("Fallback parent project is expected on unpopulated repository", NbMavenProject.isErrorPlaceholder(subMaven.getMavenProject()));
        assertTrue("Fallback subproject project is expected on unpopulated repository", NbMavenProject.isErrorPlaceholder(subMaven.getMavenProject()));
        
        primeProject(sub);
        assertFalse("Subproject must recover after priming itself", NbMavenProject.isIncomplete(subMaven.getMavenProject()));
    }
    
    /**
     * Checks that Priming action on a subproject actually runs on a reactor with --auto-make to build the subproject.
     * @throws Exception 
     */
    public void testSubprojectPrimeRunsReactor() throws Exception {
        cleanMavenRepository();
        clearWorkDir();
        
        mme = new MavenExecMonitor();
        MockLookup.setLayersAndInstances(mme);

        FileUtil.toFileObject(getWorkDir()).refresh();

        FileObject testApp = dataFO.getFileObject("projects/multiproject/democa");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        assertNotNull(p);

        Project sub = ProjectManager.getDefault().findProject(prjCopy.getFileObject("lib"));
        assertNotNull(sub);
        
        // check the project's validity:
        NbMavenProject subMaven = sub.getLookup().lookup(NbMavenProject.class);
        assertTrue("Fallback parent project is expected on unpopulated repository", NbMavenProject.isErrorPlaceholder(subMaven.getMavenProject()));
        assertTrue("Fallback subproject project is expected on unpopulated repository", NbMavenProject.isErrorPlaceholder(subMaven.getMavenProject()));
        
        primeProject(sub);
        
        assertEquals("Just single maven executed:", 1, mme.builders.size());
        
        ProcessBuilder b = mme.builders.getFirst();
        assertEquals("Runs in root project's dir", FileUtil.toFile(prjCopy),  b.directory());
        assertTrue("Specifies also-make", b.command().indexOf("--also-make") > 0);
        int idx = b.command().indexOf("--projects");
        assertTrue("Specifies projects", idx > 0);
        assertEquals("Runs up to the lib subprojectsd", "lib", b.command().get(idx + 1));
    }
    
    /**
     * Checks that subproject reload after its parent project primes.
     */
    public void testSubprojectsReloadAfterParentPriming() throws Exception {
        String jv = System.getProperty("java.specification.version");
        Assume.assumeTrue("Need JDK17", !jv.startsWith("1.") && Integer.parseInt(jv) >= 17);
        cleanMavenRepository();
        clearWorkDir();
        
        FileUtil.toFileObject(getWorkDir()).refresh();

        FileObject testApp = dataFO.getFileObject("projects/multiproject/democa");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        assertNotNull(p);

        Project sub = ProjectManager.getDefault().findProject(prjCopy.getFileObject("lib"));
        assertNotNull(sub);
        
        // check the project's validity:
        NbMavenProject parentMaven = p.getLookup().lookup(NbMavenProject.class);
        NbMavenProject subMaven = sub.getLookup().lookup(NbMavenProject.class);
        assertTrue("Fallback parent project is expected on unpopulated repository", NbMavenProject.isErrorPlaceholder(subMaven.getMavenProject()));
        assertTrue("Fallback subproject project is expected on unpopulated repository", NbMavenProject.isErrorPlaceholder(subMaven.getMavenProject()));
        
        primeProject(p);
        // subprojects are reloaded asynchronously. Watch out for child project's property for some time.
        CountDownLatch latch = new CountDownLatch(1);
        subMaven.addPropertyChangeListener((e) -> {
            if (NbMavenProject.PROP_PROJECT.equals(e.getPropertyName())) {
                latch.countDown();
            }
        });
        latch.await(10, TimeUnit.SECONDS);
        assertFalse("Subproject must recover after priming the parent", NbMavenProject.isIncomplete(subMaven.getMavenProject()));
    }
}
