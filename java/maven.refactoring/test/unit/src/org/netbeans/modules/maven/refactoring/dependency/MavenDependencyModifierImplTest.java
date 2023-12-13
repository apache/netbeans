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
package org.netbeans.modules.maven.refactoring.dependency;

import java.util.Collections;
import java.util.Optional;
import static junit.framework.TestCase.assertNotNull;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyChange;
import org.netbeans.modules.project.dependency.DependencyChangeRequest;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectModificationResult;
import org.netbeans.modules.project.dependency.Scopes;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class MavenDependencyModifierImplTest extends NbTestCase {

    public MavenDependencyModifierImplTest(String name) {
        super(name);
    }
    
    private FileObject projectDir;
    private Project project;

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        clearWorkDir();
    }
    
    
    
    private Project makeProject(String subdir, String alternateBuildscript) throws Exception {
        FileObject src = FileUtil.toFileObject(getDataDir()).getFileObject(subdir);
        projectDir = FileUtil.copyFile(src, FileUtil.toFileObject(getWorkDir()), src.getNameExt());
        if (alternateBuildscript != null) {
            FileObject fo = src.getFileObject(alternateBuildscript);
            FileObject target = projectDir.getFileObject("build.gradle");
            if (target != null) {
                target.delete();
            }
            fo.copy(projectDir, "build", "gradle");
        }
        
        Project p = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(p);
        
        OpenProjects.getDefault().open(new Project[] { p }, true);
        OpenProjects.getDefault().openProjects().get();

        this.project = p;
        return p;
    }

    public void testSimpleAdd() throws Exception {
        makeProject("simpleProject", null);
        
        DependencyResult res = ProjectDependencies.findDependencies(project, ProjectDependencies.newQuery(Scopes.DECLARED));
        assertNotNull(res);

        ArtifactSpec art = ArtifactSpec.make("io.micronaut", "micronaut-router", "4.1.12");
        Dependency toAdd = Dependency.make(art, Scopes.COMPILE);
        DependencyChange change = DependencyChange.builder(DependencyChange.Kind.ADD).
                dependency(toAdd).
                create();
        ProjectModificationResult mod = ProjectDependencies.modifyDependencies(project, new DependencyChangeRequest(Collections.singletonList(change)));
        mod.commit();
        
        NbMavenProjectImpl impl = project.getLookup().lookup(NbMavenProjectImpl.class);
        impl.fireProjectReload().waitFinished();
        
        NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
        MavenProject p = mp.getMavenProject();
        Optional<org.apache.maven.model.Dependency> opt = p.getDependencies().stream().filter(d -> "micronaut-router".equals(d.getArtifactId())).findAny();
        assertTrue(opt.isPresent());
        
        org.apache.maven.model.Dependency d = opt.get();
        assertEquals("compile", d.getScope());
    }
    
    public void testAddRuntime() throws Exception {
        makeProject("simpleProject", null);
        ArtifactSpec art = ArtifactSpec.make("io.micronaut", "micronaut-router", "4.1.12");
        Dependency toAdd = Dependency.make(art, Scopes.RUNTIME);
        DependencyChange change = DependencyChange.builder(DependencyChange.Kind.ADD).
                dependency(toAdd).
                create();
        ProjectModificationResult mod = ProjectDependencies.modifyDependencies(project, new DependencyChangeRequest(Collections.singletonList(change)));
        mod.commit();
        
        NbMavenProjectImpl impl = project.getLookup().lookup(NbMavenProjectImpl.class);
        impl.fireProjectReload().waitFinished();
        
        NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
        MavenProject p = mp.getMavenProject();
        Optional<org.apache.maven.model.Dependency> opt = p.getDependencies().stream().filter(d -> "micronaut-router".equals(d.getArtifactId())).findAny();
        org.apache.maven.model.Dependency d = opt.get();
        assertEquals("runtime", d.getScope());
    }
    
    public void testAddMultiple() throws Exception {
        makeProject("simpleProject", null);
        ArtifactSpec art = ArtifactSpec.make("io.micronaut", "micronaut-router", "4.1.12");
        ArtifactSpec art2 = ArtifactSpec.make("io.micronaut", "micronaut-websocket", "4.1.12");
        Dependency toAdd = Dependency.make(art, Scopes.RUNTIME);
        Dependency toAdd2 = Dependency.make(art2, Scopes.RUNTIME);
        DependencyChange change = DependencyChange.builder(DependencyChange.Kind.ADD).
                dependency(toAdd, toAdd2).
                create();

        ProjectModificationResult mod = ProjectDependencies.modifyDependencies(project, new DependencyChangeRequest(Collections.singletonList(change)));
        mod.commit();
        
        NbMavenProjectImpl impl = project.getLookup().lookup(NbMavenProjectImpl.class);
        impl.fireProjectReload().waitFinished();
        
        NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
        MavenProject p = mp.getMavenProject();
        Optional<org.apache.maven.model.Dependency> opt = p.getDependencies().stream().filter(d -> "micronaut-router".equals(d.getArtifactId())).findAny();
        Optional<org.apache.maven.model.Dependency> opt2 = p.getDependencies().stream().filter(d -> "micronaut-websocket".equals(d.getArtifactId())).findAny();
        org.apache.maven.model.Dependency d = opt.get();
        org.apache.maven.model.Dependency d2 = opt2.get();
        assertEquals("runtime", d.getScope());
        assertEquals("runtime", d2.getScope());
    }
}
