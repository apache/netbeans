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
package org.netbeans.performance.enterprise;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.actions.AddNewBpelProcessTest;
import org.netbeans.performance.enterprise.actions.CreateCompositeApplicationTest;
import org.netbeans.performance.enterprise.actions.WatchProjectsTest;

public class MeasureEnterpriseGCTest extends NbTestCase {

    public MeasureEnterpriseGCTest(String name) {
        super(name);
    }

    public static Test suite() {
        PerformanceTestCase.prepareForMeasurements();

        NbTestSuite suite = new NbTestSuite("Enterprise Performance GC suite");
        System.setProperty("suitename", MeasureEnterpriseGCTest.class.getCanonicalName());


        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(WatchProjectsTest.class)
                .addTest(WatchProjectsTest.class, "testInitGCProjects")
                .addTest(AddNewBpelProcessTest.class, "measureTime")

                // TODO: Uncomment once issue 138456 is fixed
                //.addTest(CreateBPELmodule.class, "measureTime")
                
                .addTest(CreateCompositeApplicationTest.class, "measureTime")
//                .addTest(OpenSchemaView.class, "testGCwithOpenComplexSchemaView")    
                .addTest(WatchProjectsTest.class, "testGCProjects")
                .enableModules(".*").clusters(".*").reuseUserDir(true)));    
/*
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(AddNewBpelProcess.class)
                .addTest("measureTime").enableModules(".*").clusters(".*")));    
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(CreateBPELmodule.class)
                .addTest("measureTime").enableModules(".*").clusters(".*")));    
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(CreateCompositeApplication.class)
                .addTest("measureTime").enableModules(".*").clusters(".*")));    
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(WatchProjects.class)
                .addTest("testGCProjects").enableModules(".*").clusters(".*")));    
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(OpenSchemaView.class)
                .addTest("testGCwithOpenComplexSchemaView").enableModules(".*").clusters(".*")));    
*/
        return suite;
    }
}
