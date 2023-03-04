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

/**
 * Highlights sequence that supports split offsets in addition to regular offsets.
 * This allows to color individual spaces within a tab character
 * or to color extra virtual characters at line end (as a split of newline character).
 *
 * @author Miloslav Metelka
 * @since 2.13.0
 */
public interface SplitOffsetHighlightsSequence extends HighlightsSequence {

    /**
     * Get zero-based offset "within" a character (usually tab or newline
     * to which {@link #getStartOffset()} points to) that starts a highlight.
     * <br>
     * Zero should be returned if the character is not intended to be split.
     * <br>
     * To highlight second and third space of a tab character at offset == 123
     * the {@link #getStartOffset() } == {@link #getEndOffset() } == 123
     * and {@link #getStartSplitOffset() } == 1 and {@link #getEndSplitOffset() } == 3.
     *
     * @return &gt;=0 start split offset.
     * @see #getStartOffset() 
     */
    int getStartSplitOffset();
    
    /**
     * Get zero-based offset "within" a character (usually tab or newline
     * to which {@link #getEndOffset()} points to) that ends a highlight.
     * <br>
     * Zero should be returned if the character is not intended to be split.
     * <br>
     * Get end of a highlight "within" a particular character (either tab or newline)
     * while {@link #getEndOffset()} points to the tab or newline character.
     *
     * @return &gt;=0 end split offset.
     * @see #getStartSplitOffset() 
     */
    int getEndSplitOffset();

}
