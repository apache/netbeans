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

package org.netbeans.performance.mobility;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.mobility.actions.*;

import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org, rashid@netbeans.org, mrkam@netbeans.org
 */
public class MeasureMobilityActionsTest  {

    public static NbTestSuite suite() {
        PerformanceTestCase.prepareForMeasurements();

        NbTestSuite suite = new NbTestSuite("UI Responsiveness Mobility Actions suite");
        System.setProperty("suitename", MeasureMobilityActionsTest.class.getCanonicalName());
        System.setProperty("suite", "UI Responsiveness Mobility Actions suite");

        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(CreateMobilityProjectTest.class)
                .addTest(OpenMIDletEditorTest.class) // Open a visual MIDlet
                .addTest(MIDletViewsSwitchTest.class) // Flow To Design Switch
                .addTest(CreateVisualMIDletTest.class) // Create Visual MIDlet
                .addTest(CreateMIDletTest.class) // Create MIDlet
// strange test   .addTest(SwitchConfigurationTest.class) // Switch Configuration
                .addTest(OpenMobileProjectTest.class) // Open Mobile CLDC project
                .enableModules(".*").clusters(".*").reuseUserDir(true)));

        return suite;
    }
    
}
