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
package org.netbeans.modules.editor.lib2;

import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.caret.EditorCaret;
import org.openide.util.Parameters;

/**
 * Editor caret undo related functionality.
 *
 * @author Miloslav Metelka
 */
public final class CaretUndo {
    
    /**
     * Create undoable edit that returns caret to its original state when the edit is undone.
     * <br/>
     * This edit is typically created both at the begining and end of an action that does some document modifications.
     *
     * @param caret non-null caret.
     * @param doc non-null document to which the undoable edit will be added.
     * @return edit allowing to restore caret state upon undo call on the returned edit or null
     *  if caret is not installed in a valid document.
     *  <br>
     *  Future optimizations may return null edit also in case when there was no change in carets
     *  since the preceding call to this method inside the same atomic transaction over the document.
     */
    public static UndoableEdit createCaretUndoEdit(@NonNull Caret caret, @NonNull Document doc) {
        Parameters.notNull("caret", caret);
        Parameters.notNull("doc", doc);
        return CaretUndoEdit.create(caret, doc);
    }

    /**
     * Check whether the given edit is a caret snapshot undoable edit.
     * <br>
     * This is typically used during merging of undoable edits.
     *
     * @param edit non-null edit.
     * @return true if the given edit is caret related undoable edit or false otherwise.
     */
    public static boolean isCaretUndoEdit(@NonNull UndoableEdit edit) {
        Class editClass = edit.getClass();
        return (editClass == CaretUndoEdit.class || editClass == CaretUndoEdit.ComplexEdit.class);
    }
    
}
