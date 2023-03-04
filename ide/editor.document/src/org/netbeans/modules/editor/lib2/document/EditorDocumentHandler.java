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

/**
 * Performer of various document services implemented currently
 * by org.netbeans.editor.BaseDocument in editor.lib module.
 *
 * @author Miloslav Metelka
 */
public final class EditorDocumentHandler {
    
    private EditorDocumentHandler() {
        // no instances
    }

    private static Class<? extends Document> editorDocClass;
    
    private static EditorDocumentServices editorDocServices;
    
    private static EditorCharacterServices charServices = new DefaultEditorCharacterServices(); // Until other impls exist
    
    public static void setEditorDocumentServices(Class<? extends Document> docClass, EditorDocumentServices docServices) {
        // Currently expect just a single implementation: BaseDocument
        if (editorDocClass != null) {
            throw new IllegalStateException("Only single registration expected. Already registered: " + editorDocClass);
        }
        EditorDocumentHandler.editorDocClass = docClass;
        EditorDocumentHandler.editorDocServices = docServices;
    }
    
    public static void runExclusive(Document doc, Runnable r) {
        if (editorDocClass != null && editorDocClass.isInstance(doc)) {
            editorDocServices.runExclusive(doc, r);
        } else {
            synchronized (doc) {
                r.run();
            }
        }
    }
    
    public static void resetUndoMerge(Document doc) {
        if (editorDocClass != null && editorDocClass.isInstance(doc)) {
            editorDocServices.resetUndoMerge(doc);
        }
    }

    public static UndoableEdit startOnSaveTasks(Document doc) {
        if (editorDocClass != null && editorDocClass.isInstance(doc)) {
            return editorDocServices.startOnSaveTasks(doc);
        }
        return null;
    }

    public static void endOnSaveTasks(Document doc, boolean success) {
        if (editorDocClass != null && editorDocClass.isInstance(doc)) {
            editorDocServices.endOnSaveTasks(doc, success);
        }
    }

    public static int getIdentifierEnd(Document doc, int offset, boolean backward) {
        return charServices.getIdentifierEnd(doc, offset, backward);
    }

}
