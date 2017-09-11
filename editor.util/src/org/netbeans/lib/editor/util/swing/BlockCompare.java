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

package org.netbeans.lib.editor.util.swing;

/**
 * Comparing of position block X to position block Y.
 * For example {@link #contains()} means that block X fully contains Y.
 *
 * @author Miloslav Metelka
 * @since 1.6
 */

public final class BlockCompare {

    /**
     * Compare block X and Y.
     *
     * @param xStartOffset start offset of block X.
     * @param xEndOffset end offset of block X must be &gt;=xStartOffset.
     * @param yStartOffset start offset of block Y.
     * @param yEndOffset end offset of block Y must be &gt;=yStartOffset.
     * @return instance of block comparing of X to Y.
     */
    public static BlockCompare get(int xStartOffset, int xEndOffset, int yStartOffset, int yEndOffset) {
        return new BlockCompare(resolve(xStartOffset, xEndOffset, yStartOffset, yEndOffset));
    }

    private static final int BEFORE = 1;
    private static final int AFTER = (BEFORE << 1);
    private static final int INSIDE = (AFTER << 1);
    private static final int CONTAINS = (INSIDE << 1);
    private static final int OVERLAP_START = (CONTAINS << 1);
    private static final int OVERLAP_END = (OVERLAP_START << 1);
    private static final int EMPTY_X = (OVERLAP_END << 1);
    private static final int EMPTY_Y = (EMPTY_X << 1);
    private static final int EQUAL_START = (EMPTY_Y << 1);
    private static final int EQUAL_END = (EQUAL_START << 1);
    private static final int LOWER_START = (EQUAL_END << 1);
    private static final int LOWER_END = (LOWER_START << 1);
    private static final int INVALID_X = (LOWER_END << 1);
    private static final int INVALID_Y = (INVALID_X << 1);

    private final int value;

    private BlockCompare(int value) {
        this.value = value;
    }

    /**
     * Check if block X is before block Y.
     *
     * @return true if end offset of block X is &lt;= start offset of block Y i.e.<br/>
     *   xEndOffset &lt;= yStartOffset.
     * 
     */
    public boolean before() {
        return (value & BEFORE) != 0;
    }

    /**
     * Check if block X is after block Y.
     *
     * @return true if start offset of block X is &gt;= end offset of block Y i.e.<br/>
     *   xStartOffset &gt;= yEndOffset.
     *   
     */
    public boolean after() {
        return (value & AFTER) != 0;
    }

    /**
     * Check if block X is contained in block Y.
     *
     * @return true if block X is contained inside block Y
     * i.e. xStartOffset &gt;= yStartOffset &amp;&amp; xEndOffset &lt;= yEndOffset.
     */
    public boolean inside() {
        return (value & INSIDE) != 0;
    }

    /**
     * Check if X is inside Y but X and Y are not equal.
     *
     * @return true if X is inside Y but they are not equal.
     */
    public boolean insideStrict() {
        return (value & (CONTAINS | INSIDE)) == INSIDE;
    }

    /**
     * Check if block X contains block Y.
     *
     * @return true if block X contains block Y.
     */
    public boolean contains() {
        return (value & CONTAINS) != 0;
    }

    /**
     * Check if X contains Y but X and Y are not equal.
     *
     * @return true if X contains Y but they are not equal.
     */
    public boolean containsStrict() {
        return (value & (CONTAINS | INSIDE)) == CONTAINS;
    }

    /**
     * Check if block X has the same boundaries as block Y.
     *
     * @return true if start and end offsets of block X are equal to start and end offsets of block Y.
     */
    public boolean equal() {
        return (value & (CONTAINS | INSIDE)) == (CONTAINS | INSIDE);
    }

    /**
     * Check if there's an overlap at start or end.
     *
     * @return true <code>overlapStart() || overlapEnd()</code>.
     */
    public boolean overlap() {
        return (value & (OVERLAP_START | OVERLAP_END)) != 0;
    }

    /**
     * Check if block X overlaps block Y at its begining.
     *
     * @return true if start offset of block X is before start offset of block Y
     *  and end offset of block X is inside block Y i.e.<br/>
     *   xStartOffset &lt; yStartOffset and <br/>
     *   xEndOffset &gt; yStartOffset and xEndOffset &lt; yEndOffset.
    */
    public boolean overlapStart() {
        return ((value & OVERLAP_START) != 0);
    }

    /**
     * Check if block X overlaps block Y at its end.
     *
     * @return true if start offset of block X is inside block Y
     *  and end offset of block X is above end of block Y i.e.<br/>
     *  xStartOffset &gt; yStartOffset && xStartOffset &lt; yEndOffset<br/>
     *  xEndOffset &gt; yEndOffset.
    */
    public boolean overlapEnd() {
        return ((value & OVERLAP_END) != 0);
    }

