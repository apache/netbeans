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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.lsp.TextDocumentEdit;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.project.dependency.DependencyChange;
import org.netbeans.modules.project.dependency.DependencyChangeException;
import org.netbeans.modules.project.dependency.DependencyChangeRequest;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.Scope;
import org.netbeans.modules.project.dependency.Scopes;
import org.netbeans.modules.project.dependency.spi.ProjectDependencyModifier;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.Union2;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(service = ProjectDependencyModifier.class, projectType = NbMavenProject.TYPE)
public class MavenDependencyModifierImpl implements ProjectDependencyModifier {
    private final Project project;

    public MavenDependencyModifierImpl(Project project) {
        this.project = project;
    }
    
    static final Map<String, String> scope2Maven;
    static final Map<String, Scope> maven2Scope;
    
    static {
        scope2Maven = new HashMap<>();
        scope2Maven.put(Scopes.PROCESS.name(), "compile");
        scope2Maven.put(Scopes.COMPILE.name(), "compile");
        scope2Maven.put(Scopes.RUNTIME.name(), "runtime");
        scope2Maven.put(Scopes.TEST.name(), "test");
        scope2Maven.put(Scopes.TEST_COMPILE.name(), "test");
        scope2Maven.put(Scopes.TEST_RUNTIME.name(), "test");
        scope2Maven.put(Scopes.EXTERNAL.name(), "provided");
        
        maven2Scope = new HashMap<>();
        maven2Scope.put("compile", Scopes.COMPILE);
        maven2Scope.put("runtime", Scopes.RUNTIME);
        maven2Scope.put("test", Scopes.TEST);
        maven2Scope.put("runtime", Scopes.RUNTIME);
    }
    

    @NbBundle.Messages({
        "# {0} - pom file",
        "ERR_CannotModifyProject=Cannot modify POM {1}",
        "ERR_UnsupportedOperation=Operation {0} is unsupported."
    })
    @Override
    public Result computeChange(DependencyChangeRequest batch) throws DependencyChangeException {
        List<TextEdit> edits = null;

        RewriteContext rewrite = new RewriteContext(project);
        for (DependencyChange request : batch.getOperations()) {
        
            switch (request.getKind()) {
                case ADD:
                    DependencyAdder adder = new DependencyAdder(project, request, rewrite);
                    adder.execute();
                    break;
                default:
                    throw new ProjectOperationException(project, ProjectOperationException.State.UNSUPPORTED, Bundle.ERR_UnsupportedOperation(request.getKind()));
            }

        }
        edits = rewrite.createEdits();
        if (edits == null) {
            throw new ProjectOperationException(project, ProjectOperationException.State.UNSUPPORTED, Bundle.ERR_CannotModifyProject(rewrite.getPomFile()));
        }
        
        TextDocumentEdit e = new TextDocumentEdit(URLMapper.findURL(rewrite.getPomFile(), URLMapper.EXTERNAL).toString(), edits);
        WorkspaceEdit we = new WorkspaceEdit(Collections.singletonList(Union2.createFirst(e)));
        return new Result() {
            @Override
            public String getId() {
                return "maven-add-dependency";
            }

            @Override
            public boolean suppresses(Result check) {
                return false;
            }

            @Override
            public WorkspaceEdit getWorkspaceEdit() {
                return we;
            }
        };
    }
}
