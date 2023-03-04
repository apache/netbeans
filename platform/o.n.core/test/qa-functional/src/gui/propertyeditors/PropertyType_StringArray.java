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

import org.netbeans.jellytools.properties.editors.StringArrayCustomEditorOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Tests of StringArray Property Editor.
 *
 * @author  Marian.Mirilovic@Sun.Com
 */
public class PropertyType_StringArray extends PropertyEditorsTest {

    public String propertyName_L;
    public String propertyValue_L;
    public String propertyValueExpectation_L;

    public boolean waitDialog = false;

    private final String ADD = "Add:";
    private final String REMOVE = "Remove:";
    private final String EDIT = "Edit:";
    private final String UP = "Up:";
    private final String DOWN = "Down:";
    
    private final String EE = "; ";
    
    /** Creates a new instance of PropertyType_StringArray */
    public PropertyType_StringArray(String testName) {
        super(testName);
    }
    
    
    public void setUp(){
        propertyName_L = "String []";
        super.setUp();
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertyType_StringArray("testByInPlace"));
        suite.addTest(new PropertyType_StringArray("verifyCustomizer"));
        suite.addTest(new PropertyType_StringArray("testCustomizerCancel"));
        suite.addTest(new PropertyType_StringArray("testCustomizerAdd"));
        suite.addTest(new PropertyType_StringArray("testCustomizerRemove"));
        suite.addTest(new PropertyType_StringArray("testCustomizerEdit"));
        suite.addTest(new PropertyType_StringArray("testCustomizerUp"));
        suite.addTest(new PropertyType_StringArray("testCustomizerDown"));
        return suite;
    }
    
    
    public void testCustomizerAdd() {
        propertyValue_L = ADD + "add";
        propertyValueExpectation_L = "remove, down, up, edit, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerEdit() {
        propertyValue_L = EDIT + "edit" + EE + "newEdit";
        propertyValueExpectation_L = "down, up, newEdit, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerRemove() {
        propertyValue_L = REMOVE + "remove";
        propertyValueExpectation_L = "down, up, edit, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }

    public void testCustomizerUp() {
        propertyValue_L = UP + "up";
        propertyValueExpectation_L = "up, down, newEdit, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerDown() {
        propertyValue_L = DOWN + "down";
        propertyValueExpectation_L = "up, newEdit, down, add";
        waitDialog = false;
        setByCustomizerOk(propertyName_L, true);
    }
    
    public void testCustomizerCancel(){
        propertyValue_L = ADD + "cancel";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByCustomizerCancel(propertyName_L, false);
    }
    
    public void testByInPlace(){
        propertyValue_L = "remove, down, up, edit";
        propertyValueExpectation_L = propertyValue_L;
        waitDialog = false;
        setByInPlace(propertyName_L, propertyValue_L, true);
    }
    
    public void verifyCustomizer(){
        verifyCustomizer(propertyName_L);
    }
    
    public void setCustomizerValue() {
        StringArrayCustomEditorOperator customizer = new StringArrayCustomEditorOperator(propertyCustomizer);
        
        if(propertyValue_L.startsWith(ADD)){
            customizer.add(getItem(propertyValue_L,ADD));
        }
        
        if(propertyValue_L.startsWith(REMOVE)){
            customizer.remove(getItem(propertyValue_L,REMOVE));
        }
        
        if(propertyValue_L.startsWith(EDIT)){
            customizer.edit(getItem(propertyValue_L,EDIT), getItem(propertyValue_L,EE));
        }
        
        if(propertyValue_L.startsWith(UP)){
            customizer.up(getItem(propertyValue_L,UP));
        }
        
        if(propertyValue_L.startsWith(DOWN)){
            customizer.down(getItem(propertyValue_L,DOWN));
        }
        
    }
    
    public void verifyPropertyValue(boolean expectation) {
        verifyExpectationValue(propertyName_L,expectation, propertyValueExpectation_L, propertyValue_L, waitDialog);
    }
    
    
    private String getItem(String str, String delim) {
        int first = str.indexOf(delim);
        int end = str.indexOf(EE);

        if(end > 0 && !delim.equals(EE)){
            return str.substring(delim.length(), end);
        } else {
            return str.substring(first + delim.length());
        }
    }
    
    public void verifyCustomizerLayout() {
        StringArrayCustomEditorOperator customizer = new StringArrayCustomEditorOperator(propertyCustomizer);
        customizer.verify();
        customizer.btOK();
        customizer.btCancel();
    }    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new NbTestSuite(PropertyType_StringArray.class));
        junit.textui.TestRunner.run(suite());
    }
    
}
