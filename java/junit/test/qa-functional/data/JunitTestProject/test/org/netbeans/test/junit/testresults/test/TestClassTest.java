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

package org.netbeans.test.junit.testresults.test;

import junit.framework.*;

/**
 *
 * @author ms159439
 */
public class TestClassTest extends TestCase {
    
    public TestClassTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        junit.framework.TestSuite suite = 
                new junit.framework.TestSuite(TestClassTest.class);
        return suite;
    }

    /**
     * Test of add0 method, of class org.netbeans.test.junit.testresults.test.TestClass.
     */
    public void testAdd0() {
        System.out.println("add0");
        
        int a = 1;
        int b = 1;
        TestClass instance = new TestClass();
        
        int expResult = 2;
        int result = instance.add0(a, b);
        assertEquals(expResult, result);
    }

    /**
     * Test of add1 method, of class org.netbeans.test.junit.testresults.test.TestClass.
     */
    public void testAdd1() {
        System.out.println("add1");
        
        int a = 1;
        int b = 1;
        TestClass instance = new TestClass();
        
        int expResult = 0; //wrong -- should fail
        int result = instance.add1(a, b);
        assertEquals(expResult, result);
    }
    
}
