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

package org.netbeans.lib.editor.util.swing;

import java.util.Comparator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

/**
 * A pair of positions delimiting a text region in a swing document.
 * <br>
 * At all times it should be satisfied that
 * {@link #getStartOffset()} &lt;= {@link #getEndOffset()}.
 *
 * @author Miloslav Metelka
 * @since 1.6
 */

public class PositionRegion {

    /** Copmarator for position regions */
    private static Comparator<PositionRegion> comparator;
    
    /**
     * Get comparator for position regions comparing start offsets
     * of the two given regions.
     *
     * @return non-null comparator comparing the start offsets of the two given
     *  regions.
     */
    public static final Comparator<PositionRegion> getComparator() {
        if (comparator == null) {
            comparator = new Comparator<PositionRegion>() {
                public int compare(PositionRegion pr1, PositionRegion pr2) {
                    return pr1.getStartOffset() - pr2.getStartOffset();
                }
            };
        }
        return comparator;
    }

    /**
     * Create a fixed position instance that just wraps a given integer offset.
     * <br>
     * This may be useful for situations where a position needs to be used
     * but the document is not available yet. Once the document becomes
     * available the regular position instance (over an existing document)
     * may be used instead.
     *
     * @param offset &gt;=0 offset at which the position should be created.
     * @since 1.10
     */
    public static Position createFixedPosition(final int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
        return new Position() {
            public int getOffset() {
                return offset;
            }
        };
    }

    /**
     * Check whether a list of position regions is sorted
     * according the start offsets of the regions.
     *
     * @param positionRegionList list of the regions to be compared.
     * @return true if the regions are sorted according to the starting offset
     *  of the given regions or false otherwise.
     */
    public static boolean isRegionsSorted(List<? extends PositionRegion> positionRegionList) {
        for (int i = positionRegionList.size() - 2; i >= 0; i--) {
            if (getComparator().compare(positionRegionList.get(i),
                    positionRegionList.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    private Position startPosition;
    
    private Position endPosition;
    
    /**
     * Construct new position region.
     *
     * @param startPosition non-null start position of the region &lt;= end position.
     * @param endPosition non-null end position of the region &gt;= start position.
     */
    public PositionRegion(Position startPosition, Position endPosition) {
        assertPositionsValid(startPosition, endPosition);
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
    
    /**
     * Construct new position region based on the knowledge
     * of the document and starting and ending offset.
     */
    public PositionRegion(Document doc, int startOffset, int endOffset) throws BadLocationException {
        this(doc.createPosition(startOffset), doc.createPosition(endOffset));
    }
    
    /**
     * Get starting offset of this region.
     *
     * @return &gt;=0 starting offset of this region.
     */
    public final int getStartOffset() {
        return startPosition.getOffset();
    }
    
    /**
     * Get starting position of this region.
     *
     * @return non-null starting position of this region.
     */
    public final Position getStartPosition() {
        return startPosition;
    }
    
    /**
     * Get ending offset of this region.
     *
     * @return &gt;=0 ending offset of this region.
     */
    public final int getEndOffset() {
        return endPosition.getOffset();
    }
    
    /**
     * Get ending position of this region.
     *
     * @return non-null ending position of this region.
     */
    public final Position getEndPosition() {
        return endPosition;
    }
    
    /**
     * Get length of this region.
     *
     * @return &gt;=0 length of this region
     *  computed as <code>getEndOffset() - getStartOffset()</code>.
     */
    public final int getLength() {
        return getEndOffset() - getStartOffset();
    }

    /**
     * Get text enclosed by this position region as a character sequence.
     * 
     * @param doc non-null document to which this position region belongs.
     * @return non-null character sequence representing the enclosed text.
     * @throws IndexOutOfBoundsException if the bounds are wrong
     *  (likely positions were created for another document).
     */
    public CharSequence getText(Document doc) {
        int startOffset = getStartOffset();
        return DocumentUtilities.getText(doc).subSequence(startOffset, getEndOffset() - startOffset);
    }

    /**
     * Get text enclosed by this position region as a String.
     * 
     * @param doc non-null document to which this position region belongs.
     * @return non-null java.lang.String instance representing the enclosed text.
     * @throws BadLocationException if the bounds are wrong
     *  (likely positions were created for another document).
     */
    public String getString(Document doc) throws BadLocationException {
        int startOffset = getStartOffset();
        return doc.getText(startOffset, getEndOffset() - startOffset);
    }

    /**
     * Debug info about this region that includes line and column info.
     * 
     * @param doc non-null document to which this position region belongs.
     * @return debugging info describing this region (inlcuding line and column info).
     */
    public String toString(Document doc) {
        return new StringBuilder(35).append('<').append(DocumentUtilities.debugOffset(doc, getStartOffset())).
                append(',').append(DocumentUtilities.debugOffset(doc, getEndOffset())).append('>').toString();
    }

    /**
     * Debug info about this region.
     *
     * @return debugging info describing this region.
     */
    @Override
    public String toString() {
        return new StringBuilder(15).append('<').append(getStartOffset()).
                append(", ").append(getEndOffset()).append('>').toString();
    }

    /**
     * {@link MutablePositionRegion} uses this package private method
     * to set a new start position of this region.
     */
    void resetImpl(Position startPosition, Position endPosition) {
        assertPositionsValid(startPosition, endPosition);
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
    
    /**
     * {@link MutablePositionRegion} uses this package private method
     * to set a new start position of this region.
     */
    void setStartPositionImpl(Position startPosition) {
        assertPositionsValid(startPosition, endPosition);
        this.startPosition = startPosition;
    }

    /**
     * {@link MutablePositionRegion} uses this package private method
     * to set a new start position of this region.
     */
    void setEndPositionImpl(Position endPosition) {
        assertPositionsValid(startPosition, endPosition);
        this.endPosition = endPosition;
    }

    private static void assertPositionsValid(Position startPos, Position endPos) {
        assert (startPos.getOffset() <= endPos.getOffset())
            : "startPosition=" + startPos.getOffset() + " > endPosition=" // NOI18N
                + endPos.getOffset();
    }

}
