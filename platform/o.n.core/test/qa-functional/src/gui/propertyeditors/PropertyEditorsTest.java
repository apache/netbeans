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

import java.io.PrintStream;
import java.io.PrintWriter;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.Operator;


/**
 * JellyTestCase test case with implemented Property Editors Test support stuff
 *
 * @author  mmirilovic@netbeans.org
 */
public abstract class PropertyEditorsTest extends JellyTestCase {
    
    protected static PrintStream err;
    protected static PrintStream log;
    
    public String propertyInitialValue;
    public String propertyValue;
    
    protected static JTableOperator tableOperator;

    protected static NbDialogOperator propertyCustomizer;
    
    protected static NbDialogOperator propertiesWindow = null;
    
    private static final String CAPTION = "\n===========================";
    
    /** Creates a new instance of PropertyEditorsTest */
    public PropertyEditorsTest(String testName) {
        super(testName);
    }
    
    
    public void setUp() {
        //err = System.out;
        err = getLog();
        log = getRef();
        
        try {
            JemmyProperties.getProperties().setOutput(new TestOut(null, new PrintWriter(err, true), new PrintWriter(err, true), null));
            initializeWorkplace();
        }catch(Exception exc) {
            failTest(exc, "SetUp failed. It seems like initializeWorkplace cause exception:"+exc.getMessage());
        }
    }
    
