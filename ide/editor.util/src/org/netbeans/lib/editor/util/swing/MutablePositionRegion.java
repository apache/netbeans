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

public class MutablePositionRegion extends PositionRegion {

    /**
     * Construct new mutable position region.
     *
     * @param startPosition non-null start position of the region &lt;= end position.
     * @param endPosition non-null end position of the region &gt;= start position.
     */
    public MutablePositionRegion(Position startPosition, Position endPosition) {
        super(startPosition, endPosition);
    }
    
    /**
     * Construct new mutable position region based on the knowledge
     * of the document and starting and ending offset.
     */
    public MutablePositionRegion(Document doc, int startOffset, int endOffset) throws BadLocationException {
        this(doc.createPosition(startOffset), doc.createPosition(endOffset));
    }

    /**
     * Set a new start and end positions of this region.
     * <br>
     * They should satisfy
     * {@link #getStartOffset()} &lt;= {@link #getEndOffset()}.
     *
     * @param startPosition non-null new start position of this region.
     * @since 1.10
     */
    public final void reset(Position startPosition, Position endPosition) {
        resetImpl(startPosition, endPosition);
    }
    
    /**
     * Set a new start position of this region.
     * <br>
     * It should satisfy
     * {@link #getStartOffset()} &lt;= {@link #getEndOffset()}.
     *
     * @param startPosition non-null new start position of this region.
     */
    public final void setStartPosition(Position startPosition) {
        setStartPositionImpl(startPosition);
    }

    /**
     * Set a new end position of this region.
     * <br>
     * It should satisfy
     * {@link #getStartOffset()} &lt;= {@link #getEndOffset()}.
     *
     * @param endPosition non-null new start position of this region.
     */
    public final void setEndPosition(Position endPosition) {
        setEndPositionImpl(endPosition);
    }

}
