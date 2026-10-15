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
package org.netbeans.modules.gradle.dependencies;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.project.dependency.DependencyChange;
import org.netbeans.modules.project.dependency.DependencyChangeException;
import org.netbeans.modules.project.dependency.DependencyChangeRequest;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.Scopes;
import org.netbeans.modules.project.dependency.spi.ProjectDependencyModifier;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(service = ProjectDependencyModifier.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class DependencyModifierImpl implements ProjectDependencyModifier {
    private final Project   project;

    public DependencyModifierImpl(Project project) {
        this.project = project;
    }
    
    @NbBundle.Messages({
        "ERR_ProjectFilesOutOfSync=Some project files are unsaved."
    })
    void assertProjectMetadataCorrect() throws DependencyChangeException {
        NbGradleProject gp = NbGradleProject.get(project);
        Set<FileObject> files = new LinkedHashSet<>();
        for (File f : gp.getGradleFiles().getProjectFiles()) {
            FileObject fo = FileUtil.toFileObject(f);
            if (fo == null) {
                continue;
            }
            EditorCookie cake = fo.getLookup().lookup(EditorCookie.class);
            if (cake != null && cake.isModified()) {
                files.add(fo);
            }
        }
        if (!files.isEmpty()) {
            throw new ProjectOperationException(project, ProjectOperationException.State.OUT_OF_SYNC, Bundle.ERR_ProjectFilesOutOfSync(), files);
        }
    }

    @Override
    public Result computeChange(DependencyChangeRequest request) throws DependencyChangeException {
        DependencyResult r = ProjectDependencies.findDependencies(project, ProjectDependencies.newQuery(Scopes.DECLARED));
        if (r == null) {
            return null;
        }
        assertProjectMetadataCorrect();
        RewriteContext context = new RewriteContext(project, r);
        DependencyAdder adder = new DependencyAdder(project, context);
        
        for (DependencyChange change : request.getOperations()) {
            switch (change.getKind()) {
                case ADD:
                    adder.processRequest(change);
                    break;
                case REMOVE:
                default:
                    throw new DependencyChangeException(change, null, DependencyChangeException.Reason.MALFORMED);
            }
        }
        
        adder.execute();
        WorkspaceEdit e = context.createWorkspaceEdit();
        
        return new Result() {
            @Override
            public String getId() {
                return "project-dependency-add";
            }

            @Override
            public boolean suppresses(Result check) {
                return false;
            }

            @Override
            public WorkspaceEdit getWorkspaceEdit() {
                return e;
            }
        };
    }
}
