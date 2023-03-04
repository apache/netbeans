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
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 * A single caret inside {@link EditorCaret} handled internally by EditorCaret.
 * <br>
 * For API client methods {@link CaretInfo} class is used. There is one-to-one reference
 * between caret item and caret info. But the info is immutable so once the caret item
 * gets mutated its corresponding caret info becomes obsolete and new info gets created
 * lazily.
 *
 * @author Miloslav Metelka
 */
final class CaretItem implements Comparable {
    
    // -J-Dorg.netbeans.modules.editor.lib2.CaretItem.level=FINEST
    private static final Logger LOG = Logger.getLogger(CaretItem.class.getName());

    private static final int REMOVED_IN_TRANSACTION = 1;
    
    private static final int INFO_OBSOLETE = 2;

    private static final int UPDATE_CARET_BOUNDS = 4;

    private static final int CARET_PAINTED = 8;

    private final EditorCaret editorCaret;
    
    private Position dotPos;

    private Position.Bias dotBias;

    private Position markPos;

    private Position.Bias markBias;

    private Point magicCaretPosition;
    
    /**
     * Last info or null if info became obsolete and should be recomputed.
     */
    private CaretInfo info;
    
    private Rectangle caretBounds;
    
    /**
     * Hint of index of this caret item in replaceCaretItems in transaction context.
     */
    private int transactionIndexHint;
    
    /**
     * Transaction uses this flag to mark this item for removal.
     */
    private int statusBits;

    CaretItem(EditorCaret editorCaret, Position dotPos, Position.Bias dotBias, Position markPos, Position.Bias markBias) {
        this.editorCaret = editorCaret;
        this.dotPos = dotPos;
        this.dotBias = dotBias;
        this.markPos = markPos;
        this.markBias = markBias;
        this.statusBits = UPDATE_CARET_BOUNDS; // Request visual bounds updating automatically
    }
    
    EditorCaret editorCaret() {
        return editorCaret;
    }
    
    void ensureValidInfo() {
        // No explicit locking - locking managed by EditorCaret
        if (info == null) {
            info = new CaretInfo(this);
        }
    }
    
    void clearInfo() {
        // No explicit locking - locking managed by EditorCaret
        this.info = null;
    }

    CaretInfo getValidInfo() {
        ensureValidInfo();
        return info;
    }

//    void clearTransactionInfo() {
//        // No explicit locking - locking managed by EditorCaret
//        this.transactionInfo = null;
//    }
//
//    CaretInfo getValidTransactionInfo() {
//        if (transactionInfo == null) {
//            transactionInfo = new CaretInfo(this);
//        }
//        return transactionInfo;
//    }
    
    /**
     * Get position of the caret itself.
     *
     * @return non-null position of the caret placement. The position may be
     * virtual so methods in {@link org.netbeans.api.editor.document.ComplexPositions} may be used if necessary.
     */
    @CheckForNull
    Position getDotPosition() {
        return dotPos;
    }
    
    /**
     * Get the bias of the dot either forward or backward bias.
     *
     * @return either forward or backward bias.
     */
    @NonNull
    Position.Bias getDotBias() {
        return dotBias;
    }

    /**
     * Return either the same object like {@link #getDotPosition()} if there's
     * no selection or return position denoting the other end of an existing
     * selection (which is either before or after the dot position depending of
     * how the selection was created).
     *
     * @return non-null position of the caret placement. The position may be
     * virtual so methods in {@link org.netbeans.api.editor.document.ComplexPositions} may be used if necessary.
     */
    @CheckForNull
    Position getMarkPosition() {
        return markPos;
    }

    /**
     * Get the bias of the mark either forward or backward bias.
     *
     * @return either forward or backward bias.
     */
    @NonNull
    Position.Bias getMarkBias() {
        return markBias;
    }

    int getDot() {
        return (dotPos != null) ? dotPos.getOffset() : 0;
    }

    int getMark() {
        return (markPos != null) ? markPos.getOffset() : 0;
    }

    /**
     * @return true if there's a selection or false if there's no selection for
     * this caret.
     */
    boolean isSelection() {
        return (dotPos != null && markPos != null &&
                markPos != dotPos && dotPos.getOffset() != markPos.getOffset());
    }
    
    boolean isSelectionShowing() {
        return editorCaret.isSelectionVisible() && isSelection();
    }

    Position getSelectionStart() {
        return dotPos; // TBD - possibly inspect virtual columns etc.
    }

    Position getSelectionEnd() {
        return dotPos; // TBD - possibly inspect virtual columns etc.
    }

    Point getMagicCaretPosition() {
        return magicCaretPosition;
    }

    void setDotPos(Position dotPos) {
        this.dotPos = dotPos;
    }

    void setMarkPos(Position markPos) {
        this.markPos = markPos;
    }

    void setMagicCaretPosition(Point newMagicCaretPosition) { // [TODO] move to transaction context
        this.magicCaretPosition = newMagicCaretPosition;
    }

