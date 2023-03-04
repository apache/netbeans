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

import java.util.ArrayList;
import java.util.List;
import javax.swing.UIManager;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

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
                name = UIManager.getString("AbstractUndoableEdit.undoText")
                        + " " + name;
            } else {
                name = UIManager.getString("AbstractUndoableEdit.undoText");
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
                name = UIManager.getString("AbstractUndoableEdit.redoText")
                        + " " + name;
            } else {
                name = UIManager.getString("AbstractUndoableEdit.redoText");
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
