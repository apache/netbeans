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
package org.openide.text;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Undoable edit that wraps each undoable edit added to CloneableEditorSupport's undo manager.
 * <br/>
 * It is needed to
 * <ul>
 *   <li>Prohibit addEdit() operation on last edit when the undo manager is at savepoint.</li>
 *   <li>Prohibit replaceEdit() operation on last edit when the undo manager is at savepoint.</li>
 *   <li>Identify non-significant edits that are otherwise not reported by editToBeUndone()
 *       and editToBeRedone().</li>
 * </ul>
 * 
 * @author Miloslav Metelka
 * @since 6.43
 */
final class WrapUndoEdit implements UndoableEdit {

    /**
     * Support for which this edit is created.
     */
    final UndoRedoManager undoRedoManager; // 8=Object + 4 = 12 bytes

    /**
     * Real undoable edit passed to undoableEditHappened().
     */
    private UndoableEdit delegate; // 12 + 4 = 16 bytes

    WrapUndoEdit(UndoRedoManager undoRedoManager, UndoableEdit delegate) {
        assert (delegate != null) : "Delegate is null"; // NOI18N
        this.undoRedoManager = undoRedoManager;
        this.delegate = delegate;
    }
    
    UndoableEdit delegate() {
        return delegate;
    }

    void setDelegate(UndoableEdit delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void undo() throws CannotUndoException {
        undoRedoManager.checkLogOp("WrapUndoEdit.undo", this);
        boolean savepoint = undoRedoManager.isAtSavepoint();
        if (savepoint) {
            undoRedoManager.beforeUndoAtSavepoint(this);
        }
        boolean done = false;
        try {
            delegate.undo();
            done = true;
            // This will only happen if delegate.undo() does not throw CannotUndoException
            undoRedoManager.afterUndoCheck(this);
        } finally {
            if (!done && savepoint) {
                undoRedoManager.delegateUndoFailedAtSavepoint(this);
            }
        }
    }

    @Override
    public boolean canUndo() {
        return delegate.canUndo();
    }

    @Override
    public void redo() throws CannotRedoException {
        undoRedoManager.checkLogOp("WrapUndoEdit.redo", this);
        boolean savepoint = undoRedoManager.isAtSavepoint();
        if (savepoint) {
            undoRedoManager.beforeRedoAtSavepoint(this);
        }
        boolean done = false;
        try {
            delegate.redo();
            done = true;
            // This will only happen if delegate.redo() does not throw CannotRedoException
            undoRedoManager.afterRedoCheck(this);
        } finally {
            if (!done && savepoint) {
                undoRedoManager.delegateRedoFailedAtSavepoint(this);
            }
        }
    }

    @Override
    public boolean canRedo() {
        return delegate.canRedo();
    }

    @Override
    public void die() {
        undoRedoManager.checkLogOp("WrapUndoEdit.die", this);
        delegate.die();
        undoRedoManager.notifyWrapEditDie(this);
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        if (undoRedoManager.isAtSavepoint()) {
            // Check whether save actions can be merged with last edit. The check is done here
            // since at this time trimEdits() was called already so this is truly a last edit.
            undoRedoManager.mergeSaveActionsToLastEdit(this);

            // Prohibit adding to existing edit at savepoint since undo to savepoint could no longer be done
            // when merging with another edit.
            // Since addEdit() will fail the anEdit.replaceEdit() will be attempted; but it will not succeed too
            // (see impl) so anEdit will be added as a fresh edit to end of UM.edits.
            // However undoRedoManager.savepointEdit must be set by undoRedoManager.addEdit() (not here)
            // since UM.edits may be empty in which case WUE.addEdit() is not called.
            return false;
        }
        // anEdit is already wrapped for possible regular addition
        WrapUndoEdit wrapEdit = (WrapUndoEdit) anEdit;
        boolean added = delegate.addEdit(wrapEdit.delegate);
        return added;
    }

    @Override
    public boolean replaceEdit(UndoableEdit anEdit) {
        if (undoRedoManager.isAtSavepoint()) { // Prohibit replacing at savepoint
            return false;
        }
        WrapUndoEdit wrapEdit = (WrapUndoEdit) anEdit;
        boolean replaced = delegate.replaceEdit(wrapEdit.delegate);
        undoRedoManager.checkLogOp("WrapUndoEdit.replaceEdit=" + replaced, anEdit);
        if (replaced) {
            undoRedoManager.checkReplaceSavepointEdit(wrapEdit, this);
        }
        return replaced;
    }

    @Override
    public boolean isSignificant() {
        return delegate.isSignificant();
    }

    @Override
    public String getPresentationName() {
        return delegate.getPresentationName();
    }

    @Override
    public String getUndoPresentationName() {
        return delegate.getUndoPresentationName();
    }

    @Override
    public String getRedoPresentationName() {
        return delegate.getRedoPresentationName();
    }

}

