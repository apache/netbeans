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
package org.netbeans.modules.refactoring.spi.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.spi.editor.document.UndoableEditWrapper;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
@MimeRegistration(mimeType="", service=UndoableEditWrapper.class)
public class UndoableWrapper implements UndoableEditWrapper {

    private AtomicBoolean active = new AtomicBoolean();
    private Map<BaseDocument, UndoableEditDelegate> docToFirst = new HashMap<>();
    private RefactoringSession session;

    public UndoableWrapper() {
    }
    

    @Override
    public UndoableEdit wrap(UndoableEdit ed, Document doc) {
        if (!active.get())
            return ed;
        final Object stream = doc.getProperty(BaseDocument.StreamDescriptionProperty);
        DataObject dob = null;
        if(stream instanceof DataObject) {
            dob = (DataObject) stream;
        } else if(stream instanceof FileObject) {
            FileObject fileObject = (FileObject) stream;
            try {
                dob = DataObject.find(fileObject);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if(dob == null) {
            //no dataobject
            return ed;
        }
        UndoableEditDelegate current = new UndoableEditDelegate(ed, dob, session);
        UndoableEditDelegate first = docToFirst.get(doc);
        if (first == null) {
            docToFirst.put((BaseDocument) doc, current);
        }
        return current;
    }

    public void close() {
        for (UndoableEditDelegate first: docToFirst.values()) {
            first.end();
        }
        docToFirst.clear();
    }

    public void setActive(boolean b, RefactoringSession session) {
        this.session = session;
        active.set(b);
        
    }

    public class UndoableEditDelegate implements UndoableEdit {

        private UndoManager undoManager;
        private CloneableEditorSupport ces;
        private UndoableEdit delegate;
        private CompoundEdit inner;
        private RefactoringSession session;

        private UndoableEditDelegate(UndoableEdit ed, DataObject dob, RefactoringSession session) {
            undoManager = UndoManager.getDefault();
            ces = dob.getLookup().lookup(CloneableEditorSupport.class);
            //this.delegate = ed;
            this.inner = new CompoundEdit();
            inner.addEdit(ed);
            delegate = ed;
            this.session = session;
        }

        @Override
        public void undo() throws CannotUndoException {
            JTextComponent focusedComponent = EditorRegistry.focusedComponent();
            if (focusedComponent != null) {
                if (focusedComponent.getDocument() == ces.getDocument()) {
                    //call global undo only for focused component
                    undoManager.undo(session, ces.getDocument());
                }
            }
            //delegate.undo();
            inner.undo();
        }

        @Override
        public boolean canUndo() {
            //return delegate.canUndo();
            return inner.canUndo();
        }

        @Override
        public void redo() throws CannotRedoException {
            JTextComponent focusedComponent = EditorRegistry.focusedComponent();
            if (focusedComponent != null) {
                if (focusedComponent.getDocument() == ces.getDocument()) {
                    //call global undo only for focused component
                    undoManager.redo(session);
                }
            }
            //delegate.redo();
            inner.redo();
        }

        @Override
        public boolean canRedo() {
            //return delegate.canRedo();
            return inner.canRedo();
        }

        @Override
        public void die() {
            //delegate.die();
            inner.die();
        }

        @Override
        public boolean addEdit(UndoableEdit ue) {
            if (ue instanceof List) {
                List<UndoableEdit> listEdit = (List<UndoableEdit>) ue;
                UndoableEdit topEdit = listEdit.get(listEdit.size() - 1);
                // Check that there's only original document's edit and the wrapping refactoring edit
                boolean refatoringEditOnly = listEdit.size() == 2;
                if (refatoringEditOnly && topEdit instanceof UndoableEditDelegate) {
                    inner.addEdit(listEdit.get(0));
                    return true;
            }
            return false;
        }
            return false;
        }
        
        public UndoableEdit unwrap() {
            return delegate;
        }

        @Override
        public boolean replaceEdit(UndoableEdit ue) {
            return inner.replaceEdit(ue);
            //return delegate.replaceEdit(ue);
        }

        @Override
        public boolean isSignificant() {
            return inner.isSignificant();
            //return delegate.isSignificant();
        }

        @Override
        public String getPresentationName() {
            return undoManager.getUndoDescription(session);
        }

        @Override
        public String getUndoPresentationName() {
            return undoManager.getUndoDescription(session);
        }

        @Override
        public String getRedoPresentationName() {
            return undoManager.getRedoDescription(session);
        }

        private void end() {
            inner.end();
        }
    }
}
