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

package org.netbeans.performance.enterprise.setup;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase2;
import org.openide.util.Exceptions;

/**
 * Test suite that actually does not perform any test but sets up user directory
 * for UI responsiveness tests
 *
 * @author  mmirilovic@netbeans.org, mrkam@netbeans.org
 */
public class EnterpriseSetup extends PerformanceTestCase2 {

    public EnterpriseSetup(java.lang.String testName) {
        super(testName);
    }

    public void testCloseMemoryToolbar() {
        CommonUtilities.closeMemoryToolbar();
    }

    public void testAddApplicationServer() {
        CommonUtilities.addApplicationServer();
    }

    public void testAddTomcatServer() {
        CommonUtilities.addTomcatServer();
    }

    public void testOpenReservationPartnerServicesProject() {

        try {
            this.openDataProjects("TravelReservationService" + File.separator + "ReservationPartnerServices");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void testOpenTravelReservationServiceProject() {

        try {
            this.openDataProjects("TravelReservationService" + File.separator + "TravelReservationService");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void testOpenTravelReservationServiceApplicationProject() {

        try {
            this.openDataProjects("TravelReservationService" + File.separator + "TravelReservationServiceApplication");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void testOpenSoaTestProject() {

        try {
            this.openDataProjects("SOATestProject");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void testOpenBPELTestProject() {

        try {
            this.openDataProjects("BPELTestProject");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
