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
 * org.netbeans.jellytools.modules.form.properties.editors.PropertyPickerOperator.
 * * @author Jiri Skrivanek
 */
public class PropertyPickerOperatorTest extends FormPropertiesEditorsTestCase {

    public static final String[] tests = new String[]{
        "testLblComponent",
        "testCboComponent",
        "testLblProperties",
        "testLstProperties",
        "testSetComponent",
        "testSetProperty",
        "testClose"
    };

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static NbTestSuite suite() {
        return (NbTestSuite) createModuleTest(PropertyPickerOperatorTest.class, tests);
    }

    /** Redirect output to log files, wait before each test case. */
    @Override
    protected void setUp() throws IOException {
        super.setUp();
        if (ppo == null) {
            // need to wait because combo box is not refreshed in time
            new EventTool().waitNoEvent(1000);
            // set "Value from existing component"
            fceo.setMode(Bundle.getString("org.netbeans.modules.form.Bundle", "CTL_FormConnection_DisplayName"));
            ParametersPickerOperator paramPicker = new ParametersPickerOperator(PROPERTY_NAME);
            paramPicker.property();
            paramPicker.selectProperty();
            ppo = new PropertyPickerOperator();
        }
    }
    private static PropertyPickerOperator ppo;

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public PropertyPickerOperatorTest(java.lang.String testName) {
        super(testName);
    }

    /** Test of lblComponent method. */
    public void testLblComponent() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", "CTL_CW_Component");
        String label = ppo.lblComponent().getText();
        assertEquals("Wrong label found.", expected, label);
    }

    /** Test of cboComponent method. */
    public void testCboComponent() {
        ppo.cboComponent();
    }

    /** Test of lblProperties method. */
    public void testLblProperties() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", "CTL_CW_PropertyList");
        String label = ppo.lblProperties().getText();
        assertEquals("Wrong label found.", expected, label);
    }

    /** Test of lstProperties method. */
    public void testLstProperties() {
        ppo.lstProperties();
    }

    /** Test of setComponent method. */
    public void testSetComponent() {
        String expected = Bundle.getString("org.netbeans.modules.form.Bundle", "CTL_FormTopContainerName");
        ppo.setComponent(expected);
        assertEquals("Select component failed.", expected, ppo.cboComponent().getSelectedItem());
    }

    /** Test of setProperty method. Also closes opened windows. */
    public void testSetProperty() {
        String expected = "title";
        ppo.setProperty(expected);
        assertEquals("Select property failed.", expected, ppo.lstProperties().getSelectedValue());
    }

    /** Clean-up after tests. Close opened dialog and property sheet. */
    public void testClose() {
        ppo.close();
        fceo = null;
        new PropertySheetOperator("[JFrame]").close();
        new FormDesignerOperator(SAMPLE_FRAME_NAME).closeDiscard();
    }
}
