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

package org.netbeans.modules.java.source.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import junit.framework.*;

/** Basic class for testing iterators.
 *
 * @author Petr Hrebejk
 */
public class IteratorTest extends TestCase {
    
    private static final int TEST_SEQS = 1000;
    private static final int TEST_SEQ_SIZE = 1011;

    public IteratorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(IteratorTest.class);        
        return suite;
    }
                   
    // Test methods ------------------------------------------------------------
    
    
    public void testChainedIterables () {
        final List<List<Integer>> data = new ArrayList<List<Integer>>();
        final List<Integer> result = new ArrayList<Integer>();
        for (int i=0; i<TEST_SEQS; i++) {
            List<Integer> l = createSequentialList(TEST_SEQ_SIZE);
            data.add (l);
            result.addAll(l);
        }
        Iterable<Integer> i = Iterators.chained(data);
        assertEquals (result,i);
        //Next try, didn't work in prev version
        i = Iterators.chained(data);
        assertEquals (result,i);
    }
    
    public void testFilteredIterables () {
        final List<Integer> data = createSequentialList (TEST_SEQ_SIZE);
        final List<Integer> expected = new LinkedList<Integer>();
        final List<Integer> filtered = new LinkedList<Integer>();
        for (Integer i = 0; i < data.size(); i++) {
            Integer x = data.get(i);
            if (i%2 == 0) {                
                expected.add(x);                
            }
            else {
                filtered.add(x);
            }
        }
        final Iterable<Integer> res = Iterators.filter(data, new Comparable<Integer>() {
            public int compareTo(Integer i) {
                return filtered.contains(i) ? 0 : -1;
            }
        });
        assertEquals (expected,res);
    }
    
    private static void assertEquals (List<Integer> expected, Iterable<Integer> data) {
        Iterator<Integer> it = data.iterator();
        for (Integer i : expected) {
            assertTrue (it.hasNext());
            assertEquals(i, it.next());
        }
    }
    
    
    public static List<Integer> createSequentialList( int size ) {        
        List<Integer> result = new ArrayList<Integer>( size );                
        for( int i = 0; i < size; i++ ) {
            result.add( new Integer(i) );
        }
        return result;
    }
        
}
