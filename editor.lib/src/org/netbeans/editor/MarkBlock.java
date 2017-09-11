/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
* Block of text created using two marks. These blocks can be chained.
* Existing block can be compared to other block (better pair of positions)
* and there's extensive list of result values of such comparison.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class MarkBlock {

    /** This value is not used directly in this class
    * but can be used by other classes to report that
    * the comparison of blocks has no sense for some reason.
    */
    public static final int INVALID = 0;

    /** Single bit value that means that tested block
    * and THIS block partially or fully overlap. If this bit is not
    * set then the blocks don't overlap at all. The values for which
    * this bit is set are: OVERLAP_BEGIN, OVERLAP_END, EXTEND_BEGIN,
    * EXTEND_END, INCLUDE, INSIDE_BEGIN, INSIDE_END, SAME, INNER.
    * The values for which this bit is not set are: BEFORE, AFTER,
    * CONTINUE_BEGIN, CONTINUE_END.
    */
    public static final int OVERLAP = 1;

    /** Single bit value that means that the tested block doesn't
    * overlap with THIS block, but either its start position
    * is equal to end position of THIS block or its end position
    * is equal to the start position of THIS block. Simply they together
    * make one continuous block. The values for which this bit is set
    * are: CONTINUE_BEGIN, CONTINUE_END.
    */
    public static final int CONTINUE = 2;

    /** Single bit value meaning that the tested block has zero size.
    */
    public static final int EMPTY = 4;

    /** Single bit value meaning that THIS block has zero size.
    */
    public static final int THIS_EMPTY = 8;

    /** Two bit value meaning that the tested block fully
    * includes THIS block. The block must be extended at least by
    * one character, otherwise the 'inside' values will be used.
    * It is included in the following
    * values: EXTEND_BEGIN, INCLUDE, EXTEND_END.
    * The value includes OVERLAP.
    */
    public static final int EXTEND = 16 | OVERLAP;

    /** Two bit value meaning that the tested block is fully
    * inside THIS block. It is included in the following
    * values: INSIDE_BEGIN, SAME, INSIDE_END.
    * The value includes OVERLAP.
    */
    public static final int INSIDE = 32 | OVERLAP;


    /** Tested block completely before THIS mark block.
    */
    public static final int BEFORE = 64;

    /** Tested block completely after THIS mark block.
    */
    public static final int AFTER = 128;

    /** Tested block completely before THIS mark block but its
    * end position equals to the start position of THIS block.
    * They together make one continuous block.
    * The value is BEFORE | CONTINUE.
    */
    public static final int CONTINUE_BEGIN = BEFORE | CONTINUE;

    /** Tested block completely after THIS mark block but its
    * start position equals to the end position of THIS block.
    * They together make one continuous block.
    * The value is AFTER | CONTINUE.
    */
    public static final int CONTINUE_END = AFTER | CONTINUE;

    /** Tested block partly covers begining of THIS mark block.
    * The value includes OVERLAP.
    */
    public static final int OVERLAP_BEGIN = 256 | OVERLAP;

    /** Tested block partly covers end of THIS mark block.
    * The value includes OVERLAP.
    */
    public static final int OVERLAP_END = 512 | OVERLAP;

    /** Start position of the tested block is lower than
    * the start position of THIS block and both end positions
    * are the same.
    * The value is OVERLAP_BEGIN | EXTEND.
    */
    public static final int EXTEND_BEGIN = OVERLAP_BEGIN | EXTEND;

    /** End position of the tested block is greater than
    * the end position of THIS block and both start positions
    * are the same.
    * The value is OVERLAP_END | EXTEND.
    */
    public static final int EXTEND_END = OVERLAP_END | EXTEND;

    /** Tested block fully includes THIS block and extends it
    * by at least one character in both directions.
    * The value is EXTEND_BEGIN | EXTEND_END.
    */
    public static final int INCLUDE = EXTEND_BEGIN | EXTEND_END;

    /** Tested block completely inside THIS block and its end
    * position is lower than end position of THIS block
    * and start positions are the same.
    * The value includes INSIDE.
    */
    public static final int INSIDE_BEGIN = 1024 | INSIDE;

    /** Tested block completely inside THIS block and its start
    * position is greater than THIS block start position and
    * end positions are the same.
    * The value includes INSIDE.
    */
    public static final int INSIDE_END = 2048 | INSIDE;

    /** Tested block is fully inside THIS block and there
    * is at least one more character left in THIS block
    * after the end of the tested block in both directions.
    * The value includes INSIDE.
    */
    public static final int INNER = 4096 | INSIDE;

    /** The blocks have exactly the same start and end positions.
    * They simply cover exactly the same area.
    * The value is INSIDE_BEGIN | INSIDE_END.
    */
    public static final int SAME = INSIDE_BEGIN | INSIDE_END;


    /** This value can be used to clear the two bits saying
    * if the tested or THIS block are empty (The EMPTY and THIS_EMPTY are cleared).
    * To do that, use value ANDed by IGNORE_EMPTY expression.
    */
    public static final int IGNORE_EMPTY = ~(EMPTY | THIS_EMPTY);


    /** Next block in the chain */
    protected MarkBlock next;

    /** Previous block in the chain */
    protected MarkBlock prev;

    public Mark startMark;

    public Mark endMark;

    protected BaseDocument doc;
    
    public MarkBlock(BaseDocument doc, Mark startMark,
                     Mark endMark) {
        this.doc = doc;
        this.startMark = startMark;
        this.endMark = endMark;
    }

    /** Construct block with given marks */
    public MarkBlock(BaseDocument doc, int startPos, int endPos)
    throws BadLocationException {
        this(doc, new Mark(), new Mark(), startPos, endPos);
    }

    /** Construct block from positions on some document */
    public MarkBlock(BaseDocument doc, Mark startMark,
                     Mark endMark, int startPos, int endPos)
    throws BadLocationException {
        this(doc, startMark, endMark);
        try {
            startMark.insert(doc, startPos);
            try {
                endMark.insert(doc, endPos);
            } catch (BadLocationException e) {
                try {
                    startMark.remove();
                } catch (InvalidMarkException e2) {
                    Utilities.annotateLoggable(e2);
                }
                throw e;
            } catch (InvalidMarkException e) {
                Utilities.annotateLoggable(e);
            }
        } catch (InvalidMarkException e) {
            Utilities.annotateLoggable(e);
        }
    }

    /** Insert block before this one
    * @return inserted block
    */
    public MarkBlock insertChain(MarkBlock blk) {
        MarkBlock thisPrev = this.prev;
        blk.prev = thisPrev;
        blk.next = this;
        if (thisPrev != null) {
            thisPrev.next = blk;
        }
        this.prev = blk;
        return blk;
    }

    /** Add block after this one
    * @return added block
    */
    public MarkBlock addChain(MarkBlock blk) {
        if (next != null) {
            next.insertChain(blk);
        } else {
            setNextChain(blk);
        }
        return blk;
    }

    /** Remove this block from the chain
    * @return next chain member or null for end of chain
    */
    public MarkBlock removeChain() {
        MarkBlock thisNext = this.next;
        MarkBlock thisPrev = this.prev;
        if (thisPrev != null) { // not the first
            thisPrev.next = thisNext;
            this.prev = null;
        }
        if (thisNext != null) {
            thisNext.prev = thisPrev;
            this.next = null;
        }
        destroyMarks();
        return thisNext;
    }

    /** Compares the position of the given block against current block.
    * @param startPos starting position of the compared block
    * @param endPos ending position of the compared block or it is the same
    *   as startPos when testing just for insert
    * @return relation of compared block against this guarded block
    */
    public int compare(int startPos, int endPos) {
        if (startMark == null || endMark == null)
            return INVALID;
        try {
            int startThis = startMark.getOffset();
            int endThis = endMark.getOffset();
            if (startThis > endThis) { // swap if necessary
                int tmp = startThis;
                startThis = endThis;
                endThis = tmp;
            }
            if (startPos == endPos) { // tested empty
                if (startThis == endThis) { // both empty
                    if (startPos < startThis) {
                        return EMPTY | THIS_EMPTY | BEFORE;
                    } else if (startPos > startThis) {
                        return EMPTY | THIS_EMPTY | AFTER;
                    } else {
                        return EMPTY | THIS_EMPTY | SAME;
                    }
                } else { // tested empty, this non-empty
                    if (startPos <= startThis) {
                        return (startPos < startThis) ? (EMPTY | BEFORE)
                               : (EMPTY | INSIDE_BEGIN);
                    } else if (startPos >= endThis) {
                        return (startPos > endThis) ? (EMPTY | AFTER)
                               : (EMPTY | INSIDE_END);
                    } else {
                        return EMPTY | INNER;
                    }
                }

            }
            if (startThis == endThis) { // this empty, tested non-empty
                if (startPos >= startThis) {
                    return (startPos > startThis) ? (THIS_EMPTY | AFTER)
                           : (THIS_EMPTY | EXTEND_END);
                } else if (endPos >= startThis) {
                    return (endPos > startThis) ? (THIS_EMPTY | BEFORE)
                           : (THIS_EMPTY | EXTEND_BEGIN);
                } else {
                    return THIS_EMPTY | INCLUDE;
                }
            }
            // both non-empty
            if (endPos <= startThis) {
                return (endPos < startThis) ? BEFORE : CONTINUE_BEGIN;
            } else if (startPos >= endThis) {
                return (startPos > endThis) ? AFTER : CONTINUE_END;
            } else {
                if (endPos < endThis) {
                    if (startPos > startThis) {
                        return INNER;
                    } else if (startPos == startThis) {
                        return INSIDE_BEGIN;
                    } else { // startPos < startThis
                        return OVERLAP_BEGIN;
                    }
                } else if (endPos == endThis) {
                    if (startPos > startThis) {
                        return INSIDE_END;
                    } else if (startPos == startThis) {
                        return SAME;
                    } else { // startPos < startThis
                        return EXTEND_BEGIN;
                    }
                } else { // endPos > endThis
                    if (startPos > startThis) {
                        return OVERLAP_END;
                    } else if (startPos == startThis) {
                        return EXTEND_END;
                    } else { // startPos < startThis
                        return INCLUDE;
                    }
                }
            }
        } catch (InvalidMarkException e) {
            return INVALID;
        }
    }

    public final MarkBlock getNext() {
        return next;
    }

    public final void setNext(MarkBlock b) {
        next = b;
    }

    /** Set the next block of this one in the chain. */
    public void setNextChain(MarkBlock b) {
        this.next = b;
        if (b != null) {
            b.prev = this;
        }
    }

    public final MarkBlock getPrev() {
        return prev;
    }

    public final void setPrev(MarkBlock b) {
        prev = b;
    }

    /** Set the previous block of this one in the chain */
    public void setPrevChain(MarkBlock b) {
        this.prev = b;
        if (b != null) {
            b.next = this;
        }
    }

    public boolean isReverse() {
        try {
            return (startMark.getOffset() > endMark.getOffset());
        } catch (InvalidMarkException e) {
            return false;
        }
    }

    public void reverse() {
        Mark tmp = startMark;
        startMark = endMark;
        endMark = tmp;
    }

    public boolean checkReverse() {
        if (isReverse()) {
            reverse();
            return true;
        }
        return false;
    }

    /** Possibly move start mark if its position is above the given number.
    * @return new position
    */
    public int extendStart(int startPos) throws BadLocationException {
        try {
            int markPos = startMark.getOffset();
            startPos = Math.min(startPos, markPos);
            if (startPos != markPos) {
                startMark.move(doc, startPos);
            }
            return startPos;
        } catch (InvalidMarkException e) {
            Utilities.annotateLoggable(e);
            return 0;
        }
    }

    /** Possibly move end mark if its position is above the given number.
    * @return new position
    */
    public int extendEnd(int endPos) throws BadLocationException {
        try {
            int markPos = endMark.getOffset();
            endPos = Math.max(endPos, markPos);
            if (endPos != markPos) {
                endMark.move(doc, endPos);
            }
            return endPos;
        } catch (InvalidMarkException e) {
            Utilities.annotateLoggable(e);
            return 0;
        }
    }

    /** Extend this mark block by start and end positions. First test whether
    * the given block intersects with this mark block. If not nothing is done.
    * @return whether this mark block has been extended
    */
    public boolean extend(int startPos, int endPos, boolean concat) throws BadLocationException {
        try {
            boolean extended = false;
            int rel = compare(startPos, endPos);
            if ((rel & OVERLAP_BEGIN) == OVERLAP_BEGIN
                    || ((rel & CONTINUE_BEGIN) == CONTINUE_BEGIN && concat)
               ) {
                extended = true;
                startMark.move(doc, startPos);
            }
            if ((rel & OVERLAP_END) == OVERLAP_END
                    || ((rel & CONTINUE_END) == CONTINUE_END && concat)
               ) {
                extended = true;
                endMark.move(doc, endPos);
            }
            return extended;
        } catch (InvalidMarkException e) {
            Utilities.annotateLoggable(e);
            return false;
        }
    }

    /** Extend this mark block by some other block.
    * @return whether the block was extended. If it was, the caller
    *   is responsible for possibly removing blk from the chain
    */
    public boolean extend(MarkBlock blk, boolean concat) {
        try {
            return extend(blk.startMark.getOffset(), blk.endMark.getOffset(), concat);
        } catch (BadLocationException e) {
            Utilities.annotateLoggable(e);
        } catch (InvalidMarkException e) {
            Utilities.annotateLoggable(e);
        }
        return false;
    }

    /** Shrink this mark block by the block specified.
    * startMark is moved to the endPos if OVERLAP_BEGIN
    * or INSIDE_BEGIN is returned from compare().
    * endMark is moved to the startPos if OVERLAP_END
    * or INSIDE_END is returned from compare().
    * If other status is returned or either block
    * is empty, then no action is taken. It's up
    * to caller to handle these situations.
    * @return relation of tested block to mark block
    */
    public int shrink(int startPos, int endPos) throws BadLocationException {
        try {
            int rel = compare(startPos, endPos);
            switch (rel) {
            case OVERLAP_BEGIN:
            case INSIDE_BEGIN:
                startMark.move(doc, endPos);
                break;
            case OVERLAP_END:
            case INSIDE_END:
                endMark.move(doc, startPos);
                break;
            }
            return rel;
        } catch (InvalidMarkException e) {
            Utilities.annotateLoggable(e);
            return INVALID;
        }
    }

    public Document getDocument() {
        return doc;
    }

    public int getStartOffset() {
        try {
            return startMark.getOffset();
        } catch (InvalidMarkException e) {
            return 0;
        }
    }

    public int getEndOffset() {
        try {
            return endMark.getOffset();
        } catch (InvalidMarkException e) {
            return 0;
        }
    }

    /** Remove the marks if they were not removed yet */
    void destroyMarks() {
        // now remove the marks
        try {
            if (startMark != null) {
                startMark.remove();
                startMark = null;
            }
        } catch (InvalidMarkException e) {
            // already removed
        }
        try {
            if (endMark != null) {
                endMark.remove();
                endMark = null;
            }
        } catch (InvalidMarkException e) {
            // already removed
        }
    }

    /** Destroy the marks if necessary */
    protected void finalize() throws Throwable {
        destroyMarks();
        super.finalize();
    }

    /** Debugs this mark block */
    public String toString() {
        try {
            return "startPos="
                    + ((startMark != null)  // NOI18N
                        ? (String.valueOf(startMark.getOffset()) + '['
                            + Utilities.debugPosition(doc, startMark.getOffset()) + ']')
                        : "null")
                    + ", endPos=" // NOI18N
                    + ((endMark != null)
                            ? (String.valueOf(endMark.getOffset()) + '['
                                + Utilities.debugPosition(doc, endMark.getOffset()) + ']')
                            : "null") // NOI18N
                    + ", " + ((prev != null) ? ((next != null) ? "chain member" // NOI18N
                                   : "last member") : ((next != null) ? "first member" // NOI18N
                                                                   : "standalone member")); // NOI18N
        } catch (InvalidMarkException e) {
            return ""; // NOI18N
        }
    }

    /** Debug possibly whole chain of marks */
    public String toStringChain() {
        return toString() + ((next != null) ? ("\n" + next.toStringChain()) : ""); // NOI18N
    }

    public static String debugRelation(int rel) {
        String s = ((rel & EMPTY) != 0) ? "EMPTY | " : ""; // NOI18N
        s += ((rel & THIS_EMPTY) != 0) ? "THIS_EMPTY | " : ""; // NOI18N
        rel &= IGNORE_EMPTY;
        switch (rel) {
        case BEFORE:
            return s + "BEFORE"; // NOI18N
        case AFTER:
            return s + "AFTER"; // NOI18N
        case CONTINUE_BEGIN:
            return s + "CONTINUE_BEGIN"; // NOI18N
        case CONTINUE_END:
            return s + "CONTINUE_END"; // NOI18N
        case OVERLAP_BEGIN:
            return s + "OVERLAP_BEGIN"; // NOI18N
        case OVERLAP_END:
            return s + "OVERLAP_END"; // NOI18N
        case EXTEND_BEGIN:
            return s + "EXTEND_BEGIN"; // NOI18N
        case EXTEND_END:
            return s + "EXTEND_END"; // NOI18N
        case INCLUDE:
            return s + "INCLUDE"; // NOI18N
        case INSIDE_BEGIN:
            return s + "INSIDE_BEGIN"; // NOI18N
        case INSIDE_END:
            return s + "INSIDE_END"; // NOI18N
        case INNER:
            return s + "INNER"; // NOI18N
        case SAME:
            return s + "SAME"; // NOI18N
        case INVALID:
            return s + "INVALID"; // NOI18N
        default:
            return s + "UNKNOWN_STATE " + rel; // NOI18N
        }
    }

}
