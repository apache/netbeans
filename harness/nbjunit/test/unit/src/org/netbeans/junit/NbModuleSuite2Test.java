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


import test.pkg.not.in.junit.NbModuleSuiteT;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.netbeans.junit.NbModuleSuite.Configuration;
import test.pkg.not.in.junit.NbModuleSuiteMeta;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuite2Test extends TestCase {
    
    public NbModuleSuite2Test(String testName) {
        super(testName);
    }            
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
    public void testServices() throws Exception{
        Configuration conf = NbModuleSuite.createConfiguration(NbModuleSuiteMeta.class).gui(false);
        Test test = conf.suite();
        test.run(new TestResult());
        assertNotNull("The test was running", System.getProperty("meta"));
        assertEquals("result" + System.getProperty("meta"), "ok", System.getProperty("meta"));
    }

    public void testRun() {
        System.setProperty("t.one", "no");
        Test instance = NbModuleSuite.createConfiguration(NbModuleSuiteT.class).gui(false).suite();
        junit.textui.TestRunner.run(instance);
        
        assertEquals("OK", System.getProperty("t.one"));
        NbModuleSuiteTest.assertProperty("netbeans.full.hack", "true");
    }

    public void testRunEmptyConfig() {
        System.setProperty("t.one", "no");
        
        Test instance = NbModuleSuite.emptyConfiguration().gui(false).suite();
        junit.textui.TestRunner.run(instance);
        
        assertEquals("nothing has been executed", "no", System.getProperty("t.one"));
        NbModuleSuiteTest.assertProperty("netbeans.full.hack", "true");
    }

    public void testRunEmptyConfigWithOneAdd() {
        System.setProperty("t.one", "no");
        
        Test instance = NbModuleSuite.emptyConfiguration().addTest(NbModuleSuiteT.class).gui(false).suite();
        junit.textui.TestRunner.run(instance);
        
        assertEquals("OK", System.getProperty("t.one"));
        NbModuleSuiteTest.assertProperty("netbeans.full.hack", "true");
    }

    public void testRunEmptyConfigFails() {
        try {
            NbModuleSuite.emptyConfiguration().addTest("ahoj");
            fail("Shall fail as there is no class registered yet");
        } catch (IllegalStateException ex) {
            // ok
        }
    }

    public void testTestCount() throws Exception{
        Test test  = NbModuleSuite.createConfiguration(NbModuleSuiteT.class).gui(false).suite();
        assertEquals(0, test.countTestCases());
        test.run(new TestResult());
        assertEquals("one+fullhack+startuparg", 3, test.countTestCases());
    }
}
