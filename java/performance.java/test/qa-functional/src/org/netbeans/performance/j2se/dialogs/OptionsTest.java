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

import javax.swing.JComponent;
import junit.framework.Test;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 * Test of Options.
 *
 * @author mmirilovic@netbeans.org
 */
public class OptionsTest extends PerformanceTestCase {

    OptionsOperator options;

    /**
     * Creates a new instance of Options
     *
     * @param testName test name
     */
    public OptionsTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 500;
    }

    /**
     * Creates a new instance of Options
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public OptionsTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 500;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar")
                .addTest(OptionsTest.class)
                .suite();
    }

    public void testOptions() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        repaintManager().addRegionFilter(new LoggingRepaintManager.RegionFilter() {

            @Override
            public boolean accept(JComponent c) {
                return !"javax.swing.JProgressBar".equals(c.getClass().getName());
            }

            @Override
            public String getFilterName() {
                return "Ignore search field cursor";
            }
        });
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        new ActionNoBlock("Tools|Options", null).perform();
        return new NbDialogOperator("Options");
    }
}
