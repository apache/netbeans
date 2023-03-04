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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.undo.UndoableEdit;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.editor.lib2.document.ContentEdit.InsertEdit;
import org.netbeans.modules.editor.lib2.document.ContentEdit.RemoveEdit;

/**
 * Content of the document.
 * <br/>
 * It's similar to swing's GapConent but it is a bit more consistent in terms of position sharing
 * in the following scenario:
 * <ul>
 *   <li> Create position at pos1(off==1) and pos2(off==2) </li>
 *   <li> Remove(1,1) so pos2 is at offset==1. </li>
 *   <li> createPosition(1): GapContent picks either pos1 or pos2 by binary search.
 *    Which one is picked depends on total number of positions so creation of extra positions
 *    on different offsets affects that. EDC always returns pos2 in this scenario regardless
 *    on total number of positions.
 *   </li>
 * </ul>
 * <br/>
 * Both GapContent and EDC may reuse position which returns into a middle of removed area
 * upon undo:
 * <ul>
 *   <li> Create position at pos1(off==2)</li>
 *   <li> Remove(1,2) so pos1 is at offset==1.</li>
 *   <li> createPosition(1): pos1 is returned. </li>
 *   <li> Undo (which means Insert(1,2)) returns pos1 to offset==2 (someone might expect offset==3). </li>
 * </ul>
 * 
 * <br/>
 * Content intentionally does not reference a document instance to which it belongs
 * and the EditorPosition should also not reference document instance.
 * Therefore one may create position and hold it strongly and wait for document instance
 * to be released (and then e.g. release the position).
 *
 * @author Miloslav Metelka
 * @since 1.46
 */

public final class EditorDocumentContent implements AbstractDocument.Content {
    
    // -J-Dorg.netbeans.modules.editor.lib2.document.EditorDocumentContent.level=FINE
    private static final Logger LOG = Logger.getLogger(EditorDocumentContent.class.getName());

    /** Character contents of document. */
    private final CharContent charContent;

    /** Vector holding the marks for the document */
    private final MarkVector markVector;
    
    /** Vector holding backward-bias marks - it's null until at least one backward-bias mark gets created. */
    private MarkVector bbMarkVector;
    
    public EditorDocumentContent() {
        // In compliance with AbstractDocument the content has one extra unmodifiable '\n' at the end
        charContent = new CharContent();
        markVector = new MarkVector(this, false);
    }
    
    /**
     * Perform additional initialization of document by this content (set properties).
     *
     * @param doc non-null document
     */
    public void init(Document doc) {
        doc.putProperty(CharSequence.class, (CharSequence)charContent);
    }
    
    @Override
    public UndoableEdit insertString(int offset, String text) throws BadLocationException {
        if (text.length() == 0) {
            // Empty insert should be eliminated in parent (Document impl).
            // It could break MarkVector.insertUpdate() operation
            throw new IllegalArgumentException("EditorDocumentContent: Empty insert"); // NOI18N
        }
        checkOffsetInDoc(offset);
        InsertEdit insertEdit = new InsertEdit(this, offset, text);
        insertEdit(insertEdit, ""); // NOI18N
        return insertEdit;
    }
    
