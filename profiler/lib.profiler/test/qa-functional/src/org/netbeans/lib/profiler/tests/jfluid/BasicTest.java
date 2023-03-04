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
import junit.textui.TestRunner;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.lib.profiler.*;
import org.netbeans.lib.profiler.tests.jfluid.utils.*;
import org.netbeans.lib.profiler.tests.jfluid.utils.TestProfilerAppHandler;


public class BasicTest extends CommonProfilerTestCase {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public BasicTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(BasicTest.class).addTest(
            "testCalibrate").enableModules(".*").clusters(".*").gui(false));
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void testCalibrate() {
        ProfilerEngineSettings settings;
        settings = new ProfilerEngineSettings();
        setTargetVM(settings);
        settings.setPortNo(5140);
        settings.setSeparateConsole(false);
        setStatus(STATUS_NONE);

        setProfilerHome(settings);

        TargetAppRunner runner = new TargetAppRunner(settings, new TestProfilerAppHandler(this),
                                                     new TestProfilingPointsProcessor());
        runner.addProfilingEventListener(Utils.createProfilingListener(this));

        try {
            assertTrue("Error in calibration", runner.calibrateInstrumentationCode());
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse("Error in calibration", true);
        } finally {
            runner.terminateTargetJVM();

            //            waitForStatus(STATUS_FINISHED, 60 * 1000);
        }
    }
}
