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

import junit.framework.Test;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;
import junit.textui.TestRunner;
import org.junit.Ignore;

/**
 * Test JUnit @Ignore annotation support 
 * @author Hector Espert
 */
public class IgnoreTest extends TestCase {

    public void testNbTestCase() throws Exception {
        assertTestClass(BasicTestCase.class, 3, 1, 1);
        assertTestClass(IgnoredTestCase.class, 0, 0, 0);
        assertTestClass(IgnoredTestCaseWithMessage.class, 0, 0, 0);
        assertTestClass(IgnoredMethodTestCase.class, 2, 1, 0);
        assertTestClass(IgnoredMethodTestCaseWithMessage.class, 1, 0, 0);
    }
    
    public void testNbTestSuite() throws Exception {
        assertTestClass(BasicTestSuite.class, 3, 1, 1);
        assertTestClass(IgnoredTestSuite.class, 0, 0, 0);
        assertTestClass(IgnoredMethodTestSuite.class, 3, 1, 0);
    }
    
    private void assertTestClass(Class testClass, int runCount, int failures, int errors) {
        BaseTestRunner runner = new TestRunner();
        TestResult result = new TestResult();
        runner.getTest(testClass.getName()).run(result);
        assertEquals(runCount, result.runCount());
        assertEquals(failures, result.failureCount());
        assertEquals(errors, result.errorCount());
    }
    
    public static class BasicTestCase extends NbTestCase {
        
        public BasicTestCase(String n) {
            super(n);
        }
        
        public void testSuccesful() {
            assertTrue(true);
        }
        
        public void testFailed() {
            fail();
        }
        
        public void testError() {
            throw new RuntimeException();
        }
        
    }
    
    @Ignore
    public static class IgnoredTestCase extends NbTestCase {
        
        public IgnoredTestCase(String n) {
            super(n);
        }
        
        public void testSuccesful() {
            assertTrue(true);
        }
        
        public void testFailed() {
            fail();
        }
        
        public void testError() {
            throw new RuntimeException();
        }
        
    }
    
    @Ignore("Ignored test case")
    public static class IgnoredTestCaseWithMessage extends NbTestCase {
        
        public IgnoredTestCaseWithMessage(String n) {
            super(n);
        }
        
        public void testFailed() {
            fail();
        }

    }
    
    public static class IgnoredMethodTestCase extends NbTestCase {
        
        public IgnoredMethodTestCase(String n) {
            super(n);
        }
        
        public void testSuccesful() {
            assertTrue(true);
        }
        
        public void testFailed() {
            fail();
        }
        
        @Ignore
        public void testIgnored() {
            fail("Test method should be ignored");
        }
        
    }
    
    public static class IgnoredMethodTestCaseWithMessage extends NbTestCase {
        
        public IgnoredMethodTestCaseWithMessage(String n) {
            super(n);
        }
        
        public void testSuccesful() {
            assertTrue(true);
        }

        @Ignore("Ignored method with message")
        public void testIgnored() {
            fail("Test method should be ignored");
        }
        
    }
    
    public static class BasicTestSuite extends TestCase {
        public static Test suite() {
            return new NbTestSuite(BasicTestCase.class);
        }
    }
    
    public static class IgnoredTestSuite extends TestCase {
        public static Test suite() {
            NbTestSuite suite = new NbTestSuite();
            suite.addTestSuite(IgnoredTestCase.class);
            suite.addTestSuite(IgnoredTestCaseWithMessage.class);
            return suite;
        }
    }
    
    public static class IgnoredMethodTestSuite extends TestCase {
        public static Test suite() {
            NbTestSuite suite = new NbTestSuite();
            suite.addTestSuite(IgnoredMethodTestCase.class);
            suite.addTestSuite(IgnoredMethodTestCaseWithMessage.class);
            return suite;
        }
    }
    
}
