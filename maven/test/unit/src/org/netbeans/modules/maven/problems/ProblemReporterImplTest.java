/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
