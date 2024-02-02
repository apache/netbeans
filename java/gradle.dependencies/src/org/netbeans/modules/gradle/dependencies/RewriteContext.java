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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lsp.TextDocumentEdit;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.queries.GradleScopes;
import org.netbeans.modules.gradle.java.queries.GradleScopesBuilder;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.Union2;

/**
 * Accumulates changes from the dependency change.
 * @author sdedic
 */
public class RewriteContext {
    private final Project project;
    private final DependencyResult current;
    private LineDocument lineDoc;
    private GradleScopes scopes;
    private List<TextEdit> edits = new ArrayList<>();
    private FileObject buildFile;

    public RewriteContext(Project project, DependencyResult current) {
        this.project = project;
        this.current = current;
        
        scopes = new GradleScopesBuilder(project).build();
        NbGradleProject nbgp = NbGradleProject.get(project);
        File f = nbgp.getGradleFiles().getBuildScript();
        buildFile = FileUtil.toFileObject(f);
    }

    public GradleScopes getScopes() {
        return scopes;
    }
    
    @NbBundle.Messages({
        "# {0} - project buildscript name",
        "ERR_ProjectFileNotFound=Cannot locate buildscript {0}"
    })
    public LineDocument openDocument() throws ProjectOperationException {
        if (lineDoc != null) {
            return lineDoc;
        }
        NbGradleProject nbgp = NbGradleProject.get(project);
        File f = nbgp.getGradleFiles().getBuildScript();
        buildFile = FileUtil.toFileObject(f);
        if (buildFile == null) {
            throw new ProjectOperationException(project, ProjectOperationException.State.UNSUPPORTED, 
                Bundle.ERR_ProjectFileNotFound(f.toString()),
                new FileNotFoundException(f.toString()));
        }
        EditorCookie cake = buildFile.getLookup().lookup(EditorCookie.class);
        try {
            lineDoc = LineDocumentUtils.asRequired(cake.openDocument(), LineDocument.class);
            return lineDoc;
        } catch (IOException ex) {
            throw new ProjectOperationException(project, ProjectOperationException.State.ERROR, 
                Bundle.ERR_ProjectFileNotFound(f.toString()),
                ex);
        }
    }
    
    public DependencyResult getCurrentDependencies() {
        return current;
    }
    
    public void addTextEdits(List<TextEdit> add) {
        edits.addAll(add);
    }
    
    public WorkspaceEdit createWorkspaceEdit() {
        FileObject fo;
        return new WorkspaceEdit(
            edits.isEmpty() ? 
                    Collections.emptyList() : 
                    Arrays.asList(
                    Union2.createFirst(new TextDocumentEdit(URLMapper.findURL(buildFile, URLMapper.EXTERNAL).toString(), edits)
                    )
            )
        );
    }
    
}
