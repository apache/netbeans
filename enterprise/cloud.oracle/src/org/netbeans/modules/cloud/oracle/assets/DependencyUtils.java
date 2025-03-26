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
package org.netbeans.modules.cloud.oracle.assets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyChange;
import org.netbeans.modules.project.dependency.DependencyChange.Options;
import org.netbeans.modules.project.dependency.DependencyChangeException;
import org.netbeans.modules.project.dependency.DependencyChangeRequest;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.Scopes;
import org.netbeans.modules.refactoring.spi.ModificationResult;

/**
 * Utility class with methods for modifying project dependencies.
 * 
 * @author Jan Horvath
 */
public class DependencyUtils {

    public static void addDependency(Project project, String[] pairs) {
        Project projectToModify = getProjectToModify(project, "oci");
        
        if (projectToModify != null) {
            List<Dependency> dependencies = new ArrayList<> ();
            for (int i = 0; i < pairs.length - 1; i += 2) {
                ArtifactSpec spec = ArtifactSpec.make(pairs[i], pairs[i + 1]);
                dependencies.add(Dependency.make(spec, Scopes.COMPILE));
            }
            DependencyChange change = DependencyChange.add(dependencies, Options.skipConflicts);
            try {
                ModificationResult mod = ProjectDependencies.modifyDependencies(projectToModify, change);
                mod.commit();
            } catch (IOException | DependencyChangeException | ProjectOperationException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
    
    public static void addAnnotationProcessor(Project project, String groupId, String artifactId) {
        Project projectToModify = getProjectToModify(project, "lib");
        
        if (projectToModify != null) {
            ArtifactSpec spec = ArtifactSpec.make(groupId, artifactId);
            Dependency dep = Dependency.make(spec, Scopes.PROCESS);
            DependencyChange change = DependencyChange.builder(DependencyChange.Kind.ADD)
                    .dependency(dep)
                    .option(Options.skipConflicts)
                    .create();

            try {
                ModificationResult mod = ProjectDependencies
                        .modifyDependencies(projectToModify, new DependencyChangeRequest(Collections.singletonList(change)));
                mod.commit();
            } catch (IOException | DependencyChangeException | ProjectOperationException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
    
    private static Project getProjectToModify(Project project, String projectDirectory) {
        Project projectToModify = null;
        Set<Project> subProjects = ProjectUtils.getContainedProjects(project, false);
        for (Project subProject : subProjects) {
            if (projectDirectory.equals(subProject.getProjectDirectory().getName())) { //NOI18N
                projectToModify = subProject;
                break;
            }
        }
        if (projectToModify == null) {
            projectToModify = project;
        }
        
        return projectToModify;
    }
}
