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
package org.netbeans.modules.editor.lib2;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Miloslav Metelka
 */
public class WeakReferenceStableListTest extends NbTestCase {
    
    private List<Object> expected;
    
    private WeakReferenceStableList<Object> tested;
    
    public WeakReferenceStableListTest(String testName) {
        super(testName);
        List<String> includes = new ArrayList<String>();
//        includes.add("testSimple1");
//        filterTests(includes);
    }

    private void filterTests(List<String> includeTestNames) {
        List<Filter.IncludeExclude> includeTests = new ArrayList<Filter.IncludeExclude>();
        for (String testName : includeTestNames) {
            includeTests.add(new Filter.IncludeExclude(testName, ""));
        }
        Filter filter = new Filter();
        filter.setIncludes(includeTests.toArray(new Filter.IncludeExclude[0]));
        setFilter(filter);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); //To change body of generated methods, choose Tools | Templates.
        expected = new ArrayList<Object>();
        tested= new WeakReferenceStableList<Object>();
    }

    @Override
    protected Level logLevel() {
        return Level.INFO; // null;
//        return Level.FINEST;
    }

    public void testAddNull() throws Exception {
        try {
            tested.add(null);
            fail("Not expected to accept nulls");
        } catch (AssertionError ex) {
            // Expected
        }
    }

    public void testAddRemove() throws Exception {
        Integer i1 = new Integer(1001);
        Integer i2 = new Integer(1002);
        Integer i3 = new Integer(1003);
        Integer i4 = new Integer(1004);
        Integer i5 = new Integer(1005);
        Integer i6 = new Integer(1006);
        add(i1);
        check();
        add(i2);
        check();
        add(i3);
        check();
        add(i4);
        check();

        assertTrue(expected.remove(i4));
        Reference<Integer> refI4 = new WeakReference<Integer>(i4);
        i4 = null;
        assertGC("i4 not GCable", refI4);
        gc();
        check();
        
        add(i5);
        check();
        add(i6);
        check();
        
        assertTrue(expected.remove(i3));
        i3 = null;
        assertTrue(expected.remove(i1));
        i1 = null;
        gc(); // should remove i3 from tested
        check();
        assertTrue(expected.remove(i5));
        i5 = null;
        assertTrue(expected.remove(i6));
        i6 = null;
        gc(); // should remove i3 from tested
        check();
    }
    
    public void testMultiGC() throws Exception {
        int COUNT = 50;
        Integer[] array = new Integer[COUNT];
        for (int i = 0; i < COUNT; i++) {
            array[i] = new Integer(1000 + i);
            tested.add(array[i]);
        }
        for (int i = 0; i < COUNT; i++) {
            if (i % 2 == 0) {
                array[i] = null;
            }
        }
        gc();

        for (Object o : tested.getList()) {
            tested.add(new Integer(2000 + ((Integer)o).intValue())); // Modify during scan
        }
    }

    private void add(Object o) {
        expected.add(o);
        tested.add(o);
    }
    
    private void check() {
        List<Object> testedList = tested.getList();
        int j = 0;
        for (int i = 0; i < expected.size(); i++) {
            Object testedValue;
            while ((testedValue = testedList.get(j++)) == null) { }
            assertSame("Index=" + i, expected.get(i), testedValue);
        }
        while (j < testedList.size()) {
            Object testedValue = testedList.get(j++);
            assertNull("Expected null", testedValue);
        }
        
        int i = 0;
        for (Object testedValue : testedList) { // Iterator skips null values
            assertSame("Index=" + i, expected.get(i), testedValue);
            i++;
        }
        assertEquals("Wrong size", expected.size(), i);
    }
    
    private static void gc() {
        System.gc();
        Runtime.getRuntime().runFinalization();
        System.gc();
        System.gc();
    }

}
