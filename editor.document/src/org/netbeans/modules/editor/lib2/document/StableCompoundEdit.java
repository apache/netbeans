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

import java.util.ArrayList;
import java.util.List;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.openide.util.NbBundle;

/**
 * Similar to Swing's {@link javax.swing.undo.CompoundEdit} but guarantees
 * stability of internal state even if undo()/redo() throws an exception.
 *
 * @author Miloslav Metelka
 */
public class StableCompoundEdit implements UndoableEdit {
    
    private static final int HAS_BEEN_DONE = 1;
    private static final int ALIVE = 2;
    private static final int IN_PROGRESS = 4;
    
    private int statusBits;
            
    private ArrayList<UndoableEdit> edits; // ArrayList due to trim

    public StableCompoundEdit() {
        statusBits = HAS_BEEN_DONE | ALIVE | IN_PROGRESS;
	edits = new ArrayList<UndoableEdit>(4);
    }
    
    public final List<UndoableEdit> getEdits() { // Due to BaseDocumentEvent.replaceEdit()
        return edits;
    }

    @Override
    public void die() {
	clearStatusBits(ALIVE);
	int size = edits.size();
	for (int i = size-1; i >= 0; i--) {
	    edits.get(i).die();
	}
    }

    @Override
    public void undo() throws CannotUndoException {
	if (!canUndo()) {
	    throw new CannotUndoException();
	}
        int i = edits.size() - 1;
        try {
            for (; i >= 0; i--) {
                edits.get(i).undo();
            }
            clearStatusBits(HAS_BEEN_DONE);
        } finally {
            if (i != -1) { // i-th edit's undo failed => redo the ones above
                int size = edits.size();
                while (++i < size) {
                    edits.get(i).redo();
                }
            }
        }
    }

    @Override
    public boolean canUndo() {
	return isAnyStatusBit(ALIVE) && isAnyStatusBit(HAS_BEEN_DONE) && !isInProgress();
    }

    @Override
    public void redo() throws CannotRedoException {
	if (!canRedo()) {
	    throw new CannotRedoException();
	}
        int i = 0;
        int size = edits.size();
        try {
            for (; i < size; i++) {
                edits.get(i).redo();
            }
            setStatusBits(HAS_BEEN_DONE);
        } finally {
            if (i != size) { // i-th edit's redo failed => undo the ones below
                while (--i >= 0) {
                    edits.get(i).undo();
                }
            }
        }
    }

    @Override
    public boolean canRedo() {
	return isAnyStatusBit(ALIVE) && !isAnyStatusBit(HAS_BEEN_DONE) && !isInProgress();
    }
	
    @Override
    public boolean addEdit(UndoableEdit anEdit) {
	if (!isInProgress()) {
	    return false;
	} else {
	    UndoableEdit last = lastEdit();

	    // If this is the first subedit received, just add it.
	    // Otherwise, give the last one a chance to absorb the new
	    // one.  If it won't, give the new one a chance to absorb
	    // the last one.

	    if (last == null) {
		edits.add(anEdit);
	    }
	    else if (!last.addEdit(anEdit)) {
		if (anEdit.replaceEdit(last)) {
		    edits.remove(edits.size()-1);
		}
		edits.add(anEdit);
	    }

	    return true;
	}
    }

    @Override
    public boolean replaceEdit(UndoableEdit anEdit) {
	return false;
    }

    @Override
    public boolean isSignificant() {
        int size = edits.size();
        for (int i = 0; i < size; i++) {
            if (edits.get(i).isSignificant()) {
                return true; // At least one is significant
            }
        }
	return false; // None is significant
    }

    @Override
    public String getPresentationName() {
	UndoableEdit last = lastEdit();
	if (last != null) {
	    return last.getPresentationName();
	} else {
	    return "";
	}
    }

    @Override
    public String getUndoPresentationName() {
	UndoableEdit last = lastEdit();
	if (last != null) {
	    return last.getUndoPresentationName();
	} else {
            String name = getPresentationName();
            if (!"".equals(name)) {
                name = NbBundle.getMessage(ContentEdit.class, "AbstractUndoableEdit.undoText.param", name); // NOI18N
            } else {
                name = NbBundle.getMessage(ContentEdit.class, "AbstractUndoableEdit.undoText"); // NOI18N
            }

            return name;
	}
    }
        
    @Override
    public String getRedoPresentationName() {
	UndoableEdit last = lastEdit();
	if (last != null) {
	    return last.getRedoPresentationName();
	} else {
            String name = getPresentationName();
            if (!"".equals(name)) {
                name = NbBundle.getMessage(ContentEdit.class, "AbstractUndoableEdit.redoText.param", name); // NOI18N
            } else {
                name = NbBundle.getMessage(ContentEdit.class, "AbstractUndoableEdit.redoText"); // NOI18N
            }

            return name;
	}
    }
        
    public void end() {
	clearStatusBits(IN_PROGRESS);
        edits.trimToSize();
    }

    public boolean isInProgress() {
	return isAnyStatusBit(IN_PROGRESS);
    }

    protected UndoableEdit lastEdit() {
	int count = edits.size();
	if (count > 0)
	    return edits.get(count-1);
	else
	    return null;
    }

    private boolean isAnyStatusBit(int bits) {
        return (statusBits & bits) != 0;
    }

    private void setStatusBits(int bits) {
        statusBits |= bits;
    }
    
    private void clearStatusBits(int bits) {
        statusBits &= ~bits;
    }
    
    @Override
    public String toString() {
	return super.toString()
	    + " hasBeenDone: " + isAnyStatusBit(HAS_BEEN_DONE)
	    + " alive: " + isAnyStatusBit(ALIVE)
	    + " inProgress: " + isInProgress()
	    + " edits: " + edits;
    }

}
