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
package org.netbeans.modules.maven.queries;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.modelcache.MavenProjectCache;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.ProjectReload;
import org.netbeans.modules.project.dependency.ProjectReload.ProjectState;
import org.netbeans.modules.project.dependency.ProjectReload.Quality;
import org.netbeans.modules.project.dependency.ProjectReload.StateRequest;
import org.netbeans.modules.project.dependency.reload.ProjectReloadInternal;
import org.netbeans.modules.project.dependency.reload.Reloader;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation.ProjectStateData;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOProvider;

/**
 *
 * @author sdedic
 */
public class MavenReloadImplementationTest extends NbTestCase {
    private FileObject d;
    private File repo;
    private FileObject repoFO;
    private FileObject dataFO;

    Collection<Logger> loggers = new ArrayList<>();
    
    public MavenReloadImplementationTest(String name) {
        super(name);
    }
    
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
        
        // This is needed, otherwose the core window's startup code will redirect
        // System.out/err to the IOProvider, and its Trivial implementation will redirect
        // it back to System.err - loop is formed. Initialize IOProvider first, it gets
        // the real System.err/out references.
        IOProvider p = IOProvider.getDefault();
        d = FileUtil.toFileObject(getWorkDir());
        System.setProperty("test.reload.sync", "false");
        repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        repoFO = FileUtil.toFileObject(repo);
        dataFO = FileUtil.toFileObject(getDataDir());
        
        // Configure the DummyFilesLocator with NB harness dir
        File destDirF = getTestNBDestDir();
        DummyInstalledFileLocator.registerDestDir(destDirF);
        
