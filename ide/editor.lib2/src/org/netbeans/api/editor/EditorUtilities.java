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

package org.netbeans.api.editor;

import javax.swing.Action;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.CustomUndoDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.modules.editor.lib2.CaretUndo;
import org.netbeans.modules.editor.lib2.actions.EditorActionUtilities;


/**
 * Various utility methods related to editor.
 *
 * @author Miloslav Metelka
 * @since 1.13
 */

public final class EditorUtilities {
    
    /**
     * Client property of editor component which determines
     * whether caret is currently in overwrite mode (Boolean.TRUE) or insert mode (Boolean.FALSE or null).
     * <br>
     * It's modified by appropriate editor actions.
     * @since 2.6
     */
    public static final String CARET_OVERWRITE_MODE_PROPERTY = "caret-overwrite-mode";

    private EditorUtilities() {
        // No instances
    }

    /**
     * Find an action with the given name in the editor kit.
     *
     * @param editorKit non-null editor kit in which search is performed.
     * @param actionName non-null action name to search for.
     * @return action instance with the given name or null if action not found.
     */
    public static Action getAction(EditorKit editorKit, String actionName) {
        return EditorActionUtilities.getAction(editorKit, actionName);
    }

    /**
     * Add an undoable edit describing current state of caret(s) during document's atomic section.
     * <br>
     * This method is typically called at the beginning of the atomic section over the document
     * so that a subsequent undo would restore original caret offsets that were not yet modified
     * by the actual changes performed during the atomic section.
     * <br>
     * The method may also be called at the end of the atomic section
     * in case the atomic section performed explicit caret movements.
     * <br>
     * The created undoable edit will be added to document's compound undoable edit created for the atomic section.
     * That edit will be fired by the document to an undo manager's listener upon completion of the atomic section.
     * Therefore the document should adhere to {@link CustomUndoDocument} otherwise the method would do nothing.
     *
     * @param doc document to which the created undoable edit will be added.
     *   Null may be passed then the method has no effect.
     * @param caret non-null caret which state should be stored
     * @see CustomUndoDocument
     * @see AtomicLockDocument
     * @throws IllegalStateException if this method is called outside of an atomic section.
     * @since 2.11
     */
    public static void addCaretUndoableEdit(Document doc, Caret caret) {
        CustomUndoDocument customUndoDocument = LineDocumentUtils.as(doc, CustomUndoDocument.class);
        if (customUndoDocument != null) {
            UndoableEdit caretUndoEdit = CaretUndo.createCaretUndoEdit(caret, doc);
            if (caretUndoEdit != null) {
                customUndoDocument.addUndoableEdit(caretUndoEdit);
            } // Might be null if caret is not installed in a text component and its document
        }
    }
    

//    /**
//     * Reset caret's magic position.
//     * @param component target text component.
//     */
//    public static void resetCaretMagicPosition(JTextComponent component) {
//        Caret caret;
//        if (component != null && (caret = component.getCaret()) != null) {
//            caret.setMagicCaretPosition(null);
//        }
//    }
//    
//    /**
//     * Reset a possible undo merging so any upcoming edits will be undone separately.
//     * @param component target text component.
//     */
//    public static void resetUndoMerge(JTextComponent component) {
//        Document doc;
//        if (component != null && (doc = component.getDocument()) != null) {
//            EditorDocumentUtils.resetUndoMerge(doc);
//        }
//    }
//    
//    /**
//     * Reset word match so that a possible next press of Ctrl+K/L starts from scratch.
//     *
//     * @param component target text component.
//     */
//    public static void resetWordMatch(JTextComponent component) {
//        if (component != null) {
//            resetWordMatch(component.getDocument());
//        }
//    }
//    
//    public static void resetWordMatch(@NonNull Document doc) {
//        WordMatch.get(doc).reset();
//    }

}