    /**
     * Check if block X is empty.
     *
     * @return true if start offset of block X equals to end offset of block X.
     */
    public boolean emptyX() {
        return (value & EMPTY_X) != 0;
    }

    /**
     * Check if block Y is empty.
     *
     * @return true if start offset of block Y equals to end offset of block Y.
     */
    public boolean emptyY() {
        return (value & EMPTY_Y) != 0;
    }
    
    /**
     * Check if block X has invalid bounds (start offset higher than its end offset).
     *
     * @return true if X has start offset higher than end offset i.e.<br/>
     *   xStartOffset &gt; xEndOffset.
     *  <br/>
     *   If true the block is treated as empty (end offset value is explicitly corrected to the start offset value)
     *   and the rest of flags is set accordingly.
     * @since 1.49
     */
    public boolean invalidX() {
        return (value & INVALID_X) != 0;
    }

    /**
     * Check if block Y has invalid bounds (start offset higher than its end offset).
     *
     * @return true if Y has start offset higher than end offset i.e.<br/>
     *   yStartOffset &gt; yEndOffset.
     *   <br/>
     *   If true the block is treated as empty (end offset value is explicitly corrected to the start offset value)
     *   and the rest of flags is set accordingly.
     * @since 1.49
     */
    public boolean invalidY() {
        return (value & INVALID_Y) != 0;
    }
    
    /**
     * Check if block X and Y have same start offset.
     *
     * @return true if start offset of block X equals to start offset of block Y i.e. xStartOffset == yStartOffset.
     * @since 1.44
     */
    public boolean equalStart() {
        return (value & EQUAL_START) != 0;
    }

    /**
     * Check if block X and Y have same end offset.
     *
     * @return true if end offset of block X equals to end offset of block Y i.e. xEndOffset == yEndOffset.
     * @since 1.44
     */
    public boolean equalEnd() {
        return (value & EQUAL_END) != 0;
    }

    /**
     * Check if block X starts lower than Y.
     *
     * @return true if start offset of block X is &lt; start offset of block Y i.e. xStartOffset &lt; yStartOffset.
     * @since 1.44
     */
    public boolean lowerStart() {
        return (value & LOWER_START) != 0;
    }

    /**
     * Check if block X ends lower than Y.
     *
     * @return true if end offset of block X is &lt; end offset of block Y i.e. xEndOffset &lt; yEndOffset.
     * @since 1.44
     */
    public boolean lowerEnd() {
        return (value & LOWER_END) != 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(50);
        appendBit(sb, BEFORE, "BEFORE");
        appendBit(sb, AFTER, "AFTER");
        appendBit(sb, CONTAINS, "CONTAINS");
        appendBit(sb, INSIDE, "INSIDE");
        appendBit(sb, OVERLAP_START, "OVERLAP_START");
        appendBit(sb, OVERLAP_END, "OVERLAP_END");
        appendBit(sb, EMPTY_X, "EMPTY_X");
        appendBit(sb, EMPTY_Y, "EMPTY_Y");
        appendBit(sb, EQUAL_START, "EQUAL_START");
        appendBit(sb, EQUAL_END, "EQUAL_END");
        appendBit(sb, LOWER_START, "LOWER_START");
        appendBit(sb, LOWER_END, "LOWER_END");
        appendBit(sb, INVALID_X, "INVALID_X");
        appendBit(sb, INVALID_Y, "INVALID_Y");
        return sb.toString();
    }

    private void appendBit(StringBuilder sb, int bitValue, String bitText) {
        if ((value & bitValue) != 0) {
            if (sb.length() != 0)
                sb.append('|');
            sb.append(bitText);
        }
    }

