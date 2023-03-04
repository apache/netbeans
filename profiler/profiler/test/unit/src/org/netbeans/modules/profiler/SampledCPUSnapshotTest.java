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
package org.netbeans.modules.profiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.lib.profiler.results.ResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.CPUCCTContainer;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class SampledCPUSnapshotTest {

    public SampledCPUSnapshotTest() {
    }

    @Test
    public void testGetCPUSnapshot() throws Exception {
        final String name = "jdk11-sample1.npss";
        final SampledCPUSnapshot snapshot = loadSnapshot(name);
        assertEquals(442, snapshot.getSamplesCount());

        LoadedSnapshot interval = snapshot.getCPUSnapshot(88, 103);
        Map<String, Long> timesByThreadName = timesByThreadName(interval);

        assertEquals(Long.valueOf(5053), timesByThreadName.remove("command-pool-6"));
        assertEquals(Long.valueOf(5053), timesByThreadName.remove("logging-service-client-akka.actor.default-dispatcher-5"));
        assertEquals(Long.valueOf(4151), timesByThreadName.remove("pool-2-thread-1"));
        assertEquals(Long.valueOf(5506), timesByThreadName.remove("scala-execution-context-global-57"));
        assertEquals(Long.valueOf(10788), timesByThreadName.remove("searcher.db-1"));
        assertEquals(Long.valueOf(50314), timesByThreadName.remove("job-pool-1"));

        assertTrue("No other threads working: " + timesByThreadName, timesByThreadName.isEmpty());
    }

    private static Map<String, Long> timesByThreadName(LoadedSnapshot interval) {
        ResultsSnapshot result = interval.getSnapshot();
        assertEquals(CPUResultsSnapshot.class, result.getClass());
        Map<String,Long> timesByThreadName = new TreeMap<>();
        CPUResultsSnapshot cpu = (CPUResultsSnapshot) result;
        for (int id : cpu.getThreadIds()) {
            String threadName = cpu.getThreadNameForId(id);
            CPUCCTContainer container = cpu.getContainerForThread(id, 0);
            long t1 = container.getWholeGraphNetTime1();
            if (t1 > 0) {
                Long prev = timesByThreadName.put(threadName, t1);
                assertNull("There should be just a single " + threadName, prev);
            }
        }
        return timesByThreadName;
    }

    private static SampledCPUSnapshot loadSnapshot(final String name) throws IOException {
        InputStream is = SampledCPUSnapshotTest.class.getResourceAsStream(name);
        assertNotNull("Sample npss file found", is);
        FileObject sample = FileUtil.createMemoryFileSystem().getRoot().createData("sample.npss");
        try (OutputStream os = sample.getOutputStream()) {
            FileUtil.copy(is, os);
        }
        return new SampledCPUSnapshot(sample);
    }

}
