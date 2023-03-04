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
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.lib.editor.util.ListenerList;
import org.netbeans.lib.editor.util.random.RandomTestContainer;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.SplitOffsetHighlightsSequence;

/**
 *
 * @author mmetelka
 */
public class DirectMergeContainerTest {
    
    public DirectMergeContainerTest() {
    }

    @Test
    public void testSingleLayer() throws Exception {
        RandomTestContainer container = HighlightsMergeTesting.createContainer();
        AttributeSet attrs0 = HighlightsMergeTesting.attrSets[0];
        AttributeSet attrs1 = HighlightsMergeTesting.attrSets[1];
        AttributeSet attrs2 = HighlightsMergeTesting.attrSets[2];
        HighlightsMergeTesting.addFixedLayer(container.context(), 1000, 10, 12, attrs0, 12, 15, attrs1, 15, 20, attrs2);
        HighlightsMergeTesting.checkMerge(container.context(), false);
    }

    @Test
    public void testSimpleMerges() throws Exception {
        RandomTestContainer container = HighlightsMergeTesting.createContainer();
        AttributeSet attrs0 = HighlightsMergeTesting.attrSets[0];
        AttributeSet attrs1 = HighlightsMergeTesting.attrSets[1];
        AttributeSet attrs2 = HighlightsMergeTesting.attrSets[2];
        AttributeSet attrs3 = HighlightsMergeTesting.attrSets[3];
        AttributeSet attrs4 = HighlightsMergeTesting.attrSets[4];
        HighlightsMergeTesting.addFixedLayer(container.context(), 1000, 10, 12, attrs0);
        HighlightsMergeTesting.addFixedLayer(container.context(), 2000, 5, 15, attrs1);
        HighlightsMergeTesting.addFixedLayer(container.context(), 3000, 5, 11, attrs2);
        HighlightsMergeTesting.addFixedLayer(container.context(), 4000, 13, 20, attrs3);
        HighlightsMergeTesting.addFixedLayer(container.context(), 4000, 25, 40, attrs4);
        HighlightsMergeTesting.checkMerge(container.context(), false);
    }

    @Test
    public void testRandomMerges() throws Exception {
        RandomTestContainer container = HighlightsMergeTesting.createContainer();
        container.setName("testRandomMerges");
        RandomTestContainer.Round round = HighlightsMergeTesting.addRound(container);
        round.setOpCount(100);
//        container.setLogOp(true);
//        HighlightsMergeTesting.setLogChecks(true);
        container.runInit(1303832573413L);
        container.runOps(1);
        container.runOps(1);
        container.runOps(0); // Run till end

//        container.run(0L);
    }
    
    @Test
    public void testShiftHighlights() {
        AttributeSet attrs0 = HighlightsMergeTesting.attrSets[0];
        AttributeSet attrs1 = HighlightsMergeTesting.attrSets[1];
        AttributeSet attrs2 = HighlightsMergeTesting.attrSets[2];
        AttributeSet attrs3 = HighlightsMergeTesting.attrSets[3];
        SplitOffsetLayer sl0 = new SplitOffsetLayer(1, 0, 2, 1, attrs0, 4, 2, 4, 5, attrs1);
        SplitOffsetLayer sl1 = new SplitOffsetLayer(1, 1, 2, 0, attrs2, 3, 1, 4, 3, attrs3);
        DirectMergeContainer dmc = new DirectMergeContainer(new HighlightsContainer[]{ sl0, sl1 }, false);
        SplitOffsetHighlightsSequence shs = (SplitOffsetHighlightsSequence) dmc.getHighlights(0, Integer.MAX_VALUE);
        assertHighlight(shs, 1, 0, 1, 1, attrs0);
        assertHighlight(shs, 1, 1, 2, 0, attrs2);
        assertHighlight(shs, 2, 0, 2, 1, attrs0);
        assertHighlight(shs, 3, 1, 4, 2, attrs3);
        assertHighlight(shs, 4, 2, 4, 3, attrs3);
        assertHighlight(shs, 4, 3, 4, 5, attrs1);
        
    }
    
    private static void assertHighlight(SplitOffsetHighlightsSequence shs, int startOffset, int startShift,
            int endOffset, int endShift, AttributeSet attrs)
    {
        assertTrue(shs.moveNext());
        assertEquals(startOffset, shs.getStartOffset());
        assertEquals(startShift, shs.getStartSplitOffset());
        assertEquals(endOffset, shs.getEndOffset());
        assertEquals(endShift, shs.getEndSplitOffset());
        assertEquals(attrs, shs.getAttributes());
    }

    private static final class SplitOffsetLayer implements HighlightsContainer {
        
        private final ListenerList<HighlightsChangeListener> listenerList = new ListenerList<>();
        
        private final Object[] highlights;

        public SplitOffsetLayer(Object... highlights) { // [startOffset, startShift, endOffset, endShift, attrs]...
            this.highlights = highlights;
        }
        
        @Override
        public HighlightsSequence getHighlights(int startOffset, int endOffset) {
            return new HS();
        }

        @Override
        public void addHighlightsChangeListener(HighlightsChangeListener listener) {
            listenerList.add(listener);
        }

        @Override
        public void removeHighlightsChangeListener(HighlightsChangeListener listener) {
            listenerList.remove(listener);
        }
        
        private final class HS implements SplitOffsetHighlightsSequence {
            
            private int index = -1;

            @Override
            public boolean moveNext() {
                index++;
                if (5 * index < highlights.length) {
                    return true;
                }
                index--;
                return false;
            }

            @Override
            public int getStartOffset() {
                return (Integer) highlights[5 * index];
            }

            @Override
            public int getStartSplitOffset() {
                return (Integer) highlights[5 * index + 1];
            }

            @Override
            public int getEndOffset() {
                return (Integer) highlights[5 * index + 2];
            }

            @Override
            public int getEndSplitOffset() {
                return (Integer) highlights[5 * index + 3];
            }

            @Override
            public AttributeSet getAttributes() {
                return (AttributeSet) highlights[5 * index + 4];
            }
            
            
        }
        
        
    }
}
