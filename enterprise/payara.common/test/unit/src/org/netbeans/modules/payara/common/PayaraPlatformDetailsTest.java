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

package org.netbeans.modules.payara.common;

import java.io.File;
import java.util.Optional;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersion;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersionAPI;

/**
 *
 * @author vkraemer
 */
public class PayaraPlatformDetailsTest {

    public PayaraPlatformDetailsTest() {
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
     * Test of getVersionFromInstallDirectory method, of class PayaraPlatformDetails.
     */
    @Test
    public void testGetVersionFromInstallDirectory() {
        System.out.println("getVersionFromInstallDirectory");
        File glassfishDir = null;
        Optional<PayaraPlatformVersionAPI> result = PayaraPlatformDetails.getVersionFromInstallDirectory(glassfishDir);
        assertFalse(result.isPresent());
    }

    /**
     * Test of isInstalledInDirectory method, of class PayaraPlatformDetails.
     */
    @Test
    public void testIsInstalledInDirectory() {
        System.out.println("isInstalledInDirectory");
        File glassfishDir = null;
        PayaraPlatformVersionAPI instance = PayaraPlatformVersion.getLatestVersion();
        boolean expResult = false;
        boolean result = PayaraPlatformDetails.isInstalledInDirectory(instance, glassfishDir);
        assertEquals(expResult, result);
    }

}