    /** Open Property Customizer for <b>propertyName</b>, set value by customizer and press Ok button, verify value with <b>expectance</b>.
     * @param propertyName name of property to be customized
     * @param expectance true- new value must be the same as expected value, false-value needn't be the same as expected
     */
    public void setByCustomizerOk(String propertyName, boolean expectance){
        try {
            err.println(CAPTION + " Trying to set value by customizer-ok {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            
            openAndGetPropertyCustomizer(propertyName);
            setCustomizerValue();
            
            if(propertyCustomizer.isShowing())
                propertyCustomizer.ok();
            
            err.println(CAPTION + " Trying to set value by customizer-ok {name="+propertyName+" / value="+propertyValue+"} - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCustomizer("+propertyName+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    /** Open Property Customizer for <b>propertyName</b>, set value by customizer and press Cancel button, verify value with <b>expectance</b>.
     * @param propertyName name of property to be customized
     * @param expectance true- new value must be the same as expected value, false-value needn't be the same as expected
     */
    public void setByCustomizerCancel(String propertyName, boolean expectance) {
        try {
            err.println(CAPTION + " Trying to set value by customizer-cancel {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            openAndGetPropertyCustomizer(propertyName);
            setCustomizerValue();
            
            if(propertyCustomizer.isShowing())
                propertyCustomizer.cancel();
            
            err.println(CAPTION + " Trying to set value by customizer-cancel {name="+propertyName+" / value="+propertyValue+"} - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCustomizerCancel("+propertyName+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    /** Set value <b>propertyValue</b> of property <b>propertyName</b> by in-place, verify value with <b>expectance</b>.
     * @param propertyName name of property to be changed
     * @param propertyValue new value of property
     * @param expectance true- new value must be the same as expected value, false-value needn't be the same as expected
     */
    public void setByInPlace(String propertyName, String propertyValue, boolean expectance) {
        try {
            err.println(CAPTION + " Trying to set value by in-place {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            
//            ((TextFieldProperty) findProperty(propertyName, "TextFieldProperty")).setValue(propertyValue);
            new PropertySheetOperator(propertiesWindow).tblSheet().changeCellObject(findProperty(propertyName, propertiesWindow).getRow(),1, propertyValue);
            
            err.println(CAPTION + " Trying to set value by in-place {name="+propertyName+" / value="+propertyValue+"}  - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByInPlace("+propertyName+", "+propertyValue+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    /** Set value <b>propertyValue</b> of property <b>propertyName</b> by combobox, verify value with <b>expectance</b>.
     * @param propertyName name of property to be changed
     * @param propertyValue new value of property
     * @param expectance true- new value must be the same as expected value, false-value needn't be the same as expected
     */
    public void setByCombo(String propertyName, String propertyValue, boolean expectance) {
        try {
            err.println(CAPTION + " Trying to set value by combo box {name="+propertyName+" / value="+propertyValue+"} .");
            propertyInitialValue = getValue(propertyName);
            
            findProperty(propertyName, propertiesWindow).setValue(propertyValue);
            
            err.println(CAPTION + " Trying to set value by combo box {name="+propertyName+" / value="+propertyValue+"}  - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCombo("+propertyName+", "+propertyValue+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    /** Set indexed value <b>propertyValueIndex</b> of property <b>propertyName</b> by combobox, verify value with <b>expectance</b>.
     * @param propertyName name of property to be changed
     * @param propertyValueIndex index of new value in combobox
     * @param expectance true- new value must be the same as expected value, false-value needn't be the same as expected
     */
    public void setByCombo(String propertyName, int propertyValueIndex, boolean expectance) {
        try {
            err.println(CAPTION + " Trying to set value by combo box {name="+propertyName+" / value="+propertyValueIndex+"} .");
            propertyInitialValue = getValue(propertyName);
            
            findProperty(propertyName, propertiesWindow).setValue(propertyValueIndex);
            
            err.println(CAPTION + " Trying to set value by combo box {name="+propertyName+" / value="+propertyValueIndex+"}  - finished.");
            verifyPropertyValue(expectance);
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: setByCombo("+propertyName+", "+propertyValueIndex+", "+expectance+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    /** Verify customizer layout for property <b>propertyName</b>.
     * @param propertyName name of property to be changed
     */
    public void verifyCustomizer(String propertyName){
        try {
            err.println(CAPTION + " Trying to verify customizer {name="+propertyName+"} .");
            openAndGetPropertyCustomizer(propertyName);
            verifyCustomizerLayout();
            
            if(propertyCustomizer.isShowing())
                propertyCustomizer.cancel();
            
            err.println(CAPTION + " Trying to verify customizer {name="+propertyName+"}  - finished.");
            
        }catch(Exception exc) {
            failTest(exc, "EXCEPTION: Verification of Property Customizer Layout for property("+propertyName+") failed and cause exception:"+exc.getMessage());
        }
    }
    
    /** Open property customizer for property <b>propertyName</b>.
     * @param propertyName name of property to be changed
     * @return Property Customizer
     */
    public static NbDialogOperator openAndGetPropertyCustomizer(String propertyName) {
        // hack for troubles with request focus on already focused property
        new PropertySheetOperator(propertiesWindow).tblSheet().selectCell(0,0);
        
        findProperty(propertyName, propertiesWindow).openEditor();
        propertyCustomizer = findPropertyCustomizer(propertyName);
        return propertyCustomizer;
    }
    
    /** Return Property Customizer.
     * @return Property Customizer.
     */
    public NbDialogOperator getPropertyCustomizer() {
        return propertyCustomizer;
    }
    
    /** Return Informational dialog
     * @return Informational dialog
     */
    public NbDialogOperator getInformationDialog() {
        String title = org.netbeans.jellytools.Bundle.getString("org.openide.Bundle", "NTF_InformationTitle");
        
        err.println(CAPTION + " Waiting dialog {"+title+"} .");
        NbDialogOperator dialog = new NbDialogOperator(title);
        err.println(CAPTION + " Waiting dialog {"+title+"} - finished.");
        return dialog;
    }
    
    /** Get value of property <b>propertyName</b>
     * @param propertyName name of property asked for value
     * @return value of property
     */
    public String getValue(String propertyName) {
        String returnValue = findProperty(propertyName, propertiesWindow).getValue();
        err.println("GET VALUE = [" + returnValue + "].");
        return returnValue;
    }
    
    /** Find Property Cusotmizer by name of property <b>propertyName</b>
     * @param propertyName name of property
     * @return founded Property Customizer
     */
    private static NbDialogOperator findPropertyCustomizer(String propertyName){
        return new NbDialogOperator(propertyName);
    }
    
    /** Verify exceptation value.
     * @param propertyName name of property
     * @param expectation true - expected value must be the same as new value, false - expected value should not be the same
     * @param propertyValueExpectation expected value
     * @param propertyValue new value
     * @param waitDialog true - after changing value Informational dialog about impissibility to set invalid value arise
     */
    public void verifyExpectationValue(String propertyName, boolean expectation, String propertyValueExpectation, String propertyValue, boolean waitDialog){
        
        // Dialog isn't used for informing user about Invalid new value: Class,
        if(waitDialog) {
            getInformationDialog().ok();
            err.println(CAPTION + " Dialog closed by [Ok].");
            
            if(propertyCustomizer!=null && propertyCustomizer.isShowing()){
                err.println(CAPTION + " Property Customizer is still showing.");
                propertyCustomizer.cancel();
                err.println(CAPTION + " Property Customizer closed by [Cancel].");
            }
            
        }
        
        String newValue = getValue(propertyName);
        String log = "Actual value is {"+newValue+"} and initial is{"+propertyInitialValue+"} - set value is {"+propertyValue+"} / expectation value is {"+propertyValueExpectation+"}";
        
        err.println(CAPTION + " Trying to verify value ["+log+"].");
        
        if(expectation){
            if(newValue.equals(propertyValueExpectation) ) {
                log(log + " --> PASS");
            }else {
                fail(log + " --> FAIL");
            }
        }else {
            if(newValue.equals(propertyInitialValue)){
                log(log + " --> PASS");
            }else{
                fail(log + " --> FAIL");
            }
            
        }
    }
    
    
    /** Reinitialize Workplace. */
    public static NbDialogOperator reInitializeWorkplace() {
        propertiesWindow = null;
        return openPropertySheet();
    }
    
    /** Initialize Workplace. */
    public static NbDialogOperator initializeWorkplace() {
        return openPropertySheet();
    }
    
    /** Open property sheet (bean customizer). */
    private static NbDialogOperator openPropertySheet() {
        String waitDialogTimeout = "DialogWaiter.WaitDialogTimeout";
        long findTimeout = JemmyProperties.getCurrentTimeout(waitDialogTimeout);
        JemmyProperties.setCurrentTimeout(waitDialogTimeout, 3000);
        
        try{
            propertiesWindow = new NbDialogOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties", new Object[]{new Integer(1),"TestNode"}));
        }catch(org.netbeans.jemmy.TimeoutExpiredException exception){
            new PropertiesTest();
            propertiesWindow = new NbDialogOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties", new Object[]{new Integer(1),"TestNode"}));
        }
        
        JemmyProperties.setCurrentTimeout(waitDialogTimeout, findTimeout);
        
        return propertiesWindow;
    }
    
    
    /** Find Property in Property Sheet and return them.
     * This is first hack for new Jelly2, because it isn't possible to set String Comparator only for one operator.
     * @param propertyName name of property
     * @param type  TextFieldProperty - textfield property, ComboBoxProperty - combobox property
     * @return property by <b>propertyName</b> and <b>type</b>.
     */
    protected static Property findProperty(String propertyName, NbDialogOperator propertiesWindow) {
        PropertySheetOperator propertySheet = new PropertySheetOperator(propertiesWindow);
        Property property = new Property(propertySheet, propertyName);
        
        // property.openEditor(); - doesn't work - custom editor is opened without Users Event
        // hack for invoking Custom Editor by pushing shortcut CTRL+SPACE
        tableOperator = propertySheet.tblSheet();
        // Need to request focus before selection because invokeCustomEditor action works
        // only when table is focused
        tableOperator.makeComponentVisible();
        tableOperator.requestFocus();
        tableOperator.waitHasFocus();
        // need to select property first
        ((javax.swing.JTable)tableOperator.getSource()).changeSelection(property.getRow(), 0, false, false);
//        return new Property(new PropertySheetOperator(propertiesWindow), propertyName);
        return property;
    }
    
    public void tearDown() {
        closeAllModal();
    }
    
    /** Print full stack trace to log files, get message and log to test results if test fails.
     * @param exc Exception logged to description
     * @param message written to test results
     */
    protected static void failTest(Exception exc, String message) {
        err.println("################################");
        exc.printStackTrace(err);
        err.println("################################");
        fail(message);
    }
    
    /** Make IDE screenshot of whole IDE
     * @param testCase it is needed for locate destination directory of saving screenshot file
     */
    public static void makeIDEScreenshot(JellyTestCase testCase) {
        try{
            testCase.getWorkDir();
            org.netbeans.jemmy.util.PNGEncoder.captureScreen(testCase.getWorkDirPath()+System.getProperty("file.separator")+"IDEscreenshot.png");
        }catch(Exception ioexc){
            testCase.log("Impossible make IDE screenshot!!! \n" + ioexc.toString());
        }
    }
    
    public abstract void setCustomizerValue();
    
    public abstract void verifyCustomizerLayout();
    
    public abstract void verifyPropertyValue(boolean expectation);
    
}
