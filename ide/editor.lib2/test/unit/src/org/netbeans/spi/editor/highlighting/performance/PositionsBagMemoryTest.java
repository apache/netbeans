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

package org.netbeans.spi.editor.highlighting.performance;

import java.lang.reflect.Method;
import java.util.Collections;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.junit.MemoryFilter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.spi.editor.highlighting.*;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;

/**
 *
 * @author Vita Stejskal
 */
public class PositionsBagMemoryTest extends NbTestCase {

    private static final int CNT = 1000;
    
    /** Creates a new instance of HighlightsBagPerformanceTest */
    public PositionsBagMemoryTest(String name) {
        super(name);
    }
    
    public void testMemoryConsumptionBestCase() {
        checkMemoryConsumption(false, true);
    }
    
    public void testMemoryConsumptionWorstCase() {
        checkMemoryConsumption(false, false);
    }
    
    public void testIteratorMemoryConsumptionBestCase() {
        checkIteratorMemoryConsumption(false, true);
    }
    
    public void testIteratorMemoryConsumptionWorstCase() {
        checkIteratorMemoryConsumption(false, false);
    }
    
    public void testMergingBagMemoryConsumptionBestCase() {
        checkMemoryConsumption(true, true);
    }
    
    public void testMergingBagMemoryConsumptionWorstCase() {
        checkMemoryConsumption(true, false);
    }
    
    public void testMergingBagIteratorMemoryConsumptionBestCase() {
        checkIteratorMemoryConsumption(true, true);
    }
    
    public void testMergingBagIteratorMemoryConsumptionWorst() {
        checkIteratorMemoryConsumption(true, false);
    }

    private void checkMemoryConsumption(boolean merging, boolean bestCase) {
        PositionsBag bag = new PositionsBag(new PlainDocument(), merging);

        for(int i = 0; i < CNT; i++) {
            if (bestCase) {
                bag.addHighlight(new SimplePosition(i * 10), new SimplePosition((i + 1) * 10), SimpleAttributeSet.EMPTY);
            } else {
                bag.addHighlight(new SimplePosition(i * 10), new SimplePosition(i* 10 + 5), SimpleAttributeSet.EMPTY);
            }
        }

        compact(bag);
        
        assertSize("PositionsBag of " + CNT + " highlights " + (bestCase ? "(best case)" : "(worst case)"),
            Collections.singleton(bag), bestCase ? 8500 : 16500, new MF());
    }

    private void checkIteratorMemoryConsumption(boolean merging, boolean bestCase) {
        PositionsBag bag = new PositionsBag(new PlainDocument(), merging);

        for(int i = 0; i < CNT; i++) {
            if (bestCase) {
                bag.addHighlight(new SimplePosition(i * 10), new SimplePosition((i + 1) * 10), SimpleAttributeSet.EMPTY);
            } else {
                bag.addHighlight(new SimplePosition(i * 10), new SimplePosition(i* 10 + 5), SimpleAttributeSet.EMPTY);
            }
        }

        compact(bag);
        
        HighlightsSequence sequence = bag.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);

        // Do not iterate through the whole sequence, otherwise it will discard its contents
        for(int i = 0; i < CNT - 1; i++) {
            boolean hasHighlight = sequence.moveNext();

            assertTrue("Wrong number of highlights in the sequence; found only " + i, hasHighlight);
            assertEquals("Wrong start offset of " + i + ". highlight", 
                i * 10, sequence.getStartOffset());
            assertEquals("Wrong end offset of " + i + ". highlight", 
                bestCase ? (i + 1) * 10 : i * 10 + 5, sequence.getEndOffset());
            assertSame("Wrong attributes of " + i + ". highlight", SimpleAttributeSet.EMPTY, sequence.getAttributes());
        }
        
        assertSize("HighlightsSequence of " + CNT + " highlights " + (bestCase ? "(best case)" : "(worst case)"),
            Collections.singleton(sequence), bestCase ? 8500 : 16500, new MF());
    }

    @SuppressWarnings("unchecked")
    private void compact(PositionsBag bag) {
        try {
            Method m = bag.getClass().getDeclaredMethod("getMarks");
            m.setAccessible(true);
            GapList<Position> marks = (GapList<Position>) m.invoke(bag);
            marks.trimToSize();

            m = bag.getClass().getDeclaredMethod("getAttributes");
            m.setAccessible(true);
            GapList<Position> attributes = (GapList<Position>) m.invoke(bag);
            attributes.trimToSize();
        } catch (Exception e) {
            AssertionError ae = new AssertionError(e.getMessage());
            ae.initCause(e);
            throw ae;
        }
    }
    
    private static final class MF implements MemoryFilter {
        public boolean reject(Object obj) {
            if (Position.class.isAssignableFrom(obj.getClass()) ||
                AttributeSet.class.isAssignableFrom(obj.getClass()) ||
                Document.class.isAssignableFrom(obj.getClass()))
            {
                return true;
            } else {
                return false;
            }
        }
    } // End of MF class
}
