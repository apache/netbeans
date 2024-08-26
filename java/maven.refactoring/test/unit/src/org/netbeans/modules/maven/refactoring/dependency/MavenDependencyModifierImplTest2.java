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
import org.apache.maven.project.MavenProject;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyChange;
import org.netbeans.modules.project.dependency.DependencyChangeException;
import org.netbeans.modules.project.dependency.DependencyChangeRequest;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectModificationResult;
import org.netbeans.modules.project.dependency.Scopes;

/**
 *
 * @author sdedic
 */
public class MavenDependencyModifierImplTest2 extends MavenDependencyModifierImplTestBase {

    public MavenDependencyModifierImplTest2(String name) {
        super(name);
    }
    
    /**
     * Must run as a module suite, since Maven Indexer includes Lucene 9.x, while Parsing API 
     * transitively depends on Lucene 3.x. Module system isolates the incompatible libraries from each other.
     * Bad day.
     */
    public static junit.framework.Test suite() {
        return NbModuleSuite.createConfiguration(MavenDependencyModifierImplTest2.class).
            gui(false).
            enableModules("org.netbeans.modules.maven.refactoring").
            honorAutoloadEager(true).
            suite();
    }

    public void testAddAnnotationProcessor() throws Exception {
        makeProject("simpleProject", null);
        ArtifactSpec art = ArtifactSpec.make("io.micronaut.data", "micronaut-data-processor");
        Dependency toAdd = Dependency.make(art, Scopes.PROCESS);
        DependencyChange change = DependencyChange.builder(DependencyChange.Kind.ADD).
                dependency(toAdd).
                create();
        
        ProjectModificationResult mod = ProjectDependencies.modifyDependencies(project, new DependencyChangeRequest(Collections.singletonList(change)));
        mod.commit();
        
        NbMavenProjectImpl impl = project.getLookup().lookup(NbMavenProjectImpl.class);
        impl.fireProjectReload().waitFinished();
        NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
        MavenProject p = mp.getMavenProject();
    }
    
    public void testAddAnnotationProcessorToExistingConfig() throws Exception {
        makeProject("simpleProject", "pom-with-processor.xml");
        ArtifactSpec art = ArtifactSpec.make("io.micronaut.data", "micronaut-data-processor", "4.50");
        Dependency toAdd = Dependency.make(art, Scopes.PROCESS);
        DependencyChange change = DependencyChange.builder(DependencyChange.Kind.ADD).
                dependency(toAdd).
                create();
        
        ProjectModificationResult mod = ProjectDependencies.modifyDependencies(project, new DependencyChangeRequest(Collections.singletonList(change)));
        mod.commit();
        
        NbMavenProjectImpl impl = project.getLookup().lookup(NbMavenProjectImpl.class);
        impl.fireProjectReload().waitFinished();
        NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
        MavenProject p = mp.getMavenProject();
        DependencyResult res = ProjectDependencies.findDependencies(project, ProjectDependencies.newQuery(Scopes.PROCESS));
        
        Dependency dep = res.getRoot().getChildren().stream().filter(d -> d.getArtifact().getArtifactId().equals("micronaut-data-processor")).findAny().orElse(null);
        assertNotNull(dep);
    }

    public void testAddAnnotationProcessorAlreadyExists() throws Exception {
        makeProject("simpleProject", "pom-with-processor.xml");
        ArtifactSpec art = ArtifactSpec.make("nbtest.grp", "test-processor");
        Dependency toAdd = Dependency.make(art, Scopes.PROCESS);
        DependencyChange change = DependencyChange.builder(DependencyChange.Kind.ADD).
                dependency(toAdd).
                create();
        
        try {
            ProjectModificationResult mod = ProjectDependencies.modifyDependencies(project, new DependencyChangeRequest(Collections.singletonList(change)));
            fail("Should have failed");
        } catch (DependencyChangeException ex) {
            assertSame(DependencyChangeException.Reason.CONFLICT, ex.getReason());
            Dependency conflict = ex.getConflictSource(toAdd);
            assertNotNull(conflict);
            assertEquals("12.6", conflict.getArtifact().getVersionSpec());
        }
    }
}
