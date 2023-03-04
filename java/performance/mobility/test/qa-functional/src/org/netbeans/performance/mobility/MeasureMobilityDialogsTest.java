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
import org.netbeans.performance.mobility.dialogs.*;

import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;


/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org, mrkam@netbeans.org
 */
public class MeasureMobilityDialogsTest  {

    public static NbTestSuite suite() {
        PerformanceTestCase.prepareForMeasurements();

        NbTestSuite suite = new NbTestSuite("UI Responsiveness Mobility Dialogs suite");
        System.setProperty("suitename", MeasureMobilityDialogsTest.class.getCanonicalName());
        System.setProperty("suite", "UI Responsiveness Mobility Dialogs suite");

        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(NewConfigurationDialogTest.class)
                .addTest(MobilityDeploymentManagerDialogTest.class, "measureTime")
                .addTest(QuickRunDialogTest.class)
                .addTest(ProjectPropertiesDialogTest.class)
                .addTest(CloseProjectPropertyTest.class)
                .enableModules(".*").clusters(".*").reuseUserDir(true)));

        return suite;
    }
    
}
