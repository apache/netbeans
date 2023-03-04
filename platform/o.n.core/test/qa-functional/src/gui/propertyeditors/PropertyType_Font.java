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

import org.netbeans.jellytools.properties.FontProperty;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jellytools.properties.editors.FontCustomEditorOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of  Font Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_Font extends PropertyEditorsTest {
    
    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;
    
    public boolean waitDialog = false;
    
    /** Creates a new instance of PropertyType_Font */
    public PropertyType_Font(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "Font";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_Font("verifyCustomizer"));
        suite.addTest(new PropertyType_Font("testCustomizerCancel"));
        suite.addTest(new PropertyType_Font("testCustomizerOk"));
        suite.addTest(new PropertyType_Font("testCustomizerOkUnknownSize"));
        suite.addTest(new PropertyType_Font("testCustomizerInvalid"));
        return suite;
    }
    
    public void testCustomizerOk() {
        propertyValue_L = "Monospaced, 10, Bold";
        propertyValueExpectation_L = "Monospaced 10 Bold";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerOkUnknownSize() {
        propertyValue_L = "Monospaced, 13, Bold";
        propertyValueExpectation_L = "Monospaced 13 Bold";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "Monospaced, 100, Bold";
        propertyValueExpectation_L = "Monospaced 100 Bold";
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testCustomizerInvalid(){
        propertyValue_L = "Monospaced, xx, Bold Italic";
        propertyValueExpectation_L = "Monospaced xx Bold Italic";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, false);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        FontCustomEditorOperator customizer = new FontCustomEditorOperator(propertyCustomizer);
        
        int index1,index2,index3;
        index1 = propertyValue_L.indexOf(", ");
        
        if(index1>0){
            customizer.setFontName(propertyValue_L.substring(0,index1).trim());
            index2 = propertyValue_L.indexOf(", ", index1+1);
            
            if(index2>0){
                customizer.setFontSize(propertyValue_L.substring(index1+1,index2).trim());
                customizer.setFontStyle(propertyValue_L.substring(index2+1).trim());
            }
        }
        
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    public String getValue(String propertyName) {
        String returnValue;
        PropertySheetOperator propertiesTab = new PropertySheetOperator(propertiesWindow);
        
        returnValue = new FontProperty(propertiesTab, propertyName_L).getValue();
        err.println("GET VALUE = [" + returnValue + "].");
        
        // hack for color poperty, this action expects, that right value is displayed as tooltip
//        returnValue = new Property(propertiesTab, propertyName_L).valueButtonOperator().getToolTipText();
//        err.println("GET VALUE TOOLTIP = [" + returnValue + "].");
        
        return returnValue;
    }
    
    public void verifyCustomizerLayout() {
        FontCustomEditorOperator customizer = new FontCustomEditorOperator(propertyCustomizer);
        customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_Font.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
