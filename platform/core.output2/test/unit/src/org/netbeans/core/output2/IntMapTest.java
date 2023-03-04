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

import java.util.Arrays;
import junit.framework.TestCase;

/**
 *
 * @author Tim Boudreau
 */
public class IntMapTest extends TestCase {

    public IntMapTest(String testName) {
        super(testName);
    }

    public void testFirst() {
        System.out.println("testFirst");
        IntMap map = new IntMap();

        int[] indices = new int [] { 5, 12, 23, 62, 247, 375, 489, 5255};

        Object[] values = new Object[] {
            "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", 
            "seventh"};
        
        assert indices.length == values.length;
        
        for (int i=0; i < indices.length; i++) {
            map.put (indices[i], values[i]);
        }
        
        assertTrue ("First entry should be 5", map.first() == 5);
    }
    
    public void testNextEntry() {
        System.out.println("testNextEntry");
        IntMap map = new IntMap();
        
        int[] indices = new int [] { 5, 12, 23, 62, 247, 375, 489, 5255};
        
        Object[] values = new Object[] {
            "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", 
            "seventh"};
        
        assert indices.length == values.length;
        
        for (int i=0; i < indices.length; i++) {
            map.put (indices[i], values[i]);
        }
        
        for (int i=0; i < indices.length-1; i++) {
            int val = indices[i+1];
            int next = map.nextEntry (indices[i]);
            assertTrue ("Entry after " + indices[i] + " should be " + val + " not " + next, next == val);
        }
    }
    
    public void testPrevEntry() {
        System.out.println("testPrevEntry");
        IntMap map = new IntMap();
        
        int[] indices = new int [] { 5, 12, 23, 62, 247, 375, 489, 5255};
        
        Object[] values = new Object[] {
            "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", 
            "seventh"};
        
        assert indices.length == values.length;
        
        for (int i=0; i < indices.length; i++) {
            map.put (indices[i], values[i]);
        }
        
        for (int i=indices.length-1; i > 0; i--) {
            int val = indices[i-1];
            int next = map.prevEntry (indices[i]);
            assertTrue ("Entry before " + indices[i] + " should be " + val + " not " + next, next == val);
        }
    }
    
    public void testNearest() {
        System.out.println("testNearest");
        IntMap map = new IntMap();
        
        int[] indices = new int [] { 5, 12, 23, 62, 247, 375, 489, 5255};
        
        Object[] values = new Object[] {
            "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", 
            "seventh"};
        
        assert indices.length == values.length;
        
        for (int i=0; i < indices.length; i++) {
            map.put (indices[i], values[i]);
        }
        
        for (int i=0; i < indices.length-1; i++) {
            int toTest = indices[i] + ((indices[i+1] - indices[i]) / 2);
            int next = map.nearest (toTest, false);
            assertTrue ("Nearest value to " + toTest + " should be " + indices[i+1] + ", not " + next, next == indices[i+1]);
        }
        
        assertTrue ("Value after last entry should be 0th", map.nearest (indices[indices.length-1] + 1000, false) == indices[0]);
        
        assertTrue ("Value before first entry should be last", map.nearest (-1, true) == indices[indices.length-1]);
        
        assertTrue ("Value after < first entry should be 0th", map.nearest (-1, false) == indices[0]);
        
        for (int i = indices.length-1; i > 0; i--) {
//            int toTest = indices[i] - (indices[i-1] + ((indices[i] - indices[i-1]) / 2));
            int toTest = indices[i-1] + ((indices[i] - indices[i-1]) / 2);
            int prev = map.nearest (toTest, true);
            assertTrue ("Nearest value to " + toTest + " should be " + indices[i-1] + ", not " + prev, prev == indices[i-1]);
        }
        
        assertTrue ("Entry previous to value lower than first entry should be last entry", 
            map.nearest(indices[0] - 1, true) == indices[indices.length -1]);
        
        assertTrue ("Value after > last entry should be last 0th", map.nearest(indices[indices.length-1] + 100, false) == indices[0]);
        
        assertTrue ("Value before > last entry should be last entry", map.nearest(indices[indices.length-1] + 100, true) == indices[indices.length-1]);
        
        assertTrue ("Value after < first entry should be 0th", map.nearest(-10, false) == indices[0]);
        
    }    
    
    
    /**
     * Test of get method, of class org.netbeans.core.output2.IntMap.
     */
    public void testGet() {
        System.out.println("testGet");
        
        IntMap map = new IntMap();
        
        int[] indices = new int [] { 5, 12, 23, 62, 247, 375, 489, 5255};
        
        Object[] values = new Object[] {
            "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", 
            "seventh"};
        
        assert indices.length == values.length;
        
        for (int i=0; i < indices.length; i++) {
            map.put (indices[i], values[i]);
        }
        
        for (int i=0; i < indices.length; i++) {
            assertTrue (map.get(indices[i]) == values[i]);
        }
    }
    
    public void testGetKeys() {
        IntMap map = new IntMap();
        
        int[] indices = new int [] { 5, 12, 23, 62, 247, 375, 489, 5255};
        
        Object[] values = new Object[] {
            "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", 
            "seventh"};
            
        for (int i=0; i < indices.length; i++) {
            map.put (indices[i], values[i]);
        }            

        int[] keys = map.getKeys();
        assertTrue ("Keys returned should match those written.  Expected: " + i2s(indices) + " Got: " + i2s(keys), Arrays.equals(keys, indices));
    }
    
    private static String i2s (int[] a) {
        StringBuffer result = new StringBuffer(a.length*2);
        for (int i=0; i < a.length; i++) {
            result.append (a[i]);
            if (i != a.length-1) {
                result.append(',');
            }
        }
        return result.toString();
    }
}
