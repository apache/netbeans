/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor.caret;

import java.awt.Point;
import java.util.logging.Logger;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Immutable info about a single caret used in caret API - see {@link EditorCaret}.
 * <br>
 * There is one-to-one reference between immutable caret info and mutable {@link CaretItem}.
 * CaretItem is not accessible through the caret API and it's managed privately by EditorCaret.
 * Once the caret item gets mutated its corresponding caret info becomes obsolete
 * and new caret info instance gets created lazily.
 *
 * @author Miloslav Metelka
 * @author Ralph Ruijs
 * @see EditorCaret
 * @since 2.6
 */
public final class CaretInfo {
    
    // -J-Dorg.netbeans.api.editor.CaretInfo.level=FINEST
    private static final Logger LOG = Logger.getLogger(CaretInfo.class.getName());
    
    private final CaretItem caretItem;

    private final Position dotPos;
    
    private final Position.Bias dotBias;

    private final Position markPos;
    
    private final Position.Bias markBias;

    private final Point magicCaretPosition;

    CaretInfo(CaretItem caretItem) {
        this.caretItem = caretItem;
        this.dotPos = caretItem.getDotPosition();
        this.dotBias = caretItem.getDotBias();
        this.markPos = caretItem.getMarkPosition();
        this.markBias = caretItem.getMarkBias();
        this.magicCaretPosition = caretItem.getMagicCaretPosition();
    }

    /**
     * Get position of the caret itself.
     * @return non-null position of the caret placement. The position may be virtual
     *  so methods in {@link org.netbeans.api.editor.document.ComplexPositions} may be used if necessary.
     */
    @CheckForNull
    public Position getDotPosition() {
        return dotPos;
    }

    /**
     * Get a bias of the dot position which is either
     * {@link Position.Bias.Forward} or {@link Position.Bias.Backward} depending
     * on whether the caret biases towards the next character or previous one.
     * The bias is always forward for non bidirectional text.
     *
     * @return either forward or backward bias.
     * @since 2.12
     */
    @NonNull
    public Position.Bias getDotBias() {
        return dotBias;
    }

    /**
     * Return either the same object like {@link #getDotPosition()} if there's no selection
     * or return position denoting the other end of an existing selection (which is either before
     * or after the dot position depending of how the selection was created).
     * @return non-null position of the caret placement. The position may be virtual
     *  so methods in {@link org.netbeans.api.editor.document.ComplexPositions} may be used if necessary.
     */
    @CheckForNull
    public Position getMarkPosition() {
        return markPos;
    }
    
    /**
     * Get a bias of the mark position which is either
     * {@link Position.Bias.Forward} or {@link Position.Bias.Backward} depending
     * on whether the caret biases towards the next character or previous one.
     * The bias is always forward for non bidirectional text.
     *
     * @return either forward or backward bias.
     * @since 2.12
     */
    @NonNull
    public Position.Bias getMarkBias() {
        return markBias;
    }

    /**
     * Fetches the current position of the caret.
     *
     * @return the position &gt;=0
     */
    public int getDot() {
        return (dotPos != null) ? dotPos.getOffset() : 0;
    }

    /**
     * Fetches the current position of the mark.  If there
     * is a selection, the mark will not be the same as
     * the dot.
     *
     * @return the position &gt;=0
     */
    public int getMark() {
        return (markPos != null) ? markPos.getOffset() : getDot();
    }
    
    /**
     * Determines if there currently is a selection.
     * 
     * @return true if there's a selection or false if there's no selection for this caret.
     */
    public boolean isSelection() {
        return (dotPos != null && markPos != null &&
                markPos != dotPos && dotPos.getOffset() != markPos.getOffset());
    }
    
    /**
     * Determines if the selection is currently visible.
     * 
     * @return true if both {@link #isSelection() } and {@link EditorCaret#isSelectionVisible() } are true.
     */
    public boolean isSelectionShowing() {
        return caretItem.editorCaret().isSelectionVisible() && isSelection();
    }

    /**
     * Returns the selected text's start position.  Return 0 for an
     * empty document, or the value of dot if no selection.
     *
     * @return the start position &ge; 0
     */
    public int getSelectionStart() {
        return Math.min(getDot(), getMark());
    }

    /**
     * Returns the selected text's end position.  Return 0 if the document
     * is empty, or the value of dot if there is no selection.
     *
     * @return the end position &ge; 0
     */
    public int getSelectionEnd() {
        return Math.max(getDot(), getMark());
    }
    
    /**
     * Gets the current caret visual location.
     *
     * @return the visual position.
     */
    public @CheckForNull Point getMagicCaretPosition() {
        return magicCaretPosition;
    }
    
    CaretItem getCaretItem() {
        return caretItem;
    }
    
    @Override
    public String toString() {
        return "dotPos=" + dotPos + ", markPos=" + markPos + ", magicCaretPosition=" + magicCaretPosition + // NOI18N
                "\n  caretItem=" + caretItem; // NOI18N
    }
    
}
