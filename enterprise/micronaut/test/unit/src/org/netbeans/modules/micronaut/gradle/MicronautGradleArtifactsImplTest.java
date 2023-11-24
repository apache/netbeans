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
package org.netbeans.modules.micronaut.gradle;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.api.GradleReport;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.windows.IOProvider;

/**
 *
 * @author sdedic
 */
public class MicronautGradleArtifactsImplTest extends NbTestCase {

    static {
        // TODO remove ASAP from MicronautGradleArtifactsImplTest and ProjectViewTest
        // investigate "javax.net.ssl.SSLHandshakeException: Received fatal alert: handshake_failure"
        // during gradle download "at org.netbeans.modules.gradle.spi.newproject.TemplateOperation$InitStep.execute(TemplateOperation.java:317)"
        // this looks like a misconfigured webserver to me
        System.setProperty("https.protocols", "TLSv1.2");
    }

    public MicronautGradleArtifactsImplTest(String name) {
        super(name);
    }
    
    private static File getTestNBDestDir() throws Exception {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }
    
    private FileObject dataFO;

    @org.openide.util.lookup.ServiceProvider(service=org.openide.modules.InstalledFileLocator.class, position = 1000)
    public static class InstalledFileLocator extends DummyInstalledFileLocator {
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        
        // This is needed, otherwose the core window's startup code will redirect
        // System.out/err to the IOProvider, and its Trivial implementation will redirect
        // it back to System.err - loop is formed. Initialize IOProvider first, it gets
        // the real System.err/out references.
        IOProvider p = IOProvider.getDefault();
        System.setProperty("test.reload.sync", "true");
        dataFO = FileUtil.toFileObject(getDataDir());
        
       // Configure the DummyFilesLocator with NB harness dir
        File destDirF = getTestNBDestDir();
        DummyInstalledFileLocator.registerDestDir(destDirF);
        Main.getModuleSystem();
    }
    
    public void testProjectArtifactWithNormalQuery() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("gradle/artifacts/simple");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simple");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectTrust.getDefault().trustProject(p);

        NbGradleProject.get(p).toQuality("Test", NbGradleProject.Quality.FULL, true).toCompletableFuture().get();
        assertNoProblems(p);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("jar", spec.getType());
    }
    
    public void testNativeOutputInRegularBuild() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("gradle/artifacts/simple");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simple");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectTrust.getDefault().trustProject(p);
        
        NbGradleProject.get(p).toQuality("Test", NbGradleProject.Quality.FULL, true).toCompletableFuture().get();
        assertNoProblems(p);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery("exe"));
        
        assertNotNull(ar);
        assertEquals(0, ar.getArtifacts().size());
    }
    
    public void testNativeCompileDefaultOutput() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("gradle/artifacts/simple");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simple");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectTrust.getDefault().trustProject(p);

        NbGradleProject.get(p).toQuality("Test", NbGradleProject.Quality.FULL, true).toCompletableFuture().get();
        assertNoProblems(p);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, null, 
                        ProjectActionContext.newBuilder(p).forProjectAction("native-build").context()
                )
        );
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("exe", spec.getType());
    }
    
    public void testDefaultNativeCompilationOutput() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("gradle/artifacts/simple");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simple");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectTrust.getDefault().trustProject(p);

        NbGradleProject.get(p).toQuality("Test", NbGradleProject.Quality.FULL, true).toCompletableFuture().get();
        assertNoProblems(p);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, null, 
                        ProjectActionContext.newBuilder(p).forProjectAction("native-build").context()
                )
        );
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("exe", spec.getType());
    }

    public void testJarInNativeCompilation() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("gradle/artifacts/simple");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simple");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectTrust.getDefault().trustProject(p);

        NbGradleProject.get(p).toQuality("Test", NbGradleProject.Quality.FULL, true).toCompletableFuture().get();
        assertNoProblems(p);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery("jar", null, 
                        ProjectActionContext.newBuilder(p).forProjectAction("native-build").context()
                )
        );
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("jar", spec.getType());
    }

    public void testNativeOutputInNativeBuild() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("gradle/artifacts/simple");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simple");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectTrust.getDefault().trustProject(p);

        NbGradleProject.get(p).toQuality("Test", NbGradleProject.Quality.FULL, true).toCompletableFuture().get();
        assertNoProblems(p);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery("exe", null, 
                        ProjectActionContext.newBuilder(p).forProjectAction("native-build").context()
                )
        );
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("exe", spec.getType());
    }
    
    
    public void testSubprojectJarOutput() throws Exception {
        clearWorkDir();
        FileUtil.toFileObject(getWorkDir()).refresh();

        FileObject testApp = dataFO.getFileObject("gradle/artifacts/multi");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simple");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy.getFileObject("oci"));
        ProjectTrust.getDefault().trustProject(p);

        NbGradleProject.get(p).toQuality("Test", NbGradleProject.Quality.FULL, true).toCompletableFuture().get();
        assertNoProblems(p);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("jar", spec.getType());
        assertEquals(null, spec.getClassifier());
    }

    public void testSubprojectShadowOutput() throws Exception {
        clearWorkDir();
        FileUtil.toFileObject(getWorkDir()).refresh();

        FileObject testApp = dataFO.getFileObject("gradle/artifacts/multi");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simple");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy.getFileObject("oci"));
        ProjectTrust.getDefault().trustProject(p);

        NbGradleProject.get(p).toQuality("Test", NbGradleProject.Quality.FULL, true).toCompletableFuture().get();
        assertNoProblems(p);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, null, null, ArtifactSpec.TAG_SHADED)
        );
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("jar", spec.getType());
        assertEquals("all", spec.getClassifier());
    }

    public void testNativeOutputInSubprojectNativeBuild() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("gradle/artifacts/multi");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simple");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy.getFileObject("oci"));
        ProjectTrust.getDefault().trustProject(p);

        NbGradleProject.get(p).toQuality("Test", NbGradleProject.Quality.FULL, true).toCompletableFuture().get();
        assertNoProblems(p);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery("exe", null, 
                        ProjectActionContext.newBuilder(p).forProjectAction("native-build").context()
                )
        );
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("exe", spec.getType());
    }

    private static void assertNoProblems(Project project) {
        assertNotNull(project);
        Set<GradleReport> problems = ((NbGradleProjectImpl)project).getGradleProject().getBaseProject().getProblems();
        assertTrue(
            problems.size()+" problem(s) found>>>\n"+
            problems.stream()
                .map(p -> p.toString())
                .collect(Collectors.joining("\n"))
            +"\n<<<end of problems",
            problems.isEmpty()
        );
    }
}

