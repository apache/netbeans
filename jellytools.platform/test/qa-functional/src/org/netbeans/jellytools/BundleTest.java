/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.jellytools;

import java.util.ResourceBundle;
import junit.framework.Test;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test of org.netbeans.jellytools.Bundle
 * @author Jiri Skrivanek
 */
public class BundleTest extends JellyTestCase {

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(BundleTest.class).
                addTest("testGetBundle",
                "testGetString",
                "testGetStringParams",
                "testGetStringTrimmed",
                "testGetStringTrimmedParams").gui(false).enableModules(".*").clusters(".*"));
    }

    /** Redirect output to log files, wait before each test case and
     * show dialog to test. */
    @Override
    protected void setUp() {
        System.out.println("### " + getName() + " ###");
    }

    /** Clean up after each test case. */
    @Override
    protected void tearDown() {
    }

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public BundleTest(java.lang.String testName) {
        super(testName);
    }

    /** Test of getBundle method. */
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

    /** Test of getString method. Tests also negative cases */
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

    /** Test of getString method with parameter to format. */
    public void testGetStringParams() {
        String pattern = Bundle.getString("org.netbeans.core.windows.view.ui.Bundle", "CTL_FMT_LocalProperties");
        Object[] params = new Object[]{new Integer(1), "AnObject"};
        String value = Bundle.getString("org.netbeans.core.windows.view.ui.Bundle", "CTL_FMT_LocalProperties", params);
        String expected = java.text.MessageFormat.format(pattern, params);
        assertEquals("Parameters not properly formattted.", expected, value);
    }

    /** Test of getStringTrimmed method. */
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

    /** Test of getStringTrimmed method with parameter to format. */
    public void testGetStringTrimmedParams() {
        String pattern = Bundle.getString("org.netbeans.core.windows.view.ui.Bundle", "CTL_FMT_LocalProperties");
        Object[] params = new Object[]{new Integer(1), "AnOb&ject"};
        String value = Bundle.getStringTrimmed("org.netbeans.core.windows.view.ui.Bundle", "CTL_FMT_LocalProperties", params);
        String expected = java.text.MessageFormat.format(pattern, params);
        expected = new StringBuffer(expected).deleteCharAt(expected.indexOf('&')).toString();
        assertEquals("Parameters not properly formattted.", expected, value);
    }
}
