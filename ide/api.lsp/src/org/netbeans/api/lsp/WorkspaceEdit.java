/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import org.openide.util.Union2;

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
    
}
