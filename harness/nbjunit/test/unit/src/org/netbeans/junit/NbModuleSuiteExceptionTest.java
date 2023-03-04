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


import java.util.logging.Level;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import test.pkg.not.in.junit.NbModuleSuiteException;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuiteExceptionTest extends TestCase {

    public NbModuleSuiteExceptionTest(String testName) {
        super(testName);
    }

    public void testNoLoggingCheckByDefault() {
        System.setProperty("generate.msg", "true");
        System.setProperty("generate.exc", "true");

        Test instance = NbModuleSuite.createConfiguration(NbModuleSuiteException.class).gui(false).suite();
        TestResult r = junit.textui.TestRunner.run(instance);
        assertEquals("No failures", 0, r.failureCount());
        assertEquals("No errors", 0, r.errorCount());
    }

    public void testFailOnMessage() {
        System.setProperty("generate.msg", "true");
        System.setProperty("generate.exc", "false");

        Test instance =
            NbModuleSuite.createConfiguration(NbModuleSuiteException.class).
            gui(false).
            failOnMessage(Level.WARNING)
        .suite();
        TestResult r = junit.textui.TestRunner.run(instance);
        assertEquals("One failure", 1, r.failureCount());
        assertEquals("No errors", 0, r.errorCount());
        TestFailure f = r.failures().nextElement();
        assertEquals("Failure name", "testGenerateMsgOrException", ((TestCase)f.failedTest()).getName());
    }

    public void testFailOnException() {
        System.setProperty("generate.msg", "false");
        System.setProperty("generate.exc", "true");

        Test instance =
            NbModuleSuite.createConfiguration(NbModuleSuiteException.class).
            gui(false).
            failOnException(Level.INFO)
        .suite();
        TestResult r = junit.textui.TestRunner.run(instance);
        assertEquals("One failure", 1, r.failureCount());
        assertEquals("No errors", 0, r.errorCount());
        TestFailure f = r.failures().nextElement();
        assertEquals("Failure name", "testGenerateMsgOrException", ((TestCase)f.failedTest()).getName());
    }
}
