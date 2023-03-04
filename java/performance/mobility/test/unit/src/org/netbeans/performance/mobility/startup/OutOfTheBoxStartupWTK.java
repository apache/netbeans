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

package org.netbeans.performance.mobility.startup;

import java.io.IOException;
import org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase;

/**
 * Measure startup time by org.netbeans.core.perftool.StartLog.
 * Number of starts with new userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat.with.new.userdir </code>
 * <br> and number of starts with old userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat </code>
 * Run measurement defined number times, but forget first measured value,
 * it's a attempt to have still the same testing conditions with
 * loaded and cached files.
 *
 * @author mmirilovic@netbeans.org
 */
public class OutOfTheBoxStartupWTK extends MeasureStartupTimeTestCase {
    
    /** Define testcase
     * @param testName name of the testcase
     */
    public OutOfTheBoxStartupWTK(String testName) {
        super(testName);
    }
    
    /** Testing start of IDE with measurement of the startup time.
     * @throws IOException
     */
    public void testStartIDE() throws java.io.IOException {
        String performanceDataName = "Startup Time with WTK";
        
        // don't report first run, try to have still the same testing conditions
        runIDE(MeasureStartupTimeTestCase.getIdeHome(),
                new java.io.File(getWorkDir(),"ideuserdir_prepare"), getMeasureFile(0,0),1000);
        
        for (int n=1;n <= repeatNewUserdir; n++){
            for (int i=1; i <= repeat; i++) {
                long measuredTime = runIDEandMeasureStartup(performanceDataName, getMeasureFile(i,n), getUserdirFile(n),10000);
                reportPerformance(performanceDataName, measuredTime, "ms", i>1?2:1);
            }
        }
    }
}
