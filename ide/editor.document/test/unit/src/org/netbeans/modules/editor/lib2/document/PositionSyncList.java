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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.netbeans.modules.editor.lib2.document.ExpectedDocument;
import org.netbeans.modules.editor.lib2.document.TestEditorDocument;

/**
 * Sync positions creation in both tested and expected document.
 * <br/>
 * Since the tested document (and also swing document impls) use position sharing
 * for repetitive positions creations at the same offset the position creation
 * for both tested and expected document must comply to certain rules.
 * <br/>
 * Pairs of tested and expected positions are created upon expected position creation.
 * However there can be a following scenario:
 * 1. Tested pos is created without pair creation.
 * 2. Removal occurs with the tested pos inside the removed area.
 * 3. Expected position gets created (so pair gets created).
 * 4. Undo occurs. Expected position gets undone to the end of removed area while
 *  tested pos gets its original position before removal.
 * <br/>
 * Therefore the pair must be created immediately for each tested position creation
 * in that way both positions will have the same "undo history".
 *
 * @author Miloslav Metelka
 */
class PositionSyncList {
    
    private List<PositionPair> positionPairList;
    
    private Map<Position, PositionPair> testPos2pair;
    
    private TestEditorDocument testDoc;

    private final ExpectedDocument expectedDoc;
    
    private MayDifferUndoItem mayDifferUndoItem;
    
    PositionSyncList(ExpectedDocument expectedDoc) {
        this.expectedDoc = expectedDoc;
        positionPairList = new ArrayList<PositionPair>();
        testPos2pair = new HashMap<Position, PositionPair>();
    }

    public void setTestDocument(TestEditorDocument testDoc) {
        this.testDoc = testDoc;
    }

    synchronized int size() {
        return positionPairList.size();
    }

    synchronized void clear() {
        positionPairList.clear();
        testPos2pair.clear();
    }
    
    Position createSyncedTestPosition(int offset, boolean backwardBias) throws BadLocationException {
        Position testPos = backwardBias
                ? testDoc.createBackwardBiasPosition(offset)
                : testDoc.createPosition(offset);
        return findOrCreatePair(testPos, backwardBias).testPos;
    }
    
    synchronized PositionPair findOrCreatePair(Position testPos, boolean backwardBias) throws BadLocationException {
        PositionPair pair = testPos2pair.get(testPos);
        if (pair == null) {
            Position expectedPos = backwardBias
                    ? expectedDoc.createBackwardBiasPosition(testPos.getOffset())
                    : expectedDoc.createPosition(testPos.getOffset());
            pair = new PositionPair(testPos, expectedPos);
            if (mayDifferUndoItem != null) {
                mayDifferUndoItem.addPair(pair);
            }
            addPair(pair);
        }
        return pair;
    }

    private void addPair(PositionPair pair) {
        positionPairList.add(pair);
        testPos2pair.put(pair.testPos, pair);
    }

    void removePair(int index) {
        PositionPair pair = positionPairList.remove(index);
        testPos2pair.remove(pair.testPos);
    }

    void releaseTestPos(Position testPos) {
        boolean found = false;
        for (int i = positionPairList.size() - 1; i >= 0; i--) {
            PositionPair pair = positionPairList.get(i);
            if (pair.testPos == testPos) {
                found = true;
                removePair(i);
                break;
            }
        }
        assert found : "Test Position testPos=" + testPos + " not found in PositionPairList"; // NOI18N
    }

    void insertUpdate(DefaultDocumentEvent chng) {
        updateMayDifferItem(chng);
    }

    void removeUpdate(DefaultDocumentEvent chng) {
        updateMayDifferItem(chng);
    }
    
    private void updateMayDifferItem(DefaultDocumentEvent chng) {
        MayDifferUndoItem item = new MayDifferUndoItem();
        chng.addEdit(item);
    }

    void checkConsistency() {
        for (int i = 0; i < positionPairList.size(); i++) {
            PositionPair positionPair = positionPairList.get(i);
            if (!positionPair.mayDiffer) {
                int expOffset = positionPair.expectedPos.getOffset();
                int offset = positionPair.testPos.getOffset();
                if (expOffset != offset) {
                    assert false : "[" + i + "]: expOffset=" + expOffset + " != offset=" + offset
                            + ", pair: " + positionPair + ", content:\n"
                            + ((TestEditorDocument) testDoc).getDocumentContent().toStringDetail();
                }
            }
        }
    }


    private static class PositionPair {

        final Position testPos;

        final Position expectedPos;
        
        /**
         * Due to position sharing implemented by both NB and Swing document impls
         * a position may be created that has a "removals undo history". If position
         * laid inside a region being removed it will be moved to its start upon remove.
         * However upon undo the position will restore its position inside region
         * that was removed. However its freshly created mirror expectedPos is not aware
         * of the undo history and it will restore to the region end.
         * Therefore this flag will be set to true once a modification that precedes
         * position's creation gets undone. There's a special edit added
         * by ExpectedDocument impl for this purpose into each modification.
         */
        boolean mayDiffer;
        
        public PositionPair(Position testPos, Position expectedPos) {
            assert (testPos != null);
            assert (expectedPos != null);
            this.testPos = testPos;
            this.expectedPos = expectedPos;
        }

        @Override
        public String toString() {
            return "testPos=" + testPos + ", expectedPos=" + expectedPos + ", mayDiffer=" + mayDiffer; // NOI18N
        }

    }

    private final class MayDifferUndoItem extends AbstractUndoableEdit {
        
        private MayDifferUndoItem previousItem;
        
        private List<PositionPair> mayDifferPairs;
        
        MayDifferUndoItem() {
            this.previousItem = mayDifferUndoItem;
            mayDifferUndoItem = this;
        }
        
        synchronized void addPair(PositionPair pair) {
            if (mayDifferPairs == null) {
                mayDifferPairs = new ArrayList<>();
            }
            mayDifferPairs.add(pair);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            if (mayDifferPairs != null) {
                for (PositionPair pair : mayDifferPairs) {
                    assert (!pair.mayDiffer) : "Invalid pair: " + pair; // NOI18N
                    pair.mayDiffer = true;
                }
            }
            assert (mayDifferUndoItem == this) : "Invalid mayDifferUndoItem=" + mayDifferUndoItem;
            mayDifferUndoItem = previousItem;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            if (mayDifferPairs != null) {
                for (PositionPair pair : mayDifferPairs) {
                    assert (pair.mayDiffer) : "Invalid pair: " + pair; // NOI18N
                    pair.mayDiffer = false;
                }
            }
            mayDifferUndoItem = this;
        }

        @Override
        public void die() {
            previousItem = null;
        }
        
    }

}
