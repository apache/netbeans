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

import javax.swing.event.DocumentEvent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * Utilities related to document post-modification.
 *
 * @author Miloslav Metelka
 */
public class DocumentPostModificationUtils {

    /**
     * Mark the given document event as post-modification edit.
     * This is used by document implementations to distinguish between regular edits
     * and the edits created as a result of post-modification e.g. an instant rename functionality.
     * 
     * @param evt event into which the post-modification marker should be added.
     *  The passed event has to have the event property storage already initialized
     *  by {@link DocumentUtilities#addEventPropertyStorage(javax.swing.event.DocumentEvent)}.
     */
    public static void markPostModification(@NonNull DocumentEvent evt) {
        DocumentUtilities.putEventPropertyIfSupported(evt, DocumentPostModificationUtils.class, Boolean.TRUE);
    }

    /**
     * Test whether the given document event is a document post-modification edit event.
     * For example a caret may treat the post-modification edits differently than regular edits.
     *
     * @param evt event to test.
     * @return true if the event is a post-modification edit or false otherwise.
     */
    public static boolean isPostModification(@NonNull DocumentEvent evt) {
        return Boolean.TRUE.equals(DocumentUtilities.getEventProperty(evt, DocumentPostModificationUtils.class));
    }

}
