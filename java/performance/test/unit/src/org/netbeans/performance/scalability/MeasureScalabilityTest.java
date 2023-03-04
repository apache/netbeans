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
package org.netbeans.performance.scalability;

import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

/**
 *
 * @author petr
 */
public class MeasureScalabilityTest {
    public static NbTestSuite suite() {
        PerformanceTestCase.prepareForMeasurements();

        NbTestSuite suite = new NbTestSuite("Scalability suite");
        System.setProperty("suitename", MeasureScalabilityTest.class.getCanonicalName());
        System.setProperty("suite", "Scalability suite");

        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(ExpandFolderTest.class)
        .addTest(TabSwitchSpeedTest.class)
        .enableModules(".*").clusters(".*").reuseUserDir(true)));
        return suite;
    }
}
