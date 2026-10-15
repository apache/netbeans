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
package org.netbeans.modules.gradle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import org.gradle.tooling.ProjectConnection;
import org.junit.After;
import org.junit.Before;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class NbGradleProjectImplTest extends AbstractGradleProjectTestCase {
    
    public NbGradleProjectImplTest(String name) {
        super(name);
    }
    
    private FileObject projectDir;
    private Project prj;
    
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        prj = createProject();
    }
    
    @After
    @Override
    public void tearDown() throws Exception {
        ProjectConnection pconn = prj.getLookup().lookup(ProjectConnection.class);
        if (pconn instanceof GradleProjectConnection) {
            GradleProjectConnection gpconn = (GradleProjectConnection) pconn;
            gpconn.close();
        }
        prj = null;
        super.tearDown();
    }
    
    private Project createProject() throws Exception {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "apply plugin: 'java'\n", "");
        projectDir = a;
        return ProjectManager.getDefault().findProject(a);
    }
    
    private void assertHasNoConnection(Project p) throws Exception {
        ProjectConnection pconn = p.getLookup().lookup(ProjectConnection.class);
        if (pconn instanceof GradleProjectConnection) {
            GradleProjectConnection gpconn = (GradleProjectConnection) pconn;
            assertFalse(gpconn.hasConnection());
        }
    }
    
    /**
     * Checks that untrusted unopened project will present itself as a fallback.
     * @throws Exception 
     */
    public void testUntrustedProjectFallback() throws Exception {
        NbGradleProject ngp = NbGradleProject.get(prj);
        
        assertTrue(ngp.getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
    }
    
    /**
     * Checks that untrusted unopened project will present itself as a fallback.
     * @throws Exception 
     */
    public void testInitialLoadDoesNotFireChange() throws Exception {
        NbGradleProject ngp = NbGradleProject.get(prj);
        assertTrue(ngp.getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
        assertHasNoConnection(prj);
    }
    
    /**
     * Checks that an attempt to reload project with escalated quality will fail
     * for an untrusted project.
     * @throws Exception 
     */
    public void testUntrustedProjectCannotGoUp() throws Exception {
        
        NbGradleProjectImpl prjImpl = prj.getLookup().lookup(NbGradleProjectImpl.class);
        assertTrue(prjImpl.getGradleProject().getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
        // attempt to load everything
        prjImpl.setAimedQuality(NbGradleProject.Quality.FULL);
        // ... it loaded, but did not escalate the quality bcs not trusted
        assertTrue(prjImpl.getGradleProject().getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
        assertHasNoConnection(prj);
    }
    
    /**
     * Check that a trusted project can be loaded as 'evaluated' quality, at least.
     * @throws Exception 
     */
    public void testTrustedProjectLoadsToEvaluated() throws Exception {
        
        NbGradleProjectImpl prjImpl = prj.getLookup().lookup(NbGradleProjectImpl.class);
        assertTrue(prjImpl.getGradleProject().getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
        ProjectTrust.getDefault().trustProject(prj);
        
        // attempt to load everything
        prjImpl.setAimedQuality(NbGradleProject.Quality.FULL);
        // ... it loaded, but did not escalate the quality bcs not trusted
        assertTrue(prjImpl.getGradleProject().getQuality().atLeast(NbGradleProject.Quality.EVALUATED));
    }

    class L implements PropertyChangeListener {
        volatile PropertyChangeEvent e;
        volatile Semaphore block = new Semaphore(20);
        Queue<PropertyChangeEvent> all = new ArrayBlockingQueue<>(20);
        Semaphore processing = new Semaphore(0);
        volatile Future projectLoadState;
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (prjImpl != null) {
                projectLoadState = prjImpl.loadOwnProject0("Test reload", true, false, Quality.EVALUATED, false, false);
            }
            processing.release();
            try {
                block.acquire();
            } catch (InterruptedException ex) {
                throw new AssertionFailedErrorException(ex);
            }
            e = evt;
            all.add(evt);
        }
    }
    
    L projL = new L();
    
    /**
     * Checks that initial load with 'fallback' quality does not fire 'project reloaded'
     * event.
     */
    public void testInitialLoadReloadNotFired() throws Exception {
        NbGradleProject ngp = NbGradleProject.get(prj);
        
        ngp.addPropertyChangeListener(projL);
        
        Quality q = ngp.getQuality();
        assertNull(projL.e);
    }
    
    /**
     * After the project changes (increases) the quality after initial load, check
     * that the ProjectInfo property change is fired.
     */
    public void testProjectQualityUpgradeFiresChange() throws Exception {
        NbGradleProject ngp = NbGradleProject.get(prj);
        ProjectTrust.getDefault().trustProject(prj);
        // initializes the project
        assertTrue(ngp.getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
        
        // force project reload
        ngp.addPropertyChangeListener(projL);
        NbGradleProjectImpl prjImpl = prj.getLookup().lookup(NbGradleProjectImpl.class);
        prjImpl.setAimedQuality(NbGradleProject.Quality.FULL);
        assertTrue(prjImpl.getGradleProject().getQuality().atLeast(NbGradleProject.Quality.EVALUATED));
        
        assertNotNull(projL.e);
        assertEquals(NbGradleProject.PROP_PROJECT_INFO, projL.e.getPropertyName());
    }
    
    volatile NbGradleProjectImpl prjImpl;
    
    /**
     * Checks that ProjectInfo events are processed before completion of the load future
     */
    public void testEventsProcessedBeforeCompletion() throws Exception {
        NbGradleProject ngp = NbGradleProject.get(prj);
        ProjectTrust.getDefault().trustProject(prj);
        // initializes the project
        assertTrue(ngp.getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
        
        ngp.addPropertyChangeListener(projL);
        prjImpl = prj.getLookup().lookup(NbGradleProjectImpl.class);
        
        // block the dispatch
        projL.block.drainPermits();
        CompletableFuture<GradleProject> f = prjImpl.loadOwnProject0("Test reload", true, true, Quality.FULL, false, true);
        assertFalse(f.isDone());
        projL.processing.acquire();
        // when listener comes, the future must be completed, otherwise the listener that can request project features
        // could block
        assertTrue(projL.projectLoadState.isDone());
        projL.block.release();
        // finally wait
        GradleProject p = f.get();
        assertTrue(p.getQuality().atLeast(NbGradleProject.Quality.EVALUATED));
    }
    
    /**
     * Checks that request to increase the quality changes the project. This case checks the project
     * going from state {@link Quality#FALLBACK} (never built, trusted) to at least {@link Quality#EVALUATED}
     * as Gradle script execution is permitted (now).
     */
    public void testIncreaseAimedQualityChangesProject() throws Exception {
        NbGradleProject ngp = NbGradleProject.get(prj);
        ProjectTrust.getDefault().trustProject(prj);
        // initializes the project
        assertTrue(ngp.getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
        
        NbGradleProjectImpl prjImpl = prj.getLookup().lookup(NbGradleProjectImpl.class);
        GradleProject curProject = prjImpl.getGradleProject();
        prjImpl.setAimedQuality(Quality.FULL);
        GradleProject newProject = prjImpl.getGradleProject();
        assertTrue(newProject.getQuality().atLeast(NbGradleProject.Quality.EVALUATED));
        assertNotSame(newProject, curProject);
        
    }
    
    /**
     * Aiming for EVALUATED quality does not force execution of the Gradle project, if
     * the evaluated state does not exist.
     */
    public void testEvaluateTrustedDoesNotExecuteScript() throws Exception {
        NbGradleProject ngp = NbGradleProject.get(prj);
        ProjectTrust.getDefault().trustProject(prj);
        // initializes the project
        assertTrue(ngp.getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
        assertHasNoConnection(prj);
        NbGradleProjectImpl prjImpl = prj.getLookup().lookup(NbGradleProjectImpl.class);
        GradleProject curProject = prjImpl.getGradleProject();
        prjImpl.setAimedQuality(Quality.EVALUATED);
        GradleProject newProject = prjImpl.getGradleProject();
        assertTrue(newProject.getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
        assertSame(newProject, curProject);
        assertHasNoConnection(prj);
    }
    
    /**
     * Trusts and closes the project. To avoid project caches, creates a copy in another
     * folder & swaps (FileObject will be different, name the same).
     * Upon new open checks that the project loaded to at least evaluted quality (provided it
     * was full before).
     */
    public void testAllowedProjectLoadsImmediately() throws Exception {
        NbGradleProject ngp = NbGradleProject.get(prj);
        ProjectTrust.getDefault().trustProject(prj);

        FileObject s = projectDir.getParent().createFolder("second");
        
        // open and close the project.
        OpenProjects.getDefault().open(new Project[] { prj }, false);
        OpenProjects.getDefault().openProjects().get();
        OpenProjects.getDefault().close(new Project[] { prj });
        OpenProjects.getDefault().openProjects().get();
        
        assertTrue(ngp.getQuality().atLeast(Quality.EVALUATED));

        copy(projectDir, s);

        String origName = projectDir.getName();
        // rename the files; the cache encodes full path to the project.
        try (FileLock fl = projectDir.lock()) {
            projectDir.rename(fl, "old", null);
        }
        try (FileLock fl = s.lock()) {
            s.rename(fl, origName, null);
        }
        
        Project prj2 = FileOwnerQuery.getOwner(s);
        NbGradleProject ngp2 = NbGradleProject.get(prj2);
        // initializes the project, since it is trusted (reopened), should come
        // in at least EVALUATED quality
        assertTrue(ngp2.getQuality().atLeast(NbGradleProject.Quality.EVALUATED));
    }

    private static void copy(FileObject orig, FileObject to) throws IOException {
        for (FileObject x : orig.getChildren()) {
            if (x.isFolder()) {
                FileObject cp = FileUtil.createFolder(to, x.getNameExt());
                copy(x, cp);
            } else {
                FileObject f = x.copy(to, x.getName(), x.getExt());
                // see https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8177809
                Files.setLastModifiedTime(FileUtil.toFile(f).toPath(),
                        Files.getLastModifiedTime(FileUtil.toFile(x).toPath()));
            }
        }
    }
    
    public void testSufficientAimedQualityNoop() throws Exception {
        checkProjectDoesNotChange(Quality.FULL);
    }
    
    public void testDecreaseAimedQualityNoop() throws Exception {
        checkProjectDoesNotChange(Quality.EVALUATED);
    }
    
    private void checkProjectDoesNotChange(Quality aimed) throws Exception {
        NbGradleProject ngp = NbGradleProject.get(prj);
        ProjectTrust.getDefault().trustProject(prj);
        NbGradleProjectImpl prjImpl = prj.getLookup().lookup(NbGradleProjectImpl.class);
        prjImpl.setAimedQuality(Quality.FULL);
        assertTrue(ngp.getQuality().atLeast(Quality.FULL));
        
        GradleProject curProject = prjImpl.getGradleProject();
        
        prjImpl.setAimedQuality(aimed);
        GradleProject newProject = prjImpl.getGradleProject();
        assertTrue(newProject.getQuality().atLeast(Quality.FULL));
        assertSame(newProject, curProject);
        assertTrue(prjImpl.getAimedQuality().notBetterThan(aimed));
    }
}


