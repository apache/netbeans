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
package org.netbeans.api.editor.caret;

import java.awt.Point;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Context for carets moving within {@link org.netbeans.spi.editor.caret.CaretMoveHandler}.
 *
 * @author Miloslav Metelka
 * @since 2.6
 */
public final class CaretMoveContext {
    
    private CaretTransaction transaction;
    
    CaretMoveContext(CaretTransaction transactionContext) {
        this.transaction = transactionContext;
    }

    /**
     * Get list of carets at the time when transaction started.
     * <br>
     * <b>Note</b>: information contained in the returned list will not reflect changes
     * performed by the dot/selection modification methods contained in this class.
     * It will always return the original state from the beginning of the move transaction.
     * All the performed modifications will be incorporated at the end of the move transaction.
     *
     * @return list of carets at the time when moving transaction has started.
     */
    public @NonNull List<CaretInfo> getOriginalCarets() {
        return transaction.getOriginalCarets();
    }
    
    /**
     * Get last item from the list returned by {@link #getOriginalCarets()}.
     * @return last caret at the time when caret moving transaction has started.
     * @see #getOriginalCarets()
     */
    public @NonNull CaretInfo getOriginalLastCaret() {
        List<CaretInfo> origCarets = getOriginalCarets();
        return origCarets.get(origCarets.size() - 1);
    }
    
    /**
     * Get list of carets at the time when transaction started
     * sorted by dot positions in ascending order.
     * <br>
     * If some of the carets are {@link org.netbeans.api.editor.document.ComplexPositions}
     * their order will reflect the increasing split offset.
     * <br>
     * <b>Note</b>: information contained in the returned list will not reflect changes
     * performed by the dot/selection modification methods contained in this class.
     * It will always return the original state from the beginning of the move transaction.
     * All the performed modifications will be incorporated at the end of the move transaction.
     *
     * @return list of carets at the time when caret moving transaction has started.
     */
    public @NonNull List<CaretInfo> getOriginalSortedCarets() {
        return transaction.getOriginalSortedCarets();
    }
    
    /**
     * Change dot of the given caret.
     *
     * @param caret non-null caret.
     * @param dotPos new dot position.
     * @return false if passed caret is obsolete or invalid (e.g. a member of another {@link EditorCaret})
     *  or true otherwise.
     */
    public boolean setDot(@NonNull CaretInfo caret, @NonNull Position dotPos, @NonNull Position.Bias dotBias) {
        NavigationFilter naviFilter = transaction.getCaret().getNavigationFilterNoDefault(transaction.getOrigin());
        if (naviFilter != null) {
            FilterBypassImpl fbi = new FilterBypassImpl(transaction, caret, transaction.getDocument());
            naviFilter.setDot(fbi, dotPos.getOffset(), Position.Bias.Forward);
            return fbi.getResult();
        } else {
            return setDotAndMark(caret, dotPos, dotBias, dotPos, dotBias);
        }
    }
    
    /**
     * Move dot of the given caret so caret selection gets created or changed.
     *
     * @param caret non-null caret.
     * @param dotPos new dot position.
     * @return false if passed caret is obsolete or invalid (e.g. a member of another {@link EditorCaret})
     *  or true otherwise.
     */
    public boolean moveDot(@NonNull CaretInfo caret, @NonNull Position dotPos, @NonNull Position.Bias dotBias) {
        NavigationFilter naviFilter = transaction.getCaret().getNavigationFilterNoDefault(transaction.getOrigin());
        if (naviFilter != null) {
            FilterBypassImpl fbi = new FilterBypassImpl(transaction, caret, transaction.getDocument());
            naviFilter.moveDot(fbi, dotPos.getOffset(), Position.Bias.Forward);
            return fbi.getResult();
        } else {
            return transaction.moveDot(caret.getCaretItem(), dotPos, dotBias);
        }
    }
    
    /**
     * Move dot of the given caret so caret selection gets created or changed.
     *
     * @param caret non-null caret.
     * @param dotPos new dot position.
     * @param markPos starting position of the selection or the same position like dotPos if there should be no selection.
     *  <br>
     *  The position may point to a lower offset than dotPos in case the selection
     *  should extend from a higher offset to a lower offset.
     * @return false if passed caret is obsolete or invalid (e.g. a member of another {@link EditorCaret})
     *  or true otherwise.
     */
    public boolean setDotAndMark(@NonNull CaretInfo caret, @NonNull Position dotPos, @NonNull Position.Bias dotBias,
            @NonNull Position markPos, @NonNull Position.Bias markBias) {
        return transaction.setDotAndMark(caret.getCaretItem(), dotPos, dotBias, markPos, markBias);
    }

    /**
     * Set magic caret position of the given caret.
     *
     * @param caret non-null caret.
     * @param p new magic caret position.
     * @return false if passed caret is obsolete or invalid (e.g. a member of another {@link EditorCaret})
     *  or true otherwise.
     */
    public boolean setMagicCaretPosition(@NonNull CaretInfo caret, Point p) {
        return transaction.setMagicCaretPosition(caret.getCaretItem(), p);
    }

    /**
     * Return component in which the caret is currently installed.
     * @return non-null component in which the caret is installed.
     */
    public @NonNull JTextComponent getComponent() {
        return transaction.getComponent();
    }
    
    /**
     * Get document associated with the text component.
     * @return document of the associated component.
     * @see #getComponent()
     */
    public Document getDocument() {
        return getComponent().getDocument();
    }
    
}
