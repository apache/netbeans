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
package org.netbeans.modules.profiler.heapwalk.model;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapSegmentTest;
import org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker;

public class HeapWalkerNodeTest extends NbTestCase {

    public HeapWalkerNodeTest(String n) {
        super(n);
    }

    private static void assertChildMode(ObjectNode on) {
        ObjectNode ch = new ObjectNode(null, "any", on);
        assertEquals("Same mode on child", on.getMode(), ch.getMode());
    }

    public void testModeFields1() {
        ObjectNode on = new ObjectNode(null, "any", null, HeapWalkerNode.MODE_FIELDS);
        assertTrue("Showing fields", on.isModeFields());
        assertChildMode(on);
    }


    public void testModeFields2() {
        ObjectNode on = new ObjectNode(null, "any", null, HeapWalkerNode.MODE_FIELDS_NO_CLASSLOADER);
        assertTrue("Showing fields", on.isModeFields());
        assertChildMode(on);
    }

    public void testModeFields3() {
        ObjectNode on = new ObjectNode(null, "any", null, HeapWalkerNode.MODE_REFERENCES);
        assertFalse("Not showing fields", on.isModeFields());
        assertChildMode(on);
    }

    public void testSimpleHeapDumpAnalysis() throws Exception {
        clearWorkDir();
        File mydump = new File(getWorkDir(), "sample.hprof");
        mydump.getParentFile().mkdirs();
        Heap heap = HeapSegmentTest.generateSampleDump(mydump);

        HeapFragmentWalker walker = new HeapFragmentWalker(heap, null);
        assertEquals("No classloaders", 0, walker.countClassLoaders());
    }

    public void testComplexHeapDumpAnalysis() throws Exception {
        clearWorkDir();
        File mydump = new File(getWorkDir(), "complex.hprof");
        mydump.getParentFile().mkdirs();
        Heap heap = HeapSegmentTest.generateComplexDump(mydump);

        HeapFragmentWalker walker = new HeapFragmentWalker(heap, null);
        assertEquals("Heap dump uses some classloaders", 2, walker.countClassLoaders());
    }
}
