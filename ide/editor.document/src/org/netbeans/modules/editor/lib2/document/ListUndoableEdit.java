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

import java.util.AbstractList;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Undoable edit that delegates all operation to its delegate. The delegate
 * may be created by multiple wrapping of a document's undoable edit. This edit
 * tracks all the wrappings by adding resulting edits into a list
 * (because wrap edit does not allow to obtain the original edit being wrapped).
 *
 * @author Miloslav Metelka
 */
public class ListUndoableEdit extends AbstractList<UndoableEdit> implements UndoableEdit {
    
    private UndoableEdit[] edits;
    
    public ListUndoableEdit(UndoableEdit e) {
        edits = new UndoableEdit[] { e };
    }
    
    public ListUndoableEdit(UndoableEdit e0, UndoableEdit e1) {
        edits = new UndoableEdit[] { e0, e1 };
    }
    
    public void setDelegate(UndoableEdit edit) {
        UndoableEdit[] newEdits = new UndoableEdit[edits.length + 1];
        System.arraycopy(edits, 0, newEdits, 0, edits.length);
        newEdits[edits.length] = edit;
        edits = newEdits;
    }

    @Override
    public UndoableEdit get(int index) {
        return edits[index];
    }

    @Override
    public int size() {
        return edits.length;
    }
    
    public UndoableEdit delegate() {
        return edits[edits.length - 1];
    }

    @Override
    public void undo() throws CannotUndoException {
        delegate().undo();
    }

    @Override
    public boolean canUndo() {
        return delegate().canUndo();
    }

    @Override
    public void redo() throws CannotRedoException {
        delegate().redo();
    }

    @Override
    public boolean canRedo() {
        return delegate().canRedo();
    }

    @Override
    public void die() {
        delegate().die();
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        return delegate().addEdit(anEdit);
    }

    @Override
    public boolean replaceEdit(UndoableEdit anEdit) {
        return delegate().replaceEdit(anEdit);
    }

    @Override
    public boolean isSignificant() {
        return delegate().isSignificant();
    }

    @Override
    public String getPresentationName() {
        return delegate().getPresentationName();
    }

    @Override
    public String getUndoPresentationName() {
        return delegate().getUndoPresentationName();
    }

    @Override
    public String getRedoPresentationName() {
        return delegate().getRedoPresentationName();
    }

}
