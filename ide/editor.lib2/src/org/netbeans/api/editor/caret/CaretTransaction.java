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
package org.netbeans.api.editor.caret;

import org.netbeans.spi.editor.caret.CaretMoveHandler;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.document.ComplexPositions;
import org.netbeans.lib.editor.util.GapList;

/**
 * Context passed to caret transaction allowing to create/remove/modify the carets during the transaction.
 *
 * @author Miloslav Metelka
 */
final class CaretTransaction {
    
    // -J-Dorg.netbeans.api.editor.caret.CaretTransaction.level=FINEST
    private static final Logger LOG = Logger.getLogger(CaretTransaction.class.getName());
    
    private final EditorCaret editorCaret;
    
    private final JTextComponent component;
    
    private final Document doc;
    
    /**
     * Original items held here mainly due to latter repaints of the removed items.
     */
    private final GapList<CaretItem> origCaretItems;
    
    private int modIndex;
    
    /**
     * For MOVE_HANDLER and CARETS_REMOVE this is end index of modified area.
     * For CARETS_ADD and MIXED this is not filled.
     */
    private int modEndIndex;
    
    private CaretItem[] addCaretItems;
    
    private int affectedStartIndex = Integer.MAX_VALUE;

    private int affectedEndIndex;

    private int affectedStartOffset = Integer.MAX_VALUE;
    
    private int affectedEndOffset;
    
    private boolean addOrRemove;
    
    private boolean anyDotChanged;

    private boolean anyMarkChanged;
    
    private boolean magicPosChanged;
    
    private boolean scrollToLastCaret;
    
    private GapList<CaretItem> replaceItems;
    
    private GapList<CaretItem> replaceSortedItems;
    
    private GapList<CaretItem> extraRemovedItems;
    
    private GapList<CaretItem> allRemovedItems;
    
    private List<Position> expandFoldPositions;
    
    private int[] indexes;
    
    private int indexesLength;
    
    /**
     * End of area where index hints were updated.
     */
    private int indexHintEnd;
    
    private boolean fullResort;
    
    private final MoveCaretsOrigin origin;
    
    CaretTransaction(EditorCaret caret, JTextComponent component, Document doc, MoveCaretsOrigin origin) {
        this.editorCaret = caret;
        this.component = component;
        this.doc = doc;
        this.origCaretItems = editorCaret.getCaretItems();
        this.origin = origin;
    }
    

    @NonNull EditorCaret getCaret() {
        return editorCaret;
    }
    
    @NonNull JTextComponent getComponent() {
        return component;
    }
    
    @NonNull Document getDocument() {
        return doc;
    }
    
    /**
     * Check for a change in caret structure or dot or mark change but
     * it does not include magic caret position change which is checked by
     * {@link #isMagicPosChange() }.
     *
     * @return true if change happened during transaction.
     */
    boolean isDotOrStructuralChange() {
        return addOrRemove || anyDotChanged || anyMarkChanged;
    }
    
    boolean isAnyDotChanged() {
        return anyDotChanged;
    }
    
    boolean isAnyMarkChanged() {
        return anyMarkChanged;
    }
    
    boolean isMagicPosChange() {
        return magicPosChanged;
    }
    
    boolean moveDot(@NonNull CaretItem caret, @NonNull Position dotPos, @NonNull Position.Bias dotBias) {
        Position markPos = caret.getMarkPosition();
        Position.Bias markBias = caret.getMarkBias();
        if (markPos == null) {
            markPos = caret.getDotPosition();
            markBias = caret.getDotBias();
        }
        return setDotAndMark(caret, dotPos, dotBias, markPos, markBias);
    }

