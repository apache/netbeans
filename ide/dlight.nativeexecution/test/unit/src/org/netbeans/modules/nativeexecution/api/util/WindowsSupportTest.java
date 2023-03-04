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
package org.netbeans.modules.nativeexecution.api.util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.openide.util.Utilities;

public class WindowsSupportTest extends NativeExecutionBaseTestCase {

    private final boolean isWindows;

    public WindowsSupportTest(String name) {
        super(name);
        isWindows = Utilities.isWindows();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private boolean na() {
        if (!isWindows) {
            System.out.println("NOT APPLICABLE on " + System.getProperty("os.name"));
        }

        WindowsSupport instance = WindowsSupport.getInstance();
        if (instance.getShell() == null) {
            System.out.println("NOT APPLICABLE: shell not found");
        }

        return !isWindows;
    }

    @Before
    @Override
    public void setUp() {
    }

    @After
    @Override
    public void tearDown() {
    }

    /**
     * Test of getInstance method, of class WindowsSupport.
     */
    @Test
    public void testGetInstance() {
        System.out.println("--- getInstance ---");
        WindowsSupport instance = WindowsSupport.getInstance();
        assertNotNull(instance);
        System.out.println("WindowsSupport instance: " + instance);
    }

    /**
     * Test of getShell method, of class WindowsSupport.
     */
    @Test
    public void testGetShell() {
        System.out.println("--- getShell ---");
        String shell = WindowsSupport.getInstance().getShell();
        assertTrue((shell != null) == isWindows);
        System.out.println("WindowsInstance's default shell is " + shell);
    }

    /**
     * Test of convertToCygwinPath method, of class WindowsSupport.
     */
    @Test
    public void testConvertToCygwinPath() {
        System.out.println("--- convertToCygwinPath ---");

        if (na()) {
            return;
        }

        String winPath = "C:\\Documents and Settings";
        String cygwinPath = "/cygdrive/c/Documents and Settings";
        WindowsSupport instance = WindowsSupport.getInstance();
        String result = instance.convertToCygwinPath(winPath);
        assertEquals(cygwinPath.toLowerCase(), result.toLowerCase());
    }

    /**
     * Test of convertFromCygwinPath method, of class WindowsSupport.
     */
    @Test
    public void testConvertFromCygwinPath() {
        System.out.println("--- convertFromCygwinPath ---");

        if (na()) {
            return;
        }

        String winPath = "C:\\Documents and Settings";
        String cygwinPath = "/cygdrive/c/Documents and Settings";
        WindowsSupport instance = WindowsSupport.getInstance();
        String result = instance.convertFromCygwinPath(cygwinPath);
        assertEquals(winPath.toLowerCase(), result.toLowerCase());
    }

    /**
     * Test of convertToMSysPath method, of class WindowsSupport.
     */
    @Test
    public void testConvertToMSysPath() {
        System.out.println("--- convertToMSysPath ---");

        if (na()) {
            return;
        }

        String winPath = "c:\\Documents and Settings";
        String msysPath = "/c/Documents and Settings";
        WindowsSupport instance = WindowsSupport.getInstance();
        String result = instance.convertToMSysPath(winPath);
        assertEquals(msysPath.toLowerCase(), result.toLowerCase());
    }

    /**
     * Test of convertFromMSysPath method, of class WindowsSupport.
     */
    @Test
    public void testConvertFromMSysPath() {
        System.out.println("--- convertFromMSysPath ---");

        if (na()) {
            return;
        }

        String winPath = "c:\\Documents and Settings";
        String msysPath = "/c/Documents and Settings";
        WindowsSupport instance = WindowsSupport.getInstance();
        String result = instance.convertFromMSysPath(msysPath);
        assertEquals(winPath.toLowerCase(), result.toLowerCase());
    }

    /**
     * Test of convertToShellPath method, of class WindowsSupport.
     */
    @Test
    public void testConvertToShellPath() {
        System.out.println("--- convertToShellPath ---");

        if (na()) {
            return;
        }

//        String path = "";
//        WindowsSupport instance = null;
//        String expResult = "";
//        String result = instance.convertToShellPath(path);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of convertToWindowsPath method, of class WindowsSupport.
     */
    @Test
    public void testConvertToWindowsPath() {
        System.out.println("--- convertToWindowsPath ---");

        if (na()) {
            return;
        }

//        String path = "";
//        WindowsSupport instance = null;
//        String expResult = "";
//        String result = instance.convertToWindowsPath(path);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of convertToAllShellPaths method, of class WindowsSupport.
     */
    @Test
    public void testConvertToAllShellPaths() {
        System.out.println("--- convertToAllShellPaths ---");

        if (na()) {
            return;
        }

//        String paths = "";
//        WindowsSupport instance = null;
//        String expResult = "";
//        String result = instance.convertToAllShellPaths(paths);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
}
