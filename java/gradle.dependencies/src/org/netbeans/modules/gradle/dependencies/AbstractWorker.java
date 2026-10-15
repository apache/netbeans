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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.java.queries.GradleScope;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyChange;
import org.netbeans.modules.project.dependency.DependencyChangeException;

/**
 * Code hopefully common for Remove and Add workers.
 * 
 * @author sdedic
 */
public abstract class AbstractWorker {
    protected final Project project;
    protected DependencyChange request;
    protected final RewriteContext context;
    
    public AbstractWorker(Project project, RewriteContext context) {
        this.project = project;
        this.context = context;
    }

    /**
     * Dependencies that are accepted for adding. They do not exist in the 
     * file yet.
     */
    protected final List<Dependency> accepted = new ArrayList<>();
    
    /**
     * Accumulated text edits.
     */
    protected List<TextEdit>  textEdits = new ArrayList<>();

    /**
     * Line document opened for the project's buildscript.
     */
    protected LineDocument lineDoc;
    
    protected Map<Dependency, Dependency> offending = new HashMap<>();
    
    static StringBuilder indent(StringBuilder sb, int size) {
        for (int i = 0; i < size; i++) {
            sb.append(" ");
        }
        return sb;
    }
    
    protected abstract boolean checkDependencyConflicts(Dependency current, Dependency toAdd)  throws DependencyChangeException;
    
    protected abstract void throwDependencyConflicts() throws DependencyChangeException;
   
    protected abstract void generateDependencies()  throws DependencyChangeException;
    
    protected GradleScope getScope(Dependency d) throws DependencyChangeException {
        GradleScope gs = context.getScopes().toGradleScope(d.getScope().name());
        if (gs == null) {
            throwUnknownScope(d);
        }
        return gs;
    }

    protected void throwUnknownScope(Dependency d) throws DependencyChangeException {
        throw new DependencyChangeException(request, d, DependencyChangeException.Reason.MALFORMED);
    }
    
    protected void recordConflict(Dependency requested, Dependency existing) {
        if (!request.getOptions().contains(DependencyChange.Options.skipConflicts)) {
            offending.putIfAbsent(requested, existing);
        }
    }
    
    public void processRequest(DependencyChange change)  throws DependencyChangeException {
        this.request = change;
        for (Dependency d : request.getDependencies()) {
            boolean toAccept = true;
            for (Dependency c : context.getCurrentDependencies().getRoot().getChildren()) {
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
        this.request = null;
    }
    
    public void execute() throws DependencyChangeException {
        lineDoc = context.openDocument();
        generateDependencies();
        context.addTextEdits(textEdits);
    }
}
