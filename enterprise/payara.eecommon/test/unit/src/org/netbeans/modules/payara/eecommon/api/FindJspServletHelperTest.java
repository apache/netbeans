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

package org.netbeans.modules.payara.eecommon.api;


import static org.junit.Assert.*;

public class FindJspServletHelperTest {

    public FindJspServletHelperTest() {
    }

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
    }

    @org.junit.AfterClass
    public static void tearDownClass() throws Exception {
    }

    @org.junit.Before
    public void setUp() throws Exception {
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    //test copied from
    //j2ee.sun.appsrv81/test/unit/src/org/netbeans/modules/j2ee/sun/ide/j2ee/jsps/FindJSPServletImplTest.java
    /**
     * Test of getServletResourcePath method, of class FindJSPServletHelper.
     * 
     */
    @org.junit.Test
    public void testGetServletResourcePath() {
        System.out.println("getServletResourcePath");
        String moduleContextPath = "";
        String jspResourcePath = "/test/index.jsp";
        String expResult = "org/apache/jsp/test/index_jsp.java";
        String result = FindJSPServletHelper.getServletResourcePath(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        jspResourcePath = "/index.jsp";
        expResult = "org/apache/jsp/index_jsp.java";
        result = FindJSPServletHelper.getServletResourcePath(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        jspResourcePath = "index.jsp";
        expResult = "org/apache/jsp/index_jsp.java";
        result = FindJSPServletHelper.getServletResourcePath(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        jspResourcePath = "a";
        expResult = "org/apache/jsp/a.java";
        result = FindJSPServletHelper.getServletResourcePath(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        try {
            jspResourcePath = "";
            expResult = "";
            result = FindJSPServletHelper.getServletResourcePath(moduleContextPath, jspResourcePath);
            fail("should have triggered an exception");            
        } catch (IllegalArgumentException iae) {
            
        }
    }

    
}
