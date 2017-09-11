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

package org.netbeans.modules.editor.lib2.view;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

/**
 * Immutable pair of start and end offset held as positions.
 *
 * @author Miloslav Metelka
 */

public final class OffsetRegion {
    
    public static OffsetRegion create(Document doc, int startOffset, int endOffset) {
        checkBounds(startOffset, endOffset);
        return new OffsetRegion(createPos(doc, startOffset), createPos(doc, endOffset));
    }

    public static OffsetRegion create(Position startPos, Position endPos) {
        checkBounds(startPos.getOffset(), endPos.getOffset());
        return new OffsetRegion(startPos, endPos);
    }
    
    public static OffsetRegion union(OffsetRegion region, Document doc,
            int startOffset, int endOffset, boolean ignoreEmpty)
    {
        return (region != null)
                ? region.union(doc, startOffset, endOffset, ignoreEmpty)
                : create(doc, startOffset, endOffset);
    }

    public static OffsetRegion union(OffsetRegion region1, OffsetRegion region2, boolean ignoreEmpty) {
        return (region1 != null) ? region1.union(region2, ignoreEmpty) : region2;
    }

    private static Position createPos(Document doc, int offset) {
        try {
            return doc.createPosition(offset);
        } catch (BadLocationException ex) {
            throw new IllegalArgumentException("Invalid offset=" + offset + " in doc: " + doc, ex);
        }
    }

    private static void checkBounds(int startOffset, int endOffset) {
        if (startOffset > endOffset) {
            throw new IllegalArgumentException("startOffset=" + startOffset + " > endOffset=" + endOffset); // NOI18N
        }
    }

    private final Position startPos;
    
    private final Position endPos;
    
    private OffsetRegion(Position startPos, Position endPos) {
        this.startPos = startPos;
        this.endPos = endPos;
    }
    
    public int startOffset() {
        return startPos.getOffset();
    }
    
    public Position startPosition() {
        return startPos;
    }
    
    public int endOffset() {
        return endPos.getOffset();
    }
    
    public Position endPosition() {
        return endPos;
    }

    public int length() {
        return endOffset() - startOffset();
    }
    
    public boolean isEmpty() {
        return (length() == 0);
    }

    /**
     * Return union of this region with the given region.
     * <br>
     * If the given region is empty then return "this" region.
     * 
     * @param doc non-null document to which the offsets relate.
     *  Only the regions for the same documents should be compared and merged.
     * @param startOffset region's start offset
     * @param endOffset region's end offset.
     * @param ignoreEmpty if false and "this" region is empty (or startOffset == endOffset)
     *  then the returned region will "include" bounds of the empty region.
     * @return new region instance which is union of the given bounds
     */
    public OffsetRegion union(Document doc, int startOffset, int endOffset, boolean ignoreEmpty) {
        checkBounds(startOffset, endOffset);
        int thisStartOffset = this.startOffset();
        int thisEndOffset = this.endOffset();
        if (ignoreEmpty) {
            if (startOffset == endOffset) {
                return this;
            }
            if (thisStartOffset == thisEndOffset) {
                return create(doc, startOffset, endOffset);
            }
        }
        if (startOffset >= thisStartOffset) {
            if (endOffset <= thisEndOffset) {
                return this; // Included
            } else { // endOffset > this.endOffset
                return new OffsetRegion(this.startPos, createPos(doc, endOffset));
            }
        } else { // startOffset < this.startOffset
            Position endP = (endOffset > thisEndOffset) ? createPos(doc, endOffset) : this.endPos;
            return new OffsetRegion(createPos(doc, startOffset), endP);
        }
    }
    
    /**
     * Return union of this region with the given region.
     * <br>
     * If the given region is empty then return "this" region.
     * 
     * @param region region to union with.
     * @param ignoreEmpty if false and "this" region is empty (or region.isEmpty())
     *  then the returned region will "include" bounds of the empty region.
     * @return new region instance which is union of the given bounds
     */
    public OffsetRegion union(OffsetRegion region, boolean ignoreEmpty) {
        int thisStartOffset = this.startOffset();
        int thisEndOffset = this.endOffset();
        if (ignoreEmpty) {
            if (region.isEmpty()) {
                return this;
            }
            if (thisStartOffset == thisEndOffset) {
                return region;
            }
        }
        if (region.startOffset() >= thisStartOffset) {
            if (region.endOffset() <= thisEndOffset) {
                return this; // Included
            } else { // endOffset > this.endOffset
                return new OffsetRegion(this.startPos, region.endPos);
            }
        } else { // startOffset < this.startOffset
            Position endP = (region.endOffset() > thisEndOffset) ? region.endPos : this.endPos;
            return new OffsetRegion(region.startPos, endP);
        }
    }

