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

import java.lang.ref.WeakReference;
import junit.framework.TestCase;

/**
 *
 * @author tim
 */
public class PairMapTest extends TestCase {

    public PairMapTest(String testName) {
        super(testName);
    }

    private PairMap map;
    protected void setUp() throws Exception {
        map = new PairMap();
    }

    /**
     * Test of size method, of class org.netbeans.core.output2.PairMap.
     */
    public void testSize() {
        System.out.println("testSize");

        for (int i=0; i < 10; i++) {
            String name = "Item " + i;
            map.add (name, new NbIO(name));
        }
        
        assertTrue ("Map.size returns " + map.size() + " but should return 10",
            map.size() == 10);
        
    }
    
    /**
     * Test of isEmpty method, of class org.netbeans.core.output2.PairMap.
     */
    public void testIsEmpty() {
        assertTrue ("Map should be initially empty", map.isEmpty());
        
        String[] s = new String[10];
        for (int i=0; i < s.length; i++) {
            s[i] = "Item " + i;
            map.add (s[i], new NbIO(s[i]));
        }
        
        assertFalse("Map should not be empty, it should contain 10 items", 
            map.isEmpty());
        
        for (int i=0; i < s.length; i++) {
            s[i] = "Item " + i;
            map.remove(s[i]);
        }
        
        assertTrue ("After removing the 10 items added, map should be empty",
            map.isEmpty());
        
        map.setWeak(true);
        for (int i=0; i < 5; i++) {
            System.gc();
        }
        assertTrue ("After gc'ing, map should be empty", map.isEmpty());
    }
    
    /**
     * Test of setWeak method, of class org.netbeans.core.output2.PairMap.
     */
    public void testSetWeak() {
        System.out.println("testSetWeak");
        
        String[] s = new String[10];
        for (int i=0; i < s.length; i++) {
            s[i] = "Item " + i;
            map.add (s[i], new NbIO(s[i]));
        }
        
        Object[] vals = map.vals;
        for (int i=0; i < vals.length; i++) {
            assertTrue ("Should be null or NbIO, not " + vals[i].getClass() + " at " + i,
            vals[i] == null || vals[i] instanceof NbIO);
        }

        map.setWeak(true);
        for (int i=0; i < vals.length; i++) {
            assertTrue ("Should be null or NbIO, not " + vals[i].getClass() + " at " + i,
            vals[i] == null || vals[i] instanceof WeakReference);
        }
        
        map.setWeak(false);
        for (int i=0; i < vals.length; i++) {
            assertTrue ("Should be null or NbIO, not " + vals[i].getClass() + " at " + i,
            vals[i] == null || vals[i] instanceof NbIO);
        }
        
        map.clear();
        NbIO[] ios = new NbIO[s.length];
        for (int i=0; i < s.length; i++) {
            NbIO io = new NbIO(s[i]);
            if (i % 2 == 0) {
                ios[i] = io;
            }
            s[i] = "Item " + i;
            map.add (s[i], io);
        }
        
        map.setWeak(true);
        for (int i=0; i < 5; i++) {
            System.gc();
        }
        int size = map.size();
        assertTrue ("Five object should have been garbage collected", size == 5);
        ios = null;
        for (int i=0; i < 5; i++) {
            System.gc();
        }
        
        size = map.size();
        assertTrue ("All object should have been garbage collected, but size is " + size, size == 0);
        
    }
    
}
