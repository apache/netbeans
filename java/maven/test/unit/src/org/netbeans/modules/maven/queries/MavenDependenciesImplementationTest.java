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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.Scopes;
import org.netbeans.modules.project.dependency.SourceLocation;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOProvider;

/**
 *
 * @author sdedic
 */
public class MavenDependenciesImplementationTest extends NbTestCase {
    private FileObject d;
    private File repo;
    private FileObject repoFO;
    private FileObject dataFO;

    public MavenDependenciesImplementationTest(String name) {
        super(name);
    }
    
    // InstalledFilesLocator is needed so that Maven module finds maven's installation
    @org.openide.util.lookup.ServiceProvider(service=org.openide.modules.InstalledFileLocator.class, position = 1000)
    public static class InstalledFileLocator extends DummyInstalledFileLocator {
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
     * Primes the project including dependency fetch, waits for the operation to complete.
     * @throws Exception 
     */
    void primeProject(Project p) throws Exception {
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        if (ap == null) {
            throw new IllegalStateException("No action provider");
        }
        assertTrue(Arrays.asList(ap.getSupportedActions()).contains(ActionProvider.COMMAND_PRIME));
        
        CountDownLatch primeLatch = new CountDownLatch(1);
        ActionProgress prg = new ActionProgress() {
            @Override
            protected void started() {
            }

            @Override
            public void finished(boolean success) {
                primeLatch.countDown();
            }
        };
        ap.invokeAction(ActionProvider.COMMAND_PRIME, Lookups.fixed(prg));
        primeLatch.await(20, TimeUnit.SECONDS);
    }
    
    public void testCompileDependencies() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        assertNotNull(p);
        
        primeProject(p);
        
        DependencyResult dr = ProjectDependencies.findDependencies(p, ProjectDependencies.newQuery(Scopes.COMPILE));
        Dependency root = dr.getRoot();
        assertContents(printDependencyTree(root), getName());
    }
    
    public void testRuntimeDependencies() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        assertNotNull(p);
        
        primeProject(p);
        
        DependencyResult dr = ProjectDependencies.findDependencies(p, ProjectDependencies.newQuery(Scopes.RUNTIME));
        Dependency root = dr.getRoot();
        assertContents(printDependencyTree(root), getName());
    }
    
    public void testDirectDependencySource() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        assertNotNull(p);
 
        primeProject(p);

        DependencyResult dr = ProjectDependencies.findDependencies(p, ProjectDependencies.newQuery(Scopes.RUNTIME));
        
        Dependency dep = dr.getRoot().getChildren().stream().filter(d -> d.getArtifact().getArtifactId().equals("test-lib")).findAny().get();
        SourceLocation srcLoc = dr.getDeclarationRange(dep);
        assertNotNull(srcLoc);
        assertFalse(srcLoc.isEmpty());
        assertTrue(srcLoc.hasPosition());
        
        Path pomPath = p.getLookup().lookup(NbMavenProject.class).getMavenProject().getFile().toPath();
        assertEquals(pomPath.toFile(), FileUtil.toFile(srcLoc.getFile()));

        String s = String.join("\n", Files.readAllLines(pomPath));
        assertEquals("<dependency", s.substring(srcLoc.getStartOffset(), srcLoc.getStartOffset() + 11));
        assertEquals("</dependency>", s.substring(srcLoc.getEndOffset() - 13, srcLoc.getEndOffset()));
    }
    
    public void testNestedDependencySource() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        assertNotNull(p);

        primeProject(p);

        DependencyResult dr = ProjectDependencies.findDependencies(p, ProjectDependencies.newQuery(Scopes.RUNTIME));
        
        Dependency libDep = dr.getRoot().getChildren().stream().filter(d -> d.getArtifact().getArtifactId().equals("test-lib")).findAny().get();
        Dependency annoDep = libDep.getChildren().stream().filter(d -> d.getArtifact().getArtifactId().equals("javax.annotation-api")).findAny().get();

        SourceLocation srcLoc = dr.getDeclarationRange(annoDep);

        Path pomPath = p.getLookup().lookup(NbMavenProject.class).getMavenProject().getFile().toPath();
        assertEquals(pomPath.toFile(), FileUtil.toFile(srcLoc.getFile()));

        String s = String.join("\n", Files.readAllLines(pomPath));
        assertEquals("<dependency", s.substring(srcLoc.getStartOffset(), srcLoc.getStartOffset() + 11));
        assertEquals("</dependency>", s.substring(srcLoc.getEndOffset() - 13, srcLoc.getEndOffset()));
    }
    
    void assertContents(String contents, String golden) throws IOException {
        File f = new File(getDataDir(), "projects/dependencies/golden/" + golden);
        Path res = Files.write(getWorkDir().toPath().resolve(getName() + ".output"), 
                Arrays.asList(contents.split("\n")),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        assertFile(res.toFile(), f, new File(getWorkDir(), getName() + ".diff"));
    }
    
    static String printDependencyTree(Dependency root) {
        StringBuilder sb = new StringBuilder();
        printDependencyTree(root, -1, sb);
        return sb.toString();
    }

    static void printDependencyTree(Dependency from, int levels, StringBuilder sb) {
        if (levels >= 0) {
            sb.append(" ");
        }
        for (int i = 0; i < levels; i++) {
            sb.append("|    ");
        }
        if (levels < 0) {
            sb.append("[ ] ");
        } else {
            sb.append("+-- [ ] ");
        }
        sb.append(from.getArtifact());
        if (from.getScope() != null) {
            sb.append(" / "); sb.append(from.getScope());
        }
        sb.append("\n");
        int index = 0;
        for (Dependency c : (List<Dependency>)from.getChildren()) {
            printDependencyTree(c, levels + 1, sb);
            index++;
        }
    }
}
