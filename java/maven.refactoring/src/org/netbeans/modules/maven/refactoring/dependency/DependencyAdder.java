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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyChange;
import org.netbeans.modules.project.dependency.DependencyChangeException;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.Scopes;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class DependencyAdder {
    private final Project project;
    private final DependencyChange request;
    private final RewriteContext rewrite;
    
    public DependencyAdder(Project project, DependencyChange request, RewriteContext rewrite) {
        this.project = project;
        this.request = request;
        this.rewrite = rewrite;
    }
    
    private List<Dependency> accepted = new ArrayList<>();
    protected Map<Dependency, Dependency> offending = new HashMap<>();
    
    protected void throwDependencyConflicts() throws DependencyChangeException {
        if (!offending.isEmpty()) {
            throw new DependencyChangeException(request, DependencyChangeException.Reason.CONFLICT, offending);
        }
    }

    protected void throwUnknownScope(Dependency d) throws DependencyChangeException {
        throw new DependencyChangeException(request, d, DependencyChangeException.Reason.MALFORMED);
    }
    
    protected void recordConflict(Dependency requested, Dependency existing) {
        if (!request.getOptions().contains(DependencyChange.Options.skipConflicts)) {
            offending.putIfAbsent(requested, existing);
        }
    }
    
    protected boolean checkDependencyConflicts(Dependency existing, Dependency d) throws DependencyChangeException {
        ArtifactSpec existingA = existing.getArtifact();
        ArtifactSpec toAdd = d.getArtifact();
        
        if (!(existingA.getGroupId().equals(toAdd.getGroupId()) &&
            existingA.getArtifactId().equals(toAdd.getArtifactId()))) {
            // different artifacts -> no conflicts
            return true;
        }
        String existingC = existingA.getClassifier();
        if (existingA != null) {
            if (!Objects.equals(existingC, toAdd.getClassifier())) {
                return true;
            }
        }
        String mavenScope = rewrite.mavenScope(d);
        String existingScope = rewrite.mavenScope(existing);
        if (!mavenScope.equals(existingScope)) {
            // second chance -- the specified scope could be a meta-scope that maps to Gradle configuration
            return true;
        }
        recordConflict(d, existing);
        return false;
    }
    
    DependencyResult current;
    
    @NbBundle.Messages({
        "ERR_AddingDependency=Error adding dependency"
    })
    public void execute() throws DependencyChangeException {
        current = ProjectDependencies.findDependencies(project, ProjectDependencies.newQuery(Scopes.DECLARED));
        
        try {

            POMModel mutableModel = rewrite.getWriteModel();
            for (Dependency d : request.getDependencies()) {
                boolean toAccept = true;
                for (Dependency c : current.getRoot().getChildren()) {
                    if (!checkDependencyConflicts(c, d)) {
                        toAccept = false;
                        break;
                    }
                }
                if (toAccept) {
                    accepted.add(d);
                }
            }

            throwDependencyConflicts();

            mutableModel.sync();            
            mutableModel.startTransaction();
            boolean ok = false;
            try {
                for (Dependency d : accepted) {
                    ArtifactSpec a = d.getArtifact();
                    org.netbeans.modules.maven.model.pom.Dependency mavenDep = mutableModel.getFactory().createDependency();
                    mavenDep.setGroupId(a.getGroupId());
                    mavenDep.setArtifactId(a.getArtifactId());
                    if (a.getVersionSpec() != null && !a.getVersionSpec().isEmpty()) {
                        mavenDep.setVersion(a.getVersionSpec());
                    }
                    if (a.getClassifier() != null) {
                        mavenDep.setClassifier(a.getClassifier());
                    }
                    String scope = rewrite.mavenScope(d);
                    if (!"compile".equals(scope)) {
                        mavenDep.setScope(scope);
                    }
                    mutableModel.getProject().addDependency(mavenDep);
                }
                mutableModel.endTransaction();
                ok = true;
            } finally {
                if (!ok) {
                    mutableModel.rollbackTransaction();
                }
            }
        } catch (IOException ex) {
            throw new ProjectOperationException(project, ProjectOperationException.State.UNSUPPORTED, Bundle.ERR_CannotModifyProject(rewrite.getPomFile()));
        } catch (IllegalArgumentException | IllegalStateException | UnsupportedOperationException ex) {
            throw new ProjectOperationException(project, ProjectOperationException.State.ERROR, Bundle.ERR_AddingDependency(), ex);
        }
    }
}
