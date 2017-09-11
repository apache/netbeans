/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
