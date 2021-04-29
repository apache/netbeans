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
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sdedic
 */
public class NbGradleProjectImplTest extends AbstractGradleProjectTestCase {
    
    public NbGradleProjectImplTest(String name) {
        super(name);
    }
    
    private Project createProject() throws Exception {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "apply plugin: 'java'\n", "");
        return ProjectManager.getDefault().findProject(a);
    }
    
    /**
     * Checks that untrusted unopened project will present itself as a fallback.
     * @throws Exception 
     */
    public void testUntrustedProjectFallback() throws Exception {
        Project prj = createProject();
        NbGradleProject ngp = NbGradleProject.get(prj);
        
        assertTrue(ngp.getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
    }
    
    /**
     * Checks that untrusted unopened project will present itself as a fallback.
     * @throws Exception 
     */
    public void testInitialLoadDoesNotFireChange() throws Exception {
        Project prj = createProject();
        NbGradleProject ngp = NbGradleProject.get(prj);
        assertTrue(ngp.getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
    }
    
    /**
     * Checks that an attempt to reload project with escalated quality will fail
     * for an untrusted project.
     * @throws Exception 
     */
    public void testUntrustedProjectCannotGoUp() throws Exception {
        Project prj = createProject();
        
        NbGradleProjectImpl prjImpl = prj.getLookup().lookup(NbGradleProjectImpl.class);
        assertTrue(prjImpl.getGradleProject().getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
        // attempt to load everything
        prjImpl.setAimedQuality(NbGradleProject.Quality.FULL);
        // ... it loaded, but did not escalate the quality bcs not trusted
        assertTrue(prjImpl.getGradleProject().getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
    }
    
    /**
     * Check that a trusted project can be loaded as 'evaluated' quality, at least.
     * @throws Exception 
     */
    public void testTrustedProjectLoadsToEvaluated() throws Exception {
        Project prj = createProject();
        
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
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
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
        Project prj = createProject();
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
        Project prj = createProject();
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
    
    /**
     * Checks that ProjectInfo events are processed before completion of the load future
     */
    public void testEventsProcessedBeforeCompletion() throws Exception {
        Project prj = createProject();
        NbGradleProject ngp = NbGradleProject.get(prj);
        ProjectTrust.getDefault().trustProject(prj);
        // initializes the project
        assertTrue(ngp.getQuality().worseThan(NbGradleProject.Quality.EVALUATED));
        
        ngp.addPropertyChangeListener(projL);
        NbGradleProjectImpl prjImpl = prj.getLookup().lookup(NbGradleProjectImpl.class);
        
        // block the dispatch
        projL.block.drainPermits();
        CompletableFuture<GradleProject> f = prjImpl.loadOwnProject0("Test reload", true, true, Quality.FULL, false);
        assertFalse(f.isDone());
        projL.processing.acquire();
        // still not done, since blocked inside the listener
        assertFalse(f.isDone());
        projL.block.release();
        // finally wait
        GradleProject p = f.get();
        assertTrue(p.getQuality().atLeast(NbGradleProject.Quality.EVALUATED));
    }
    
    
    public void testIncreaseAimedQualityChangesProject() throws Exception {
        Project prj = createProject();
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
    
    public void testSufficientAimedQualityNoop() throws Exception {
        checkProjectDoesNotChange(Quality.FULL);
    }
    
    public void testDecreaseAimedQualityNoop() throws Exception {
        checkProjectDoesNotChange(Quality.EVALUATED);
    }
    
    private void checkProjectDoesNotChange(Quality aimed) throws Exception {
        Project prj = createProject();
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
