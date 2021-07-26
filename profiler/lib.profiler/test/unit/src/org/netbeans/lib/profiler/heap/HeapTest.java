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

import java.io.BufferedOutputStream;
import java.util.Map;
import java.util.Date;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
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
     * Test of getGCRoots method, of class Heap.
     */
    @Test
    public void testGetGCRoots() {
        Collection<GCRoot> result = heap.getGCRoots();
        Set<Instance> unique = new HashSet<>();
        for (GCRoot r : result) {
            unique.add(r.getInstance());
        }
        assertEquals("Unique root instances", 429, unique.size());
        assertEquals("Roots with duplicted instances", 453, result.size());
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
        Instance printStreamInstance = (Instance) printStream.getInstances().get(0);
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

    @Test
    public void testHeapDumpLog() throws IOException, URISyntaxException {
        File outFile = File.createTempFile("heapDumpLog", ".txt");
        URL url = getClass().getResource("heapDumpLog.txt");
        File goledFile = new File(url.toURI());
        OutputStream outs = new FileOutputStream(outFile);
        PrintStream out = new PrintStream(new BufferedOutputStream(outs, 128 * 1024), false, "UTF-8");
        HeapSummary summary = heap.getSummary();
        out.println("Heap Summary");
        DateFormat df = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aaa", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        out.println("Time " + df.format(new Date(summary.getTime())));
        out.println("Live instances " + summary.getTotalLiveInstances());
        out.println("Live bytes " + summary.getTotalLiveBytes());
        out.println("Total alloc instances " + summary.getTotalAllocatedInstances());
        out.println("Total alloc bytes " + summary.getTotalAllocatedBytes());
        Collection classes = heap.getAllClasses();
        out.println("Classes size " + classes.size());
        out.println("System properties: ");
        for (Object en : heap.getSystemProperties().entrySet()) {
            Map.Entry entry = (Map.Entry) en;

            out.println(entry.getKey() + " " + entry.getValue());
        }
        for (Object c : classes) {
            JavaClass jc = (JavaClass) c;
            JavaClass sc = jc.getSuperClass();
            out.println(" Id 0x" + Long.toHexString(jc.getJavaClassId()) + " Class " + jc.getName() + " SuperClass " + (sc == null ? "null" : sc.getName())
                    + " Instance size " + jc.getInstanceSize() + " Instance count " + jc.getInstancesCount() + " All Instances Size " + jc.getAllInstancesSize());

            for (Object v : jc.getStaticFieldValues()) {
                FieldValue fv = (FieldValue) v;

                out.println("  Static Field name " + fv.getField().getName() + " type " + fv.getField().getType().getName() + " value " + fv.getValue());
                if (fv instanceof ObjectFieldValue) {
                    ObjectFieldValue objectField = (ObjectFieldValue) fv;
                    Instance refInstance = objectField.getInstance();
                    if (refInstance != null) {
                        out.println("   Ref object " + refInstance.getJavaClass().getName() + "#" + refInstance.getInstanceNumber());
                    }
                }
            }

            for (Object f : jc.getFields()) {
                Field in = (Field) f;

                out.println("  Field name " + in.getName() + " type " + in.getType().getName());
            }

            for (Object i : jc.getInstances()) {
                Instance in = (Instance) i;

                out.println("  Instance Id 0x" + Long.toHexString(in.getInstanceId()) + " number " + in.getInstanceNumber() + " retained size " + in.getRetainedSize());

                for (Object v : in.getFieldValues()) {
                    FieldValue inField = (FieldValue) v;

                    out.println("   Instance Field name " + inField.getField().getName() + " type " + inField.getField().getType().getName() + " value " + inField.getValue());
                    if (inField instanceof ObjectFieldValue) {
                        ObjectFieldValue objectField = (ObjectFieldValue) inField;
                        Instance refInstance = objectField.getInstance();
                        if (refInstance != null) {
                            out.println("    Ref object " + refInstance.getJavaClass().getName() + "#" + refInstance.getInstanceNumber());
                        }
                    }
                }
                Collection references = in.getReferences();
                out.println("   References count " + references.size());
                for (Object v : references) {
                    Value val = (Value) v;

                    if (val instanceof ArrayItemValue) {
                        ArrayItemValue arrVal = (ArrayItemValue) val;

                        out.println("   Element " + arrVal.getIndex() + " of array 0x" + Long.toHexString(arrVal.getDefiningInstance().getInstanceId()));
                    } else if (val instanceof FieldValue) {
                        FieldValue fieldVal = (FieldValue) val;
                        Field f = fieldVal.getField();

                        if (f.isStatic()) {
                            out.println("   Field " + f.getName() + " of Class " + f.getDeclaringClass().getName());
                        } else {
                            out.println("   Field " + f.getName() + " of instance 0x" + Long.toHexString(fieldVal.getDefiningInstance().getInstanceId()));
                        }
                    } else {
                        out.println("   Error " + val);
                    }
                }
                out.println("   Path to nearest GC root");
                Instance p = in;
                Instance next = p.getNearestGCRootPointer();
                while (!p.equals(next)) {
                    if (next == null) {
                        out.println("    Null");
                        break;
                    }
                    out.println("    Next object " + next.getJavaClass().getName() + "#" + next.getInstanceNumber());
                    p = next;
                    next = next.getNearestGCRootPointer();
                }
            }
        }
        Collection<GCRoot> roots = heap.getGCRoots();
        out.println("GC roots " + roots.size());

        Set<Instance> unique = new LinkedHashSet<>();
        for (GCRoot r : roots) {
            unique.add(r.getInstance());
        }
        for (Instance i : unique) {
            GCRoot root = heap.getGCRoot(i);
            out.println("Root kind " + root.getKind() + " Class " + i.getJavaClass().getName() + "#" + i.getInstanceNumber());
        }
        out.close();
        compareTextFiles(goledFile, outFile);
        outFile.delete();
    }

    private void compareTextFiles(File goledFile, File outFile) throws IOException {
        InputStreamReader goldenIsr = new InputStreamReader(new FileInputStream(goledFile), "UTF-8");
        LineNumberReader goldenReader = new LineNumberReader(goldenIsr);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(outFile), "UTF-8");
        LineNumberReader reader = new LineNumberReader(isr);
        String goldenLine = "";
        String line = "";

        while (goldenLine != null && goldenLine.equals(line)) {
            goldenLine = goldenReader.readLine();
            line = reader.readLine();
        }
        assertEquals("File " + goledFile.getAbsolutePath() + " and " + outFile.getAbsolutePath() + " differs on line " + goldenReader.getLineNumber(), goldenLine, line);
    }
}
