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

package org.netbeans.lib.profiler.tests.jfluid.cpu;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.lib.profiler.ProfilerEngineSettings;


/**
 *
 * @author ehucka
 */
public class CPUSnapshotTest extends CPUSnapshotTestCase {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of BasicTest */
    public CPUSnapshotTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(CPUSnapshotTest.class).addTest(
            "testMethods",
            "testMethodsServer",
            "testNoThreads",
            "testSimple",
            "testSimpleServer",
            "testThreads",
            "testThreadsServer",
            "testWaits",
            "testWaitsServer").enableModules(".*").clusters(".*").gui(false));
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void testMethods() {
        ProfilerEngineSettings settings = initSnapshotTest("j2se-simple", "simple.cpu.Methods", null);
        startSnapshotTest(settings, new String[] { "simple.cpu.Methods", "method27", "()V" }, 0, 1.0,
                          new String[] { "sun", "java" });
    }

    public void testMethodsServer() {
        ProfilerEngineSettings settings = initSnapshotTest("j2se-simple", "simple.cpu.Methods", null);
        addJVMArgs(settings, "-server");
        startSnapshotTest(settings, new String[] { "simple.cpu.Methods", "method27", "()V" }, 0, 1.0,
                          new String[] { "sun", "java" });
    }

    public void testNoThreads() {
        ProfilerEngineSettings settings = initSnapshotTest("j2se-simple", "simple.cpu.Region", null);
        settings.setInstrumentSpawnedThreads(false);
        startSnapshotTest(settings, new String[] { "simple.cpu.Region", "run100", "()V" }, 0, 1.0, new String[] { "sun", "java" });
    }

    public void testSimple() {
        ProfilerEngineSettings settings = initSnapshotTest("j2se-simple", "simple.CPU", null);
        startSnapshotTest(settings, new String[] { "simple.CPU", "test20", "()V" }, 0, 1.0, new String[] { "sun", "java" });
    }

    public void testSimpleServer() {
        ProfilerEngineSettings settings = initSnapshotTest("j2se-simple", "simple.CPU", null);
        addJVMArgs(settings, "-server");
        startSnapshotTest(settings, new String[] { "simple.CPU", "test20", "()V" }, 0, 1.0, new String[] { "sun", "java" });
    }

    public void testThreads() {
        ProfilerEngineSettings settings = initSnapshotTest("j2se-simple", "simple.cpu.Region", null);
        settings.setInstrumentSpawnedThreads(true);
        startSnapshotTest(settings, new String[] { "simple.cpu.Region", "run100", "()V" }, 0, 1.0, new String[] { "sun", "java" });
    }

    public void testThreadsServer() {
        ProfilerEngineSettings settings = initSnapshotTest("j2se-simple", "simple.cpu.Region", null);
        addJVMArgs(settings, "-server");
        settings.setInstrumentSpawnedThreads(true);
        startSnapshotTest(settings, new String[] { "simple.cpu.Region", "run100", "()V" }, 0, 1.0, new String[] { "sun", "java" });
    }

    public void testWaits() {
        ProfilerEngineSettings settings = initSnapshotTest("j2se-simple", "simple.cpu.WaitingTest", null);
        startSnapshotTest(settings, new String[] { "simple.cpu.WaitingTest", "method1000", "()V" }, 0, 1.0,
                          new String[] { "sun", "java" });
    }

    public void testWaitsServer() {
        ProfilerEngineSettings settings = initSnapshotTest("j2se-simple", "simple.cpu.WaitingTest", null);
        addJVMArgs(settings, "-server");
        startSnapshotTest(settings, new String[] { "simple.cpu.WaitingTest", "method1000", "()V" }, 0, 1.0,
                          new String[] { "sun", "java" });
    }
}
