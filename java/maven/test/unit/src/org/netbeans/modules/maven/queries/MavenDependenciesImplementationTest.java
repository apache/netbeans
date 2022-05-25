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
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

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
    
    
    
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
        System.setProperty("test.reload.sync", "true");
        repo = EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile();
        repoFO = FileUtil.toFileObject(repo);
        dataFO = FileUtil.toFileObject(getDataDir());
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

    public void testCompileDependencies() throws Exception {
        FileUtil.toFileObject(getWorkDir()).refresh();
        installCompileResources();
        
        FileObject testApp = dataFO.getFileObject("projects/dependencies/src/simpleProject");
        FileObject prjCopy = FileUtil.copyFile(testApp, FileUtil.toFileObject(getWorkDir()), "simpleProject");
        
        Project p = ProjectManager.getDefault().findProject(prjCopy);
        assertNotNull(p);
        DependencyResult dr = ProjectDependencies.findDependencies(p, null, Scopes.COMPILE);
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
        DependencyResult dr = ProjectDependencies.findDependencies(p, null, Scopes.RUNTIME);
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
        DependencyResult dr = ProjectDependencies.findDependencies(p, null, Scopes.RUNTIME);
        
        Dependency dep = dr.getRoot().getChildren().stream().filter(d -> d.getArtifact().getArtifactId().equals("test-lib")).findAny().get();
        SourceLocation srcLoc = dr.getDeclarationRange(dep);
        assertNotNull(srcLoc);
        assertFalse(srcLoc.isEmpty());
        assertTrue(srcLoc.hasPosition());
        
        Path pomPath = p.getLookup().lookup(NbMavenProject.class).getMavenProject().getFile().toPath();
        assertEquals(pomPath.toFile(), FileUtil.toFile(srcLoc.getFile()));

        String s = Files.readString(pomPath);
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
        DependencyResult dr = ProjectDependencies.findDependencies(p, null, Scopes.RUNTIME);
        
        Dependency libDep = dr.getRoot().getChildren().stream().filter(d -> d.getArtifact().getArtifactId().equals("test-lib")).findAny().get();
        Dependency annoDep = libDep.getChildren().stream().filter(d -> d.getArtifact().getArtifactId().equals("javax.annotation-api")).findAny().get();

        SourceLocation srcLoc = dr.getDeclarationRange(annoDep);

        Path pomPath = p.getLookup().lookup(NbMavenProject.class).getMavenProject().getFile().toPath();
        assertEquals(pomPath.toFile(), FileUtil.toFile(srcLoc.getFile()));

        String s = Files.readString(pomPath);
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
