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

package org.netbeans.performance.j2ee.startup;

import java.io.IOException;
import org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase;


/**
 * Measures startup time by MeasureStartupTimeTestCase class.
 * Martin.Schovanek@sun.com
 */
public class MeasureJ2EEStartupTime extends MeasureStartupTimeTestCase {
    
    public static final String suiteName="J2EE Startup suite";    
    
    /** Define testcase
     * @param testName name of the testcase
     */
    public MeasureJ2EEStartupTime(java.lang.String testName) {
        super(testName);
    }
    
    @Override
    public void setUp() {
        System.out.println("########  "+getName()+"  ########");
    }

    /** Testing start of IDE with measurement of the startup time.
     * @throws IOException
     */
    public void testStartIDE() throws IOException {
        measureComplexStartupTime("Startup Time");
    }
    
    /** Testing start of IDE with measurement of the startup time.
     * @throws IOException
     */
    public void testStartIDEWithOpenedFiles() throws IOException {
        measureComplexStartupTime("Startup Time with opened J2EE projects");
    }
    
    /** Testing start of IDE with measurement of the startup time.
     * @throws IOException
     */
    public void testStartIDEWithWeb() throws IOException {
        measureComplexStartupTime("Startup Time with opened Web projects");
    }
}
