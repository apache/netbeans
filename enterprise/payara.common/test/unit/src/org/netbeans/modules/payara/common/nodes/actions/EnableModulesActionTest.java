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

package org.netbeans.modules.payara.common.nodes.actions;

import java.util.ArrayList;
import java.util.List;
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
public class EnableModulesActionTest {

    public EnableModulesActionTest() {
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
     * Test of noDups method, of class EnableModulesAction.
     */
    @Test
    public void testGetDup() {
        System.out.println("getDup");
        List<String> targets = null;
        String expResult = null;
        String result = EnableModulesAction.getDup(targets);
        assertEquals(expResult, result);
        targets = new ArrayList<String>();
        targets.add("A");
        result = EnableModulesAction.getDup(targets);
        assertEquals(expResult, result);
        targets.add("B");
        result = EnableModulesAction.getDup(targets);
        assertEquals(expResult, result);
        targets.add("C:1");
        result = EnableModulesAction.getDup(targets);
        assertEquals(expResult, result);
        expResult = "C";
        targets.add("C");
        result = EnableModulesAction.getDup(targets);
        assertEquals(expResult, result);
    }

}