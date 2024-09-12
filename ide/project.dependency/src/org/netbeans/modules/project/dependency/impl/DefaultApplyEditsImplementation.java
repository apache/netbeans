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
package org.netbeans.modules.project.dependency.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.lsp.ResourceModificationException;
import org.netbeans.api.lsp.ResourceOperation;
import org.netbeans.api.lsp.TextDocumentEdit;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.spi.lsp.ApplyEditsImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Union2;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = ApplyEditsImplementation.class)
public class DefaultApplyEditsImplementation implements ApplyEditsImplementation {

    @Override
    public CompletableFuture<List<String>> applyChanges(List<WorkspaceEdit> edits, boolean saveResources) {
        Worker wk = new Worker(edits, saveResources);
        // PENDING: shouldn't I make this somehow a Filesystem Atomic action ?
        try {
            wk.execute();
            return CompletableFuture.completedFuture(wk.getProcessedResources());
        } catch (ResourceModificationException ex) {
            return CompletableFuture.failedFuture(ex);
        }
   }
    
    static class Worker {
        final boolean doSave;
        final List<WorkspaceEdit> edits;
        final List<WorkspaceEdit> completed = new ArrayList<>();
        final Set<String> saved = new LinkedHashSet<>();
        final Set<String> processed = new LinkedHashSet<>();
        
        WorkspaceEdit currentEdit;
        int currentIndex = -1;
        
        public Worker(List<WorkspaceEdit> edits, boolean doSave) {
            this.edits = edits;
            this.doSave = doSave;
        }
        
        public List<String> getProcessedResources() {
            return new ArrayList<>(processed);
        }
        
        void applyChangeToFile(TextDocumentEdit tde) throws ResourceModificationException {
            TextDocumentEditProcessor proc = new TextDocumentEditProcessor(tde).setSaveAfterEdit(doSave);
            try {
                proc.execute();
            } catch (IOException ex) {
                throw new ResourceModificationException(this.completed, currentEdit, currentIndex, proc.getFailedOperationIndex(), this.saved, ex.getMessage(), ex);
            }
        }
        
        void applyResourceOperation(ResourceOperation ro) throws IOException {
            if (ro instanceof ResourceOperation.CreateFile) {
                applyCreateOperation((ResourceOperation.CreateFile)ro);
            } else {
                throw new UnsupportedOperationException(ro.getClass().getName());
            }
        }
        
        void applyCreateOperation(ResourceOperation.CreateFile cf) throws IOException {
            Path filePath;
            try {
                URI u = URI.create(cf.getNewFile());
                filePath = Paths.get(u);
            } catch (FileSystemNotFoundException ex) {
                IOException e = new IOException("Invalid resource specification");
                e.initCause(ex);
                throw e;
            } catch (IllegalArgumentException ex) {
                String filename = cf.getNewFile();
                try {
                    filePath = Paths.get(filename);
                } catch (IllegalArgumentException ex2) {
                    IOException e = new IOException("Invalid resource specification");
                    e.initCause(ex2);
                    throw e;
                }
            }
             
            if (Files.exists(filePath)) {
                throw new FileAlreadyExistsException(filePath.toString());
            }
            Path parent = filePath;
            do {
                parent = parent.getParent();
            } while (parent != null && !Files.exists(parent));
            if (parent == null) {
                throw new IOException("Cannot create file with no existing parent: " + filePath);
            } 
            FileObject parentFile = URLMapper.findFileObject(parent.toUri().toURL());
            String relativePath = parent.relativize(filePath).toString();
            FileUtil.createData(parentFile, relativePath);
            processed.add(cf.getNewFile());
        }
        
        public void execute() throws ResourceModificationException {
            for (WorkspaceEdit e : edits) {
                currentEdit = e;
                currentIndex = 0;
                
                for (Union2<TextDocumentEdit, ResourceOperation> ch : currentEdit.getDocumentChanges()) {
                    if (ch.hasFirst()) {
                        TextDocumentEdit te = ch.first();
                        applyChangeToFile(te);
                        if (doSave) {
                            saved.add(te.getDocument());
                        }
                        processed.add(te.getDocument());
                    } else if (ch.hasSecond()) {
                        try {
                            applyResourceOperation(ch.second());
                        } catch (IOException ex) {
                            throw new ResourceModificationException(this.completed, currentEdit, currentIndex, ResourceModificationException.UNSPECIFIED_EDIT, this.saved, ex.getMessage(), ex);
                        }
                    }
                    currentIndex++;
                }
            }
        }
    }
}
