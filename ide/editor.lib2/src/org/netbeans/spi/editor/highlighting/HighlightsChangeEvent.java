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

package org.netbeans.spi.editor.highlighting;

import java.util.EventObject;

/**
 * An event object notifying about a change in highlighting of certain area
 * of a document. The area where the highlighting has changed is specified by
 * its starting and ending offsets. Whoever receives this event should consider
 * re-requesting the new list of highlighted areas from the 
 * <code>HighlightsContainer</code> that fired the event.
 *
 * @author Vita Stejskal
 */
public final class HighlightsChangeEvent extends EventObject {
    
    private int startOffset;
    private int endOffset;
    
    /** 
     * Creates a new instance of <code>HighlightsChangeEvent</code>. The
     * <code>startOffset</code> and <code>endOffset</code> parameters specify
     * the area of a document where highlighting has changed. It's possible to
     * use <code>Integer.MAX_VALUE</code> for the <code>endOffset</code> parameter
     * meaning that the end of the change is unknown or the change spans up to
     * the end of a document.
     *
     * @param source         The highlight layer that fired this event.
     * @param startOffset    The beginning of the area that has changed.
     * @param endOffset      The end of the changed area.
     */
    public HighlightsChangeEvent(HighlightsContainer source, int startOffset, int endOffset) {
        super(source);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    /**
     * Gets the beginning of an area in the document where highlighting has
     * changed.
     *
     * @return The starting offset of the chaged area. Should always be greater than
     *         or equal to zero.
     */
    public int getStartOffset() {
        return startOffset;
    }
    
    /**
     * Gets the end of an area in the document where highlighting has
     * changed.
     *
     * @return The ending offset of the chaged area. May return <code>Integer.MAX_VALUE</code>
     *         if the ending position is unknown.
     */
    public int getEndOffset() {
        return endOffset;
    }
}
