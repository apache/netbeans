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
import java.net.URI;
import java.util.concurrent.Exchanger;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOProvider;

/**
 *
 * @author sdedic
 */
public class MavenArtifactsImplementationTest extends NbTestCase {
    private FileObject d;
    private File repo;
    private FileObject repoFO;
    private FileObject dataFO;

    public MavenArtifactsImplementationTest(String name) {
        super(name);
    }

    private static File getTestNBDestDir() {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        
        // This is needed, otherwose the core window's startup code will redirect
        // System.out/err to the IOProvider, and its Trivial implementation will redirect
        // it back to System.err - loop is formed. Initialize IOProvider first, it gets
        // the real System.err/out references.
        IOProvider p = IOProvider.getDefault();
        d = FileUtil.toFileObject(getWorkDir());
        System.setProperty("test.reload.sync", "true");
        repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        repoFO = FileUtil.toFileObject(repo);
        dataFO = FileUtil.toFileObject(getDataDir());
        
        // Configure the DummyFilesLocator with NB harness dir
        File destDirF = getTestNBDestDir();
        DummyInstalledFileLocator.registerDestDir(destDirF);
    }
    
    private void installCompileResources() throws Exception {
        FileUtil.copyFile(dataFO.getFileObject("projects/dependencies/repo"), repoFO, "nbtest");
    }

    @Override
    protected void tearDown() throws Exception {
        FileObject nbtest = repoFO.getFileObject("nbtest");
        if (nbtest != null && nbtest.isValid()) {
            nbtest.delete();
        }
    }
    
    /**
     * Checks that the project can report its output artifact even though
     * it was not built yet.
     */
    public void testProjectArtifactWithoutBuild() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertNotNull(spec);
        URI u = spec.getLocation();
        assertNotNull(u);
        assertNull(spec.getLocalFile());
    }
    
    public void testProjectArtifactSpecificType() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p,
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR)
        );
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertNotNull(spec);
        URI u = spec.getLocation();
        assertNotNull(u);
        assertNull(spec.getLocalFile());
    }

    /**
     * Checks that a compiled project will report its artifact including the  local file.
     * @throws Exception 
     */
    public void testCompileProjectArtifactWithFile() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        Exchanger<Boolean> status = new Exchanger<>();
        
        ap.invokeAction(ActionProvider.COMMAND_BUILD, Lookups.fixed(new ActionProgress() {
            @Override
            protected void started() {
            }

            @Override
            public void finished(boolean success) {
                try {
                    status.exchange(success);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }));
        
        Boolean s = status.exchange(true);
        assertTrue(s);
        // the project has been built
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, ProjectArtifactsQuery.newQuery(null));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
        ArtifactSpec spec = ar.getArtifacts().get(0);
        assertNotNull(spec.getLocation());
        assertNotNull(spec.getLocalFile());
        
        assertEquals(spec.getLocation(), URLMapper.findURL(spec.getLocalFile(), URLMapper.EXTERNAL).toURI());
    }
    
    
    public void testArtifactTypeNotProvided() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_EAR)
        );
        
        assertNotNull(ar);
        assertEquals(0, ar.getArtifacts().size());
    }

    public void testArtifactClassifierNotProvided() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, "tests", null)
        );
        
        assertNotNull(ar);
        assertEquals(0, ar.getArtifacts().size());
    }
    
    public void testArtifactForCompileAction() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, null, 
                ProjectActionContext.newBuilder(p)
                        .forProjectAction(ActionProvider.COMMAND_BUILD)
                        .context()
        ));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
    }

    public void testArtifactForRebuildAction() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, null, 
                ProjectActionContext.newBuilder(p)
                        .forProjectAction(ActionProvider.COMMAND_REBUILD)
                        .context()
        ));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
    }
    
    public void testArtifactForRunAction() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, null, 
                ProjectActionContext.newBuilder(p)
                        .forProjectAction(ActionProvider.COMMAND_RUN)
                        .context()
        ));
        
        assertNotNull(ar);
        assertEquals(1, ar.getArtifacts().size());
    }
    
    public void testArtifactForCleanAction() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        ProjectArtifactsQuery.ArtifactsResult ar = ProjectArtifactsQuery.findArtifacts(p, 
                ProjectArtifactsQuery.newQuery(NbMavenProject.TYPE_JAR, null, 
                ProjectActionContext.newBuilder(p)
                        .forProjectAction(ActionProvider.COMMAND_CLEAN)
                        .context()
        ));
        
        assertNotNull(ar);
        assertEquals(0, ar.getArtifacts().size());
    }

}
