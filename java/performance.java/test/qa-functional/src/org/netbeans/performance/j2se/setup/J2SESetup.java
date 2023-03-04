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
package org.netbeans.performance.j2se.setup;

import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase2;

/**
 * Test suite that actually does not perform any test but sets up user directory
 * for UI responsiveness tests
 *
 * @author mmirilovic@netbeans.org
 */
public class J2SESetup extends PerformanceTestCase2 {

    public J2SESetup(java.lang.String testName) {
        super(testName);
    }

    public void testOpenDataProject() throws Exception {
        this.openDataProjects("PerformanceTestData");
    }

    public void testOpenFoldersProject() throws Exception {
        this.openDataProjects("PerformanceTestFoldersData");
    }

    public void testOpenNBProject() throws Exception {
        this.openDataProjects("SystemProperties");
    }

    public void testCloseMemoryToolbar() {
        CommonUtilities.closeMemoryToolbar();
        closeAllModal();
    }
}
