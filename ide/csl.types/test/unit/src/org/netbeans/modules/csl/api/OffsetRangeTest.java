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

package org.netbeans.modules.csl.api;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.junit.NbTestCase;

/**
 * @author Tor Norbye
 */
public class OffsetRangeTest extends NbTestCase {

    public OffsetRangeTest(String testName) {
        super(testName);
    }

    public void testOverlaps() {
        OffsetRange range1 = new OffsetRange(1, 4);
        OffsetRange range2 = new OffsetRange(2, 6);
        OffsetRange range3 = new OffsetRange(4, 6);
        OffsetRange range4 = new OffsetRange(0, 1);
        OffsetRange range5 = new OffsetRange(0, 6);
        assertTrue(range1.overlaps(range2));
        assertTrue(range2.overlaps(range1));
        assertFalse(range1.overlaps(range3));
        assertFalse(range3.overlaps(range1));
        assertFalse(range1.overlaps(range4));
        assertFalse(range4.overlaps(range1));
        assertTrue(range1.overlaps(range5));
        assertTrue(range5.overlaps(range1));
        
        assertFalse(range1.overlaps(OffsetRange.NONE));
        assertFalse(OffsetRange.NONE.overlaps(range5));
        assertFalse(OffsetRange.NONE.overlaps(OffsetRange.NONE));
    }
    
    public void testGetStart() {
        OffsetRange range = new OffsetRange(1, 4);
        assertEquals(1, range.getStart());
    }

    public void testGetEnd() {
        OffsetRange range = new OffsetRange(1, 4);
        assertEquals(4, range.getEnd());
    }

    public void testGetLength() {
        OffsetRange range = new OffsetRange(1, 4);
        assertEquals(3, range.getLength());
    }

    public void testContainsInclusive() {
        OffsetRange range = new OffsetRange(1, 4);
        assertTrue(range.containsInclusive(1));
        assertTrue(range.containsInclusive(3));
        assertTrue(range.containsInclusive(4));
        assertFalse(range.containsInclusive(5));
        assertFalse(range.containsInclusive(0));
    }
    
    public void testEquals() {
        assertEquals(new OffsetRange(1,3), new OffsetRange(1,3));
        assertEquals(new OffsetRange(0,0), new OffsetRange(0,0));
        assertFalse(new OffsetRange(1,3).equals(new Object()));

        boolean success = false;
        try {
            // Should generate an assertion!
            new OffsetRange(9,8);
        } catch (AssertionError e) {
            success = true;
        }
        assertTrue(success);
    }
    
    public void testComparator() {
        assertTrue(new OffsetRange(1,3).compareTo(new OffsetRange(3,5)) < 0);
        assertTrue(new OffsetRange(3,5).compareTo(new OffsetRange(1,3)) > 0);
        assertTrue(new OffsetRange(3,5).compareTo(new OffsetRange(3,5)) == 0);
        assertTrue(new OffsetRange(1,3).compareTo(new OffsetRange(1,5)) < 0);
        assertTrue(new OffsetRange(1,5).compareTo(new OffsetRange(1,3)) > 0);
    }
    
    public void testEmpty() {
        assertTrue(new OffsetRange(5,5).isEmpty());
        assertFalse(new OffsetRange(5,6).isEmpty());
    }

    public void testBoundTo() {
        assertEquals(new OffsetRange(1,3), new OffsetRange(1,3).boundTo(1, 3));
        assertEquals(new OffsetRange(1,3), new OffsetRange(0,4).boundTo(1, 3));
        assertEquals(new OffsetRange(1,3), new OffsetRange(1,3).boundTo(0, 4));
        assertEquals(new OffsetRange(1,2), new OffsetRange(1,3).boundTo(0, 2));
        assertEquals(new OffsetRange(2,3), new OffsetRange(1,3).boundTo(2, 4));
        assertEquals(new OffsetRange(2,2), new OffsetRange(1,3).boundTo(2, 2));
        assertEquals(new OffsetRange(101,101), new OffsetRange(102,103).boundTo(0, 101));
        assertEquals(new OffsetRange(100,101), new OffsetRange(100,103).boundTo(0, 101));
        assertEquals(new OffsetRange(100,100), new OffsetRange(90,95).boundTo(100, 150));
    }
}
