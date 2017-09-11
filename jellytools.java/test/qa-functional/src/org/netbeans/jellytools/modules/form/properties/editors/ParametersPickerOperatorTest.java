/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
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
 * org.netbeans.jellytools.modules.form.properties.editors.ParametersPickerOperator.
 *  *
 * @author Jiri Skrivanek
 */
public class ParametersPickerOperatorTest extends FormPropertiesEditorsTestCase {

    public static final String[] tests = new String[]{
        "testLblGetParameterFrom",
        "testRbComponent",
        "testCboComponent",
        "testRbProperty",
        "testTxtProperty",
        "testBtSelectProperty",
        "testRbMethodCall",
        "testTxtMethodCall",
        "testBtSelectMethod",
        // Component radion button is disabled in sample JFrame
        //"testComponent",
        //"testSetComponent",
        "testProperty",
        "testSelectProperty",
        "testMethodCall",
        "testSelectMethod",
        "testClose"
    };

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static NbTestSuite suite() {
        return (NbTestSuite) createModuleTest(ParametersPickerOperatorTest.class, tests);
    }

    /** Opens method picker. */
    @Override
    protected void setUp() throws IOException {
        super.setUp();
        if (ppo == null) {
            // need to wait because combo box is not refreshed in time
            new EventTool().waitNoEvent(1000);
            // set "Value from existing component"
            fceo.setMode(Bundle.getString("org.netbeans.modules.form.Bundle", "CTL_FormConnection_DisplayName"));
            ppo = new ParametersPickerOperator(PROPERTY_NAME);
        }
    }
    private static ParametersPickerOperator ppo;

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public ParametersPickerOperatorTest(String testName) {
        super(testName);
    }

    /** Test of lblGetParameterFrom method. */
    public void testLblGetParameterFrom() {
        String expected = Bundle.getString("org.netbeans.modules.form.Bundle",
                "ConnectionCustomEditor.jLabel1.text");
        String label = ppo.lblGetParameterFrom().getText();
        assertEquals("Wrong label found.", expected, label);
    }

    /** Test of rbComponent method. */
    public void testRbComponent() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                "ConnectionCustomEditor.beanRadio.text");
        String found = ppo.rbComponent().getText();
        assertEquals("Wrong radio button found.", expected, found);
    }

    /** Test of cboComponent method. */
    public void testCboComponent() {
        assertTrue("Wrong combo box found. Should not be enabled.", !ppo.cboComponent().isEnabled());
    }

    /** Test of rbProperty method. */
    public void testRbProperty() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                "ConnectionCustomEditor.propertyRadio.text");
        String found = ppo.rbProperty().getText();
        assertEquals("Wrong radio button found.", expected, found);
    }

    /** Test of txtProperty method. */
    public void testTxtProperty() {
        ppo.property();
        assertTrue("Wrong text field found. Should be enabled.", ppo.txtProperty().isEnabled());
    }

    /** Test of btSelectProperty method. */
    public void testBtSelectProperty() {
        ppo.property();
        assertTrue("Wrong button found. Should be enabled.", ppo.btSelectProperty().isEnabled());
    }

    /** Test of rbMethodCall method. */
    public void testRbMethodCall() {
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", "CTL_CW_Method");
        String found = ppo.rbMethodCall().getText();
        assertEquals("Wrong radio button found.", expected, found);
    }

    /** Test of txtMethodCall method. */
    public void testTxtMethodCall() {
        ppo.methodCall();
        assertTrue("Wrong text field found. Should be enabled.", ppo.txtMethodCall().isEnabled());
    }

    /** Test of btSelectMethod method. */
    public void testBtSelectMethod() {
        ppo.methodCall();
        assertTrue("Wrong button found. Should be enabled.", ppo.btSelectMethod().isEnabled());
    }

    /** Test of component method. */
    public void testComponent() {
        ppo.component();
        assertTrue("Pushing radio button failed. It should be selected.", ppo.rbComponent().isSelected());
    }

    /** Test of setComponent method. */
    public void testSetComponent() {
        ppo.component();
        String expected = "item";
        ppo.setComponent(expected);
        String found = ppo.cboComponent().getSelectedItem().toString();
        assertEquals("Set item of component field failed.", expected, found);
    }

    /** Test of property method. */
    public void testProperty() {
        ppo.property();
        assertTrue("Pushing radio button failed. It should be selected.", ppo.rbProperty().isSelected());
    }

    /** Test of selectProperty method. */
    public void testSelectProperty() {
        ppo.property();
        ppo.selectProperty();
        new PropertyPickerOperator().close();
    }

    /** Test of methodCall method. */
    public void testMethodCall() {
        ppo.methodCall();
        assertTrue("Pushing radio button failed. It should be selected.", ppo.rbMethodCall().isSelected());
    }

    /** Test of selectMethod method. */
    public void testSelectMethod() {
        ppo.methodCall();
        ppo.selectMethod();
        new MethodPickerOperator().close();
    }

    /** Clean-up after tests. Close opened dialog and property sheet. */
    public void testClose() {
        ppo.close();
        fceo = null;
        new PropertySheetOperator("[JFrame]").close();
        new FormDesignerOperator(SAMPLE_FRAME_NAME).closeDiscard();
    }
}
