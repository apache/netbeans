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
package org.netbeans.api.editor.document;

import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.editor.lib2.document.ComplexPos;
import org.openide.util.Parameters;

/**
 * A regular Swing position together with an extra integer to express a split offset
 * inside the character to which the position's offset points to - typically a tab or newline character.
 *
 * @author Miloslav Metelka
 * @since 1.9
 */
public final class ComplexPositions {

    private ComplexPositions() {
        // No instances
    }
    
    /**
     * Produce a new complex position as an immutable pair of a Swing position
     * (or another complex position) and a split offset "inside" the character at the position.
     * <br>
     * The returned position acts like the original position
     * in terms of changing its "main" offset according to document modifications.
     * The initial split offset of the complex position never changes.
     *
     * @param pos non-null position. If this is already a complex position its split offset
     *  gets added to the split offset parameter passed to this method.
     * @param splitOffset >= 0 zero-based offset "inside" a character at pos parameter.
     *  For example a third "space" of a tab character at offset == 100 is expressed
     *  as (100,2). Negative value throws an IllegalArgumentException.
     * @return virtual position whose {@link Position#getOffset()} returns the same value
     *  like the pos parameter.
     * @exception IllegalArgumentException for negative splitOffset parameter.
     */
    public static Position create(@NonNull Position pos, int splitOffset) {
        Parameters.notNull("pos", pos);   //NOI18N
        if (splitOffset > 0) {
            if (pos.getClass() == ComplexPos.class) {
                return new ComplexPos((ComplexPos)pos, splitOffset);
            } else {
                return new ComplexPos(pos, splitOffset);
            }
        } else if (splitOffset == 0) {
            return pos;
        } else {
            throw new IllegalArgumentException("splitOffset=" + splitOffset + " < 0");
        }
    }

    /**
     * Return split offset of a passed complex position or zero for non-complex positions.
     * @param pos non-null position.
     * @return >=0 split offset or zero for non-complex positions.
     */
    public static int getSplitOffset(@NonNull Position pos) {
        return getSplitOffsetImpl(pos);
    }

    /**
     * Compare positions.
     * @param pos1 non-null position.
     * @param pos2 non-null position.
     * @return offset of pos1 minus offset of pos2 or diff of their split offsets in case
     *  both positions have the same position's offsets.
     * @NullPointerException if any passed position is null unless both positions are null
     *  in which case the method would return 0.
     */
    public static int compare(@NonNull Position pos1, @NonNull Position pos2) {
        if (pos1 == pos2) {
            return 0;
        }
        int offsetDiff = pos1.getOffset() - pos2.getOffset();
        return (offsetDiff != 0) ? offsetDiff : getSplitOffsetImpl(pos1) - getSplitOffsetImpl(pos2);
    }
    
    /**
     * Compare positions by providing their offsets and split offsets obtained earlier.
     * @param offset1 offset of first position.
     * @param splitOffset1 split offset of character at offset1.
     * @param offset2 offset of second position.
     * @param splitOffset2 split offset of character at offset2.
     * @return offset1 minus offset2 or splitOffset1 minus splitOffset2 in case
     *  offset1 and offset2 are equal.
     */
    public static int compare(int offset1, int splitOffset1, int offset2, int splitOffset2) {
        int offsetDiff = offset1 - offset2;
        return (offsetDiff != 0) ? offsetDiff : splitOffset1 - splitOffset2;
    }
    
    private static int getSplitOffsetImpl(Position pos) {
        return (pos.getClass() == ComplexPos.class)
                ? ((ComplexPos)pos).getSplitOffset()
                : 0;
    }
    
}
