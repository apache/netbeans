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

package org.netbeans.modules.payara.common;

import java.io.File;
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
public class ServerDetailsTest {

    public ServerDetailsTest() {
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
     * Test of valueOf method, of class ServerDetails.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");
        String name = "PAYARA_SERVER_4_1_144";
        ServerDetails expResult = ServerDetails.PAYARA_SERVER_4_1_144;
        ServerDetails result = ServerDetails.valueOf(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of getVersionFromInstallDirectory method, of class ServerDetails.
     */
    @Test
    public void testGetVersionFromInstallDirectory() {
        System.out.println("getVersionFromInstallDirectory");
        File glassfishDir = null;
        int expResult = -1;
        int result = ServerDetails.getVersionFromInstallDirectory(glassfishDir);
        assertEquals(expResult, result);
    }

    /**
     * Test of getVersionFromDomainXml method, of class ServerDetails.
     */
    @Test
    public void testGetVersionFromDomainXml() {
        System.out.println("getVersionFromDomainXml");
        File domainXml = null;
        int expResult = -1;
        int result = ServerDetails.getVersionFromDomainXml(domainXml);
        assertEquals(expResult, result);
    }

    /**
     * Test of isInstalledInDirectory method, of class ServerDetails.
     */
    @Test
    public void testIsInstalledInDirectory() {
        System.out.println("isInstalledInDirectory");
        File glassfishDir = null;
        ServerDetails instance = ServerDetails.PAYARA_SERVER_4_1_144;
        boolean expResult = false;
        boolean result = instance.isInstalledInDirectory(glassfishDir);
        assertEquals(expResult, result);
    }

}