    boolean setDotAndMark(@NonNull CaretItem caretItem, @NonNull Position dotPos, @NonNull Position.Bias dotBias,
            @NonNull Position markPos, @NonNull Position.Bias markBias)
    {
        assert (dotPos != null) : "dotPos must not be null";
        assert (markPos != null) : "markPos must not be null";
        int index = findCaretItemIndex(origCaretItems, caretItem);
        if (index != -1) {
            Position origDotPos = caretItem.getDotPosition();
            Position origMarkPos = caretItem.getMarkPosition();
            boolean dotChanged = origDotPos == null || ComplexPositions.compare(dotPos, origDotPos) != 0 ||
                    dotBias != caretItem.getDotBias();
            boolean markChanged = origMarkPos == null || ComplexPositions.compare(markPos, origMarkPos) != 0 ||
                    markBias != caretItem.getMarkBias();
            scrollToLastCaret = true; // Scroll even if setDot() to same offset
            if (dotChanged || markChanged) {
                editorCaret.ensureValidInfo(caretItem);
                if (expandFoldPositions == null) {
                    expandFoldPositions = new ArrayList<>(2);
                }
                if (dotChanged) {
                    caretItem.setDotPos(dotPos);
                    expandFoldPositions.add(dotPos);
                    anyDotChanged = true;
                }
                if (markChanged) {
                    caretItem.setMarkPos(markPos);
                    expandFoldPositions.add(markPos);
                    anyMarkChanged = true;
                }
                updateAffectedIndexes(index, index + 1);
                caretItem.markUpdateCaretBounds();
                caretItem.markInfoObsolete();
                return true;
            }
            return false;
        }
        return false;
        //caret.setDotCaret(offset, this, true);
    }
    
    boolean setMagicCaretPosition(@NonNull CaretItem caretItem, Point p) {
        int index = findCaretItemIndex(origCaretItems, caretItem);
        if (index != -1) {
            caretItem.setMagicCaretPosition(p);
            magicPosChanged = true;
            caretItem.markInfoObsolete();
            updateAffectedIndexes(index, index + 1);
            return true;
        }
        return false;
    }
    
    void documentInsertAtZeroOffset(int insertEndOffset) {
        // Nested insert inside active transaction for caret moving
        // Since carets may already be moved - do the operations with CaretItems directly
        Position insertEndPos = null;
        for (CaretItem caretItem : editorCaret.getSortedCaretItems()) {
            Position dotPos = caretItem.getDotPosition();
            Position.Bias dotBias = caretItem.getDotBias();
            boolean modifyDot = (dotPos == null || dotPos.getOffset() == 0);
            Position markPos = caretItem.getMarkPosition();
            Position.Bias markBias = caretItem.getMarkBias();
            boolean modifyMark = (markPos == null || markPos.getOffset() == 0);
            if (modifyDot || modifyMark) {
                if (insertEndPos == null) {
                    try {
                        insertEndPos = doc.createPosition(insertEndOffset);
                    } catch (BadLocationException ex) {
                        // Should never happen
                        return;
                    }
                }
                if (modifyDot) {
                    dotPos = insertEndPos;
                    // current impl retains dotBias
                }
                if (modifyMark) {
                    markPos = insertEndPos;
                    // current impl retains markBias
                }
                setDotAndMark(caretItem, dotPos, dotBias, markPos, markBias);
            }
            // Do not break the loop when caret's pos is above zero offset
            // since the carets may be already moved during the transaction
            // - possibly to offset zero. But there could be optimization
            // at least scan position of only the caret items that were modified and not others.
        }
        if (insertEndPos != null) {
            updateAffectedOffsets(0, insertEndOffset); // TODO isn't this extra work that setDotAndMark() already did??
            fullResort = true;
        }
    }
    
    void documentRemove(int offset) {
        fullResort = true; // TODO modify to more specific update
    }

    void handleCaretRemove(@NonNull CaretInfo caret) {
        
    }

    GapList<CaretItem> getReplaceItems() {
        return replaceItems;
    }
    
    GapList<CaretItem> getSortedCaretItems() {
        return replaceSortedItems;
    }

    public boolean isScrollToLastCaret() {
        return scrollToLastCaret;
    }
    
    List<CaretInfo> getOriginalCarets() {
        // Current impl ignores possible replaceItems content since client transactions only operate over
        // original infos from editorCaret. Internal transactions know the type of transaction
        // that was performed so they will skip carets possibly removed by the transaction.
        return editorCaret.getCarets();
    }

    List<CaretInfo> getOriginalSortedCarets() {
        // Current impl ignores possible replaceItems content - see getOriginalCarets()
        return editorCaret.getSortedCarets();
    }
    
    MoveCaretsOrigin getOrigin() {
        return origin;
    }

