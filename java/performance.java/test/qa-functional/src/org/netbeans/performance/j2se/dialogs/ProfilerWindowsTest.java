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
package org.netbeans.performance.j2se.dialogs;

import junit.framework.Test;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 *
 * @author mkhramov@netbeans.org
 * @author Jiri Skrivanek
 */
public class ProfilerWindowsTest extends PerformanceTestCase {

    private String commandName;
    private String windowName;

    /**
     * Creates new instance
     *
     * @param testName test name
     */
    public ProfilerWindowsTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates new instance
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public ProfilerWindowsTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar")
                .addTest(ProfilerWindowsTest.class)
                .suite();
    }

    public void testProfilerSnapshots() {
        commandName = "Window|Profiling|Snapshots";
        windowName = "Snapshots";
        doMeasurement();
    }

    public void testProfilerProfilingPoints() {
        commandName = "Window|Profiling|Profiling Points";
        windowName = "Profiling Points";
        doMeasurement();
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        MainWindowOperator.getDefault().menuBar().closeSubmenus();
        MainWindowOperator.getDefault().menuBar().pushMenu(commandName);
        return new TopComponentOperator(windowName);
    }

    @Override
    public void close() {
        if (testedComponentOperator != null && testedComponentOperator.isShowing()) {
            ((TopComponentOperator) this.testedComponentOperator).close();
        }
    }
}
