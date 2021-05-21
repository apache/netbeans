/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.editor.lib2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.api.editor.document.ComplexPositions;
import org.openide.util.Parameters;

/**
 * Undoable edit for caret.
 *
 * @author Miloslav Metelka
 */
class CaretUndoEdit extends AbstractUndoableEdit {
    
    static final int COMPLEX_POSITIONS_MARKER = Integer.MAX_VALUE;
    
    static final int BACKWARD_BIAS_BIT = 1 << 31;
    
    public static UndoableEdit create(@NonNull Caret caret, @NonNull Document doc) {
        UndoableEdit ret;
        if (caret instanceof EditorCaret) {
            EditorCaret eCaret = (EditorCaret) caret;
            List<CaretInfo> carets = eCaret.getCarets();
            int caretsSize = carets.size();
            CaretInfo caretInfo = carets.get(0);
            Position dotPos = caretInfo.getDotPosition();
            Position.Bias dotBias = caretInfo.getDotBias();
            Position markPos = caretInfo.getMarkPosition();
            Position.Bias markBias = caretInfo.getMarkBias();
            if (dotPos != null) {
                if (markPos == null) {
                    markPos = dotPos;
                }
                int dotOffsetAndBias = toOffsetAndBias(dotPos.getOffset(), dotBias);
                int markOffsetAndBias = toOffsetAndBias(markPos.getOffset(), markBias);
                int dotSplitOffset = ComplexPositions.getSplitOffset(dotPos);
                int markSplitOffset = ComplexPositions.getSplitOffset(markPos);
                boolean complexPos = dotSplitOffset != 0 || markSplitOffset != 0;
                if (caretsSize == 1) { // Single-caret case
                    if (!complexPos) { // Regular positions
                        if (dotOffsetAndBias == markOffsetAndBias && dotBias == markBias) { // No selection
                            ret = new CaretUndoEdit(doc, dotOffsetAndBias);
                        } else { // Selection
                            ret = new CaretUndoEdit.ComplexEdit(doc, dotOffsetAndBias, markOffsetAndBias, null);
                        }
                    } else { // Complex positions
                        ret = new CaretUndoEdit.ComplexEdit(doc, dotOffsetAndBias, COMPLEX_POSITIONS_MARKER, new int[]{
                            dotSplitOffset, markOffsetAndBias, markSplitOffset
                        });
                    }
                } else { // Multiple carets
                    int i;
                    int caretIndex = 1;
                    int[] offsets;
                    if (!complexPos) {
                        // Suppose that all the carets will use just non-complex positions
                        offsets = new int[(caretsSize << 1) - 2]; // extra offsets; omit dotOffset and markOffset of first caret
                        i = 0;
                        for (; caretIndex < caretsSize; caretIndex++) {
                            caretInfo = carets.get(caretIndex);
                            dotPos = caretInfo.getDotPosition();
                            dotBias = caretInfo.getDotBias();
                            markPos = caretInfo.getMarkPosition();
                            markBias = caretInfo.getMarkBias();
                            if (markPos == null) {
                                markPos = dotPos;
                            }
                            dotSplitOffset = ComplexPositions.getSplitOffset(dotPos);
                            markSplitOffset = ComplexPositions.getSplitOffset(markPos);
                            if (dotSplitOffset != 0 || markSplitOffset != 0) { // Complex pos
                                // Copy existing values and insert zero split offsets between already collected values
                                int[] newOffsets = new int[(caretsSize << 2) - 1]; // Include dotSplitOffset
                                // newOffsets[0] - leave zero for dotOffset split offset
                                newOffsets[1] = markOffsetAndBias;
                                // newOffsets[2] - leave zero for markOffset split offset
                                int newI = 3;
                                for (int j = 0; j < i; j++) {
                                    newOffsets[newI++] = offsets[j];
                                    // newOffsets[newI] - leave zero
                                    newI++;
                                }
                                markOffsetAndBias = COMPLEX_POSITIONS_MARKER;
                                offsets = newOffsets;
                                i = newI;
                                // Rescan caret at caretIndex again
                                break;
                            } else {
                                offsets[i++] = toOffsetAndBias(dotPos.getOffset(), dotBias);
                                offsets[i++] = toOffsetAndBias(markPos.getOffset(), markBias);
                            }
                        }
                    } else { // Complex pos directly in first caret
                        offsets = new int[(caretsSize << 2) - 1];
                        offsets[0] = dotSplitOffset;
                        offsets[1] = markOffsetAndBias;
                        offsets[2] = markSplitOffset;
                        markOffsetAndBias = COMPLEX_POSITIONS_MARKER;
                        i = 3;
                    }
                    // Possibly complete any remaining carets
                    for (; caretIndex < caretsSize; caretIndex++) {
                        caretInfo = carets.get(caretIndex);
                        dotPos = caretInfo.getDotPosition();
                        dotBias = caretInfo.getDotBias();
                        markPos = caretInfo.getMarkPosition();
                        markBias = caretInfo.getMarkBias();
                        offsets[i++] = toOffsetAndBias(dotPos.getOffset(), dotBias);
                        offsets[i++] = toOffsetAndBias(markPos.getOffset(), markBias);
                        offsets[i++] = ComplexPositions.getSplitOffset(dotPos);
                        offsets[i++] = ComplexPositions.getSplitOffset(markPos);
                    }
                    ret = new CaretUndoEdit.ComplexEdit(doc, dotOffsetAndBias, markOffsetAndBias, offsets);
                }

            } else { // dotPos == null => return null edit
                ret = null;
            }

        } else { // legacy caret
            int dotOffset = caret.getDot();
            int markOffset = caret.getMark();
            if (markOffset != dotOffset) {
                ret = new CaretUndoEdit.ComplexEdit(doc, dotOffset, markOffset, null);
            } else {
                ret = new CaretUndoEdit(doc, dotOffset);
            }
        }
        return ret;
    }
    
