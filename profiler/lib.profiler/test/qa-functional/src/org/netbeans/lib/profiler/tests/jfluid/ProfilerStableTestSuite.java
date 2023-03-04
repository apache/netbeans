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

package org.netbeans.lib.profiler.tests.jfluid;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author tester
 */
public class ProfilerStableTestSuite {
    public static Test suite() {
    return NbModuleSuite.create(
      NbModuleSuite.emptyConfiguration()
        .addTest(org.netbeans.lib.profiler.tests.jfluid.BasicTest.class)
        .addTest(org.netbeans.lib.profiler.tests.jfluid.perf.InstrumentationTest.class)
        .addTest(org.netbeans.lib.profiler.tests.jfluid.wireio.BasicTest.class)
        .addTest(org.netbeans.lib.profiler.tests.jfluid.monitor.BasicTest.class)
        .addTest(org.netbeans.lib.profiler.tests.jfluid.memory.BasicTest.class)
	.addTest(org.netbeans.lib.profiler.tests.jfluid.memory.MemorySnapshotTest.class)
        .addTest(org.netbeans.lib.profiler.tests.jfluid.others.MeasureDiffsTest.class)
        .addTest(org.netbeans.lib.profiler.tests.jfluid.cpu.CPUSnapshotTest.class)
	.addTest(org.netbeans.lib.profiler.tests.jfluid.cpu.BasicTest.class, org.netbeans.lib.profiler.tests.jfluid.cpu.BasicTest.tests)
        .addTest(org.netbeans.lib.profiler.tests.jfluid.cpu.BasicTest.class, org.netbeans.lib.profiler.tests.jfluid.cpu.BasicTest.tests2)
        .addTest(org.netbeans.lib.profiler.tests.jfluid.cpu.BasicTest.class, org.netbeans.lib.profiler.tests.jfluid.cpu.BasicTest.tests3)
        .addTest(org.netbeans.lib.profiler.tests.jfluid.benchmarks.JbbTest.class)
    );
  }


}
