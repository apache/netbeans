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
package org.netbeans.modules.gradle.reload;

import java.awt.Dialog;
import java.awt.Frame;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import org.gradle.tooling.internal.consumer.ConnectorServices;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleDependency;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.ProjectReload;
import org.netbeans.modules.project.dependency.ProjectReload.ProjectState;
import org.netbeans.modules.project.dependency.ProjectReload.StateRequest;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.util.test.MockLookup;
import org.openide.windows.IOProvider;

/**
 *
 * @author sdedic
 */
public class GradleReloadImplementationTest extends NbTestCase {
    
    public GradleReloadImplementationTest(String name) {
        super(name);
    }
    
    FileObject workDir;
    FileObject dataDir;

    FileObject prjDir;
    FileObject libDir;
    FileObject ociDir;
    
    Project project;
    Project libProject;
    Project ociProject;
    
    private static File getTestNBDestDir() {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }
    
    static class DDImpl extends DialogDisplayer {
        List<NotifyDescriptor>   displayedDescriptors = new ArrayList<>();
        
        volatile Delegate delegate;

        interface Delegate {
            public CompletableFuture<NotifyDescriptor> notify(NotifyDescriptor d);
        }
        
        @Override
        public Object notify(NotifyDescriptor descriptor) {
            throw new UnsupportedOperationException("Should not happen during the test.");
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Should not happen during the test.");
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor, Frame parent) {
            throw new UnsupportedOperationException("Should not happen during the test.");
        }

        @Override
        public <T extends NotifyDescriptor> CompletableFuture<T> notifyFuture(T descriptor) {
            displayedDescriptors.add(descriptor);
            if (delegate != null) {
                return (CompletableFuture<T>)delegate.notify(descriptor);
            } else {
                descriptor.setValue(NotifyDescriptor.CANCEL_OPTION);
                CompletableFuture<T> f = new CompletableFuture<>();
                f.completeExceptionally(new CancellationException());
                return f;
            }
        }

        @Override
        public void notifyLater(NotifyDescriptor descriptor) {
            if (delegate != null) {
                delegate.notify(descriptor);
            } else {
                descriptor.setValue(NotifyDescriptor.CANCEL_OPTION);
            }
        }
    }
    
    DDImpl dialogImpl = new DDImpl();
    
    private List<Logger> loggers = new ArrayList<>();
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        
        MockLookup.setLayersAndInstances(dialogImpl);
        
        // if an user-level gradle.properties does not exist, create it with empty content
        Path up = Paths.get(System.getProperty("user.home"), ".gradle", "gradle.properties");
        if (!Files.exists(up)) {
            Files.write(up, Arrays.asList());
        }
        
        clearWorkDir();
        workDir = FileUtil.toFileObject(getWorkDir());
        dataDir = FileUtil.toFileObject(getDataDir());

        IOProvider.getDefault();

        File destDirF = getTestNBDestDir();
    
        DummyInstalledFileLocator.registerDestDir(destDirF);
        GradleExperimentalSettings.getDefault().setOpenLazy(false);
        
