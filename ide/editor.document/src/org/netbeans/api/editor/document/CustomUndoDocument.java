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
package org.netbeans.api.editor.document;

import javax.swing.undo.UndoableEdit;

/**
 * Document that allows adding of a custom undoable edit during atomic transaction.
 * <br/>
 * To obtain CustomUndoDocument instance the {@link LineDocumentUtils#as(javax.swing.text.Document, java.lang.Class) }
 * or {@link LineDocumentUtils#asRequired(javax.swing.text.Document, java.lang.Class) } may be used:
 * <code>
 * <pre>
 *   Document doc = ...
 *   CustomUndoDocument customUndoDoc = LineDocumentUtils.asRequired(doc, CustomUndoDocument.class);
 * </pre>
 * </code>
 *
 * @author Miloslav Metelka
 * @since 1.8
 */
public interface CustomUndoDocument {
    
    /**
     * Add a custom undoable edit to the undoable edits being created
     * during an atomic transaction over the document.
     * <br/>
     * For example editor caret may add an undo edit allowing to restore caret(s) positions
     * before (or after) modifications during the atomic lock.
     *
     * @param edit non-null undoable edit.
     * @throws IllegalStateException if the document is not under atomic lock.
     * @since 1.8
     */
    public void addUndoableEdit(UndoableEdit edit);
    
}
