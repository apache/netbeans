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
package org.netbeans.spi.lsp;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.lsp.ResourceModificationException;
import org.netbeans.api.lsp.ResourceOperation;
import org.netbeans.api.lsp.TextDocumentEdit;
import org.netbeans.api.lsp.WorkspaceEdit;

/**
 * This service takes care of "applying" resource modifications to the resources
 * and potentially saving them. Various LSP commands or services result in
 * {@link WorkspaceEdit} that need to be applied eventually. Depending on
 * "deployment" of NetBeans platform, the application needs to be done by
 * NetBeans application itself, or delegated to a LSP client.
 *
 * @author sdedic
 */
public interface ApplyEditsImplementation {

    /**
     * Requests that the edits are applied to the in-memory representation of
     * the resources, and then possibly saved. The returned Future will be completed
     * after the resources are modified and optionally saved, and will complete with a list of
     * affected resources. The returned identifiers will be the same as in {@link TextDocumentEdit#getDocument()}
     * or {@link ResourceOperation}.
     * <p>
     * If the caller requests save, the Future completes after the save is complete.
     * <p>
     * If the operation fails, the Future completes exceptionally with {@link ResourceModificationException}.
     * 
     * @param edits edits to apply
     * @param saveResources instructs to save the resources on the disk.
     */
    public CompletableFuture<List<String>> applyChanges(List<WorkspaceEdit> edits, boolean saveResources);
}
