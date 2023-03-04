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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.lib.editor.util.GapList;


final class ExpectedDocumentContent implements AbstractDocument.Content {

    // Use GapList for more efficient clearStalePositions()
    private List<WeakReference<PositionImpl>> positionRefs = new GapList<WeakReference<PositionImpl>>();
    
    private char[] buffer = new char[] { '\n' };
    
    private int length = 1;

    @Override
    public synchronized Position createPosition(int offset) throws BadLocationException {
        checkOffsetInContent(offset);
        return createPosition(offset, false);
    }

    public synchronized Position createBackwardBiasPosition(int offset) throws BadLocationException {
        checkOffsetInContent(offset);
        return createPosition(offset, true);
    }
    
    private Position createPosition(int offset, boolean backwardBias) throws BadLocationException {
        PositionImpl pos = new PositionImpl(offset, backwardBias);
        positionRefs.add(new WeakReference<PositionImpl>(pos));
        return pos;
    }

    @Override
    public int length() {
        return length;
    }

    private int docLen() {
        return length() - 1;
    }
    
    @Override
    public UndoableEdit insertString(int offset, String str) throws BadLocationException {
        checkOffsetInDoc(offset);
        UndoEdit edit = new UndoEdit(false, offset, str);
        insertEdit(edit);
        return edit;
    }
    
    @Override
    public UndoableEdit remove(int offset, int len) throws BadLocationException {
        checkBoundsInDoc(offset, len);
        UndoEdit edit = new UndoEdit(true, offset, getString(offset, len));
        removeEdit(edit);
        return edit;
    }

    @Override
    public synchronized String getString(int offset, int len) throws BadLocationException {
        checkBoundsInContent(offset, len);
        return new String(buffer, offset, len);
    }

    @Override
    public synchronized void getChars(int offset, int len, Segment txt) throws BadLocationException {
        txt.array = buffer;
        txt.offset = offset;
        txt.count = len;
    }

    synchronized void insertEdit(UndoEdit edit) {
        insertText(edit.offset, edit.text);
        // Leave bb marks with offset untouched
        int len = edit.text.length();
        clearStalePositions();
        for (WeakReference<PositionImpl> posRef : positionRefs) {
            PositionImpl pos = posRef.get();
            if (pos != null) {
                if (pos.backwardBias) {
                    if (pos.offset > edit.offset) {
                        pos.offset += len;
                    }
                    // Leave BB positions at offset untouched
                } else { // Forward bias
                    // AbstractDocument leaves position at offset 0 untouched
                    if (pos.offset > 0 && pos.offset >= edit.offset) {
                        pos.offset += len;
                    }
                }
            }
        }
        // Possibly return marks' offset for undo of removal
        if (edit.markUpdates != null) {
            for (MarkUpdate markUpdate : edit.markUpdates) {
                markUpdate.undo();
            }
        }
    }
    
    synchronized void removeEdit(UndoEdit edit) {
        int len = edit.text.length();
        removeText(edit.offset, len);
        edit.markUpdates = new ArrayList<MarkUpdate>();
        // Remember offsets inside removal area
        clearStalePositions();
        for (WeakReference<PositionImpl> posRef : positionRefs) {
            PositionImpl pos = posRef.get();
            if (pos != null) {
                // Remember original offset for both regular and BB positions
                // Also include positions right at (offset + len) due to proper handling
                // of backward-bias marks (they do not need to be explicitly handled in insertEdit())
                if (pos.offset >= edit.offset && pos.offset <= edit.offset + len) {
                    MarkUpdate markUpdate = new MarkUpdate(pos);
                    edit.markUpdates.add(markUpdate);
                    pos.offset = edit.offset;
                } else if (pos.offset > edit.offset + len) { // Move down positions above removed area
                    pos.offset -= len;
                }
            }
        }
    }
    
    private void clearStalePositions() {
        for (int i = 0; i < positionRefs.size(); i++) {
            WeakReference<PositionImpl> posRef = positionRefs.get(i);
            if (posRef.get() == null) {
                positionRefs.remove(i);
                i--;
            }
        }
    }
    
    void insertText(int offset, String text) {
        if (buffer.length - length < text.length()) {
            int newBufferLength = Math.max(8, length + text.length() + (length >> 1));
            char[] newBuffer = new char[newBufferLength];
            System.arraycopy(buffer, 0, newBuffer, 0, length);
            buffer = newBuffer;
        }
        System.arraycopy(buffer, offset, buffer, offset + text.length(), length - offset);
        text.getChars(0, text.length(), buffer, offset);
        length += text.length();
    }
    
    void removeText(int offset, int len) {
        System.arraycopy(buffer, offset + len, buffer, offset, length - (offset + len));
        length -= len;
    }

    private void checkOffsetNonNegative(int offset) throws BadLocationException {
        if (offset < 0) {
            throw new BadLocationException("Invalid offset=" + offset // NOI18N
                + " < 0; docLen=" + docLen(), offset); // NOI18N
        }
    }

    private void checkOffsetInDoc(int offset) throws BadLocationException {
        checkOffsetNonNegative(offset);
        if (offset > docLen()) {
            throw new BadLocationException("Invalid offset=" + offset // NOI18N
                + " > docLen=" + docLen(), offset); // NOI18N
        }
    }

    private void checkOffsetInContent(int offset) throws BadLocationException {
        checkOffsetNonNegative(offset);
        if (offset > length()) {
            throw new BadLocationException("Invalid offset=" + offset // NOI18N
                + " > (docLen+1)=" + docLen(), offset); // NOI18N
        }
    }

    private void checkLengthNonNegative(int length) throws BadLocationException {
        if (length < 0) {
            throw new BadLocationException("Invalid length=" + length + " < 0", length); // NOI18N
        }
    }

    private void checkBoundsInDoc(int offset, int length) throws BadLocationException {
        checkOffsetNonNegative(offset);
        checkLengthNonNegative(length);
	if (offset + length > docLen()) {
	    throw new BadLocationException("Invalid (offset=" + offset + " + length=" + length + // NOI18N
                    ")=" + (offset+length) + " > docLen=" + docLen(), offset + length); // NOI18N
	}
    }

    private void checkBoundsInContent(int offset, int length) throws BadLocationException {
        checkOffsetNonNegative(offset);
        checkLengthNonNegative(length);
	if (offset + length > length()) {
	    throw new BadLocationException("Invalid (offset=" + offset + " + length=" + length + // NOI18N
                    ")=" + (offset+length) + " > (docLen+1)=" + length(), offset + length); // NOI18N
	}
    }

    static final class PositionImpl implements Position {
        
        int offset;
        
        boolean backwardBias;

        public PositionImpl(int offset, boolean backwardBias) {
            this.offset = offset;
            this.backwardBias = backwardBias;
        }
        
        @Override
        public int getOffset() {
            return offset;
        }
        
    }
    
    class UndoEdit extends AbstractUndoableEdit {

        final boolean removal;

        final int offset;
        
        final String text;

        List<MarkUpdate> markUpdates;
        
        public UndoEdit(boolean removal, int offset, String text) {
            this.removal = removal;
            this.offset = offset;
            this.text = text;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            if (removal) {
                insertEdit(this);
            } else {
                removeEdit(this);
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            if (removal) {
                removeEdit(this);
            } else {
                insertEdit(this);
            }
        }
        
    }
    
    static final class MarkUpdate {
        
        PositionImpl pos;
        
        int origOffset;
        
        MarkUpdate(PositionImpl pos) {
            this.pos = pos;
            this.origOffset = pos.offset;
        }
        
        void undo() {
            this.pos.offset = origOffset;
        }

    }

}
