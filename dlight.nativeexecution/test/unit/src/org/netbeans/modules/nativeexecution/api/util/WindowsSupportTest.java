/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
