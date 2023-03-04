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
package org.netbeans.jellytools;

import java.util.ResourceBundle;
import junit.framework.Test;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test of org.netbeans.jellytools.Bundle
 *
 * @author Jiri Skrivanek
 */
public class BundleTest extends JellyTestCase {

    /**
     * Method used for explicit testsuite definition
     *
     * @return created suite
     */
    public static Test suite() {
        return NbModuleSuite.createConfiguration(BundleTest.class)
                .addTest("testGetBundle",
                        "testGetString",
                        "testGetStringParams",
                        "testGetStringTrimmed",
                        "testGetStringTrimmedParams")
                .gui(false)
                .enableModules(".*")
                .clusters(".*")
                .suite();
    }

    /**
     * Redirect output to log files, wait before each test case and show dialog
     * to test.
     */
    @Override
    protected void setUp() {
        System.out.println("### " + getName() + " ###");
    }

    /**
     * Clean up after each test case.
     */
    @Override
    protected void tearDown() {
    }

    /**
     * Constructor required by JUnit.
     *
     * @param testName method name to be used as testcase
     */
    public BundleTest(java.lang.String testName) {
        super(testName);
    }

    /**
     * Test of getBundle method.
     */
    public void testGetBundle() {
        try {
            ResourceBundle resBundle = Bundle.getBundle("org.netbeans.core.Bundle");
            assertNotNull("Should not return null.", resBundle);
        } catch (JemmyException e) {
            fail("Should always find org.netbeans.core.Bundle");
        }
        try {
            Bundle.getBundle("nonsense.package.Bundle");
            fail("Should not find nonsense.package.Bundle");
        } catch (JemmyException e) {
            // right, should fail
        }
        try {
            Bundle.getBundle(null);
            fail("Should not accept null parameter.");
        } catch (JemmyException e) {
            // right, should fail
        }
    }

    /**
     * Test of getString method. Tests also negative cases
     */
    public void testGetString() {
        try {
            String value = Bundle.getString("org.netbeans.core.windows.services.Bundle", "OK_OPTION_CAPTION");
            assertNotNull("Should not return null.", value);
            assertTrue("Should not return empty string.", value.length() != 0);
        } catch (JemmyException e) {
            fail("Should always find OK_OPTION_CAPTION at org.netbeans.core.windows.services.Bundle.");
        }
        try {
            Bundle.getString("org.netbeans.core.Bundle", null);
            fail("Should not accept null parameter.");
        } catch (JemmyException e) {
            // right, should fail
        }
        try {
            String bundleString = "org.netbeans.core.Bundle";
            Bundle.getString(bundleString, "nonsense key - @#$%^");
            fail("Should not find nonsense key.");
        } catch (JemmyException e) {
            // right, should fail
        }
        try {
            Bundle.getString((ResourceBundle) null, "OK_OPTION_CAPTION");
            fail("Should not accept null ResourceBundle parameter.");
        } catch (JemmyException e) {
            // right, should fail
        }
    }

    /**
     * Test of getString method with parameter to format.
     */
    public void testGetStringParams() {
        String pattern = Bundle.getString("org.netbeans.core.windows.view.ui.Bundle", "CTL_FMT_LocalProperties");
        Object[] params = new Object[]{new Integer(1), "AnObject"};
        String value = Bundle.getString("org.netbeans.core.windows.view.ui.Bundle", "CTL_FMT_LocalProperties", params);
        String expected = java.text.MessageFormat.format(pattern, params);
        assertEquals("Parameters not properly formattted.", expected, value);
    }

    /**
     * Test of getStringTrimmed method.
     */
    public void testGetStringTrimmed() {
        //Saving {0} ...
        String valueRaw = Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_SavingMessage");
        String value = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "CTL_FMT_SavingMessage");
        assertTrue("Characters '{' should be cut off from \"" + valueRaw + "\".", value.indexOf('{') == -1);
        // "&Help"
        valueRaw = Bundle.getString("org.netbeans.core.ui.resources.Bundle", "Menu/Help");
        value = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Help");
        assertTrue("Characters '&' should be removed from \"" + valueRaw + "\".", value.indexOf('&') == -1);
    }

    /**
     * Test of getStringTrimmed method with parameter to format.
     */
    public void testGetStringTrimmedParams() {
        String pattern = Bundle.getString("org.netbeans.core.windows.view.ui.Bundle", "CTL_FMT_LocalProperties");
        Object[] params = new Object[]{new Integer(1), "AnOb&ject"};
        String value = Bundle.getStringTrimmed("org.netbeans.core.windows.view.ui.Bundle", "CTL_FMT_LocalProperties", params);
        String expected = java.text.MessageFormat.format(pattern, params);
        expected = new StringBuffer(expected).deleteCharAt(expected.indexOf('&')).toString();
        assertEquals("Parameters not properly formattted.", expected, value);
    }
}
