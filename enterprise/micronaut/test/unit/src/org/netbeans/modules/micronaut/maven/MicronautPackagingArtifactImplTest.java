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
package org.netbeans.modules.micronaut.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.api.MavenConfiguration;
import org.netbeans.modules.micronaut.NbSuiteTestBase;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOProvider;

/**
 *
 * @author sdedic
 */
public class MicronautPackagingArtifactImplTest extends NbSuiteTestBase {
    public MicronautPackagingArtifactImplTest(String name) {
        super(name);
    }
    
    private static File getTestNBDestDir() throws Exception {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }
    
    FileObject d;
    FileObject dataFO;
    
    Logger l1;
    Logger l2;
    
    private List<Logger> loggers  = new ArrayList<>(); 

    protected @Override void setUp() throws Exception {
        clearWorkDir();

        // This is needed, otherwose the core window's startup code will redirect
        // System.out/err to the IOProvider, and its Trivial implementation will redirect
        // it back to System.err - loop is formed. Initialize IOProvider first, it gets
        // the real System.err/out references.
        IOProvider p = IOProvider.getDefault();
        d = FileUtil.toFileObject(getWorkDir());
        System.setProperty("test.reload.sync", "true");
        dataFO = FileUtil.toFileObject(getDataDir());
        
        // Configure the DummyFilesLocator with NB harness dir
        File destDirF = getTestNBDestDir();
        DummyInstalledFileLocator.registerDestDir(destDirF);
    }
    
    /**
     * Will open and prime project. On CI with a pristine repository, the micronaut build plugin (see test data)
     * must be downloaded in order to properly parse the pom with {@code native-image} packaging that is otherwise
     * not recognized. For that we need to prime the project before it is used in the test - this populates maven
     * local repo with appropriate dependencies.
     * 
     * @param prjCopy
     * @return
     * @throws Exception 
     */
    private Project openAndPrimeProject(FileObject prjCopy) throws Exception {
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        assertNotNull(p);
        OpenProjects.getDefault().open(new Project[] { p }, true);
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        assertNotNull(ap);
        
        CountDownLatch latch = new CountDownLatch(1);
        ActionProgress progress = new ActionProgress() {
            @Override
            protected void started() {
            }

            @Override
            public void finished(boolean success) {
                latch.countDown();
            }
            
        };
        ap.invokeAction(ActionProvider.COMMAND_PRIME, Lookups.fixed(progress));
        latch.await(10, TimeUnit.MINUTES);
        return p;
    }
    
    /**
     * Checks that native artifact provider does not obscure normal project's query
     * @throws Exception 
     */
    public void testProjectArtifactWithNormalQuery() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("maven/artifacts/simple");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simple");
        
        Project p = openAndPrimeProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null));
        
        assertNotNull(ar);  
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("jar", spec.getType());
    }
    
    /**
     * The project has no native build, so the action should produce no artifact(s)
     */
    public void testProjectNoArtifactForNonExistingNativeBuild() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("maven/artifacts/simple");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simple");
        
        Project p = openAndPrimeProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, null, 
                        ProjectActionContext.newBuilder(p).forProjectAction("native-build").context())
        );
        
        assertNotNull(ar);
        assertEquals(0, ar.getArtifacts().size());
    }
    
    /**
     * If jar packaging is the default, then no-arg query should still produce the jar artifact.
     */
    public void testProjectArtifactOptionalNativePackaging() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("maven/artifacts/native-optional");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "native-optional");
        
        Project p = openAndPrimeProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("jar", spec.getType());
    }
    
    /**
     * If jar packaging is the default, then native stuff is not built at all, so it is not present in ALL artifact enum.
     */
    public void testAllArtifactOptionalNativePackaging() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("maven/artifacts/native-optional");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "native-optional");
        
        Project p = openAndPrimeProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(ProjectArtifactsQuery.Filter.TYPE_ALL));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("jar", spec.getType());
    }
    
    /**
     * But with native-build action, the response should be 'exe'
     */
    public void testProjectArtifactOptionalNativeAndAction() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("maven/artifacts/native-optional");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "native-optional");
        
        Project p = openAndPrimeProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(null, null, 
                        ProjectActionContext.newBuilder(p).forProjectAction("native-build").context())
        );
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("exe", spec.getType());
    }
    
   /**
     * Checks that project with default native-image packaging will provide the excutable
     * @throws Exception 
     */
    public void testProjectArtifactWithDefaultNativePackaging() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("maven/artifacts/native-default");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "native-default");
        
        Project p = openAndPrimeProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null));
        
        assertNotNull(ar);
        assertEquals("Default product is just the exe", 1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("Exe must be present", "exe", spec.getType());
    }

  /**
     * Checks that project with default native-image packaging will provide the excutable
     * @throws Exception 
     */
    public void testProjectNativePackagingSearchJar() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("maven/artifacts/native-default");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "native-default");
        
        Project p = openAndPrimeProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery("jar"));
        
        assertNotNull(ar);
        assertEquals("Single jar is produced", 1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertEquals("Just jar must be present", "jar", spec.getType());
    }


   /**
     * Checks that project with default native-image packaging will provide the excutable
     * @throws Exception 
     */
    public void testAllArtifactsWithNativePackaging() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("maven/artifacts/native-default");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "native-default");
        
        Project p = openAndPrimeProject(prjCopy);

        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(ProjectArtifactsQuery.Filter.TYPE_ALL));
        Collection<ArtifactSpec> arts = ar.getArtifacts();
        assertEquals("All query should produce BOTH jar and exe ", 2, arts.size());
            
        ArtifactSpec jar = null;
        ArtifactSpec exe = null;
        for (ArtifactSpec a : arts) {
            if ("exe".equals(a.getType())) {
                exe = a;
            } else if ("jar".equals(a.getType())) {
                jar = a;
            }
        }
        assertNotNull("Jar should be present", jar);
        assertNotNull("Exe should be present", jar);
        assertFalse("Exe should not contain the version",exe.getLocation().toString().contains("0.1"));
    }
    
    /**
     * Checks that non-Micronaut project does not provide 'micronaut-auto' profile.
     * @throws Exception 
     */
    public void testNoMicronautNodevModeConfiguration() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("maven/artifacts/simple");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simple");

        Project p = openAndPrimeProject(prjCopy);
        assertFalse(findMicronautProfile(p));
    }
    
    /**
     * Checks that Micronaut 3.x style projects (with io.micronaut.build: plugin) is recognized.
     */
    public void testMicronaut3DevModeConfiguration() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("maven/artifacts/native-optional");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "mn-simple");

        Project p = openAndPrimeProject(prjCopy);
        assertTrue(findMicronautProfile(p));
    }
    
    boolean findMicronautProfile(Project p) {
        ProjectConfigurationProvider<MavenConfiguration> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        assertNotNull(pcp);
        
        for (MavenConfiguration cfg : pcp.getConfigurations()) {
            if (cfg.getDisplayName().toLowerCase().contains("micronaut")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks that Micronaut 4.x style projects (with io.micronaut.maven: plugin) is recognized.
     */
    public void testMicronaut4DevModeConfiguration() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("maven/micronaut4/simple");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "mn4-simple");

        Project p = openAndPrimeProject(prjCopy);
        assertTrue(findMicronautProfile(p));
    }
}
