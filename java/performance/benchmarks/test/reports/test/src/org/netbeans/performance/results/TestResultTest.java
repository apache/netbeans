/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * TestResultTest.java
 * JUnit based test
 *
 * Created on March 23, 2006, 9:28 AM
 */

package org.netbeans.performance.results;

import java.util.Collections;
import junit.framework.TestCase;
import junit.framework.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author radim
 */
public class TestResultTest extends TestCase {

    public TestResultTest(String testName) {
        super(testName);
    }

    public static class StatisticsTest extends TestCase {

        public StatisticsTest(String testName) {
            super(testName);
        }

    }


    public static Test suite() {
        TestSuite suite = new TestSuite(TestResultTest.class);
//        suite.addTestSuite(StatisticsTest.class);

        return suite;
    }

    /**
     * Test of computeStatistics method, of class org.netbeans.performance.results.TestResult.
     */
    public void testComputeStatistics() {

        Collection<Integer> values;
        TestResult.Statistics result;

        values = Collections.emptySet();
        result = TestResult.computeStatistics(values);
        assertEquals("wrong count", 0.0, result.getAverage());

        values = new ArrayList<>();
        values.add(1);
        result = TestResult.computeStatistics(values);
        assertEquals("wrong average", 1.0, result.getAverage());
        assertEquals("wrong count", 1, result.getCount());
        assertEquals("wrong variance", 0.0, result.getVariance());

        values.add(1);
        result = TestResult.computeStatistics(values);
        assertEquals("wrong average", 1.0, result.getAverage());
        assertEquals("wrong count", 2, result.getAverage());
        assertEquals("wrong variance", 0.0, result.getVariance());

        values.add(4);
        result = TestResult.computeStatistics(values);
        assertEquals("wrong average", 2.0, result.getAverage());
        assertEquals("wrong count", 3, result.getAverage());
        assertFalse("wrong variance", result.getVariance() == 0.0);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of compareTo method, of class org.netbeans.performance.results.TestResult.
     */
    public void testCompareTo() {
        TestResult r1 = new TestResult("Test a", 1000, "ms", 1, "Suite 1");
        TestResult r2 = new TestResult("Test b", 1000, "ms", 1, "Suite 1");
        TestResult r3 = new TestResult("Test a", 1000, "ms", 1, "Suite 2");
        TestResult r1a = new TestResult("Test a", 1000, "ms", 1, "Suite 1");

        assertTrue (r1.compareTo(r1) == 0);
        assertTrue (r1.compareTo(r1a) == 0);

        assertTrue (r1.compareTo(r2) < 0);
        assertTrue (r2.compareTo(r1) > 0);
        assertTrue (r1.compareTo(r3) < 0);
        assertTrue (r3.compareTo(r1) > 0);
        assertTrue (r2.compareTo(r3) < 0);
        assertTrue (r3.compareTo(r2) > 0);
    }
    
}
