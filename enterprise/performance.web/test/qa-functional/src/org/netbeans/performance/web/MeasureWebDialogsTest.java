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
package org.netbeans.performance.web;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.web.dialogs.WebProjectPropertiesWindowTest;
import org.netbeans.performance.web.setup.WebSetup;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author mmirilovic@netbeans.org
 */
public class MeasureWebDialogsTest {

    public static NbTestSuite suite() {
        PerformanceTestCase.prepareForMeasurements();

        NbTestSuite suite = new NbTestSuite("UI Responsiveness Web Dialogs suite");
        System.setProperty("suitename", MeasureWebDialogsTest.class.getCanonicalName());
        System.setProperty("suite", "UI Responsiveness Web Dialogs suite");

        suite.addTest(JellyTestCase.emptyConfiguration().reuseUserDir(true)
                .addTest(WebSetup.class)
                .addTest(WebProjectPropertiesWindowTest.class)
                .suite());
        return suite;
    }
}
