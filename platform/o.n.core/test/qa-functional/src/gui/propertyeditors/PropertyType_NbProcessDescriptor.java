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

import gui.propertyeditors.utilities.CoreSupport;

import org.netbeans.jellytools.properties.editors.ProcessDescriptorCustomEditorOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of NbProcessDescriptor Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_NbProcessDescriptor extends PropertyEditorsTest {

    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;

    public boolean waitDialog = false;

    private String DELIM = ";";
    
    private static String FS_Data_path;
    
    /** Creates a new instance of PropertyType_NbProcessDescriptor */
    public PropertyType_NbProcessDescriptor(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "NbProcessDescriptor";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        
        //TODO write new way for promoD
        //String path = CoreSupport.getSystemPath("gui/data", CoreSupport.beanName, "java");
        String path = ""; 
        
        FS_Data_path = path.substring(0,path.lastIndexOf(System.getProperty("file.separator")));
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_NbProcessDescriptor("testByInPlace"));
        suite.addTest(new PropertyType_NbProcessDescriptor("verifyCustomizer"));
        suite.addTest(new PropertyType_NbProcessDescriptor("testCustomizerCancel"));
        suite.addTest(new PropertyType_NbProcessDescriptor("testCustomizerOk"));
        return suite;
    }
    
    
    public void testCustomizerOk() {
        propertyValue_L = FS_Data_path + DELIM + "okArgument";
        propertyValueExpectation_L =  FS_Data_path + " okArgument";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = "cancel" + DELIM + "cancel_argument";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "process argument";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyCustomizer() {
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        ProcessDescriptorCustomEditorOperator customizer = new ProcessDescriptorCustomEditorOperator(propertyCustomizer);
        
        int index = propertyValue_L.indexOf(DELIM);
        String process = propertyValue_L.substring(0,index);
        String argument = propertyValue_L.substring(index + DELIM.length());
        
        customizer.setProcess(process);
        customizer.setArguments(argument);
        
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    public void verifyCustomizerLayout() {
        ProcessDescriptorCustomEditorOperator customizer = new ProcessDescriptorCustomEditorOperator(propertyCustomizer);
        customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_NbProcessDescriptor.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
