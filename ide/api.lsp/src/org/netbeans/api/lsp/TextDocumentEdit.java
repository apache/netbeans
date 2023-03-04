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

/**
 * A list of {@code TextEdit}s in a particular document.
 *
 * @since 1.3
 */
public class TextDocumentEdit {
    private final String document;
    private final List<TextEdit> edits;

    /**
     * Construct the TextDocumentEdit
     * @param document the URI of the document being edited
     * @param edits the list of edits
     */
    public TextDocumentEdit(String document, List<TextEdit> edits) {
        this.document = document;
        this.edits = Collections.unmodifiableList(new ArrayList<>(edits));
    }

    /**
     * The URI of the document being edited.
     *
     * @return the URI of the document being edited
     */
    public String getDocument() {
        return document;
    }

    /**
     * The list of edits.
     *
     * @return the list of {@code TextEdit}s in this {@code TextDocumentEdit}.
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<TextEdit> getEdits() {
        return edits;
    }
    
}
