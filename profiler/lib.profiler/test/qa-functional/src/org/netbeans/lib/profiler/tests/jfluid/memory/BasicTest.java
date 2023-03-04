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
public class BasicTest extends MemoryTestCase {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of BasicTest */
    public BasicTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(BasicTest.class).addTest(
            "testSettingsAllocations",
            "testSettingsAllocationsServer",
            "testSettingsAllocationsStackTraces",
            "testSettingsAllocationsStackTracesServer",
            "testSettingsDefault",
            "testSettingsLiveness",
            "testSettingsLivenessServer",
            "testSettingsLivenessStackTraces",
            "testSettingsLivenessStackTracesServer").enableModules(".*").clusters(".*").gui(false));
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void testSettingsAllocations() {
        ProfilerEngineSettings settings = initMemoryTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(0);
        startMemoryTest(settings, CommonConstants.INSTR_OBJECT_ALLOCATIONS, new String[] { "simple" });
    }

    public void testSettingsAllocationsServer() {
        ProfilerEngineSettings settings = initMemoryTest("j2se-simple", "simple.memory.Memory1");
        addJVMArgs(settings, "-server");
        settings.setAllocStackTraceLimit(0);
        startMemoryTest(settings, CommonConstants.INSTR_OBJECT_ALLOCATIONS, new String[] { "simple" });
    }

    public void testSettingsAllocationsStackTraces() {
        ProfilerEngineSettings settings = initMemoryTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(-1);
        startMemoryTest(settings, CommonConstants.INSTR_OBJECT_ALLOCATIONS, new String[] { "simple" });
    }

    public void testSettingsAllocationsStackTracesServer() {
        ProfilerEngineSettings settings = initMemoryTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(-1);
        addJVMArgs(settings, "-server");
        startMemoryTest(settings, CommonConstants.INSTR_OBJECT_ALLOCATIONS, new String[] { "simple" });
    }

    public void testSettingsDefault() {
        ProfilerEngineSettings settings = initMemoryTest("j2se-simple", "simple.Memory");
        startMemoryTest(settings, CommonConstants.INSTR_OBJECT_ALLOCATIONS, new String[] { "simple" });
    }

    public void testSettingsLiveness() {
        ProfilerEngineSettings settings = initMemoryTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(0);
        startMemoryTest(settings, CommonConstants.INSTR_OBJECT_LIVENESS, new String[] { "simple" });
    }

    public void testSettingsLivenessServer() {
        ProfilerEngineSettings settings = initMemoryTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(0);
        addJVMArgs(settings, "-server");
        startMemoryTest(settings, CommonConstants.INSTR_OBJECT_LIVENESS, new String[] { "simple" });
    }

    public void testSettingsLivenessStackTraces() {
        ProfilerEngineSettings settings = initMemoryTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(-1);
        startMemoryTest(settings, CommonConstants.INSTR_OBJECT_LIVENESS, new String[] { "simple" });
    }

    public void testSettingsLivenessStackTracesServer() {
        ProfilerEngineSettings settings = initMemoryTest("j2se-simple", "simple.memory.Memory1");
        settings.setAllocStackTraceLimit(-1);
        addJVMArgs(settings, "-server");
        startMemoryTest(settings, CommonConstants.INSTR_OBJECT_LIVENESS, new String[] { "simple" });
    }
}
