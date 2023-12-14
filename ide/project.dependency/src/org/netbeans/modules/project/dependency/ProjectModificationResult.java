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
package org.netbeans.modules.project.dependency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.actions.Savable;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.modules.project.dependency.impl.ProjectModificationResultImpl;
import org.netbeans.modules.project.dependency.impl.WorkspaceEditAdapter;
import org.netbeans.modules.refactoring.spi.ModificationResult;
import org.openide.filesystems.FileObject;

/**
 * Describes a change to the project files, that reflects a dependency-related operation
 * 
 * @since 1.7
 * @author sdedic
 */
public final class ProjectModificationResult implements ModificationResult {
    private ProjectModificationResultImpl impl;
    
    ProjectModificationResult(ProjectModificationResultImpl impl) {
        this.impl = impl;
    }
    
    /**
     * @return files that should be save in order so that build system can recognize changes.
     */
    public Collection<FileObject> getFilesToSave() {
        return impl.getFilesToSave();
    }
    
    /**
     * Describes the details of the workspace edit.
     * @return details of the edit
     */
    public WorkspaceEdit getWorkspaceEdit() {
        return impl.getWorkspaceEdit();
    }

    /**
     * Presents the text of the given source after applied changes.
     * @param file the file
     * @return changed text
     * @throws IOException if the original file could not be read or opened
     * @throws IllegalArgumentException if the `file' is not in the modified set.
     */
    @Override
    public String getResultingSource(FileObject file) throws IOException, IllegalArgumentException {
        return wrapEdits().getResultingSource(file);
    }
    
    private WorkspaceEditAdapter wrapEdits;
    
    WorkspaceEditAdapter wrapEdits() {
        if (wrapEdits == null) {
            wrapEdits = new WorkspaceEditAdapter(impl);
        }
        return wrapEdits;
    }

    @Override
    public Collection<? extends FileObject> getModifiedFileObjects() {
        Set<FileObject> modified = new LinkedHashSet<>();
        modified.addAll(wrapEdits().getModifiedFileObjects());
        ModificationResult custom = impl.getCustomEdit();
        if (custom != null){
            modified.addAll(custom.getModifiedFileObjects());
        }
        return modified;
    }

    @Override
    public Collection<? extends File> getNewFiles() {
        Set<File> r = new LinkedHashSet<>();
        r.addAll(wrapEdits().getNewFiles());
        ModificationResult custom = impl.getCustomEdit();
        if (custom != null){
            r.addAll(custom.getNewFiles());
        }
        return r;
    }

    @Override
    public void commit() throws IOException {
        WorkspaceEditAdapter r = wrapEdits();
        r.commit();
        if (impl.getCustomEdit() != null) {
            impl.getCustomEdit().commit();
        }
        // save the modified files, so project system will pick things up.
        // PENDING: make optional, at the discretion of ProjectDependencyModifier.
        for (FileObject f : r.getFilesToSave()) {
            Savable s = f.getLookup().lookup(Savable.class);
            if (s != null) {
                s.save();
            }
        }
    }
}
