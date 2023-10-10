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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;

public class WeakSetTest extends NbTestCase {

    public WeakSetTest(String testName) {
        super(testName);
    }
    
    public void testPutTwice() {
        Object obj = new Object();
        
        WeakSet<Object> ws = new WeakSet<Object>();
        assertTrue("Object added", ws.add(obj));
        assertFalse("No change", ws.add(obj));
        assertEquals("size", 1, ws.size());
        assertSame("First is obj", obj, ws.iterator().next());
    }

    public void testPutNullTwice() {
        WeakSet<Object> ws = new WeakSet<Object>();
        assertEquals("Returns null on first try", null, ws.putIfAbsent(null));
        assertFalse("No change", ws.add(null));
        assertEquals("size", 1, ws.size());
        assertNull("First is obj", ws.iterator().next());
        assertNull("Returns null on 3rd try", ws.putIfAbsent(null));
    }

    public void testToArrayMayContainNullsIssue42271 () {
        class R implements Runnable {
            Object[] arr;
            Object last;
            
            public R () {
                int cnt = 10;
                arr = new Object[cnt];
                for (int i = 0; i < cnt; i++) {
                    arr[i] = new Integer(i); // autoboxing makes test fail!
                }
            }
            
            
            public void run () {
                
                Reference<?> r = new WeakReference<Object>(last);
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = null;
                }
                arr = null;
                last = null;
                assertGC ("Last item has to disappear", r);
            }
            
            public void putToSet (NotifyWhenIteratedSet s) {
                for (int i = 0; i < arr.length; i++) {
                    s.add (arr[i]);
                }
                assertEquals (arr.length, s.size ());
                Iterator<Object> it = s.superIterator();
                Object prev = it.next ();
                while (it.hasNext ()) {
                    prev = it.next ();
                }
                last = prev;
            }
        }
        R r = new R ();
        
        
        
        NotifyWhenIteratedSet ws = new NotifyWhenIteratedSet (r, 1);
        
        r.putToSet (ws);
        
        Object[] arr = ws.toArray ();
        for (int i = 0; i < arr.length; i++) {
            assertNotNull (i + "th index should not be null", arr[i]);
        }
    }
    
    private static final class NotifyWhenIteratedSet extends WeakSet<Object> {
        private Runnable run;
        private int cnt;
        
        public NotifyWhenIteratedSet (Runnable run, int cnt) {
            this.run = run;
            this.cnt = cnt;
        }
        
        public Iterator<Object> superIterator() {
            return super.iterator ();
        }
        
        @Override
        public Iterator<Object> iterator() {
            final Iterator<Object> it = super.iterator();
            class I implements Iterator<Object> {
                public boolean hasNext() {
                    return it.hasNext ();
                }

                public Object next() {
                    if (--cnt == 0) {
                        run.run ();
                    }
                    return it.next ();
                }

                @Override
                public void remove() {
                    it.remove();
                }
            }
            return new I ();
        }
    }
    
    private static final class TestObj {
        static final Set<TestObj> testObjs = new WeakSet<TestObj>();
        private final String name;
        TestObj(String name) {
            this.name = name;
            synchronized (testObjs) {
                testObjs.add(this);
            }
        }

        @Override
        public String toString() {
            return name;
        }
        
    }
    
    private static final class GC implements Runnable {

        public void run() {
            try {
                for (int i = 0; i < 5; i++) {
                    gc();
                }
            } catch (InterruptedException ex) {
                // ignore
            }
        }

        static void gc() throws InterruptedException {
            List<byte[]> alloc = new ArrayList<byte[]>();
            int size = 100000;
            for (int i = 0; i < 50; i++) {
                System.gc();
                System.runFinalization();
                try {
                    alloc.add(new byte[size]);
                    size = (int) (((double) size) * 1.3);
                } catch (OutOfMemoryError error) {
                    size = size / 2;
                }
                if (i % 3 == 0) {
                    Thread.sleep(100);
                }
            }
        }
    }

    /**
     * test for issue #106218
     * @throws java.lang.Exception
     */
    @RandomlyFails // OOME in NB-Core-Build #1908
    public void testWeakSetIntegrity() throws Exception {
        //CharSequence log = Log.enable(WeakSet.class.getName(), Level.FINE);
        ArrayList<WeakReference<TestObj>> awr = new ArrayList<WeakReference<TestObj>>();
        ExecutorService exec = Executors.newFixedThreadPool(5);
        exec.execute(new GC());
        for (int i = 0; i < 1000; i++) {
            TestObj to = new TestObj("T" + i);
            awr.add(new WeakReference<TestObj>(to));
            Thread.yield();
            for (WeakReference<TestObj> wro : awr) {
                TestObj wroo = wro.get();
                if (wroo != null) {
                    synchronized (TestObj.testObjs) {
                        boolean found = false;
                        for (TestObj o : TestObj.testObjs) {
                            if (o == wroo) {
                                found = true;
                            }
                        }
                        if (found != TestObj.testObjs.contains(wroo)) {
                            //System.out.println(log.toString());
                            fail("Inconsistency of iterator chain and hash map");
                        }
                    }
                }
            }
        }
        exec.shutdownNow();
    }    
    
    public void testAddRemove() {
        Set<Object> set = new WeakSet<Object>();
        Object obj = new Integer(1);
        assertTrue("have to be new object", set.add(obj));
        Object obj2 = new Integer(1);
        assertFalse("object have to be already in set", set.add(obj2));
        assertTrue("object have to be removed correctly", set.remove(obj2));
        assertFalse("set have to be empty", set.remove(obj));
        assertTrue("set have to be empty", set.isEmpty());

    }

    public void testConcurrentExceptions() {
        Object[] arr = new Object[]{new Integer(1), new Long(2), new Double(3)};
        Set<Object> set = new WeakSet<Object>();
        set.addAll(Arrays.asList(arr));

        for (Object object : set) {
            set.remove(new Boolean(true));
        }

        boolean gotException = false;
        try {
            for (Object object : set) {
                set.remove(new Long(2));
            }
            fail("concurrent exception is expected");
        } catch (ConcurrentModificationException ex) {
            gotException = true;
        }
        assertTrue("ConcurrentModificationException is expected", gotException);
    }

    @RandomlyFails // NB-Core-Build #8344: after assertGC: expected:<[2, 3.0]> but was:<[2, 3.0]>
    public void testClone() {
        Object[] arr = new Object[]{new Integer(1), new Long(2), new Double(3)};
        WeakSet<Object> set = new WeakSet<Object>();
        set.addAll(Arrays.asList(arr));
        Set<Object> second = (Set<Object>) set.clone();
        assertEquals(second, set);
        assertTrue(second.size() == 3);
        set.remove(arr[0]);
        assertFalse(second.equals(set));
        String s = "GCing " + arr[0];
        Reference<Object> r = new WeakReference<>(arr[0]);
        arr[0] = null;
        NbTestCase.assertGC(s, r);
        assertEquals(second, set);
        assertTrue(second.size() == 2);
        
        class MySet extends WeakSet {
            
        }
        WeakSet<Object> cloningSet = new MySet();
        cloningSet.addAll(set);
        Object clone = cloningSet.clone();
        assertTrue(clone instanceof MySet);
        assertEquals(clone, set);
    }
}
