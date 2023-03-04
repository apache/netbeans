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

package org.netbeans.modules.maven.problems;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.test.TestFileUtils;

public class ProblemReporterImplTest extends NbTestCase { // #175472

    public ProblemReporterImplTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("test.reload.sync", "true");
    }
    
    private static ProblemReporterImpl getReporter(Project p) {
        return p.getLookup().lookup(NbMavenProjectImpl.class).getProblemReporter();
    }

    public void testMissingParent() throws Exception {
        TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion>" +
            "<parent><groupId>g</groupId><artifactId>par</artifactId><version>0</version></parent>" +
            "<artifactId>m</artifactId>" +
            "</project>");
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir()));
        assertEquals("g:m:jar:0", p.getLookup().lookup(NbMavenProject.class).getMavenProject().getId());
        ProblemReporterImpl pr = getReporter(p);
        MavenModelProblemsProvider mpp = new MavenModelProblemsProvider(p);
        Collection<? extends ProjectProblemsProvider.ProjectProblem> problems = mpp.getProblems();
        
        waitForReports();
        assertFalse(problems.isEmpty());
        
        assertEquals(Collections.singleton(a2f(new DefaultArtifact("g", "par", "0", null, "pom", null, new DefaultArtifactHandler("pom")))), pr.getMissingArtifactFiles());
    }
    
    private File a2f(Artifact a) {
        EmbedderFactory.getProjectEmbedder().getLocalRepository().find(a);
            //a.getFile should be already normalized but the find() method can pull tricks on us.
            //#225008
        return FileUtil.normalizeFile(a.getFile());
    }

    public void testMissingPlugin() throws Exception {
        TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion>" +
            "<groupId>g</groupId><artifactId>m</artifactId><version>0</version>" +
            "<build><plugins><plugin><groupId>g</groupId><artifactId>plug</artifactId><version>0</version><extensions>true</extensions></plugin></plugins></build>" +
            "</project>");
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir()));
        ProblemReporterImpl pr = getReporter(p);
        MavenModelProblemsProvider mpp = new MavenModelProblemsProvider(p);
        Collection<? extends ProjectProblemsProvider.ProjectProblem> problems = mpp.getProblems();
        waitForReports();
        assertFalse(problems.isEmpty());
        assertEquals(Collections.singleton(a2f(new DefaultArtifact("g", "plug", "0", null, "jar", null, new DefaultArtifactHandler("jar")))), pr.getMissingArtifactFiles());
    }

    public void testMissingDependency() throws Exception {
        TestFileUtils.writeFile(new File(getWorkDir(), "pom.xml"), "<project xmlns='http://maven.apache.org/POM/4.0.0'><modelVersion>4.0.0</modelVersion>" +
            "<groupId>g</groupId><artifactId>m</artifactId><version>0</version>" +
            "<dependencies><dependency><groupId>g</groupId><artifactId>b</artifactId><version>1.0-SNAPSHOT</version></dependency></dependencies>" +
            "</project>");
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(getWorkDir()));
        ProblemReporterImpl pr = getReporter(p);
        MavenModelProblemsProvider mpp = new MavenModelProblemsProvider(p);
        Collection<? extends ProjectProblemsProvider.ProjectProblem> problems = mpp.getProblems();
        waitForReports();
        assertFalse(problems.isEmpty());
        assertEquals(Collections.singleton(a2f(new DefaultArtifact("g", "b", "1.0-SNAPSHOT", "compile", "jar", null, new DefaultArtifactHandler("jar")))), pr.getMissingArtifactFiles());
    }

    // XXX write test for FCL and reloading (requires modifications to local repo)

    /**
     * Waits until reports are initialized in
     * NbMavenProjectImpl.loadOriginalMavenProject().
     */
    private void waitForReports() throws Exception {
        final Object lock = new Object();
        ProblemReporterImpl.RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });
        synchronized (lock) {
            lock.wait(5000);
        }
    }
}
