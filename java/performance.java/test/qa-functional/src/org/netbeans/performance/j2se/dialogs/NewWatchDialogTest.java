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
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of New Watch dialog
 *
 * @author anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class NewWatchDialogTest extends PerformanceTestCase {

    /**
     * Creates a new instance of NewWatchDialog
     *
     * @param testName test name
     */
    public NewWatchDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of NewWatchDialog
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public NewWatchDialogTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar")
                .addTest(NewWatchDialogTest.class)
                .suite();
    }

    public void testNewWatchDialog() {
        doMeasurement();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        new ActionNoBlock("Debug|New Watch", null).perform();
        return new NbDialogOperator("New Watch");
    }
}