    synchronized void insertEdit(ContentEdit edit, String opType) {
        charContent.insertText(edit.offset, edit.text);
        markVector.insertUpdate(edit.offset, edit.length(), edit.markUpdates);
        edit.markUpdates = null; // Allow GC
        if (bbMarkVector != null) {
            bbMarkVector.insertUpdate(edit.offset, edit.length(), edit.bbMarkUpdates);
            edit.bbMarkUpdates = null; // Allow GC
        }
        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(200);
            sb.append(opType).append("insertEdit(off=").append(edit.offset).append(", len="). // NOI18N
                    append(edit.length()).append(") text=\""). // NOI18N
                    append(CharSequenceUtilities.debugText(edit.text)).append("\"\n"); // NOI18N
            logMarkUpdates(sb, edit.markUpdates, false, false);
            logMarkUpdates(sb, edit.bbMarkUpdates, true, false);
            LOG.fine(sb.toString());
            if (LOG.isLoggable(Level.FINER)) {
                checkConsistency();
            }
        }
    }
    
    @Override
    public UndoableEdit remove(int offset, int length) throws BadLocationException {
        checkBoundsInDoc(offset, length);
        String removedText = getString(offset, length);
        return remove(offset, removedText);
    }
    
    /**
     * Optimized removal when having removal string and bounds are verified to be ok.
     * 
     * @param removedText string that corresponds to the text being removed.
     */
    public UndoableEdit remove(int offset, String removedText) throws BadLocationException { // optimized version 
        RemoveEdit removeEdit = new RemoveEdit(this, offset, removedText);
        removeEdit(removeEdit, ""); // NOI18N
        return removeEdit;
    }
    
    synchronized void removeEdit(ContentEdit edit, String opType) {
//        LOG.fine("markVector-before-remove:\n" + toStringDetail()); // NOI18N
        int len = edit.length();
        charContent.removeText(edit.offset, len);
        edit.markUpdates = markVector.removeUpdate(edit.offset, len);
        if (bbMarkVector != null) {
            edit.bbMarkUpdates = bbMarkVector.removeUpdate(edit.offset, len);
        }
        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(200);
            sb.append(opType).append("removeEdit(off=").append(edit.offset). // NOI18N
                    append(", len=").append(len).append(")\n"); // NOI18N
            logMarkUpdates(sb, edit.markUpdates, false, true);
            logMarkUpdates(sb, edit.markUpdates, true, true);
            LOG.fine(sb.toString());
            if (LOG.isLoggable(Level.FINER)) {
                checkConsistency();
            }
        }
    }

    @Override
    public synchronized Position createPosition(int offset) throws BadLocationException {
        checkOffsetInContent(offset);
        EditorPosition pos = markVector.position(offset);
//        LOG.fine("createPosition(" + offset + ")=" + pos.getMark().toStringDetail() + "\n"); checkConsistency(); // NOI18N
        return pos;
    }

    public synchronized Position createBackwardBiasPosition(int offset) throws BadLocationException {
        checkOffsetInContent(offset);
        if (bbMarkVector == null) {
            bbMarkVector = new MarkVector(this, true);
        }
        return bbMarkVector.position(offset);
    }

    @Override
    public int length() { // Not synchronized => only subtracts vars
        return charContent.length();
    }
    
    private int docLen() {
        return length() - 1;
    }
    
    public CharSequence getText() {
        return charContent;
    }

    public int getCharContentGapStart() { // Used by BaseDocument impl
        return charContent.gapStart();
    }

    @Override
    public synchronized void getChars(int offset, int length, Segment txt) throws BadLocationException {
        checkBoundsInContent(offset, length);
        charContent.getChars(offset, length, txt);
    }

    @Override
    public synchronized String getString(int offset, int length) throws BadLocationException {
        checkBoundsInContent(offset, length);
        return charContent.getString(offset, length);
    }
    
    public synchronized void compact() {
        charContent.compact();
        markVector.compact();
        if (bbMarkVector != null) {
            bbMarkVector.compact();
        }
    }
    
    public synchronized String consistencyError() {
        String err = charContent.consistencyError();
        if (err == null) {
            err = markVector.consistencyError(length());
        }
        if (err == null && bbMarkVector != null) {
            err = bbMarkVector.consistencyError(length());
        }
        return err;
    }
    
    public void checkConsistency() { // Not synced (dumpConsistency() is synced)
        String err = consistencyError();
        if (err != null) {
            throw new IllegalStateException("Content inconsistency: " + err + "\nContent:\n" + // NOI18N
                    toStringDetail());
        }
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
    
    private void logMarkUpdates(StringBuilder sb, MarkVector.MarkUpdate[] updates, boolean bbMarks, boolean forRemove) {
        if (updates != null) {
            for (int i = 0; i < updates.length; i++) {
                MarkVector.MarkUpdate update = updates[i];
                if (forRemove) {
                    sb.append("    ").append(bbMarks ? "BB" : "").append("Mark's original offset saved: "). // NOI18N
                            append(update).append('\n'); // NOI18N
                } else {
                    if (update.mark.isActive()) {
                        sb.append("    Restoring offset for ").append(bbMarks ? "BB" : "").append("MarkUpdate: "). // NOI18N
                                append(update).append('\n'); // NOI18N
                    }
                }
            }
        }
    }

    @Override
    public synchronized String toString() {
        return "chars: " + charContent.toStringDescription() + // NOI18N
                ", marks: " + markVector + // NOI18N
                ", bbMarks: " + ((bbMarkVector != null) ? bbMarkVector : "<NULL>"); // NOI18N
    }
    
    public synchronized String toStringDetail() {
        return  "marks: " + markVector.toStringDetail(null) + // NOI18N
                ", bbMarks: " + ((bbMarkVector != null) ? bbMarkVector.toStringDetail(null) : "<NULL>"); // NOI18N
    }
    
}
