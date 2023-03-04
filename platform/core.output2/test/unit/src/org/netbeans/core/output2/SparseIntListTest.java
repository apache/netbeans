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

package org.netbeans.core.output2;

import junit.framework.TestCase;

/**
 * Test for org.netbeans.core.output2.SparseIntList.
 *
 * @author Tim Boudreau
 */
public class SparseIntListTest extends TestCase {

    public SparseIntListTest(String testName) {
        super(testName);
    }

    private SparseIntList l = null;
    @Override
    protected void setUp() throws Exception {
        l = new SparseIntList(20);
    }

    public void testGetLessThanZeroReturnsZero() {
        System.out.println("testGetLessThanZeroReturnsZero");
        assertTrue (l.get(-1) == 0);
        assertTrue (l.get(Integer.MIN_VALUE) == 0);
    }
    
    public void testGetFromEmptyListReturnsRequestedIndex() {
        System.out.println("testGetFromEmptyListReturnsRequestedIndex");
        assertTrue (l.get(20) == 21);
        assertTrue (l.get(Integer.MAX_VALUE-1) == Integer.MAX_VALUE);
        assertTrue (l.get(1) == 2);
        assertTrue (l.get(0) == 1);
    }
    
    public void testGetBelowFirstEntryReturnsIndex() {
        System.out.println("testGetBelowFirstEntryReturnsIndex");
        l.add (11, 20);
        for (int i = 0; i < 11; i++) {
            assertTrue (l.get(i) == i+1);
        }
    }
    
    public void testAdd() {
        System.out.println("testAdd");
        l.add (11, 20);
        int val = l.get(11);
        assertTrue ("After add(11, 20), value at 11 should be 20, not " + val, val == 20);
        val = l.get(12);
        assertTrue ("After add(11, 20), value at 12 should be 21, not " + val, val == 21);
        
        l.add (12, 30);
        val = l.get(12);
        assertTrue ("After add(12, 30), value at 12 should be 30, not " + val, val == 30);
        val = l.get(13);
        assertTrue ("After add(12, 30), value at 13 should be 31, not " + val, val == 31);
        
        l.add (30, 80);
        val = l.get(12);
        assertTrue ("After add(30, 80), value at 12 should still be 30, not " + val + " adding an entry above should not change it", val == 30);
        val = l.get(13);
        assertTrue ("After add(30, 80), value at 13 should be 31, not " + val + " adding an entry above should not change it", val == 31);
        val = l.get(31);
        assertTrue ("After add(30, 80), value at 31 should be 81, not " + val, val == 81);
        
        for (int i=0; i < 10; i++) {
            val = l.get(i);
            assertTrue ("In a populated map, get() on an index below the first added entry should return the index, but get(" + i + ") returns " + val, val == i+1);
        }
        
    }
    
    public void testBadValuesThrowExceptions() {
        System.out.println("testBadValuesThrowExceptions");
        l.add (20, 11);
        Exception e = null;
        try {
            l.add (19, 13);
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
        
        try {
            l.add (21, 10);
        } catch (Exception ex2) {
            e = ex2;
        }
        assertNotNull(e);
        
    }
    
    public void testGetAboveFirstEntryReturnsEntryPlusIndexDiff() {
        System.out.println("testGetAboveFirstEntryReturnsEntryPlusIndexDiff");
        l.add (11, 20);
        int x = 21;
        for (int i = 12; i < 40; i++){
            assertTrue ("Entry at " + i + " should be " + x + ", not " + l.get(i), l.get(i) == x);
            x++;
        }
    }
}
