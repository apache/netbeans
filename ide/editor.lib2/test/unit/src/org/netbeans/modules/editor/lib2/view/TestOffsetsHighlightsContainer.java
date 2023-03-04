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
package org.netbeans.modules.editor.lib2.view;

import javax.swing.text.AttributeSet;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;

/**
 *
 * @author Miloslav Metelka
 */
public class TestOffsetsHighlightsContainer extends AbstractHighlightsContainer {
    
    int[] offsetPairs = new int[0];
    
    public void setOffsetPairs(int[] offsetPairs) {
        this.offsetPairs = offsetPairs;
        fireHighlightsChange(0, Integer.MAX_VALUE);
    }

    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return new HiSeq();
    }

    private final class HiSeq implements HighlightsSequence {
        
        int index;

        @Override
        public boolean moveNext() {
            if (index < offsetPairs.length) {
                index += 2;
                return true;
            }
            return false;
        }

        @Override
        public int getStartOffset() {
            return offsetPairs[index - 2];
        }

        @Override
        public int getEndOffset() {
            return offsetPairs[index - 1];
        }

        @Override
        public AttributeSet getAttributes() {
            return ViewUpdatesTesting.FONT_ATTRS[0];
        }
        
    }
    
}
