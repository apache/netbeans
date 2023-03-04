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

package org.netbeans.modules.glassfish.common;

import java.util.Collection;
import java.util.HashSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vkraemer
 */
public class GlassfishInstanceProviderTest {

    public GlassfishInstanceProviderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    /**
     * Test of computeNeedToRegister method, of class GlassfishInstanceProvider.
     */
    @Test
    public void testComputeNeedToRegister() {
//        System.out.println("computeNeedToRegister");
//
//        // the first-run value is false.. so the userdir doesn't have a value for
//        // prop-first-run
//        String firstRunValue = "false";
//        String candidate = "/a";
//        Collection<String> registeredInstalls = new HashSet<String>();
//        boolean expResult = true;
//        boolean result = GlassfishInstanceProvider.computeNeedToRegister(firstRunValue, candidate, registeredInstalls);
//        assertEquals(expResult, result);
//
//        // the value is set to the same value as the candidate...
//        firstRunValue ="/a";
//        expResult = false;
//        result = GlassfishInstanceProvider.computeNeedToRegister(firstRunValue, candidate, registeredInstalls);
//        assertEquals(expResult, result);
//
//        // the first-run and candidate are different
//        firstRunValue ="/b";
//        expResult = true;
//        result = GlassfishInstanceProvider.computeNeedToRegister(firstRunValue, candidate, registeredInstalls);
//        assertEquals(expResult, result);
//
//        // the value is true... so we have run before using the old mechanism
//        firstRunValue = "true";
//        expResult = true;
//
//        // the list of registered servers is empty...
//        result = GlassfishInstanceProvider.computeNeedToRegister(firstRunValue, candidate, registeredInstalls);
//        assertEquals(expResult, result);
//
//        // the list is not empty... but does not contain a match.
//        registeredInstalls.add("/b");
//        registeredInstalls.add("/c");
//        result = GlassfishInstanceProvider.computeNeedToRegister(firstRunValue, candidate, registeredInstalls);
//        assertEquals(expResult, result);
//
//        // the list contains a match...
//        expResult = false;
//        registeredInstalls.add("/a");
//        result = GlassfishInstanceProvider.computeNeedToRegister(firstRunValue, candidate, registeredInstalls);
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
    }

}
