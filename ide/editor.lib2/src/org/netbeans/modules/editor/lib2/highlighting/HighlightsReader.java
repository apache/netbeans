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

package org.netbeans.modules.editor.lib2.highlighting;

import javax.swing.text.AttributeSet;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 * List of highlights that can dynamically add/remove existing highlights fed from a highlights sequence.
 *
 * @author Miloslav Metelka
 */
public final class HighlightsReader {
    
    private final HighlightsList highlightsList;

    private final CoveringHighlightsSequence cHighlightsSequence;
    
    private final int endOffset;
    
    public HighlightsReader(HighlightsContainer highlightsContainer, int startOffset, int endOffset) {
        // Expecting that highlights are either bottomHighlights or topHighlights of HighlightingManager which are both
        // DirectMergeContainer instances
        this.cHighlightsSequence = (CoveringHighlightsSequence) highlightsContainer.getHighlights(startOffset, endOffset);
        assert cHighlightsSequence.isCovering() : "Non-covering HS=" + cHighlightsSequence;
        this.highlightsList = new HighlightsList(startOffset);
        this.endOffset = endOffset;
    }
    
    public HighlightsSequence highlightsSequence() {
        return cHighlightsSequence;
    }
    
    public HighlightsList highlightsList() {
        return highlightsList;
    }
    
    public void readUntil(int offset) {
        int hlEndOffset = highlightsList.endOffset();
        int hlEndSplitOffset = highlightsList.endSplitOffset();
        while (cHighlightsSequence.moveNext()) {
            int hlStartOffset = cHighlightsSequence.getStartOffset();
            int hlStartSplitOffset = cHighlightsSequence.getStartSplitOffset();
            if (hlStartOffset > hlEndOffset || hlStartOffset == hlEndOffset && hlStartSplitOffset > hlEndSplitOffset) {
                HighlightItem fillItem;
                if (hlStartSplitOffset != 0) {
                    fillItem = new SplitOffsetHighlightItem(hlStartOffset, hlStartSplitOffset, null);
                } else {
                    fillItem = new HighlightItem(hlStartOffset, null);
                }
                highlightsList.add(fillItem);
            }
            hlEndOffset = cHighlightsSequence.getEndOffset();
            hlEndSplitOffset = cHighlightsSequence.getEndSplitOffset();
            HighlightItem item;
            AttributeSet attrs = cHighlightsSequence.getAttributes();
            if (hlEndSplitOffset != 0) {
                item = new SplitOffsetHighlightItem(hlEndOffset, hlEndSplitOffset, attrs);
            } else {
                item = new HighlightItem(hlEndOffset, attrs);
            }
            highlightsList.add(item);
            if (hlEndOffset >= offset) {
                return;
            }
        }
        // Highlights from highlightsSequence end below endOffset
        if (hlEndOffset < endOffset) { // Add extra highlight till endOffset
            highlightsList.add(new HighlightItem(endOffset, null));
        }
    }

}
