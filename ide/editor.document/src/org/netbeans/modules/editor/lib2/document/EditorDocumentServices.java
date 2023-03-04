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
package org.netbeans.modules.editor.lib2.document;

import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Various services for a document implementation
 * (currently only org.netbeans.editor.BaseDocument).
 * <br/>
 * This class together with EditorDocumentHandler allows an efficient
 * performing of methods from {@link org.netbeans.api.editor.document.EditorDocumentUtils}.
 *
 * @author Miloslav Metelka
 */
public interface EditorDocumentServices {
    
    /**
     * @see {@link org.netbeans.api.editor.document.EditorDocumentUtils#runExclusive(java.lang.Runnable)}.
     */
    void runExclusive(@NonNull Document doc, @NonNull Runnable r);
    
    /**
     * Reset undo merging.
     *
     * @param doc document.
     */
    void resetUndoMerge(@NonNull Document doc);
    
    UndoableEdit startOnSaveTasks(@NonNull Document doc);
    
    void endOnSaveTasks(@NonNull Document doc, boolean success);

}
