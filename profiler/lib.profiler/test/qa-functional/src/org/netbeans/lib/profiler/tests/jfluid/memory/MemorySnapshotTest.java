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

package org.netbeans.lib.profiler.tests.jfluid.memory;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.global.CommonConstants;


/**
 *
 * @author ehucka
 */
public class MemorySnapshotTest extends MemorySnapshotTestCase {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of BasicTest */
    public MemorySnapshotTest(String name) {
        super(name);
    }
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(MemorySnapshotTest.class).addTest(
            "testSettingsAllocations",
            "testSettingsAllocationsServer",
            "testSettingsAllocationsStackTraces",
            "testSettingsAllocationsStackTracesServer",
            "testSettingsLiveness",
            "testSettingsLivenessServer",
            "testSettingsLivenessStackTraces",
            "testSettingsLivenessStackTracesServer").enableModules(".*").clusters(".*").gui(false));
    }
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void testSettingsAllocations() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(0);
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_ALLOCATIONS, new String[] { "simple" },
                                "simple.memory.Bean");
    }

    public void testSettingsAllocationsServer() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        addJVMArgs(settings, "-server");
        settings.setAllocStackTraceLimit(0);
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_ALLOCATIONS, new String[] { "simple" },
                                "simple.memory.Bean");
    }

    public void testSettingsAllocationsStackTraces() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(-1);
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_ALLOCATIONS, new String[] { "simple" },
                                "simple.memory.Bean");
    }

    public void testSettingsAllocationsStackTracesServer() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(-1);
        addJVMArgs(settings, "-server");
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_ALLOCATIONS, new String[] { "simple" },
                                "simple.memory.Bean");
    }

    public void testSettingsLiveness() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(0);
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_LIVENESS, new String[] { "simple" }, "simple.memory.Bean");
    }

    public void testSettingsLivenessServer() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(0);
        addJVMArgs(settings, "-server");
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_LIVENESS, new String[] { "simple" }, "simple.memory.Bean");
    }

    public void testSettingsLivenessStackTraces() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(-1);
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_LIVENESS, new String[] { "simple" }, "simple.memory.Bean");
    }

    public void testSettingsLivenessStackTracesServer() {
        ProfilerEngineSettings settings = initMemorySnapshotTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(-1);
        addJVMArgs(settings, "-server");
        startMemorySnapshotTest(settings, CommonConstants.INSTR_OBJECT_LIVENESS, new String[] { "simple" }, "simple.memory.Bean");
    }
}
