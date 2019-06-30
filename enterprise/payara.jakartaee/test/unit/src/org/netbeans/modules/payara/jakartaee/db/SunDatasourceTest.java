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

package org.netbeans.modules.payara.jakartaee.db;

import org.netbeans.modules.payara.jakartaee.db.SunDatasource;
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
public class SunDatasourceTest {

    public SunDatasourceTest() {
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
     * Test of equals method, of class SunDatasource.
     */
    @Test
    public void testEquals() {
        Object obj = null;
        String a="A";
        String b="B";
        String c="C";
        String d="D";
        String e="E";
        SunDatasource instance = new SunDatasource(a,b,c,d,e);
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
        obj = "String";
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = new SunDatasource("a","b","c","d","e");
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = new SunDatasource("A","B","C","D","E");
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = new SunDatasource(a,b,c,d,e);
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class SunDatasource, when at least one field
     * is set to null (resulting from a missing property for example).
     */
    @Test
    public void testEqualsNull() {
        Object obj = null;
        String a="A";
        String b="B";
        String c="C";
        String d="D";
        String e=null;
        SunDatasource instance = new SunDatasource(a,b,c,d,e);
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
        obj = "String";
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = new SunDatasource("a","b","c","d","e");
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = new SunDatasource("A","B","C","D",null);
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = new SunDatasource(a,b,c,d,e);
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
    }
}
