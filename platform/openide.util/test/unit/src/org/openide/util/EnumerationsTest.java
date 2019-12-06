/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Enumerations;

/** This is the base test for new and old enumerations. It contains
 * factory methods for various kinds of enumerations and set of tests
 * that use them. Factory methods are overriden in OldEnumerationsTest
 *
 * @author Jaroslav Tulach
 */
public class EnumerationsTest extends NbTestCase {
    
    /** Creates a new instance of EnumerationsTest */
    public EnumerationsTest(String testName) {
        super(testName);
    }
    
    //
    // Factory methods
    //
    
    protected <T> Enumeration<T> singleton(T obj) {
        return Enumerations.singleton(obj);
    }
    protected <T> Enumeration<T> concat(Enumeration<T> en1, Enumeration<T> en2) {
        return Enumerations.concat(en1, en2);
    }
    protected <T> Enumeration<T> concat(Enumeration<Enumeration<T>> enumOfEnums) {
        return Enumerations.concat(enumOfEnums);
    }
    protected <T> Enumeration<T> removeDuplicates(Enumeration<T> en) {
        return Enumerations.removeDuplicates(en);
    }
    protected <T> Enumeration<T> empty() {
        return Enumerations.empty();
    }
    protected <T> Enumeration<T> array(T[] arr) {
        return Enumerations.array(arr);
    }
    protected <T,R> Enumeration<R> convert(Enumeration<T> en, final Map<T,R> map) {
        class P implements Enumerations.Processor<T,R> {
            public R process(T obj, Collection<T> nothing) {
                return map.get(obj);
            }
        }
        return Enumerations.convert(en, new P());
    }
    protected <T> Enumeration<T> removeNulls(Enumeration<T> en) {
        return Enumerations.removeNulls(en);
    }
    protected <T> Enumeration<T> filter(Enumeration<T> en, final Set<T> filter) {
        class P implements Enumerations.Processor<T,T> {
            public T process(T obj, Collection<T> nothing) {
                return filter.contains(obj) ? obj : null;
            }
        }
        return Enumerations.filter(en, new P());
    }
    
    protected <T,R> Enumeration<R> filter(Enumeration<T> en, final QueueProcess<T,R> filter) {
        class P implements Enumerations.Processor<T,R> {
            public R process(T obj, Collection<T> nothing) {
                return filter.process(obj, nothing);
            }
        }
        return Enumerations.filter(en, new P());
    }
    
    /**
     * @param filter the set.contains (...) is called before each object is produced
     * @return Enumeration
     */
    protected <T,R> Enumeration<R> queue(Collection<T> initContent, final QueueProcess<T,R> process) {
        class C implements Enumerations.Processor<T,R> {
            public R process(T object, Collection<T> toAdd) {
                return process.process(object, toAdd);
            }
        }
        return Enumerations.queue(
                Collections.enumeration(initContent),
                new C()
                );
    }
    
    /** Processor interface.
     */
    public static interface QueueProcess<T,R> {
        public R process(T object, Collection<T> toAdd);
    }
    
    //
    // The tests
    //
    
    public void testEmptyIsEmpty() {
        Enumeration<?> e = empty();
        assertFalse(e.hasMoreElements());
        try {
            e.nextElement();
            fail("No elements");
        } catch (NoSuchElementException ex) {
            // ok
        }
    }
    
    public void testSingleIsSingle() {
        Enumeration<EnumerationsTest> e = singleton(this);
        assertTrue(e.hasMoreElements());
        assertEquals("Returns me", this, e.nextElement());
        assertFalse("Now it is empty", e.hasMoreElements());
        try {
            e.nextElement();
            fail("No elements");
        } catch (NoSuchElementException ex) {
            // ok
        }
    }
    
    public void testConcatTwoAndArray() {
        Object[] one = { 1, 2, 3 };
        Object[] two = { "1", "2", "3" };
        
        List<Object> list = new ArrayList<Object>(Arrays.asList(one));
        list.addAll(Arrays.asList(two));
        
        assertEnums(
                concat(array(one), array(two)),
                Collections.enumeration(list)
                );
    }
    
