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
package org.netbeans.lib.profiler.heap;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tomas Hurka
 */
public class HeapTest {

    private Heap heap;

    public HeapTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws IOException, URISyntaxException {
        URL url = getClass().getResource("heap_dump.bin");
        heap = HeapFactory.createHeap(new File(url.toURI()));
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getAllClasses method, of class Heap.
     */
    @Test
    public void testGetAllClasses() {
        List result = heap.getAllClasses();
        assertEquals(474, result.size());
    }

    /**
     * Test of getGCRoot method, of class Heap.
     */
    @Test
    public void testGetGCRoot() {
        Instance instance_2 = heap.getInstanceByID(3606613704l);
        GCRoot result = heap.getGCRoot(instance_2);
        assertEquals(instance_2, result.getInstance());
        assertEquals(GCRoot.MONITOR_USED, result.getKind());
    }
    
    /**
     * Test of getGCRoot method, of class Heap.
     */
    @Test
    public void testGetGCRootNew() {
        System.out.println("getGCRoot");
        Instance instance_2 = heap.getInstanceByID(3606613704l);
        Collection<GCRoot> resultList = heap.getGCRoots(instance_2);
        assertEquals(resultList.size(), 2);
        GCRoot[] results = resultList.toArray(new GCRoot[0]);
        GCRoot result = results[0];
        assertEquals(instance_2, result.getInstance());
        assertEquals(GCRoot.JAVA_FRAME, result.getKind());
        result = results[1];
        assertEquals(instance_2, result.getInstance());
        assertEquals(GCRoot.MONITOR_USED, result.getKind());
    }

    /**
     * Test of getGCRoots method, of class Heap.
     */
    @Test
    public void testGetGCRoots() {
        Collection result = heap.getGCRoots();
        assertEquals(453, result.size());
    }

    /**
     * Test of getJavaClassByName method, of class Heap.
     */
    @Test
    public void testGetJavaClassByName() {
        String fqn = "java.io.PrintStream";
        JavaClass result = heap.getJavaClassByName(fqn);
        assertEquals(fqn, result.getName());
    }

    /**
     * Test of getInstanceByID method, of class Heap.
     */
    @Test
    public void testGetInstanceByID() {
        String fqn = "java.io.PrintStream";
        JavaClass printStream = heap.getJavaClassByName(fqn);
        Instance printStreamInstance = printStream.getInstances().get(0);
        long instanceId = printStreamInstance.getInstanceId();
        Instance result = heap.getInstanceByID(instanceId);
        assertEquals(instanceId, result.getInstanceId());
    }

    /**
     * Test of getJavaClassByID method, of class Heap.
     */
    @Test
    public void testGetJavaClassByID() {
        String fqn = "java.io.PrintStream";
        JavaClass printStream = heap.getJavaClassByName(fqn);
        long javaclassId = printStream.getJavaClassId();
        JavaClass result = heap.getJavaClassByID(javaclassId);
        assertEquals(javaclassId, result.getJavaClassId());
    }

    /**
     * Test of getJavaClassesByRegExp method, of class Heap.
     */
    @Test
    public void testGetJavaClassesByRegExp() {
        String regexp = ".*Lock.*";
        Collection result = heap.getJavaClassesByRegExp(regexp);
        assertEquals(6, result.size());
    }

    /**
     * Test of getSummary method, of class Heap.
     */
    @Test
    public void testGetSummary() {
        HeapSummary result = heap.getSummary();
        assertEquals(2635552, result.getTotalLiveBytes());
        assertEquals(7788, result.getTotalLiveInstances());
    }

    /**
     * Test of getSystemProperties method, of class Heap.
     */
    @Test
    public void testGetSystemProperties() {
        Properties result = heap.getSystemProperties();
        assertEquals("4.13.0-26-generic", result.getProperty("os.version"));
    }

    /**
     * Test of getBiggestObjectsByRetainedSize method, of class Heap.
     */
    @Test
    public void testGetBiggestObjectsByRetainedSize() {
        List result = heap.getBiggestObjectsByRetainedSize(2);
        Instance i1 = (Instance) result.get(0);
        Instance i2 = (Instance) result.get(1);
        assertEquals(52283, i1.getRetainedSize());
        assertEquals(52082, i2.getRetainedSize());
    }

    /**
     * Test of getRetainedSizeByClass method, of class JavaClass.
     */
    @Test
    public void testGetRetainedSizeByClass() {
        JavaClass string = heap.getJavaClassByName(String.class.getName());
        JavaClass hashMap = heap.getJavaClassByName(HashMap.class.getName());
        JavaClass array = heap.getJavaClassByName(ArrayList.class.getName());

        assertEquals(82026, string.getRetainedSizeByClass());
        assertEquals(39703, hashMap.getRetainedSizeByClass());
        assertEquals(792, array.getRetainedSizeByClass());
    }

    /**
     * Test of getAllInstancesIterator method, of class Heap.
     */
    @Test
    public void getAllInstancesIterator() {
        Iterator instanceIt = heap.getAllInstancesIterator();
        int instances = 0;

        while (instanceIt.hasNext()) {
            Instance i = (Instance) instanceIt.next();
            instances++;
        }
        assertEquals(instances, heap.getSummary().getTotalLiveInstances());
    }

    /**
     * Test of getInstancesIterator method, of class JavaClass.
     */
    @Test
    public void getInstancesIterator() {
        List<JavaClass> classes = heap.getAllClasses();

        for (JavaClass clazz : classes) {
            List<Instance> instances = clazz.getInstances();
            Iterator instIt = clazz.getInstancesIterator();

            for (Instance i : instances) {
                assertTrue(instIt.hasNext());
                assertEquals(i, instIt.next());
            }
            assertFalse(instIt.hasNext());
        }
    }
}