    void replaceCarets(RemoveType removeType, int offset, CaretItem[] addCaretItems) {
        int size = origCaretItems.size();
        switch (removeType) {
            case NO_REMOVE:
                break;
            case TOGGLE_CARET:
                // Remove existing caret at current dot only when:
                // * at least one caret to add and 
                // * we do not remove the only remaining caret
                if (null != addCaretItems && addCaretItems.length == 1 && origCaretItems.size() >= 2) {
                    CaretItem caretToAdd = addCaretItems[0];
                    int caretFoundAtIndex = -1;
                    for (int i = 0; i < origCaretItems.size(); i++) {
                        CaretItem origCaretItem = origCaretItems.get(i);
                        if (origCaretItem.getDot() == caretToAdd.getDot()
                                && origCaretItem.getMark() == caretToAdd.getMark()) {
                            // so need to remove the current caret
                            caretFoundAtIndex = i;
                            break;
                        } 
                    }
                    if (caretFoundAtIndex != -1) {
                        // remove the caret
                        modIndex = caretFoundAtIndex;
                        modEndIndex = Math.min(caretFoundAtIndex + 1, size);
                        addOrRemove = true;
                        // clear the "add new carets" field
                        addCaretItems = null;
                    } else {
                        // add the given caret based on addCaretItems
                    }
                } else {
                    // add the given caret based on addCaretItems
                }
                break;
            case REMOVE_LAST_CARET:
                if (size > 1) {
                    modIndex = size - 1;
                    modEndIndex = size;
                    addOrRemove = true;
                }
                break;
            case RETAIN_LAST_CARET:
                if (size > 1) {
                    modEndIndex = size - 1;
                    addOrRemove = true;
                }
                break;
            case REMOVE_ALL_CARETS:
                if (size > 0) {
                    modEndIndex = size;
                    addOrRemove = true;
                }
                break;
            case DOCUMENT_REMOVE:
                break;
            case DOCUMENT_INSERT_ZERO_OFFSET:
                documentInsertAtZeroOffset(offset);
                break;

            default:
                throw new AssertionError("Unhandled removeType=" + removeType); // NOI18N
        }
        if (addCaretItems != null) {
            this.addCaretItems = addCaretItems;
            addOrRemove = true;
        }
        if (addOrRemove) {
            scrollToLastCaret = true;
        }
    }
    
    void runCaretMoveHandler(CaretMoveHandler handler) {
        CaretMoveContext context = new CaretMoveContext(this);
        handler.moveCarets(context);
    }
    
    void removeOverlappingRegions() {
        removeOverlappingRegions(0, Integer.MAX_VALUE);
    }

    void removeOverlappingRegions(int removeOffset) {
        removeOverlappingRegions(0, removeOffset); // TODO compute startIndex by binary search
    }
    