    public void testConcatTwoAndArrayAndTakeOnlyStrings() {
        Object[] one = { 1, 2, 3 };
        Object[] two = { "1", "2", "3" };
        Object[] three = { 1L };
        Object[] four = { "Kuk" };
        
        List<Object> list = new ArrayList<Object>(Arrays.asList(two));
        list.addAll(Arrays.asList(four));
        
        @SuppressWarnings("unchecked")
        Enumeration<Object>[] alls = (Enumeration<Object>[]) new Enumeration<?>[] {
            array(one), array(two), array(three), array(four)
        };
        
        assertEnums(
                filter(concat(array(alls)), new OnlyStrings()),
                Collections.enumeration(list)
                );
    }
    
    public void testRemoveDuplicates() {
        Object[] one = { 1, 2, 3 };
        Object[] two = { "1", "2", "3" };
        Object[] three = { 1 };
        Object[] four = { "2", "3", "4" };
        
        @SuppressWarnings("unchecked")
        Enumeration<Object>[] alls = (Enumeration<Object>[]) new Enumeration<?>[] {
            array(one), array(two), array(three), array(four)
        };
        
        assertEnums(
                removeDuplicates(concat(array(alls))),
                array(new Object[] { 1, 2, 3, "1", "2", "3", "4" })
                );
        
    }
    
    public void testRemoveDuplicatesAndGCWorks() {
        
        /*** Return { i1, "", "", "", i2 } */
        class WeakEnum implements Enumeration<Object> {
            public Object i1 = new Integer(1);
            public Object i2 = new Integer(1);
            
            private int state;
            
            public boolean hasMoreElements() {
                return state < 5;
            }
            
            public Object nextElement() {
                switch (state++) {
                    case 0: return i1;
                    case 1: case 2: case 3: return "";
                    default: return i2;
                }
            }
        }
        
        WeakEnum weak = new WeakEnum();
        Enumeration<Object> en = removeDuplicates(weak);
        
        assertTrue("Has some elements", en.hasMoreElements());
        assertEquals("And the first one is get", weak.i1, en.nextElement());

        /*
        try {
            Reference<?> ref = new WeakReference<Object>(weak.i1);
         */
            weak.i1 = null;
        /*
            assertGC("Try hard to GC the first integer", ref);
            // does not matter whether it GCs or not
        } catch (Throwable tw) {
            // not GCed, but does not matter
        }
         */
        assertTrue("Next object will be string", en.hasMoreElements());
        assertEquals("is empty string", "", en.nextElement());
        
        assertFalse("The second integer is however equal to the original i1 and thus" +
                " the enum should not be there", en.hasMoreElements());
    }
    
    public void testQueueEnum() {
        class Pr implements QueueProcess<Integer,Integer> {
            public Integer process(Integer i, Collection<Integer> c) {
                int plus = i + 1;
                if (plus < 10) {
                    c.add(plus);
                }
                return i;
            }
        }
        Pr p = new Pr();
        
        Enumeration<Integer> en = queue(
                Collections.nCopies(1, 0), p
                );
        
        for (int i = 0; i < 10; i++) {
            assertTrue("has next", en.hasMoreElements());
            en.nextElement();
        }
        
        assertFalse("No next element", en.hasMoreElements());
    }
    
