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
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.actions.HelpAction;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of Help Contents window
 *
 * @author anebuzelsky@netbeans.org
 */
public class HelpContentsWindowTest extends PerformanceTestCase {

    /**
     * Creates a new instance of HelpContentsWindow
     *
     * @param testName test name
     */
    public HelpContentsWindowTest(String testName) {
        super(testName);
        expectedTime = 1500;
    }

    /**
     * Creates a new instance of HelpContentsWindow
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public HelpContentsWindowTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 1500;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar")
                .addTest(HelpContentsWindowTest.class)
                .suite();
    }

    public void testHelpContentsWindow() {
        doMeasurement();
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        new HelpAction().perform();
        return new HelpOperator();
    }

    @Override
    public void close() {
        if (testedComponentOperator != null
                && testedComponentOperator.isShowing()
                && ((HelpOperator) testedComponentOperator).getTitle() != null) {
            ((HelpOperator) testedComponentOperator).close();
        } else {
            testedComponentOperator = null;
            closeAllModal();
            closeAllDialogs();
        }
    }
}