    static int toOffsetAndBias(int offset, Position.Bias bias) {
        assert (bias != null) : "Null bias not allowed"; // NOI18N
        if (bias == Position.Bias.Backward) {
            offset |= BACKWARD_BIAS_BIT;
        }
        return offset;
    }

    static int getOffset(int offsetAndBias) {
        return offsetAndBias & ~BACKWARD_BIAS_BIT;
    }
    
    static Position.Bias getBias(int offsetAndBias) {
        return ((offsetAndBias & BACKWARD_BIAS_BIT) != 0)
                ? Position.Bias.Backward
                : Position.Bias.Forward;
    }
    
    static boolean isBackwardBias(int offsetAndBias) {
        return (offsetAndBias & BACKWARD_BIAS_BIT) != 0;
    }

    
    final Document doc; // (16=super)+4=20 bytes
    
    /**
     * Offset of the dot to restore and last bit is a marker for backward bias.
     */
    protected int dotOffsetAndBias; // 24 bytes
    
    CaretUndoEdit(Document doc, int dotOffsetAndBias) {
        Parameters.notNull("doc", doc);
        this.doc = doc;
        this.dotOffsetAndBias = dotOffsetAndBias;
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        restoreCaret();
    }
    
    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        restoreCaret();
    }
    
    @Override
    public boolean isSignificant() {
        return super.isSignificant();
    }
    
    private void restoreCaret() {
        JTextComponent c = EditorRegistry.findComponent(doc);
        if (c != null) {
            Caret caret = c.getCaret();
            if (caret instanceof EditorCaret) {
                try {
                    restoreEditorCaret((EditorCaret) caret);
                } catch (BadLocationException ex) {
                    // Ignore caret restoration
                }
            } else {
                restoreLegacyCaret(caret);
            }
        }
    }

    protected void restoreEditorCaret(EditorCaret caret) throws BadLocationException {
        Position dotPos = doc.createPosition(getOffset(dotOffsetAndBias));
        List<Position.Bias> biases = isBackwardBias(dotOffsetAndBias)
                ? Arrays.asList(Position.Bias.Backward, Position.Bias.Backward)
                : null;
        caret.replaceCarets(Arrays.asList(dotPos, dotPos), biases); // TODO handle biases
    }
    
    protected void restoreLegacyCaret(Caret caret) {
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret)caret).setDot(getOffset(dotOffsetAndBias), getBias(dotOffsetAndBias));
        } else {
            caret.setDot(getOffset(dotOffsetAndBias));
        }
    }


    static final class ComplexEdit extends CaretUndoEdit {

        /**
         * Original offset of the mark to restore together with the bias bit
         * or COMPLEX_POSITIONS_MARKER as a marker value
         * for the case when one or more positions are complex positions
         * - in such case the first item of the array is a split offset
         * for dotOffset and then (markOffset,markSplitOffset) etc.
         */
        int markOffsetAndBias; // 28 bytes
        
        /**
         * Additional regular offsets (with biases) possibly intermixed with split offsets.
         */
        int[] extraOffsets; // 32 bytes
        
        ComplexEdit(Document doc, int dotOffsetAndBias, int markOffsetAndBias, int[] extraOffsets)
        {
            super(doc, dotOffsetAndBias);
            this.markOffsetAndBias = markOffsetAndBias;
            this.extraOffsets = extraOffsets;
        }

        @Override
        protected void restoreEditorCaret(EditorCaret caret) throws BadLocationException {
            List<Position> dotAndMarkPosPairs;
            Position dotPos = doc.createPosition(getOffset(dotOffsetAndBias));
            List<Position.Bias> biases = addBias(null, dotOffsetAndBias, 0);;
            int biasIndex = 1;
            if (markOffsetAndBias != COMPLEX_POSITIONS_MARKER) {
                Position markPos = doc.createPosition(getOffset(markOffsetAndBias));
                biases = addBias(biases, markOffsetAndBias, biasIndex++);
                if (extraOffsets != null) {
                    dotAndMarkPosPairs = new ArrayList<>(2 + extraOffsets.length);
                    dotAndMarkPosPairs.add(dotPos);
                    dotAndMarkPosPairs.add(markPos);
                    for (int i = 0; i < extraOffsets.length; i++) {
                        int offsetAndBias = extraOffsets[i];
                        dotAndMarkPosPairs.add(doc.createPosition(getOffset(offsetAndBias)));
                        biases = addBias(biases, offsetAndBias, biasIndex++);
                    }
                } else {
                    dotAndMarkPosPairs = Arrays.asList(dotPos, markPos);
                }
            } else { // one or more complex positions markOffsetAndBias contains the marker value
                int splitOffset = extraOffsets[0];
                int i = 1;
                dotAndMarkPosPairs = new ArrayList<>((extraOffsets.length + 1) >> 1);
                Position pos = dotPos;
                while (true) {
                    pos = ComplexPositions.create(pos, splitOffset);
                    dotAndMarkPosPairs.add(pos);
                    if (i >= extraOffsets.length) {
                        break;
                    }
                    int offsetAndBias = extraOffsets[i++];
                    splitOffset = extraOffsets[i++];
                    pos = doc.createPosition(getOffset(offsetAndBias));
                    biases = addBias(biases, offsetAndBias, biasIndex++);
                }
            }
            caret.replaceCarets(dotAndMarkPosPairs, biases);
        }

        @Override
        protected void restoreLegacyCaret(Caret caret) {
            int markOffsetAndBias = this.markOffsetAndBias;
            if (markOffsetAndBias == COMPLEX_POSITIONS_MARKER) { // complex positions at time of undo edit creation
                markOffsetAndBias = extraOffsets[1];
            }
            int markOffset = getOffset(markOffsetAndBias);
            int dotOffset = getOffset(dotOffsetAndBias);
            if (caret instanceof DefaultCaret) {
                DefaultCaret dCaret = (DefaultCaret) caret;
                dCaret.setDot(markOffset, getBias(markOffsetAndBias));
                dCaret.moveDot(dotOffset, getBias(dotOffsetAndBias));
            } else {
                caret.setDot(markOffset);
                caret.moveDot(dotOffset);
            }
        }

        static List<Position.Bias> addBias(List<Position.Bias> biases, int offsetAndBias, int index) {
            if ((offsetAndBias & BACKWARD_BIAS_BIT) != 0) {
                if (biases == null) {
                    biases = new ArrayList<>(index + 1);
                    while (--index >= 0) {
                        biases.add(Position.Bias.Forward);
                    }
                }
                biases.add(Position.Bias.Backward);
            } else { // Forward bias
                if (biases != null) {
                    biases.add(Position.Bias.Forward);
                }
            }
            return biases;
        }
    }
    
}