    public void testFilteringAlsoDoesConvertions() throws Exception {
        class Pr implements QueueProcess<Integer,Integer> {
            public Integer process(Integer i, Collection<Integer> ignore) {
                return i + 1;
            }
        }
        Pr p = new Pr();
        
        Enumeration<Integer> onetwo = array(new Integer[] { 1, 2 });
        Enumeration<Integer> twothree = array(new Integer[] { 2, 3 });
        
        assertEnums(
                filter(onetwo, p), twothree
                );
    }
    
    
    private static <T> void assertEnums(Enumeration<T> e1, Enumeration<T> e2) {
        int indx = 0;
        while (e1.hasMoreElements() && e2.hasMoreElements()) {
            T i1 = e1.nextElement();
            T i2 = e2.nextElement();
            assertEquals(indx++ + "th: ", i1, i2);
        }
        
        if (e1.hasMoreElements()) {
            fail("first one contains another element: " + e1.nextElement());
        }
        if (e2.hasMoreElements()) {
            fail("second one contains another element: " + e2.nextElement());
        }
        
        try {
            e1.nextElement();
            fail("First one should throw exception, but nothing happend");
        } catch (NoSuchElementException ex) {
            // ok
        }
        
        try {
            e2.nextElement();
            fail("Second one should throw exception, but nothing happend");
        } catch (NoSuchElementException ex) {
            // ok
        }
    }
    
    public void testConvertIntegersToStringRemoveNulls() {
        Object[] garbage = { 1, "kuk", "hle", 5 };
        
        assertEnums(
                removeNulls(convert(array(garbage), new MapIntegers())),
                array(new Object[] { "1", "5" })
                );
    }
    
    public void testQueueEnumerationCanReturnNulls() {
        Object[] nuls = { null, "NULL" };
        
        class P implements QueueProcess<Object,Object> {
            public Object process(Object toRet, Collection<Object> toAdd) {
                if (toRet == null) return null;
                
                if ("NULL".equals(toRet)) {
                    toAdd.add(null);
                    return null;
                }
                
                return null;
            }
        }
        
        assertEnums(
                array(new Object[] { null, null, null }),
                queue(Arrays.asList(nuls), new P())
                );
    }
    
    /** Filters only strings.
     */
    private static final class OnlyStrings implements Set<Object> {
        public boolean add(Object o) {
            fail("Should not be every called");
            return false;
        }
        
        public boolean addAll(Collection c) {
            fail("Should not be every called");
            return false;
        }
        
        public void clear() {
            fail("Should not be every called");
        }
        
        public boolean contains(Object o) {
            return o instanceof String;
        }
        
        public boolean containsAll(Collection c) {
            fail("Should not be every called");
            return false;
        }
        
        public boolean isEmpty() {
            fail("Should not be every called");
            return false;
        }
        
        public Iterator<Object> iterator() {
            fail("Should not be every called");
            return null;
        }
        
        public boolean remove(Object o) {
            fail("Should not be every called");
            return false;
        }
        
        public boolean removeAll(Collection c) {
            fail("Should not be every called");
            return false;
        }
        
        public boolean retainAll(Collection c) {
            fail("Should not be every called");
            return false;
        }
        
        public int size() {
            fail("Should not be every called");
            return 1;
        }
        
        public Object[] toArray() {
            fail("Should not be every called");
            return null;
        }
        
        public <T> T[] toArray(T[] a) {
            fail("Should not be every called");
            return null;
        }
    }
    
    /** Filters only strings.
     */
    private static final class MapIntegers implements Map<Object,Object> {
        public boolean containsKey(Object key) {
            fail("Should not be every called");
            return false;
        }
        
        public boolean containsValue(Object value) {
            fail("Should not be every called");
            return false;
        }
        
        public Set<Map.Entry<Object,Object>> entrySet() {
            fail("Should not be every called");
            return null;
        }
        
        public Object get(Object key) {
            if (key instanceof Integer) {
                return key.toString();
            }
            return null;
        }
        
        public Set<Object> keySet() {
            fail("Should not be every called");
            return null;
        }
        
        public Object put(Object key, Object value) {
            fail("Should not be every called");
            return null;
        }
        
        public void putAll(Map t) {
            fail("Should not be every called");
        }
        
        public Collection<Object> values() {
            fail("Should not be every called");
            return null;
        }
        
        public void clear() {
            fail("Should not be every called");
        }
        
        public boolean isEmpty() {
            fail("Should not be every called");
            return false;
        }
        
        public Object remove(Object key) {
            fail("Should not be every called");
            return null;
        }
        
        public int size() {
            fail("Should not be every called");
            return 1;
        }
        
    }
}