        /*
        System.err.println("*** Running: " + getName());

        loggers.add(Logger.getLogger(ProjectReloadInternal.class.getName()));
        loggers.add(Logger.getLogger(Reloader.class.getName()));
        loggers.forEach(logger -> {
            logger.setLevel(Level.FINER);

            ConsoleHandler h = new ConsoleHandler();
            h.setLevel(Level.FINER);
            if (!Arrays.asList(logger.getHandlers()).stream().anyMatch(x -> x instanceof ConsoleHandler)) {
                logger.addHandler(h);
            }
        });
        */
    }

    @Override
    protected void tearDown() throws Exception {
        MonitoringReloadImplementation.INSTANCE.enabled = false;
        OpenProjects.getDefault().close(OpenProjects.getDefault().getOpenProjects());
        ProjectReloadInternal.getInstance().assertNoOperations();
        super.tearDown(); 
    }
    
    FileObject prjCopy;
    Project root;
    Project oci;
    Project lib;
    
    void setupMicronautProject() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();

        FileObject testApp = dataFO.getFileObject("projects/multiproject/democa");
        prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "democa");
        root = ProjectManager.getDefault().findProject(prjCopy);
        oci = ProjectManager.getDefault().findProject(prjCopy.getFileObject("oci"));
        lib =  ProjectManager.getDefault().findProject(prjCopy.getFileObject("lib"));
        OpenProjects.getDefault().open(new Project[] { root, oci, lib }, false);
        OpenProjects.getDefault().openProjects().get();
    }
    
    private void primeProject() throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        
        ActionProvider ap = root.getLookup().lookup(ActionProvider.class);
        if (!ap.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookup.EMPTY)) {
            return;
        }

        ActionProgress prg = new ActionProgress() {
            @Override
            protected void started() {
            }

            @Override
            public void finished(boolean success) {
                cdl.countDown();
            }
        };
        ap.invokeAction(ActionProvider.COMMAND_PRIME, Lookups.fixed(prg));
        cdl.await(100, TimeUnit.SECONDS);
    }
    
    private Collection<FileObject> projectFiles(Collection<FileObject> fos, Project p) {
        return fos.stream().filter(f -> FileOwnerQuery.getOwner(f) == p).toList();
    }
    
    /**
     * Checks that root POM is reported as project file.
     * @throws Exception 
     */
    public void testSingleRootProject() throws Exception {
        setupMicronautProject();
        primeProject();
        
        Collection<FileObject> fos = projectFiles(ProjectReload.getProjectState(root, true).getLoadedFiles(), root);
        assertEquals(1, fos.size());
        assertSame(prjCopy.getFileObject("pom.xml"), fos.iterator().next());
    }
    
    /**
     * Checks that the root project just reports its own POM and the settings.
     * @throws Exception 
     */
    public void testRootProjectReloadJustOnePom() throws Exception {
        setupMicronautProject();
        primeProject();
        
        Collection<FileObject> fos = ProjectReload.getProjectState(root, true).getLoadedFiles();
        
        assertEquals(2, fos.size());
        assertTrue(fos.contains(prjCopy.getFileObject("pom.xml")));
        assertTrue(fos.contains(FileUtil.toFileObject(new File(System.getProperty("user.home"))).getFileObject(".m2/settings.xml")));
    }
    
    
    /**
     * Checks that project with dependencies and parent report both parent and the dependent projects
     * in NB workspace (must be opened). 
     * @throws Exception 
     */
    public void testReloadFilesDependentAndParent() throws Exception {
        setupMicronautProject();
        primeProject();
        
        Collection<FileObject> fos = ProjectReload.getProjectState(oci, true).getLoadedFiles();
        
        assertEquals(4, fos.size());
        assertTrue(fos.contains(prjCopy.getFileObject("pom.xml")));
        assertTrue(fos.contains(prjCopy.getFileObject("oci/pom.xml")));
        assertTrue(fos.contains(prjCopy.getFileObject("lib/pom.xml")));
        assertTrue(fos.contains(FileUtil.toFileObject(new File(System.getProperty("user.home"))).getFileObject(".m2/settings.xml")));
    }

    /**
     * Checks that reload of a subproject fails if its parent-pom is modified.
     * @throws Exception 
     */
    public void testReloadFailsWithParentPomModified() throws Exception {
        setupMicronautProject();
        
        primeProject();
        
        FileObject rootPomFile = root.getProjectDirectory().getFileObject("pom.xml");
        EditorCookie cake = rootPomFile.getLookup().lookup(EditorCookie.class);
        Document doc = cake.openDocument();
        doc.insertString(0, "aaa", null);
        doc.remove(0, 3);
        
        try {
            ProjectState p = ProjectReload.withProjectState(oci, 
                    ProjectReload.StateRequest.refresh()).toCompletableFuture().get();
            fail("Should fail as pom is modified in memory");
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof ProjectOperationException);
            ProjectOperationException ex2 = (ProjectOperationException)ex.getCause();
            // expected
            assertEquals(ProjectOperationException.State.OUT_OF_SYNC, ex2.getState());
        }
    }
    
    /**
     * Checks that the reload succeeds, if only submodule has been modified. The reload should
     * succeed immediately as on-disk state is not modified after the project load.
     * @throws Exception 
     */
    public void testReloadOKWithSubprojectModified() throws Exception {
        setupMicronautProject();
        primeProject();

        FileObject ociPomFile = oci.getProjectDirectory().getFileObject("pom.xml");
        EditorCookie cake = ociPomFile.getLookup().lookup(EditorCookie.class);
        Document doc = cake.openDocument();
        doc.insertString(0, "aaa", null);
        doc.remove(0, 3);
        
        // ensure some project state is present
        ProjectReload.getProjectState(lib, true);

        CompletableFuture f;
        synchronized (this) {
            f = ProjectReload.withProjectState(lib, 
                ProjectReload.StateRequest.refresh()).toCompletableFuture().thenAccept(p -> {
                    synchronized (this) {
                        // just block
                    }
                });
            // the reload actually does not happen. The Future completes even before the withProjectState returns.
            assertTrue(f.isDone());
        }
    }

    /**
     * Checks that the reload succeeds, if only submodule has been modified. The reload should
     * succeed immediately as on-disk state is not modified after the project load.
     * @throws Exception 
     */
    public void testInitialLoadOKWithSubprojectModified() throws Exception {
        setupMicronautProject();
        primeProject();

        FileObject ociPomFile = oci.getProjectDirectory().getFileObject("pom.xml");
        EditorCookie cake = ociPomFile.getLookup().lookup(EditorCookie.class);
        Document doc = cake.openDocument();
        doc.insertString(0, "aaa", null);
        doc.remove(0, 3);
        
        CompletableFuture<ProjectState> f;
        f = ProjectReload.withProjectState(lib, 
            ProjectReload.StateRequest.refresh()).toCompletableFuture();
        ProjectState ps = f.get();
        assertTrue(ps.isConsistent());
    }
    
    /**
     * Checks that a reload succeeds immediately when the consistency check is disabled.
     */
    public void testReloadIgnoresModifications() throws Exception {
        setupMicronautProject();
        
        // get the project model THEN mark file as modified:
        FileObject rootPomFile = root.getProjectDirectory().getFileObject("pom.xml");   
        
        // ensure that some state is established, otherwise the withProjectState will attempt to load from NONE quality to better.
        ProjectState ps = ProjectReload.withProjectState(root, 
                    ProjectReload.StateRequest.load()).get();
        
        // and make some edits; must be done after setting timestamp to avoid "save file" dialog during tests.
        EditorCookie cake = rootPomFile.getLookup().lookup(EditorCookie.class);
        Document doc = cake.openDocument();
        doc.insertString(0, "aaa", null);
        doc.remove(0, 3);
        
        CompletableFuture f;
        AtomicInteger n = new AtomicInteger();
        synchronized (this) {
            f = ProjectReload.withProjectState(root, 
                    ProjectReload.StateRequest.refresh().consistent(false)).toCompletableFuture().thenAccept(p -> {
                synchronized (this) {
                    assertEquals(0, n.get());
                    n.incrementAndGet();
                }
            });
            n.incrementAndGet();
            assertTrue(f.isDone());
        }
        f.get();
    }
    
    /**
     * Checks that a project, that desperately needs to download artifacts will fail to reach ready state in offline mode. For this, we 
     * remove micronaut-parent parent POM that is referenced by the project.
     */
    public void testReloadFailsInOfflineMode() throws Exception {
        // must remove artifacts from .m2 BEFORE the project is opened / scanned.
        Path p = Paths.get(System.getProperty("user.home"), ".m2", "repository", "io", "micronaut", "platform", "micronaut-parent");
        if (Files.exists(p)) {
            Files.walk(p)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
        setupMicronautProject();
        try {
            ProjectReload.withProjectState(root, 
                ProjectReload.StateRequest.refresh().toQuality(ProjectReload.Quality.RESOLVED).offline()).toCompletableFuture().get();
            fail("Project has no parent POM, and offline is forced");
        } catch (ExecutionException eex) {
            assertTrue(eex.getCause() instanceof ProjectOperationException);
            ProjectOperationException ex = (ProjectOperationException)eex.getCause();
            assertEquals(ProjectOperationException.State.OFFLINE, ex.getState());
        }
    }

    /**
     * Checks that project reload fails, if the POM is modified.
     */
    public void testReloadFailsWithPomModified() throws Exception {
        setupMicronautProject();
        primeProject();
        
        FileObject libPomFile = lib.getProjectDirectory().getFileObject("pom.xml");
        EditorCookie cake = libPomFile.getLookup().lookup(EditorCookie.class);
        Document doc = cake.openDocument();
        doc.insertString(0, "aaa", null);
        doc.remove(0, 3);
        
        try {
            ProjectState ps = ProjectReload.withProjectState(oci, 
                    ProjectReload.StateRequest.refresh()).toCompletableFuture().get();
            fail("Should fail as pom is modified in memory");
        } catch (ExecutionException eex) {
            assertTrue(eex.getCause() instanceof ProjectOperationException);
            ProjectOperationException ex = (ProjectOperationException)eex.getCause();
            // expected
            assertEquals(ProjectOperationException.State.OUT_OF_SYNC, ex.getState());
        }
    }
    
    public void testProjectStatePresent() throws Exception {
        setupMicronautProject();
        primeProject();
        
        AtomicReference ref = new AtomicReference();
        CountDownLatch cdl = new CountDownLatch(1);
        
        ProjectReload.withProjectState(oci, ProjectReload.StateRequest.refresh()).
                thenAccept(ps -> {
            MavenProject nbmp = ps.getLookup().lookup(MavenProject.class);
            ref.set(nbmp);
            cdl.countDown();
        });
        assertTrue(cdl.await(30, TimeUnit.SECONDS));
        assertNotNull(ref.get());
    }
    
    /**
     * Check that a cancel on the CompletableFuture that represents the project reload will cancel the
     * priming build.
     * 
     * @throws Exception 
     */
    public void testCancelStateRequest() throws Exception {
        Path p = Paths.get(System.getProperty("user.home"), ".m2", "repository", "io", "micronaut", "platform", "micronaut-parent");
        if (Files.exists(p)) {
            Files.walk(p)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
        setupMicronautProject();

        FileObject pomFile = root.getProjectDirectory().getFileObject("pom.xml");
        // this will take a while, as there's a priming build to do.
        CompletableFuture<ProjectState> f = ProjectReload.withProjectState(root, ProjectReload.StateRequest.refresh().toQuality(ProjectReload.Quality.RESOLVED));
        Thread.sleep(3 * 100);
        f.cancel(true);
        
        CountDownLatch l = new CountDownLatch(1);
        
        ProjectReloadInternal.getInstance().runProjectAction(root, () -> {
            l.countDown();
        });
        l.await(10, TimeUnit.SECONDS);
        
        NbMavenProject nbmp = root.getLookup().lookup(NbMavenProject.class);
        boolean inc = NbMavenProject.isIncomplete(nbmp.getMavenProject());
        
        assertTrue("Project must be still incomplete", inc);
    }
    
    /**
     * Checks that a ProjectState is invalidated when the Maven internally reloads its
     * project bypassing this API
     * @throws Exception 
     */
    public void testMavenInternalReloadFiresInvalid() throws Exception {
        setupMicronautProject();
        primeProject();
        
        FileObject pomFile = root.getProjectDirectory().getFileObject("pom.xml");
        ProjectState st = ProjectReload.getProjectState(root, true);
       
        NbMavenProjectImpl nbmp = root.getLookup().lookup(NbMavenProjectImpl.class);
        nbmp.fireProjectReload().waitFinished();
        // hack: wait for the project reload to dispatch eventual events
        ProjectReloadInternal.RELOAD_RP.post(()->{}, 300).waitFinished();
        assertFalse(st.isValid());
    }
    
    /**
     * The reload API is asked to refresh the state, but the underlying MavenProject
     * was already reloaded (through other means). The test checks that the project
     * is not reloaded needlessly again.
     * 
     * @throws Exception 
     */
    public void testMavenDoesNotReloadCurrent() throws Exception {
        setupMicronautProject();
        primeProject();
        
        NbMavenProjectImpl nbmp = root.getLookup().lookup(NbMavenProjectImpl.class);
        ProjectState initialState = ProjectReload.getProjectState(root, true);
        MavenProject initialProject = nbmp.getOriginalMavenProject();
        
        assertEquals(Quality.RESOLVED, initialState.getQuality());
        // wait, change timestamp, refresh the fileobject to make the state inconsistent.
        Thread.sleep(2000);
        FileObject pom = root.getProjectDirectory().getFileObject("pom.xml");
        Path pomPath = FileUtil.toFile(pom).toPath();
        Files.setLastModifiedTime(pomPath, FileTime.from(Instant.now()));
        pom.refresh();
        
        assertFalse(initialState.isConsistent());
        
        // have maven project reload the project
        nbmp.fireProjectReload().waitFinished();
        MavenProject refreshedProject = nbmp.getOriginalMavenProject();
        assertNotSame(refreshedProject, initialProject);

        ProjectReload.withProjectState(root, StateRequest.load()).thenAccept(s -> {
            MavenProject newMP = s.getLookup().lookup(MavenProject.class);
            assertSame(newMP, refreshedProject);
        }).get(10, TimeUnit.SECONDS);
    }
    
    /**
     * The reload API is asked to refresh the state, but the underlying MavenProject
     * was already reloaded (through other means). Force flag will cause the reload
     * to happen.
     * 
     * @throws Exception 
     */
    public void testForcedMavenDoesReloadCurrent() throws Exception {
        setupMicronautProject();
        primeProject();
        
        NbMavenProjectImpl nbmp = root.getLookup().lookup(NbMavenProjectImpl.class);
        ProjectState initialState = ProjectReload.getProjectState(root, true);
        MavenProject initialProject = nbmp.getOriginalMavenProject();
        
        assertEquals(Quality.RESOLVED, initialState.getQuality());
        // wait, change timestamp, refresh the fileobject to make the state inconsistent.
        Thread.sleep(2000);
        FileObject pom = root.getProjectDirectory().getFileObject("pom.xml");
        Path pomPath = FileUtil.toFile(pom).toPath();
        Files.setLastModifiedTime(pomPath, FileTime.from(Instant.now()));
        pom.refresh();
        
        assertFalse(initialState.isConsistent());
        
        // have maven project reload the project
        nbmp.fireProjectReload().waitFinished();
        MavenProject refreshedProject = nbmp.getOriginalMavenProject();
        assertNotSame(refreshedProject, initialProject);

        ProjectReload.withProjectState(root, StateRequest.reload()).thenAccept(s -> {
            MavenProject newMP = s.getLookup().lookup(MavenProject.class);
            assertNotSame(newMP, refreshedProject);
        }).get();
    }
    
    /**
     * The reload API is asked to refresh the state, but the underlying MavenProject
     * was already reloaded (through other means). A higher actual quality will
     * force the load.
     * 
     * @throws Exception 
     */
    public void testMavenCurrentReplacedWithHigherQuality() throws Exception {
        Path p = Paths.get(System.getProperty("user.home"), ".m2", "repository", "io", "micronaut", "platform", "micronaut-parent");
        if (Files.exists(p)) {
            Files.walk(p)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
        setupMicronautProject();

        NbMavenProjectImpl impl = root.getLookup().lookup(NbMavenProjectImpl.class);        
        
        ProjectState s = ProjectReload.getProjectState(root, true);
        MavenProject original = impl.getOriginalMavenProject();
        assertTrue(s.getQuality().isWorseThan(Quality.RESOLVED));
        assertTrue(s.isValid());
        
        impl.fireProjectReload().waitFinished();
        MavenProject reloaded = impl.getOriginalMavenProject();
        
        assertNotSame(original, reloaded);
        
        ProjectState s2 = ProjectReload.getProjectState(root);
        MavenProject s2Project = s2.getLookup().lookup(MavenProject.class);
        
        assertTrue(s2.getQuality().isWorseThan(Quality.RESOLVED));
        assertSame(s, s2);
        assertSame(original, s2Project);
        assertFalse(s.isValid());
        
        ProjectState s3 = ProjectReload.withProjectState(root, StateRequest.refresh()).get();
        MavenProject s3Project = s3.getLookup().lookup(MavenProject.class);
        
        assertNotSame(s3, s2);
        assertSame(reloaded, s3Project);
        assertFalse(s2.isValid());
        // still bad
        assertTrue(s3.getQuality().isWorseThan(Quality.RESOLVED));
        
        ProjectState s4 = ProjectReload.withProjectState(root, StateRequest.refresh().toQuality(Quality.RESOLVED)).get();
        assertNotSame(s4, s3);
        assertTrue(s4.getQuality().isAtLeast(Quality.RESOLVED));
    }
    
    /**
     * Checks that a new withProjectState will load a new project after internal
     * refresh.
     * @throws Exception 
     */
    public void testMavenLoadsNewProjectAfterInternalReload() throws Exception {
        setupMicronautProject();
        primeProject();
        
        NbMavenProjectImpl nbmp = root.getLookup().lookup(NbMavenProjectImpl.class);
        ProjectState initialState = ProjectReload.getProjectState(root, true);
        MavenProject initialProject = nbmp.getOriginalMavenProject();
        
        assertSame(initialProject, initialState.getLookup().lookup(MavenProject.class));
        
        ProjectReload.withProjectState(root, StateRequest.load()).thenAccept(s -> {
            assertSame("Load should suceeed immediately, as the project was already read", initialState, s);
        }).get();
        
        Thread.sleep(2000);
        FileObject pom = root.getProjectDirectory().getFileObject("pom.xml");
        Path pomPath = FileUtil.toFile(pom).toPath();
        Files.setLastModifiedTime(pomPath, FileTime.from(Instant.now()));
        pom.refresh();
        

        nbmp.fireProjectReload().waitFinished();
        
        ProjectReload.withProjectState(root, StateRequest.refresh()).thenAccept(s -> {
            assertNotSame(initialState, s);
        
            MavenProject newProject = nbmp.getOriginalMavenProject();
            assertSame(newProject, s.getLookup().lookup(MavenProject.class));
            assertNotSame(newProject, initialProject);
            long time = MavenProjectCache.getLoadTimestamp(newProject);
            assertEquals(time, s.getTimestamp());
        }).get();
    }
    
    class Blocker implements Runnable {
        CountDownLatch unblock = new CountDownLatch(1);

        @Override
        public void run() {
            try {
                unblock.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    /**
     * Simulates that a Maven project load is scheduled after a file change, but 
     * Reload API is invoked BEFORE that load completes. The cached Maven project
     * exists, but with an old timestamp. getProjectState() should return
     * inconsistent even in this case.
     * 
     * @throws Exception 
     */
    public void testInitialLoadObsoleteProject() throws Exception {
        setupMicronautProject();
        primeProject();

        FileObject pom = oci.getProjectDirectory().getFileObject("pom.xml");
        Path pomPath = FileUtil.toFile(pom).toPath();
        
        Blocker blocker = new Blocker();
        
        NbMavenProjectImpl.RELOAD_RP.post(blocker);
        try {
            Files.setLastModifiedTime(pomPath, FileTime.from(Instant.now()));
            pom.refresh();

            ProjectState ps = ProjectReload.getProjectState(oci, true);

            assertFalse(ps.isConsistent());
            MavenReloadImplementation mri = (MavenReloadImplementation)oci.getLookup().lookupAll(ProjectReloadImplementation.class).stream().
                    filter(i -> i instanceof MavenReloadImplementation).findFirst().get();
            ProjectStateData psd = mri.getLastCachedData();
            assertFalse(psd.isConsistent());
        } finally {
            blocker.unblock.countDown();
        }        
    }
    
    /**
     * Checks that if a project becomes inconsistent during the state load, the state will internally
     * reload before returning the result.
     * 
     * @throws Exception 
     */
    public void testProjectObsoletesDuringLoad() throws Exception {
        setupMicronautProject();
        primeProject();

        FileObject pom = oci.getProjectDirectory().getFileObject("pom.xml");
        Path pomPath = FileUtil.toFile(pom).toPath();
        
        ProjectState ps = ProjectReload.getProjectState(oci, true);
        
        MavenReloadImplementation mri = (MavenReloadImplementation)oci.getLookup().lookupAll(ProjectReloadImplementation.class).stream().
                filter(i -> i instanceof MavenReloadImplementation).findFirst().get();

        MonitoringReloadImplementation.INSTANCE.enabled = true;

        Thread.sleep(2000);
        Files.setLastModifiedTime(pomPath, FileTime.from(Instant.now()));
        pom.refresh();
        
        MonitoringReloadImplementation.INSTANCE.counter.set(0);
        MonitoringReloadImplementation.INSTANCE.reloadReached1.drainPermits();
        MonitoringReloadImplementation.INSTANCE.proceed.drainPermits();

        CompletableFuture<ProjectState> newState = ProjectReload.withProjectState(oci, StateRequest.refresh());
        
        MonitoringReloadImplementation.INSTANCE.reloadReached1.acquire();
        assertFalse(newState.isDone());
        int cnt = MonitoringReloadImplementation.INSTANCE.counter.get();
        ProjectStateData firstRound = mri.getLastCachedData();
        
        Thread.sleep(2000);
        Files.setLastModifiedTime(pomPath, FileTime.from(Instant.now()));
        // deliberately not calling refresh().
        
        MonitoringReloadImplementation.INSTANCE.proceed.release(100);
        
        ProjectState ps2 = newState.get();
        
        assertTrue(ps2.isValid());
        assertTrue(ps2.isConsistent());
        
        ProjectStateData secondRound = mri.getLastCachedData();
        assertNotSame(firstRound, secondRound);
        
        // one reload happened.
        assertEquals(2, MonitoringReloadImplementation.INSTANCE.counter.get());
    }
    
    /**
     * Checks that repeated file changes will not loop the reloads, but fail.
     * 
     * @throws Exception 
     */
    public void testRepeatedChangesErrorOut() throws Exception {
        setupMicronautProject();
        primeProject();

        FileObject pom = oci.getProjectDirectory().getFileObject("pom.xml");
        Path pomPath = FileUtil.toFile(pom).toPath();
        
        ProjectState ps = ProjectReload.getProjectState(oci, true);
        
        MavenReloadImplementation mri = (MavenReloadImplementation)oci.getLookup().lookupAll(ProjectReloadImplementation.class).stream().
                filter(i -> i instanceof MavenReloadImplementation).findFirst().get();

        MonitoringReloadImplementation.INSTANCE.enabled = true;

        Blocker blocker = new Blocker();
        
        Instant baseMod = Instant.now();
        Files.setLastModifiedTime(pomPath, FileTime.from(baseMod));
        pom.refresh();
        
        MonitoringReloadImplementation.INSTANCE.counter.set(0);
        MonitoringReloadImplementation.INSTANCE.reloadReached1.drainPermits();
        MonitoringReloadImplementation.INSTANCE.proceed.drainPermits();

        CompletableFuture<ProjectState> newState = ProjectReload.withProjectState(oci, StateRequest.refresh());
        
        // 1st reload
        MonitoringReloadImplementation.INSTANCE.reloadReached1.acquire();
        
        assertFalse(newState.isDone());
        int cnt = MonitoringReloadImplementation.INSTANCE.counter.get();
        ProjectStateData firstRound = mri.getLastCachedData();
        assertTrue(firstRound.isConsistent());
        
        Thread.sleep(2000);

        // prevent Maven from reloading the project
        NbMavenProjectImpl.RELOAD_RP.post(blocker);

        Instant firstMod = Instant.now();
        Files.setLastModifiedTime(pomPath, FileTime.from(firstMod));
        // deliberately not calling refresh().
        
        MonitoringReloadImplementation.INSTANCE.proceed.release(1);
        
        blocker.unblock.countDown();
        
        // 2nd reload
        MonitoringReloadImplementation.INSTANCE.reloadReached1.acquire();
        ProjectStateData secondRound = mri.getLastCachedData();
        // new state is OK, but the previous should be obsoleted.
        assertNotSame(secondRound, firstRound);
        assertTrue(secondRound.isConsistent());
        assertFalse(firstRound.isConsistent());
        
        Thread.sleep(2000);
        Instant secondMod = Instant.now();
        Files.setLastModifiedTime(pomPath, FileTime.from(secondMod));
        
        MonitoringReloadImplementation.INSTANCE.proceed.release(100);

        try {
            ProjectState ps2 = newState.get();
            fail("Should report an exception");
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof ProjectOperationException);
            ProjectOperationException pex = (ProjectOperationException)ex.getCause();
            assertSame(ProjectOperationException.State.OUT_OF_SYNC, pex.getState());
        }
        
        ProjectState ps2 = ProjectReload.getProjectState(oci);
        // may be valid or not, since Maven internally reloads the project independently, and
        // will invalidate the state after load.
        assertFalse(ps2.isConsistent());
        
        assertTrue(ps2.getTimestamp() > baseMod.toEpochMilli());
        // will be at least the 1st modification
        assertTrue(ps2.getTimestamp() >= firstMod.toEpochMilli());
        // and NOT the 2nd modification (modified twice -> error)
        assertTrue(ps2.getTimestamp() < secondMod.toEpochMilli());
        
        ProjectState ps3 = ProjectReload.withProjectState(oci, StateRequest.refresh()).get();
        assertTrue(ps3.isValid());
        assertTrue(ps3.isConsistent());
        assertTrue(ps3.getTimestamp() >= secondMod.toEpochMilli());
    }
    
    @ProjectServiceProvider(service = ProjectReloadImplementation.class, projectType = NbMavenProject.TYPE)
    public static class MonitoringReloadImplementation implements ProjectReloadImplementation {
        static MonitoringReloadImplementation INSTANCE;
        final Semaphore proceed = new Semaphore(0);
        final Semaphore reloadReached1 = new Semaphore(0);
        final Semaphore reloadReached2 = new Semaphore(0);
        volatile boolean enabled;
        AtomicInteger counter = new AtomicInteger();
        
        public MonitoringReloadImplementation() {
            INSTANCE = this;
        }
        
        
        @Override
        public CompletableFuture reload(Project project, StateRequest request, LoadContext context) {
            if (!enabled) {
                return null;
            }
            reloadReached1.release();
            try {
                proceed.acquire();
                counter.incrementAndGet();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            reloadReached2.release();
            return null;
        }
    }
}
