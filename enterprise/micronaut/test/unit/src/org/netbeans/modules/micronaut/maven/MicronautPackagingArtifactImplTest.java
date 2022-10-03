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
import java.util.Collection;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
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
public class MicronautPackagingArtifactImplTest extends NbTestCase {

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
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    /**
     * Checks that native artifact provider does not obscure normal project's query
     * @throws Exception 
     */
    public void testProjectArtifactWithNormalQuery() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        
        FileObject testApp = dataFO.getFileObject("maven/artifacts/simple");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simple");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
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
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
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
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
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
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
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
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
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
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
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
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
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
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);

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
}
