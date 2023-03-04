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
package org.netbeans.lib.profiler.heap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.netbeans.lib.profiler.heap.HeapUtils.HprofGenerator;

public class HeapSegmentTest {
    @Test
    public void singleObject() throws IOException {
        singleObject(false);
    }

    @Test
    public void singleObjectMultipleSegments() throws IOException {
        singleObject(true);
    }

    private static void singleObject(boolean flush) throws IOException {
        File mydump = File.createTempFile("mydump", ".hprof");
        Heap heap = generateSampleDump(mydump, flush);
        List<JavaClass> allClasses = heap.getAllClasses();
        assertEquals(5, allClasses.size());
        assertEquals("java.lang.String", allClasses.get(0).getName());
        assertEquals("char[]", allClasses.get(1).getName());
        assertEquals("text.HelloWorld", allClasses.get(2).getName());

        Collection<GCRoot> roots = new ArrayList<>(heap.getGCRoots());
        assertEquals("Thread & two locals", 5, roots.size());
        {
            Iterator<GCRoot> it = roots.iterator();
            while (it.hasNext()) {
                if (it.next() instanceof ThreadObjectGCRoot) {
                    continue;
                }
                it.remove();
            }
        }
        assertEquals("Only one thread", 2, roots.size());
        final Iterator<GCRoot> it = roots.iterator();
        final Instance thread = it.next().getInstance();

        Object daemon = thread.getValueOfField("daemon");
        assertNotNull("daemon field found", daemon);
        Instance value = (Instance) thread.getValueOfField("name");
        assertNotNull("name assigned", value);
        assertEquals("java.lang.String", value.getJavaClass().getName());
        assertEquals(Boolean.class, daemon.getClass());
        assertFalse("It is not daemon", (Boolean) daemon);
    }

    public static Heap generateSampleDump(File mydump) throws IOException {
        return generateSampleDump(mydump, true);
    }

    public static Heap generateComplexDump(File mydump) throws IOException, URISyntaxException {
        InputStream is = HeapUtils.class.getResourceAsStream("heap_dump.bin");
        FileOutputStream out = new FileOutputStream(mydump);
        byte[] arr = new byte[4096];
        for (;;) {
            int len = is.read(arr);
            if (len == -1) {
                break;
            }
            out.write(arr, 0, len);
        }
        is.close();
        out.close();
        return HeapFactory.createHeap(mydump);
    }

    static Heap generateSampleDump(File mydump, boolean flush) throws IOException {
        generateSingleObject(new FileOutputStream(mydump), flush);
        return HeapFactory.createHeap(mydump);
    }

    private static void generateSingleObject(OutputStream os, boolean flush) throws IOException {
        try (HprofGenerator gen = new HprofGenerator(os)) {
            gen.writeHeapSegment(new SampleDumpMemory(), flush);
            gen.writeHeapSegment(new SampleDumpMemory2(), flush);
        }
    }

    private static class SampleDumpMemory implements HprofGenerator.Generator<HprofGenerator.HeapSegment> {
        @Override
        public void generate(HprofGenerator.HeapSegment seg) throws IOException {
            int mainId = seg.dumpString("main");

            HprofGenerator.ClassInstance clazz = seg.newClass("text.HelloWorld")
                    .addField("daemon", Boolean.TYPE)
                    .addField("name", String.class)
                    .addField("priority", int.class)
                    .dumpClass();

            int threadOne = seg.dumpInstance(clazz);
            int threadTwo = seg.dumpInstance(clazz, "daemon", 0, "name", mainId, "priority", 10);

            int threadId = seg.newThread("main")
                    .addStackFrame("HelloWorld", "HelloWorld.js", 11, mainId, threadOne)
                    .addStackFrame(":program", "HelloWorld.js", 32, threadTwo)
                    .dumpThread();

            seg.dumpPrimitive(threadId);
        }
    }

    private static class SampleDumpMemory2 implements HprofGenerator.Generator<HprofGenerator.HeapSegment> {
        @Override
        public void generate(HprofGenerator.HeapSegment seg) throws IOException {
            int threadId = seg.newThread("main2")
                    .addStackFrame("HelloWorld2", "HelloWorld2.js", 23)
                    .addStackFrame(":program2", "HelloWorld2.js", 32)
                    .dumpThread();

            seg.dumpPrimitive(threadId);
        }
    }

}