    /**
     * Return intersection of this region with given bounds.
     *
     * @param doc non-null document to which startOffset and endOffset relate.
     * @param startOffset
     * @param endOffset
     * @param nullIfOutside return null (instead of empty region) in case
     *   <code>startOffset &gt; this.endOffset || this.startOffset &gt; endOffset</code>.
     * @return intersection region.
     */
    public OffsetRegion intersection(Document doc, int startOffset, int endOffset, boolean nullIfOutside) {
        checkBounds(startOffset, endOffset);
        int thisStartOffset = startOffset();
        int thisEndOffset = endOffset();
        if (thisStartOffset >= startOffset) {
           if (thisEndOffset <= endOffset) {
               return this;
           } else { // thisEndOffset > endOffset
               if (thisStartOffset > endOffset) { // Empty (use this.startPos)
                   return nullIfOutside ? null : new OffsetRegion(this.startPos, this.startPos);
               } else { // thisStartOffset <= endOffset
                   Position end = (thisStartOffset == endOffset) ? this.startPos : createPos(doc, endOffset);
                   return new OffsetRegion(this.startPos, end);
               }
           }
        } else { // thisStartOffset < startOffset
           if (thisEndOffset <= endOffset) {
               if (thisEndOffset < startOffset) { // Empty (use this.endPos)
                   return nullIfOutside ? null : new OffsetRegion(this.endPos, this.endPos);
               } else { // thisEndOffset >= startOffset
                   Position start = (thisEndOffset == startOffset) ? this.endPos : createPos(doc, startOffset);
                   return new OffsetRegion(start, this.endPos);
               }
           } else { // thisEndOffset > endOffset
               return new OffsetRegion(createPos(doc, startOffset), createPos(doc, endOffset));
           }
        }
    }

    /**
     * Return intersection of this region with given bounds.
     *
     * @param region non-null region to intersect with.
     * @param nullIfOutside return null (instead of empty region) in case
     *   <code>region.startOffset &gt; this.endOffset || this.startOffset &gt; region.endOffset</code>.
     * @return intersection region.
     */
    public OffsetRegion intersection(OffsetRegion region, boolean nullIfOutside) {
        int thisStartOffset = startOffset();
        int thisEndOffset = endOffset();
        int startOffset = region.startOffset();
        int endOffset = region.endOffset();
        if (thisStartOffset > startOffset) {
           if (thisEndOffset <= endOffset) {
               return this;
           } else { // thisEndOffset > endOffset
               if (nullIfOutside && thisStartOffset > endOffset) { // Empty (use this.startPos)
                   return nullIfOutside ? null : new OffsetRegion(this.startPos, this.startPos);
               } else {
                   Position end = (thisStartOffset == endOffset) ? this.startPos : region.endPos;
                   return new OffsetRegion(this.startPos, end);
               }
           }
        } else if (thisStartOffset < startOffset) {
           if (thisEndOffset < endOffset) {
               if (thisEndOffset < startOffset) { // Empty (use this.endPos)
                   return nullIfOutside ? null : new OffsetRegion(this.endPos, this.endPos);
               } else { // thisEndOffset >= startOffset
                   Position start = (thisEndOffset == startOffset) ? this.endPos : region.startPos;
                   return new OffsetRegion(start, this.endPos);
               }
           } else { // thisEndOffset >= endOffset
               return region;
           }
        } else { // thisStartOffset == startOffset
           if (thisEndOffset <= endOffset) {
               return this;
           } else { // thisEndOffset > endOffset
               return region;
           }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof OffsetRegion) {
            OffsetRegion region = (OffsetRegion) obj;
            return (region.startOffset() == startOffset() && region.endOffset() == endOffset());
        }
        return false;
    }
    
    @Override
    public String toString() {
        return isEmpty()
                ? "<E:" + startPos.getOffset() + ">" // NOI18N
                : "<" + startPos.getOffset() + "," + endPos.getOffset() + ">"; // NOI18N
    }

}