        setLoggers(Level.FINER, 
                org.netbeans.modules.gradle.reload.GradleReloadImplementation.class.getName(), 
                "org.netbeans.modules.project.dependency.ProjectReload");
    }
    
    void setLoggers(Level level, String... names) {
        for (String n : names) {
            Logger l = Logger.getLogger(n);
            loggers.add(l);
            l.setLevel(level);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        MockLookup.setLayersAndInstances();
        super.tearDown(); 
    }
    
    private void setupSimpleProject2() throws Exception {
        FileObject mp = dataDir.getFileObject("projects/micronaut");
        prjDir = FileUtil.copyFile(mp, workDir, mp.getName());
        }
    
    private void setupSimpleProject3() throws Exception {
        project = ProjectManager.getDefault().findProject(prjDir);
        ProjectTrust.getDefault().trustProject(project);
    }
    
    private void setupSimpleProject() throws Exception {
        setupSimpleProject2();
        setupSimpleProject3();
        NbGradleProject.get(project).toQuality("", NbGradleProject.Quality.FULL_ONLINE, false).toCompletableFuture().get();
        NbGradleProject.get(project).toQuality("", NbGradleProject.Quality.FULL_ONLINE, false).toCompletableFuture().get();
}
    
    private void setupComplexProject() throws Exception {
        FileObject mp = dataDir.getFileObject("projects/multi");
        prjDir = FileUtil.copyFile(mp, workDir, mp.getName());
        libDir = prjDir.getFileObject("app");
        ociDir = prjDir.getFileObject("oci");
        project = ProjectManager.getDefault().findProject(prjDir);
        libProject = ProjectManager.getDefault().findProject(libDir);
        ociProject = ProjectManager.getDefault().findProject(ociDir);
        
        ProjectTrust.getDefault().trustProject(project);
        ProjectTrust.getDefault().trustProject(libProject);
        ProjectTrust.getDefault().trustProject(ociProject);
        
        NbGradleProject.get(project).toQuality("", NbGradleProject.Quality.FULL_ONLINE, false).toCompletableFuture().get();
        NbGradleProject.get(libProject).toQuality("", NbGradleProject.Quality.FULL_ONLINE, false).toCompletableFuture().get();
        NbGradleProject.get(ociProject).toQuality("", NbGradleProject.Quality.FULL_ONLINE, false).toCompletableFuture().get();
    }
    
    /**
     * Checks that the root project just reports its own POM and the settings.
     * @throws Exception 
     */
    public void testRootProjectReloadJustRoot() throws Exception {
        setupComplexProject();
        
        Collection<FileObject> fos = ProjectReload.getProjectState(project, true).getLoadedFiles();
        
        assertEquals(3, fos.size());
        assertTrue(fos.contains(prjDir.getFileObject("settings.gradle")));
        assertTrue(fos.contains(prjDir.getFileObject("gradle.properties")));
        assertTrue(fos.contains(FileUtil.toFileObject(new File(System.getProperty("user.home"))).getFileObject(".gradle/gradle.properties")));
    }
    
    /**
     * Checks that project reports parent.
     * @throws Exception 
     */
    public void testReloadFilesReportsParent() throws Exception {
        setupComplexProject();
        
        Collection<FileObject> fos = ProjectReload.getProjectState(project, true).getLoadedFiles();
        
        assertEquals(3, fos.size());
        assertTrue(fos.contains(prjDir.getFileObject("settings.gradle")));
        assertTrue(fos.contains(prjDir.getFileObject("gradle.properties")));
        assertTrue(fos.contains(FileUtil.toFileObject(new File(System.getProperty("user.home"))).getFileObject(".gradle/gradle.properties")));
    }

    /**
     * Checks that reload of a subproject fails if its parent-pom is modified.
     * @throws Exception 
     */
    public void testReloadFailsWithRootProject() throws Exception {
        setupComplexProject();
        
        FileObject rootPomFile = prjDir.getFileObject("settings.gradle");
        EditorCookie cake = rootPomFile.getLookup().lookup(EditorCookie.class);
        Document doc = cake.openDocument();
        doc.insertString(0, "aaa", null);
        doc.remove(0, 3);
        
        try {
            ProjectState p = ProjectReload.withProjectState(ociProject, 
                    StateRequest.refresh()).toCompletableFuture().get();
            fail("Should fail as pom is modified in memory");
        } catch (ExecutionException ex) {
            assertSame(ProjectOperationException.class, ex.getCause().getClass());
            ProjectOperationException t = (ProjectOperationException)ex.getCause();
            // expected
            assertEquals(ProjectOperationException.State.OUT_OF_SYNC, t.getState());
        }
    }
    
    private AtomicReference<NotifyDescriptor> dialogDisplayed = new AtomicReference<>();

    /**
     * Checks that reload of a subproject fails if its parent-pom is modified.
     * @throws Exception 
     */
    public void testReloadSavesRootProject() throws Exception {
        setupComplexProject();
        
        FileObject rootPomFile = prjDir.getFileObject("settings.gradle");
        EditorCookie cake = rootPomFile.getLookup().lookup(EditorCookie.class);
        Document doc = cake.openDocument();
        doc.insertString(0, "aaa", null);
        doc.remove(0, 3);
        
        dialogImpl.delegate = (nd) -> {
            assertNull(dialogDisplayed.getAndSet(nd));
            nd.setValue(NotifyDescriptor.OK_OPTION);
            return CompletableFuture.completedFuture(nd);
        };
        
        ProjectState ps = ProjectReload.withProjectState(ociProject, 
                StateRequest.refresh().saveModifications()).toCompletableFuture().get();
        assertNotNull(ps);
        assertTrue(ps.isConsistent());
    }
    
    /**
     * Checks that the reload succeeds, if only submodule has been modified. The reload should
     * succeed immediately as on-disk state is not modified after the project load.
     * @throws Exception 
     */
    public void testReloadOKWithSubprojectModified() throws Exception {
        setupComplexProject();
        
        // need to establish some state, so the check has a baseline
        ProjectState baseline = ProjectReload.getProjectState(libProject, true);
        
        FileObject ociPomFile = ociProject.getProjectDirectory().getFileObject("build.gradle");
        EditorCookie cake = ociPomFile.getLookup().lookup(EditorCookie.class);
        Document doc = cake.openDocument();
        doc.insertString(0, "aaa", null);
        doc.remove(0, 3);

        CompletableFuture f;
        synchronized (this) {
            f = ProjectReload.withProjectState(libProject, 
                StateRequest.refresh()).toCompletableFuture().thenAccept(p -> {
                    synchronized (this) {
                        // just block
                    }
                });
            // the reload actually does not happen. The Future completes even before the withProjectState returns.
            assertTrue(f.isDone());
        }
    }
    
    /**
     * Requests an inconsistent state, which ignores on-disk modifications.
     */
    public void testReloadIgnoresModifications() throws Exception {
        setupSimpleProject();
        
        // need to establish some state, so the check has a baseline
        ProjectState baseline = ProjectReload.getProjectState(project, true);

        FileObject rootPomFile = prjDir.getFileObject("settings.gradle");
        EditorCookie cake = rootPomFile.getLookup().lookup(EditorCookie.class);
        Document doc = cake.openDocument();
        doc.insertString(0, "aaa", null);
        doc.remove(0, 3);
        
        Thread.sleep(2000);
        // let's touch the root's pom.xml file to be formally newer than the loaded
        // project
        Files.setLastModifiedTime(Paths.get(rootPomFile.toURI()), FileTime.fromMillis(System.currentTimeMillis()));
        
        CompletableFuture f;
        AtomicInteger n = new AtomicInteger();
        synchronized (this) {
            f = ProjectReload.withProjectState(project, 
                    StateRequest.refresh().consistent(false)).toCompletableFuture().thenAccept(p -> {
                // must complete synchronously.
                synchronized (this) {
                    assertEquals(0, n.get());
                }
            });
            // the Future must not complete synchronously - a good signt that the project is actually being reloaded.
            assertTrue(f.isDone());
        }
    }
    
    private void setupSimpleProjectWithMissingArtifacts() throws Exception {
        ConnectorServices.reset();
        setupSimpleProject2();
        Path p = Paths.get(System.getProperty("user.home"), ".gradle", "caches", "modules-2", "files-2.1", "io.micronaut", "micronaut-http-validation");
        int exit = new ProcessBuilder("gradle", "--stop").start().waitFor();
        assertEquals("Could not kill gradle daemons", 0, exit);
        if (Files.exists(p)) {
            Files.walk(p)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
        setupSimpleProject3();
        NbGradleProject.get(project).toQuality("", NbGradleProject.Quality.EVALUATED, false).toCompletableFuture().get();
    }
    
    /**
     * Checks that a project, that desperately needs to download artifacts will fail to reach ready state in offline mode. For this, we 
     * remove micronaut-parent parent POM that is referenced by the project.
     */
    public void testReloadFailsInOfflineMode() throws Exception {
        // must remove artifacts from .m2 BEFORE the project is opened / scanned.
        setupSimpleProjectWithMissingArtifacts();
        
        try {
            ProjectReload.withProjectState(project, 
                StateRequest.refresh().toQuality(ProjectReload.Quality.RESOLVED).offline()).toCompletableFuture().get();
            fail("Dependencies are missing, and offline is forced");
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof ProjectOperationException);
            ProjectOperationException x2 = (ProjectOperationException)ex.getCause();
            assertEquals(ProjectOperationException.State.OFFLINE, x2.getState());
        }
    }

    /**
     * Checks that refresh while all project files are up-to-date does nothing
     * and completes immediately.
     */
    public void testRefreshWhileCurrent() throws Exception {
        setupSimpleProject();

        // need to establish some state, so the check has a baseline
        ProjectReload.getProjectState(project, true);

        // block the possibly completed future from entering the mutex ... if it runs in a separate thread,
        // which it would do, if the project is reloaded.
        AtomicInteger i = new AtomicInteger(1);
        synchronized (this) {
            ProjectReload.withProjectState(project, StateRequest.refresh()).thenApply(p -> {
                synchronized (this) {
                    i.incrementAndGet();
                }
                return p;
            });
            
            assertEquals("Up-to-date project reload should complete synchronously", 2, i.get());
        }
    }
    
    /**
     * Checks that a project whose files have changed will be reloaded.
     * @throws Exception 
     */
    public void testRefreshStaleProject() throws Exception {
        setupSimpleProject();

        // need to establish some state, so the check has a baseline
        ProjectState baseline = ProjectReload.getProjectState(project, true);

        GradleBaseProject gbp = GradleBaseProject.get(project);
        Collection<? extends GradleDependency> origDeps = gbp.getConfigurations().get("implementation").getConfiguredDependencies();
        FileObject buildFile = prjDir.getFileObject("build.gradle");
        List<String> fileLines = new ArrayList<>(buildFile.asLines());
        int idx = fileLines.indexOf(fileLines.stream().filter(s -> s.contains("dependencies {")).findAny().get());
        fileLines.add(idx + 1, "implementation(\"org.reactivestreams:reactive-streams\")");
        // wait, since sometimes timestamps have 2-sec granularity
        Thread.sleep(2000);
        // overwrite, adding a dependency
        Files.write(Paths.get(buildFile.toURI()), fileLines);
        // force events
        buildFile.refresh();
        gbp = GradleBaseProject.get(project);
        
        AtomicInteger i = new AtomicInteger(1);

        CompletableFuture<ProjectState> f;
        
        synchronized (this) {
            f = ProjectReload.withProjectState(project, StateRequest.refresh()).toCompletableFuture().thenApply(p -> {
                synchronized (this) {
                    i.incrementAndGet();
                }
                return p;
            });
            assertEquals("Loading should fork to a separate thread", 1, i.get());
        }
        f.get();
        gbp = GradleBaseProject.get(project);
        Collection<? extends GradleDependency> afterRefreshDeps = new ArrayList<>(gbp.getConfigurations().get("implementation").getConfiguredDependencies());
        afterRefreshDeps.removeAll(origDeps);
        assertEquals("One more dependency should appear", 1, afterRefreshDeps.size());
    }
    
    /**
     * Checks that a project that is up-to-date will be still reloaded, if the load is forced.
     * @throws Exception 
     */
    public void testForceRefreshCurrent() throws Exception {
        setupSimpleProject();

        GradleBaseProject gbp = GradleBaseProject.get(project);
        CompletableFuture<ProjectState> f;
        
        AtomicInteger i = new AtomicInteger(1);
        synchronized (this) {
            f = ProjectReload.withProjectState(project, StateRequest.reload()).toCompletableFuture().thenApply(p -> {
                synchronized (this) {
                    i.incrementAndGet();
                }
                return p;
            });
            assertEquals("Loading should fork to a separate thread", 1, i.get());
        }
        f.get();
    }
    
    /**
     * Check that a project load will fail, if the project files are modified in the memory.
     * @throws Exception 
     */
    public void testFailWithModifiedFiles() throws Exception {
        setupSimpleProject();

        // need to establish some state, so the check has a baseline
        ProjectState baseline = ProjectReload.getProjectState(project, true);

        FileObject build = prjDir.getFileObject("build.gradle");
        Document doc = build.getLookup().lookup(EditorCookie.class).openDocument();
        doc.insertString(0, "/* comment */", null);
        try {
            ProjectState f = ProjectReload.withProjectState(project, StateRequest.refresh()).toCompletableFuture().get();
            fail("Should fail with ProjectOperationException");
        } catch (ExecutionException ee) {
            assertTrue(ee.getCause() instanceof ProjectOperationException);
            ProjectOperationException ex = (ProjectOperationException)ee.getCause();
            assertEquals(1, ex.getFiles().size());
            assertSame(build, ex.getFiles().iterator().next());
        }
    }
    
    /**
     * Check that a project load will fail, if the project files are modified in the memory.
     * @throws Exception 
     */
    public void testFailWithModifiedFilesInRoot() throws Exception {
        setupComplexProject();

        FileObject build = prjDir.getFileObject("settings.gradle");
        Document doc = build.getLookup().lookup(EditorCookie.class).openDocument();
        doc.insertString(0, "/* comment */", null);
        try {
            ProjectState f = ProjectReload.withProjectState(libProject, StateRequest.refresh()).toCompletableFuture().get();
            fail("Should fail with ProjectOperationException");
        } catch (ExecutionException ee) {
            assertTrue(ee.getCause() instanceof ProjectOperationException);
            ProjectOperationException ex = (ProjectOperationException)ee.getCause();
            assertEquals(1, ex.getFiles().size());
            assertSame(build, ex.getFiles().iterator().next());
        }
    }

    /**
     * Check that the project can be loaded ignoring modified files.
     * @throws Exception 
     */
    public void testIgnoreModifiedFiles() throws Exception {
        setupSimpleProject();

        FileObject build = prjDir.getFileObject("build.gradle");
        Document doc = build.getLookup().lookup(EditorCookie.class).openDocument();
        doc.insertString(0, "/* comment */", null);
        CompletableFuture<ProjectState> f = ProjectReload.withProjectState(project, 
                StateRequest.refresh().
                // use fallback, to avoid downloads with empty cache
                toQuality(ProjectReload.Quality.FALLBACK).consistent(false)).toCompletableFuture();
        f.get();
    }
    
    /**
     * Checks that if the caller asks for a better quality than available, the project loads 
     * even though the caller would accept inconsistent data.
     * 
     * @throws Exception 
     */
    public void testBetterQualityLoadsInconsistent() throws Exception {
        setupSimpleProjectWithMissingArtifacts();

        // need to establish some state, so the check has a baseline
        ProjectState baseline = ProjectReload.getProjectState(project, true);

        FileObject build = prjDir.getFileObject("build.gradle");
        // let some time pass, so that the timestamp really changes.
        Thread.sleep(1000);
        FileUtil.toFile(build).setLastModified(System.currentTimeMillis());
        build.refresh();

        AtomicInteger phase = new AtomicInteger();
        CompletableFuture<ProjectState> f;
        synchronized (this) {
            f = ProjectReload.withProjectState(project, 
                    StateRequest.refresh().
                    // use fallback, to avoid downloads with empty cache
                    toQuality(ProjectReload.Quality.SIMPLE).consistent(false)).toCompletableFuture();
            f = f.thenApply((x) -> {
                // should block until the caller's sync blok ends
                synchronized (this) {
                    assertEquals(1, phase.getAndIncrement());
                }
                return x;
            });
            
            assertEquals(0, phase.getAndIncrement());
        }
        f.get();
        NbGradleProject gp = NbGradleProject.get(project);
        assertTrue(gp.getQuality().atLeast(NbGradleProject.Quality.EVALUATED));
    }
}