    private static int resolve(int xStartOffset, int xEndOffset, int yStartOffset, int yEndOffset) {
        int value = 0;
        if (xStartOffset > xEndOffset) {
            value |= INVALID_X;
            xEndOffset = xStartOffset;
        }
        if (yStartOffset > yEndOffset) {
            yEndOffset = yStartOffset;
            value |= INVALID_Y;
        }
        if (xEndOffset < yStartOffset) {
            value |= BEFORE | LOWER_START | LOWER_END;
            if (xStartOffset == xEndOffset) {
                value |= EMPTY_X;
            }
            if (yStartOffset == yEndOffset) {
                value |= EMPTY_Y;
            }

        } else if (xEndOffset == yStartOffset) { // xStartOffset ?? xEndOffset == yStartOffset ?? yEndOffset
            if (xStartOffset == xEndOffset) { // xStartOffset == xEndOffset == yStartOffset ?? yEndOffset
                if (yStartOffset == yEndOffset) { // xStartOffset == xEndOffset == yStartOffset == yEndOffset
                    value |= EMPTY_X | EMPTY_Y | BEFORE | AFTER | INSIDE | CONTAINS | EQUAL_START | EQUAL_END;
                } else { // xStartOffset == xEndOffset == yStartOffset < yEndOffset
                    value |= EMPTY_X | BEFORE | CONTAINS | EQUAL_START | LOWER_END;
                }
            } else { // xStartOffset < xEndOffset == yStartOffset ?? yEndOffset
                if (yStartOffset == yEndOffset) { // xStartOffset < xEndOffset == yStartOffset == yEndOffset
                    value |= EMPTY_Y | BEFORE | INSIDE | LOWER_START | EQUAL_END;
                } else { // xStartOffset < xEndOffset == yStartOffset < yEndOffset
                    value |= BEFORE | LOWER_START | LOWER_END;
                }
            }

        } else { // yStartOffset < xEndOffset
            if (yEndOffset < xStartOffset) { // yStartOffset ?? yEndOffset < xStartOffset ?? xEndOffset
                value |= AFTER;
                if (xStartOffset == xEndOffset) {
                    value |= EMPTY_X;
                }
                if (yStartOffset == yEndOffset) {
                    value |= EMPTY_Y;
                }

            } else if (xStartOffset == yEndOffset) { // yStartOffset < xEndOffset && xStartOffset == yEndOffset
                if (xStartOffset == xEndOffset) { // yStartOffset ?? yEndOffset == xStartOffset == xEndOffset
                    if (yStartOffset == yEndOffset) { // yStartOffset == yEndOffset == xStartOffset == xEndOffset
                        value |= EMPTY_X | EMPTY_Y | BEFORE | AFTER | INSIDE | CONTAINS | EQUAL_START | EQUAL_END;
                    } else { // yStartOffset < xStartOffset == xEndOffset == yEndOffset
                        value |= EMPTY_X | AFTER | CONTAINS | LOWER_START | EQUAL_END;
                    }
                } else { // yStartOffset <= yEndOffset == xStartOffset < xEndOffset
                    if (yStartOffset == yEndOffset) { // yStartOffset == yEndOffset == xStartOffset < xEndOffset
                        value |= EMPTY_Y | BEFORE | AFTER | INSIDE | CONTAINS | EQUAL_START;
                    } else { // yStartOffset < yEndOffset == xStartOffset < xEndOffset
                        value |= AFTER;
                    }
                }

            } else { // xStartOffset < yEndOffset && yStartOffset < xEndOffset
                if (xStartOffset < yStartOffset) { // xStartOffset < yStartOffset < xEndOffset
                    if (xEndOffset < yEndOffset) { // xStartOffset < yStartOffset < xEndOffset < yEndOffset
                        value |= OVERLAP_START | LOWER_START | LOWER_END;
                    } else { // xStartOffset < yStartOffset < yEndOffset <= xEndOffset
                        value |= CONTAINS | LOWER_START;
                        if (xEndOffset == yEndOffset) { // xStartOffset < yStartOffset < yEndOffset == xEndOffset
                            value |= EQUAL_END;
                        } // xStartOffset < yStartOffset < yEndOffset < xEndOffset
                    }
                } else if (xStartOffset == yStartOffset) { // xStartOffset == yStartOffset < yEndOffset && xEndOffset > yStartOffset
                    if (xEndOffset < yEndOffset) { // xStartOffset == yStartOffset < xEndOffset < yEndOffset
                        value |= INSIDE | EQUAL_START | LOWER_END;
                    } else if (xEndOffset == yEndOffset) { // xStartOffset == yStartOffset < xEndOffset == yEndOffset
                        value |= INSIDE | CONTAINS | EQUAL_START | EQUAL_END;
                    } else { // xStartOffset == yStartOffset < yEndOffset < xEndOffset
                        value |= CONTAINS | EQUAL_START;
                    }
                } else { // yStartOffset < xStartOffset < yEndOffset
                    if (xEndOffset <= yEndOffset) { // yStartOffset < xStartOffset < xEndOffset <= yEndOffset
                        value |= INSIDE;
                        if (xEndOffset == yEndOffset) { // yStartOffset < xStartOffset < xEndOffset == yEndOffset
                            value |= EQUAL_END;
                        } else { // yStartOffset < xStartOffset < xEndOffset < yEndOffset
                            value |= LOWER_END;
                        }
                    } else { // yStartOffset < xStartOffset < yEndOffset < xEndOffset
                        value |= OVERLAP_END;
                    }
                }
                if (xStartOffset == xEndOffset) {
                    value |= EMPTY_X;
                }
                if (yStartOffset == yEndOffset) {
                    value |= EMPTY_Y;
                }
            }
        }
        return value;
    }

}
