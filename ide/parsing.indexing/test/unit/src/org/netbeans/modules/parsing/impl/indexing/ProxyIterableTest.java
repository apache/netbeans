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

package org.netbeans.modules.parsing.impl.indexing;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class ProxyIterableTest extends NbTestCase {

    public ProxyIterableTest(final String name) {
        super(name);
    }

    public void testEmpty() {
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(Collections.<Collection<? extends Integer>>emptySet());
        assertFalse(pi.iterator().hasNext());
        try {
            pi.iterator().next();
            assertTrue(false);
        } catch (NoSuchElementException e) {}
    }
    
    public void testSingleList() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd);
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }
    
    public void testMultipleLists() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(3);
        l2.add(4);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(5);
        l3.add(6);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd);
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(4), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(6), it.next());
        assertFalse(it.hasNext());
    }

    public void testEmptyList() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(5);
        l3.add(6);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd);
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(6), it.next());
        assertFalse(it.hasNext());
    }

    public void testDuplicates() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(1);
        l2.add(2);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(1);
        l3.add(2);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd);
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    public void testNoDuplicates() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(1);
        l2.add(2);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(1);
        l3.add(2);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd, false);
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    public void testUnusual() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(3);
        l2.add(4);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(2);
        l3.add(3);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd);
        Iterator<? extends Integer> it = pi.iterator();
        for(int i = 0; i < 20; i++) {
            assertTrue("i=" + i, it.hasNext());
        }
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());
    }

    public void testUnusualNoDuplicates() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(3);
        l2.add(4);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(2);
        l3.add(3);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd, false);
        Iterator<? extends Integer> it = pi.iterator();
        for(int i = 0; i < 20; i++) {
            assertTrue("i=" + i, it.hasNext());
        }
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertFalse(it.hasNext());
    }

    public void testEmptyCached() {
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(Collections.<Collection<? extends Integer>>emptySet(),true,true);
        //First run
        assertFalse(pi.iterator().hasNext());
        try {
            pi.iterator().next();
            assertTrue(false);
        } catch (NoSuchElementException e) {}
        //Second run
        assertFalse(pi.iterator().hasNext());
        try {
            pi.iterator().next();
            assertTrue(false);
        } catch (NoSuchElementException e) {}
    }

    public void testSingleListCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd,true,true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
        //Second run
        it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    public void testMultipleListsCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(3);
        l2.add(4);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(5);
        l3.add(6);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd, true, true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(4), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(6), it.next());
        assertFalse(it.hasNext());
        //First run
        it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(4), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(6), it.next());
        assertFalse(it.hasNext());
    }

    public void testEmptyListCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(5);
        l3.add(6);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd,true,true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(6), it.next());
        assertFalse(it.hasNext());
        //Second run
        it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(6), it.next());
        assertFalse(it.hasNext());
    }

    public void testDuplicatesCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(1);
        l2.add(2);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(1);
        l3.add(2);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd,true,true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
        //Second run
        it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    public void testNoDuplicatesCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(1);
        l2.add(2);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(1);
        l3.add(2);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd, false, true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
        //First second
        it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    public void testUnusualCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(3);
        l2.add(4);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(2);
        l3.add(3);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd,true,true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        for(int i = 0; i < 20; i++) {
            assertTrue("i=" + i, it.hasNext());
        }
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());
        //First run
        it = pi.iterator();
        for(int i = 0; i < 20; i++) {
            assertTrue("i=" + i, it.hasNext());
        }
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());
    }

    public void testUnusualNoDuplicatesCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(3);
        l2.add(4);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(2);
        l3.add(3);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd, false,true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        for(int i = 0; i < 20; i++) {
            assertTrue("i=" + i, it.hasNext());
        }
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertFalse(it.hasNext());
        //Second run
        it = pi.iterator();
        for(int i = 0; i < 20; i++) {
            assertTrue("i=" + i, it.hasNext());
        }
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertFalse(it.hasNext());
    }
}