    /**
     * Set new caret bounds while repainting both original and new bounds.
     * Clear the caret-painted and update-caret-bounds flags.
     *
     * @param newCaretBounds non-null new bounds
     */
    synchronized Rectangle setCaretBoundsWithRepaint(Rectangle newCaretBounds, JTextComponent c, String logMessage, int logIndex) {
        Rectangle oldCaretBounds = this.caretBounds;
        boolean repaintOld = (oldCaretBounds != null && (this.statusBits & CARET_PAINTED) != 0);
        this.statusBits &= ~(CARET_PAINTED | UPDATE_CARET_BOUNDS);
        boolean log = EditorCaret.LOG.isLoggable(Level.FINE);
        if (repaintOld) {
            if (log) {
                logRepaint(logMessage + "-setBoundsRepaint-repaintOld", logIndex, oldCaretBounds);
            }
            c.repaint(oldCaretBounds); // First schedule repaint of the original bounds (even if new bounds will possibly be the same)
        }
        if (!repaintOld || !newCaretBounds.equals(oldCaretBounds)) {
            if (log) {
                logRepaint(logMessage + "-setBoundsRepaint-repaintNew", logIndex, newCaretBounds);
            }
            c.repaint(newCaretBounds);
        }
        this.caretBounds = newCaretBounds;
        return oldCaretBounds;
    }
    
    /**
     * Set new caret bounds (without doing any extra repaint).
     * Clear the caret-painted and update-caret-bounds flags.
     *
     * @param newCaretBounds non-null new bounds
     */
    synchronized Rectangle setCaretBounds(Rectangle newCaretBounds) {
        Rectangle oldCaretBounds = this.caretBounds;
        this.statusBits &= ~(CARET_PAINTED | UPDATE_CARET_BOUNDS);
        this.caretBounds = newCaretBounds;
        return oldCaretBounds;
    }
    
    synchronized Rectangle repaint(JTextComponent c, String logMessage, int logIndex) {
        Rectangle bounds = this.caretBounds;
        if (bounds != null) {
            this.statusBits &= ~CARET_PAINTED;
            if (EditorCaret.LOG.isLoggable(Level.FINE)) {
                logRepaint(logMessage, logIndex, bounds);
            }
            c.repaint(bounds);
        }
        return bounds;
    }

    /**
     * Repaint caret bounds if the caret is showing or do nothing
     * @param c
     * @return 
     */
    synchronized Rectangle repaintIfShowing(JTextComponent c, String logMessage, int logIndex) {
        Rectangle bounds = this.caretBounds;
        if (bounds != null) {
            boolean repaint = (this.statusBits & CARET_PAINTED) != 0;
            if (repaint) {
                this.statusBits &= ~CARET_PAINTED;
                if (EditorCaret.LOG.isLoggable(Level.FINE)) {
                    logRepaint(logMessage + "-repaintIfShowing", logIndex, bounds);
                }
                c.repaint(bounds);
            }
        }
        return bounds;
    }

    synchronized Rectangle getCaretBounds() {
        return this.caretBounds;
    }

    int getTransactionIndexHint() {
        return transactionIndexHint;
    }

    void setTransactionIndexHint(int transactionIndexHint) {
        this.transactionIndexHint = transactionIndexHint;
    }

    synchronized void markRemovedInTransaction() {
        this.statusBits |= REMOVED_IN_TRANSACTION;
    }

    synchronized boolean getAndClearRemovedInTransaction() {
        boolean ret = (this.statusBits & REMOVED_IN_TRANSACTION) != 0;
        this.statusBits &= ~REMOVED_IN_TRANSACTION;
        return ret;
    }

    synchronized void markInfoObsolete() {
        this.statusBits |= INFO_OBSOLETE;
    }

    synchronized boolean getAndClearInfoObsolete() {
        boolean ret = (this.statusBits & INFO_OBSOLETE) != 0;
        this.statusBits &= ~INFO_OBSOLETE;
        return ret;
    }

    synchronized void markUpdateCaretBounds() {
        this.statusBits |= UPDATE_CARET_BOUNDS;
    }
    
    synchronized boolean getAndClearUpdateCaretBounds() {
        boolean ret = (this.statusBits & UPDATE_CARET_BOUNDS) != 0;
        this.statusBits &= ~UPDATE_CARET_BOUNDS;
        return ret;
    }
    
    synchronized void markCaretPainted() {
        this.statusBits |= CARET_PAINTED;
    }
    
    @Override
    public int compareTo(Object o) {
        return getDot() - ((CaretItem)o).getDot();
    }

    void logRepaint(String logMessage, int itemIndex, Rectangle r) {
        EditorCaret.LOG.fine(logMessage +
                ((itemIndex != -1) ? ((itemIndex != -2) ? "[" + itemIndex + "]" : "LAST") : "") + // NOI18N
                ": Rect=" + r2s(r) + ", item=" + this + "\n"); // NOI18N
    }
    
    static String r2s(Rectangle r) {
        return (r != null) ? "[x=" + r.x + ",y=" + r.y + ",w=" + r.width + ",h=" + r.height + "]" : null;
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("CI@").append(Integer.toHexString(System.identityHashCode(this))); // NOI18N
        sb.append(", dot=").append(dotPos).append(", mark=").append(markPos); // NOI18N
        if ((statusBits & REMOVED_IN_TRANSACTION) != 0) {
            sb.append(" REMOVED_IN_TRANSACTION"); // NOI18N
        }
        if ((statusBits & INFO_OBSOLETE) != 0) {
            sb.append(" INFO_OBSOLETE"); // NOI18N
        }
        if ((statusBits & UPDATE_CARET_BOUNDS) != 0) {
            sb.append(" UPDATE_CARET_BOUNDS"); // NOI18N
        }
        if ((statusBits & CARET_PAINTED) != 0) {
            sb.append(" CARET_PAINTED"); // NOI18N
        }
        return sb.toString();
    }
    
    public synchronized String toStringDetail() {
        return toString() + ", bounds=" + r2s(caretBounds) + ", magicCP=" + magicCaretPosition; // NOI18N
    }

}
