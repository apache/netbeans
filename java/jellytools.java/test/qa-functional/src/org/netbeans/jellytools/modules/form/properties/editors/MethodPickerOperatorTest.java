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
package org.netbeans.jellytools.modules.form.properties.editors;

import java.io.IOException;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of
 * org.netbeans.jellytools.modules.form.properties.editors.MethodPickerOperator.
 *  *
 * @author Jiri Skrivanek
 */
public class MethodPickerOperatorTest extends FormPropertiesEditorsTestCase {

    public static final String[] tests = new String[]{
        "testLblComponent",
        "testCboComponent",
        "testLblMethods",
        "testLstMethods",
        "testSetComponent",
        "testSetMethods",
        "testClose"
    };

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static NbTestSuite suite() {
        return (NbTestSuite) createModuleTest(MethodPickerOperatorTest.class, tests);
    }

    /** Opens method picker. */
    @Override
    protected void setUp() throws IOException {
        super.setUp();
        if (mpo == null) {
            // need to wait because combo box is not refreshed in time
            new EventTool().waitNoEvent(1000);
            // set "Value from existing component"
            fceo.setMode(Bundle.getString("org.netbeans.modules.form.Bundle", "CTL_FormConnection_DisplayName"));
            ParametersPickerOperator paramPicker = new ParametersPickerOperator(PROPERTY_NAME);
            paramPicker.methodCall();
            paramPicker.selectMethod();
            mpo = new MethodPickerOperator();
        }
    }
    private static MethodPickerOperator mpo;

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public MethodPickerOperatorTest(String testName) {
        super(testName);
    }

    /** Test of lblComponent method. */
    public void testLblComponent() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", "CTL_CW_Component");
        String label = mpo.lblComponent().getText();
        assertEquals("Wrong label found.", expected, label);
    }

    /** Test of cboComponent method. */
    public void testCboComponent() {
        mpo.cboComponent();
    }

    /** Test of lblMethods method. */
    public void testLblMethods() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", "CTL_CW_MethodList");
        String label = mpo.lblMethods().getText();
        assertEquals("Wrong label found.", expected, label);
    }

    /** Test of lstMethods method. */
    public void testLstMethods() {
        mpo.lstMethods();
    }

    /** Test of setComponent method. */
    public void testSetComponent() {
        String expected = Bundle.getString("org.netbeans.modules.form.Bundle", "CTL_FormTopContainerName");
        mpo.setComponent(expected);
        assertEquals("Select component failed.", expected, mpo.cboComponent().getSelectedItem());
    }

    /** Test of setMethods method. */
    public void testSetMethods() {
        String expected = "getTitle()";
        mpo.setMethods(expected);
        assertEquals("Select method failed.", expected, mpo.lstMethods().getSelectedValue());
    }

    /** Clean-up after tests. Close opened dialog and property sheet. */
    public void testClose() {
        mpo.close();
        fceo.close();
        fceo = null;
        new PropertySheetOperator("[JFrame]").close();
        new FormDesignerOperator(SAMPLE_FRAME_NAME).closeDiscard();
    }
}