    void removeOverlappingRegions(int startIndex, int stopOffset) {
        if (addOrRemove) {
            initReplaceItems();
        } else if (anyDotChanged || anyMarkChanged) {
            initReplaceItems(); // TODO optimize for low number of changed items
        }
        GapList<CaretItem> origSortedItems = (replaceSortedItems != null)
                ? replaceSortedItems
                : editorCaret.getSortedCaretItems();
        int origSortedItemsSize = origSortedItems.size();
        GapList<CaretItem> nonOverlappingItems = null;
        int copyStartIndex = 0;
        int i = startIndex - 1;
        boolean itemsRemoved = false;
        CaretItemInfo lastInfo = new CaretItemInfo();
        if (i >= 0) {
            lastInfo.update(origSortedItems.get(i));
        } // Otherwise leave the default zeros in lastInfo
        
        CaretItemInfo itemInfo = new CaretItemInfo();
        while (++i < origSortedItemsSize) {
            itemInfo.update(origSortedItems.get(i));
            if (lastInfo.caretItem != null) { // If lastInfo is valid
                if (itemInfo.overlapsAtStart(lastInfo)) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("CaretTransaction.removeOverlappingRegions(): [" + i + "]: overlap at start of itemInfo=" + // NOI18N
                                itemInfo + "\n  with lastInfo=" + lastInfo + "\n"); // NOI18N
                    }
                    if (nonOverlappingItems == null) {
                        nonOverlappingItems = new GapList<CaretItem>(origSortedItemsSize - 1); // At least one will be skipped
                    }
                    itemsRemoved = true;
                    // Determine type of overlap
                    if (!lastInfo.dotAtStart) { // Caret of lastInfo moved into next block
                        if (lastInfo.startsBelow(itemInfo)) {
                            // Extend selection of itemInfo to start of lastInfo
                            updateAffectedOffsets(lastInfo.startOffset, itemInfo.startOffset);
                            setDotAndMark(itemInfo.caretItem, lastInfo.startPos, lastInfo.startBias, itemInfo.endPos, itemInfo.endBias);
                        }
                        // Remove lastInfo's caret item
                        lastInfo.caretItem.markRemovedInTransaction();
                        origSortedItems.copyElements(copyStartIndex, i - 1, nonOverlappingItems);
                        copyStartIndex = i;

                    } else { // Remove itemInfo and set selection of lastInfo to end of itemInfo
                        if (itemInfo.endsAbove(lastInfo)) {
                            updateAffectedOffsets(lastInfo.endOffset, itemInfo.endOffset);
                            setDotAndMark(lastInfo.caretItem, lastInfo.startPos, lastInfo.startBias, itemInfo.endPos, itemInfo.endBias);
                        }
                        // Remove itemInfo's caret item
                        itemInfo.caretItem.markRemovedInTransaction();
                        origSortedItems.copyElements(copyStartIndex, i, nonOverlappingItems);
                        copyStartIndex = i + 1;
                    }

                } else if (itemInfo.dotsOverlap(lastInfo)) { // Check whether dots of lastInfo and itemInfo are at same offset (and shift).
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("CaretTransaction.removeOverlappingRegions(): [" + i + "]: dots overlap: itemInfo=" + // NOI18N
                                itemInfo + "\n  with lastInfo=" + lastInfo + "\n"); // NOI18N
                    }
                    if (nonOverlappingItems == null) {
                        nonOverlappingItems = new GapList<CaretItem>(origSortedItemsSize - 1); // At least one will be skipped
                    }
                    itemsRemoved = true;
                    if (itemInfo.selection) {
                        if (itemInfo.dotAtStart) {
                            // Likely lastInfo is below itemInfo so their dots "touch" at itemInfo start
                            setDotAndMark(lastInfo.caretItem, itemInfo.endPos, itemInfo.endBias, lastInfo.startPos, lastInfo.startBias);
                            // Remove itemInfo's caret item
                            itemInfo.caretItem.markRemovedInTransaction();
                            origSortedItems.copyElements(copyStartIndex, i, nonOverlappingItems);
                            copyStartIndex = i + 1;
                        } else {
                            // Likely lastInfo is above itemInfo it so their dots "touch" at itemInfo end
                            setDotAndMark(itemInfo.caretItem, lastInfo.endPos, lastInfo.endBias, itemInfo.startPos, itemInfo.startBias);
                            // Remove lastInfo's caret item
                            lastInfo.caretItem.markRemovedInTransaction();
                            origSortedItems.copyElements(copyStartIndex, i - 1, nonOverlappingItems);
                            copyStartIndex = i;
                        }
                    } else { // itemInfo has no selection => remove itemInfo
                        // Remove itemInfo's caret item
                        itemInfo.caretItem.markRemovedInTransaction();
                        origSortedItems.copyElements(copyStartIndex, i, nonOverlappingItems);
                        copyStartIndex = i + 1;
                    }
                }
            }

            // Swap the items to reuse original lastInfo
            CaretItemInfo tmp = lastInfo;
            lastInfo = itemInfo;
            itemInfo = tmp;
            if (lastInfo.endOffset > stopOffset) {
                break;
            }
        }

        if (itemsRemoved) { // At least one item removed
            if (copyStartIndex < origSortedItemsSize) {
                origSortedItems.copyElements(copyStartIndex, origSortedItemsSize, nonOverlappingItems);
            }
            GapList<CaretItem> origItems = resultItems();
            int origItemsSize = origItems.size();
            replaceItems = new GapList<>(origItemsSize);
            for (i = 0; i < origItemsSize; i++) {
                CaretItem caretItem = origItems.get(i);
                if (caretItem.getAndClearRemovedInTransaction()) {
                    if (extraRemovedItems == null) {
                        extraRemovedItems = new GapList<>();
                    }
                    extraRemovedItems.add(caretItem);
                } else {
                    replaceItems.add(caretItem);
                }
            }
            replaceSortedItems = nonOverlappingItems;
        }
    }
    
    GapList<CaretItem> allRemovedItems() {
        int removeSize = modEndIndex - modIndex;
        int extraRemovedSize = (extraRemovedItems != null) ? extraRemovedItems.size() : 0;
        if (removeSize + extraRemovedSize > 0) {
            if (allRemovedItems == null) {
                allRemovedItems = new GapList<>(removeSize + extraRemovedSize);
                if (removeSize > 0) {
                    allRemovedItems.addAll(origCaretItems, modIndex, removeSize);
                }
                if (extraRemovedSize > 0) {
                    allRemovedItems.addAll(extraRemovedItems);
                }
            }
        }
        return allRemovedItems;
    }
    
    List<Position> expandFoldPositions() {
        return expandFoldPositions;
    }
    
    private GapList<CaretItem> resultItems() {
        return (replaceItems != null) ? replaceItems : origCaretItems;
    }

    private void initReplaceItems() {
        assert (replaceItems == null) : "replaceItems already inited to " + replaceItems; // NOI18N
        int size = origCaretItems.size();
        int removeSize = modEndIndex - modIndex;
        int addSize = (addCaretItems != null) ? addCaretItems.length : 0;
        int newSize = size - removeSize + addSize;
        replaceItems = new GapList<>(newSize);
        if (removeSize > 0) {
            replaceItems.addAll(origCaretItems, 0, modIndex);
            replaceItems.addAll(origCaretItems, modEndIndex, size - modEndIndex);
        } else {
            replaceItems.addAll(origCaretItems);
        }
        if (addCaretItems != null) {
            replaceItems.addArray(replaceItems.size(), addCaretItems);
        }

        assert (replaceItems.size() == newSize);
        boolean updateIndividual = (removeSize + addSize) < (newSize >> 2); // Threshold 1/4 of total size for full resort
        if (fullResort || true) { // Force full resort
            replaceSortedItems = replaceItems.copy();
            if (newSize > 1) {
                Collections.sort(replaceSortedItems);
            }
        } else { // Partial resort TODO
            
        }
    }

    private void resetIndexes() {
        indexesLength = 0;
    }
    
    private void addToIndexes(int index) {
        if (indexes == null) {
            indexes = new int[8];
        } else if (indexesLength == indexes.length) {
            int[] orig = indexes;
            indexes = new int[indexesLength << 1];
            System.arraycopy(orig, 0, indexes, 0, indexesLength);
        }
        indexes[indexesLength++] = index;
    }
    
    
    private int findCaretItemIndex(GapList<CaretItem> caretItems, CaretItem caretItem) {
        // Method only resolves existing items not added items
        int i = caretItem.getTransactionIndexHint();
        int size = caretItems.size();
        if (i >= size || caretItems.get(i) != caretItem) {
            while (indexHintEnd < size) {
                CaretItem c = caretItems.get(indexHintEnd);
                c.setTransactionIndexHint(indexHintEnd++);
                if (c == caretItem) {
                    return indexHintEnd - 1;
                }
            }
            return -1;
        }
        return i;
    }
    
    private void updateAffectedIndexes(int startIndex, int endIndex) {
        if (affectedStartIndex == Integer.MAX_VALUE) {
            affectedStartIndex = startIndex;
            affectedEndIndex = endIndex;
        } else {
            affectedStartIndex = Math.min(affectedStartIndex, startIndex);
            affectedEndIndex = Math.max(affectedEndIndex, endIndex);
        }
    }

    private void updateAffectedOffsets(int startOffset, int endOffset) {
        if (affectedStartOffset == Integer.MAX_VALUE) { // Affected range not inited yet
            affectedStartOffset = startOffset;
            affectedEndOffset = endOffset;
        } else { // Affected range already inited
            if (startOffset < affectedStartOffset) {
                affectedStartOffset = startOffset;
            }
            if (endOffset > affectedEndOffset) {
                affectedEndOffset = endOffset;
            }
        }
    }

    static CaretItem[] asCaretItems(EditorCaret caret, @NonNull List<Position> dotAndSelectionStartPosPairs,
            List<Position.Bias> dotAndMarkBiases)
    {
        int size = dotAndSelectionStartPosPairs.size();
        if ((size & 1) != 0) {
            throw new IllegalStateException("Passed list has size=" + size + " which is not an even number.");
        }
        CaretItem[] addedCarets = new CaretItem[size >> 1];
        int listIndex = 0;
        for (int j = 0; j < addedCarets.length; j++) {
            Position.Bias dotBias = (dotAndMarkBiases != null) ? dotAndMarkBiases.get(listIndex) : Position.Bias.Forward;
            Position dotPos = dotAndSelectionStartPosPairs.get(listIndex++);

            Position.Bias markBias = (dotAndMarkBiases != null) ? dotAndMarkBiases.get(listIndex) : Position.Bias.Forward;
            Position selectionStartPos = dotAndSelectionStartPosPairs.get(listIndex++);

            CaretItem caretItem = new CaretItem(caret, dotPos, dotBias, selectionStartPos, markBias);
            addedCarets[j] = caretItem;
        }
        return addedCarets;
    }

    enum RemoveType {
        NO_REMOVE,
        REMOVE_LAST_CARET,
        RETAIN_LAST_CARET,
        REMOVE_ALL_CARETS,
        DOCUMENT_REMOVE,
        DOCUMENT_INSERT_ZERO_OFFSET,
        TOGGLE_CARET
    }
    
    /**
     * Helper class for resolving overlapping caret selections.
     */
    private static final class CaretItemInfo {
        
        CaretItem caretItem;
        
        Position startPos;
        
        Position.Bias startBias;
        
        Position endPos;
        
        Position.Bias endBias;
        
        int dotOffset;
        
        int dotShift;

        int startOffset;

        int startShift;
        
        int endOffset;

        int endShift;
        
        /**
         * True for non empty selection and dot pos-and-shift precedes mark pos-and-shift.
         */
        boolean dotAtStart;
        
        /**
         * True for non-empty selection.
         */
        boolean selection;
        
        void update(CaretItem caret) {
            this.caretItem = caret;
            Position dotPos = caret.getDotPosition();
            if (dotPos != null) {
                dotOffset = dotPos.getOffset();
                dotShift = ComplexPositions.getSplitOffset(dotPos);
                Position markPos = caret.getMarkPosition();
                if (markPos != null && markPos != dotPos) { // Still they may be equal which means no selection
                    int markOffset = markPos.getOffset();
                    int markShift = ComplexPositions.getSplitOffset(markPos);
                    if (markOffset < dotOffset || (markOffset == dotOffset && markShift < dotShift)) {
                        startPos = markPos;
                        startBias = caret.getMarkBias();
                        endPos = dotPos;
                        endBias = caret.getDotBias();
                        startOffset = markOffset;
                        startShift = markShift;
                        endOffset = dotOffset;
                        endShift = dotShift;
                        dotAtStart = false;
                        selection = true;
                    } else {
                        if (markOffset == dotOffset && markShift == dotShift) { // No selection
                            startPos = markPos;
                            startBias = caret.getMarkBias();
                            endPos = dotPos;
                            endBias = caret.getDotBias();
                            startOffset = markOffset;
                            startShift = markShift;
                            endOffset = dotOffset;
                            endShift = dotShift;
                            dotAtStart = false;
                            selection = false;
                        } else {
                            startPos = dotPos;
                            startBias = caret.getDotBias();
                            endPos = markPos;
                            endBias = caret.getMarkBias();
                            startOffset = dotOffset;
                            startShift = dotShift;
                            endOffset = markOffset;
                            endShift = markShift;
                            dotAtStart = true;
                            selection = true;
                        }
                        
                    }
                } else {
                    startPos = endPos = dotPos;
                    startBias = endBias = caret.getDotBias();
                    startOffset = endOffset = dotOffset;
                    startShift = startShift = dotShift;
                    dotAtStart = false;
                    selection = false;
                }
            } else {
                clear();
            }
        }

        void clear() {
            caretItem = null;
            dotOffset = dotShift = 0;
            startPos = endPos = null;
            startOffset = endOffset = 0;
            startShift = startShift = 0;
            dotAtStart = false;
            selection = false;
        }
        
        boolean overlapsAtStart(CaretItemInfo info) {
            return (ComplexPositions.compare(info.endOffset, info.endShift,
                    startOffset, startShift) > 0);
        }
        
        boolean startsBelow(CaretItemInfo info) {
            return (ComplexPositions.compare(startOffset, startShift,
                    info.startOffset, info.startShift) < 0);
        }
        
        boolean endsAbove(CaretItemInfo info) {
            return (ComplexPositions.compare(endOffset, endShift,
                    info.endOffset, info.endShift) > 0);
        }
        
        boolean dotsOverlap(CaretItemInfo info) {
            return (ComplexPositions.compare(dotOffset, dotShift,
                    info.dotOffset, info.dotShift) == 0);
        }

        @Override
        public String toString() {
            return "DOS(" + dotOffset + "," + dotShift + ") <(" + startOffset + "," + startShift + "), (" + // NOI18N
                    endOffset + "," + endShift + ")> dotAtStart=" + dotAtStart + ", selection=" + selection + // NOI18N
                    "\n    caretItem=" + caretItem; // NOI18N
        }
        
    }

}
