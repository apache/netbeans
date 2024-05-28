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
package org.netbeans.modules.java.lsp.server.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.netbeans.api.lsp.ResourceModificationException;
import org.netbeans.api.lsp.ResourceOperation;
import org.netbeans.api.lsp.TextDocumentEdit;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.modules.java.lsp.server.LspServerUtils;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.SaveDocumentRequestParams;
import org.netbeans.spi.lsp.ApplyEditsImplementation;
import org.openide.util.Lookup;
import org.openide.util.Union2;

/**
 * LSP implementation of {@link ApplyEditsImplementation} interface. This class has to
 * be registered into default Lookup in the integration module.
 * 
 * @author sdedic
 */
public class AbstractApplyEditsImplementation implements ApplyEditsImplementation {
    private static final Logger LOG = Logger.getLogger(AbstractApplyEditsImplementation.class.getName());
    
    @Override
    public CompletableFuture<List<String>> applyChanges(List<WorkspaceEdit> edits, boolean saveResources) {
        NbCodeLanguageClient client = LspServerUtils.findLspClient(Lookup.getDefault());
        if (client == null) {
            return null;
        }
        Worker wk = new Worker(client, edits, saveResources);
        try {
            wk.execute();
            return CompletableFuture.completedFuture(wk.getProcessedResources());
        } catch (ResourceModificationException ex) {
            CompletableFuture failed = new CompletableFuture();
            failed.completeExceptionally(ex);
            return failed;
        }
    }
    
    final static class Worker {
        final NbCodeLanguageClient client;
        final boolean doSave;
        final List<WorkspaceEdit> edits;
        final List<WorkspaceEdit> completed = new ArrayList<>();
        final Set<String> saved = new LinkedHashSet<>();
        final Set<String> processed = new LinkedHashSet<>();
        final boolean transactionalFailure;
        
        boolean failed;
        WorkspaceEdit currentEdit;
        
        public Worker(NbCodeLanguageClient client, List<WorkspaceEdit> edits, boolean doSave) {
            this.client = client;
            this.edits = edits;
            this.doSave = doSave;

            WorkspaceEditCapabilities caps = client.getNbCodeCapabilities().getClientCapabilities().getWorkspace().getWorkspaceEdit();
            if (caps != null) {
                String failures = caps.getFailureHandling();
                switch (failures) {
                    case "transactional": // NOI18N
                    case "undo":          // NOI18N
                        transactionalFailure = true;
                        break;
                    case "abort":         // NOI18N
                    case "textOnlyTransactional":
                    default:              // NOI18N
                        transactionalFailure = false;
                        break;
                }
            } else {
                transactionalFailure = false;
            }
        }
        
        public List<String> getProcessedResources() {
            return new ArrayList<>(processed);
        }
        
        CompletableFuture<Void> handleClientResponse(WorkspaceEdit edit, ApplyWorkspaceEditResponse response) {
            int index = 0;
            int limit = response.getFailedChange() == null ? (response.isApplied() ? edit.getDocumentChanges().size() : 0) : response.getFailedChange();
            for (Union2<TextDocumentEdit, ResourceOperation> item : edit.getDocumentChanges()) {
                if (index >= limit) {
                    break;
                }
                if (item.hasFirst()) {
                    processed.add(item.first().getDocument());
                } else if (item.second() instanceof ResourceOperation.CreateFile) {
                    processed.add(((ResourceOperation.CreateFile)item.second()).getNewFile());
                }
            }
            if (response.isApplied()) {
                return CompletableFuture.completedFuture(null);
            }
            failed = true;
            
            IOException ex = new IOException(response.getFailureReason());
            Integer failedChange = response.getFailedChange();
            if (failedChange == null || transactionalFailure) {
                // the client either applies everything, or nothing
                CompletableFuture failed = new CompletableFuture();
                failed.completeExceptionally(
                    new ResourceModificationException(this.completed, this.currentEdit, 
                            ResourceModificationException.UNSPECIFIED_OPERATIION, 
                            ResourceModificationException.UNSPECIFIED_EDIT, this.saved, 
                            response.getFailureReason(), ex)
                );
                return failed;
            } else {
                CompletableFuture failed = new CompletableFuture();
                failed.completeExceptionally(
                    new ResourceModificationException(this.completed, this.currentEdit, 
                            failedChange, 
                            ResourceModificationException.UNSPECIFIED_EDIT, this.saved, 
                            response.getFailureReason(), ex)
                );
                return failed;
            }
        }
        
        public void execute() throws ResourceModificationException {
            CompletableFuture<Void> response = null;
            for (WorkspaceEdit e : edits) {
                currentEdit = e;
                
                CompletableFuture<ApplyWorkspaceEditResponse> next;
                
                if (response == null) {
                    next = client.applyEdit(new ApplyWorkspaceEditParams(Utils.workspaceEditFromApi(e, null, client)));
                } else {
                    next = response.thenCompose((v) -> client.applyEdit(new ApplyWorkspaceEditParams(Utils.workspaceEditFromApi(e, null, client))));
                }
                response = next.thenCompose((r) -> handleClientResponse(e, r));
            }
            
            if (doSave) {
                client.requestDocumentSave(new SaveDocumentRequestParams(new ArrayList<>(processed)));
            }
        }
    }
}
