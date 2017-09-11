/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
