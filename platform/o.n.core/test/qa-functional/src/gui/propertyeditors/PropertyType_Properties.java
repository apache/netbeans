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

package gui.propertyeditors;

import org.netbeans.jellytools.properties.editors.StringCustomEditorOperator;

import org.netbeans.jemmy.operators.JEditorPaneOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of Properties Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Properties extends PropertyEditorsTest {

    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;


    public boolean waitDialog = false;

    /** Creates a new instance of PropertyType_Properties */
    public PropertyType_Properties(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "property_Properties";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Properties("testByInPlace"));
        suite.addTest(new PropertyType_Properties("verifyCustomizer"));
        suite.addTest(new PropertyType_Properties("testCustomizerOk"));
        suite.addTest(new PropertyType_Properties("testCustomizerCancel"));
        return suite;
    }
    
    public void testCustomizerOk() {
        propertyValue_L = "propertyName1=propertyValue1";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "pp=xx";
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "propertyName=propertyValue";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;                                     
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        StringCustomEditorOperator customizer = new StringCustomEditorOperator(propertyCustomizer);
        //new EventTool().waitNoEvent(3000);
        //customizer.setStringValue(propertyValue_L);
        new JEditorPaneOperator(customizer).setText(propertyValue_L);
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    public void verifyCustomizerLayout() {
        StringCustomEditorOperator customizer = new StringCustomEditorOperator(propertyCustomizer);
        new JEditorPaneOperator(customizer);
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Properties.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
