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
package org.netbeans.api.lsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.openide.util.Lookup;
import org.openide.util.Union2;
import org.netbeans.spi.lsp.ApplyEditsImplementation;

/**
 * A set of edits over the workspace.
 *
 * @since 1.3
 */
public class WorkspaceEdit {

    private final List<Union2<TextDocumentEdit, ResourceOperation>> documentChanges;

    /**
     * Construct a new {@code WorkspaceEdit}.
     *
     * @param documentChanges the changes to documents in the workspace that need to be performed.
     */
    public WorkspaceEdit(List<Union2<TextDocumentEdit, ResourceOperation>> documentChanges) {
        this.documentChanges = Collections.unmodifiableList(new ArrayList<>(documentChanges));
    }

    /**
     * The changes to documents in the workspace that need to be performed.
     *
     * @return the changes to documents in the workspace that need to be performed
     */
    public List<Union2<TextDocumentEdit, ResourceOperation>> getDocumentChanges() {
        return documentChanges;
    }
    
    
    /**
     * Attempts to apply workspace edits to the resources. The resource(s) are optionally
     * saved after modification. The caller may request just save of the resources, by supplying
     * a {@link WorkspaceEdit} with {@link TextDocumentEdit}s that have empty set of changes. 
     * The implementation must apply edits so they result in the same result as if the contents
     * of the WorkspaceEdit are applied in the order.
     * <p/>
     * Upon failure, the returned Future completes with {@link ResourceModificationException}. 
     * Completed WorkspaceEdits, the failed one and the index of a failed operation within it should be reported.
     * If any resource was saved before the failure, it should be reported as saved.
     * 
     * @param edits edits to apply.
     * @param save if true, resources are saved after they are modified.
     * @return future that completes with a list of resource URIs modified, or fails with {@link ResourceModificationException}.
     * @since 1.27
     */
    public static CompletableFuture<List<String>> applyEdits(List<WorkspaceEdit> edits, boolean save) {
        ApplyEditsImplementation impl = Lookup.getDefault().lookup(ApplyEditsImplementation.class);
        if (impl == null) {
            ResourceModificationException ex = new ResourceModificationException(Collections.<WorkspaceEdit>emptyList(),
                    null, -1, -1, Collections.emptyList(), "Unsupported operation", new UnsupportedOperationException());
            return CompletableFuture.failedFuture(ex);
        }
        return impl.applyChanges(edits, save);
    }
}
 