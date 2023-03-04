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
package org.netbeans.performance.j2se.startup;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase;

/**
 * Measure startup time by org.netbeans.core.perftool.StartLog. Number of starts
 * with new userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat.with.new.userdir </code>
 * <br> and number of starts with old userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat </code> Run measurement defined
 * number times, but forget first measured value, it's a attempt to have still
 * the same testing conditions with loaded and cached files.
 *
 * @author mmirilovic@netbeans.org
 */
public class ComplexJavaProjectStartup extends MeasureStartupTimeTestCase {

    public static final String suiteName = "J2SE Startup suite";

    /**
     * Define test case
     *
     * @param testName name of the test case
     */
    public ComplexJavaProjectStartup(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("Java Complex Measurements Suite");
        System.setProperty("suitename", ComplexJavaProjectStartup.class.getCanonicalName());
        suite.addTest(JellyTestCase.emptyConfiguration()
                .addTest(PrepareIDEForComplexMeasurements.class)
                .addTest("testCloseAllDocuments")
                .addTest("testCloseMemoryToolbar")
                .addTest("testOpenFiles")
                .addTest("testSaveStatus")
                .suite());
        suite.addTestSuite(ComplexJavaProjectStartup.class);
        return suite;
    }

    /**
     * Testing start of IDE with measurement of the startup time.
     *
     * @throws java.io.IOException
     */
    public void testStartIDEWithOpenedFiles() throws java.io.IOException {
        measureComplexStartupTime("Startup Time with 10 opened java files");
        PerformanceData[] pData = this.getPerformanceData();
        for (PerformanceData pData1 : pData) {
            org.netbeans.modules.performance.utilities.CommonUtilities.processUnitTestsResults(this.getClass().getName(), pData1);
        }
    }
}
