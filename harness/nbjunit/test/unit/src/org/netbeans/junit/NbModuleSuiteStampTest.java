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
package org.netbeans.junit;

import junit.framework.TestCase;
import junit.framework.TestResult;
import test.pkg.not.in.junit.NbModuleSuiteTimestamps;

/**
 *
 * @author Jaroslav Tulach
 */
public class NbModuleSuiteStampTest extends TestCase {
    public void testTestReuseUsedir(){
        NbTestSuite instance = new NbTestSuite();
        instance.addTest(
            NbModuleSuite.emptyConfiguration().gui(false)
            .addTest(NbModuleSuiteTimestamps.class)
            .enableClasspathModules(false)
        .suite());
        instance.addTest(
            NbModuleSuite.emptyConfiguration().gui(false)
            .addTest(NbModuleSuiteTimestamps.class)
            .reuseUserDir(true)
            .enableClasspathModules(false)
        .suite());
        TestResult res = junit.textui.TestRunner.run(instance);
        assertEquals("Two tests started", 2, res.runCount());
        assertEquals("No failures", 0, res.failureCount());
        assertEquals("No errors", 0, res.errorCount());

        String value = System.getProperty("stamps");
        assertNotNull("Property provided", value);
    }
}
