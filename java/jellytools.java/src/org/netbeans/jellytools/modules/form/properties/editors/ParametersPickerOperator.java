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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Handles Form Connection panel within {@link FormCustomEditorOperator Form Custom Editor}.
 * It contains radio buttons to select source of parameter and appropriate
 * inputs to specify source.
 *
 * <p>
 * Usage:<br>
 * <pre>
 *      PropertySheetOperator pso = new PropertySheetOperator("Properties of doGarbage");
 *      String propertyName = "text";
 *      Property property = new Property(pso, propertyName);
 *      property.openEditor();
 *      FormCustomEditorOperator fceo = new FormCustomEditorOperator(propertyName);
 *      // ParametersPickerOperator
 *      fceo.setMode("Value from existing component");
 *      ParametersPickerOperator paramPicker = new ParametersPickerOperator(propertyName);
 *
 *      // PropertyPickerOperator
 *      paramPicker.property();
 *      paramPicker.selectProperty();
 *      PropertyPickerOperator propertyPicker = new PropertyPickerOperator();
 *      propertyPicker.setComponent("Form");
 *      propertyPicker.setProperty("title");
 *      propertyPicker.ok();
 *
 *      // MethodPickerOperator
 *      paramPicker.methodCall();
 *      paramPicker.selectMethod();
 *      MethodPickerOperator methodPicker = new MethodPickerOperator();
 *      methodPicker.setComponent("Form");
 *      methodPicker.setMethods("getTitle()");
 *      methodPicker.ok();
 *
 *      paramPicker.ok();
 *      fceo.ok();
 * </pre>
 *
 * @see FormCustomEditorOperator
 * @see PropertyPickerOperator
 * @see MethodPickerOperator
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class ParametersPickerOperator extends FormCustomEditorOperator {
    
    /** Components operators. */
    private JLabelOperator _lblGetParameterFrom;
    private JRadioButtonOperator _rbComponent;
    private JComboBoxOperator _cboComponent;
    private JRadioButtonOperator _rbProperty;
    private JTextFieldOperator _txtProperty;
    private JButtonOperator _btSelectProperty;
    private JRadioButtonOperator _rbMethodCall;
    private JTextFieldOperator _txtMethodCall;
    private JButtonOperator _btSelectMethod;
    private JRadioButtonOperator _rbUserCode;
    private JEditorPaneOperator _txtUserCode;
    
    /** Waits for dialog with specified title.
     * @param propertyName name of property used as title of dialog
     */
    public ParametersPickerOperator(String propertyName) {
        super(propertyName);
    }
    
    /** Returns operator of "Get Value From:" label.
     * @return  JLabelOperator instance of "Get Value From:" label
     */
    public JLabelOperator lblGetParameterFrom() {
        if(_lblGetParameterFrom == null) {
            _lblGetParameterFrom = new JLabelOperator(this, 
                            Bundle.getString("org.netbeans.modules.form.Bundle", 
                                             "ConnectionCustomEditor.jLabel1.text"));
        }
        return _lblGetParameterFrom;
    }
    
    /** Returns operator of "Component:" radio button.
     * @return  JRadioButtonOperator instance of "Component:" radio button
     */
    public JRadioButtonOperator rbComponent() {
        if(_rbComponent == null) {
            _rbComponent = new JRadioButtonOperator(this, 
                            Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                                                    "ConnectionCustomEditor.beanRadio.text"));
        }
        return _rbComponent;
    }
    
    /** Returns operator of "Component:" combo box.
     * @return  JComboBoxOperator instance of "Component:" combo box
     */
    public JComboBoxOperator cboComponent() {
        if(_cboComponent == null) {
            _cboComponent = new JComboBoxOperator(this, 0);
        }
        return _cboComponent;
    }
    
    /** Returns operator of "Property:" radio button.
     * @return  JRadioButtonOperator instance of "Property:" radio button
     */
    public JRadioButtonOperator rbProperty() {
        if(_rbProperty == null) {
            _rbProperty = new JRadioButtonOperator(this, 
                            Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                                                    "ConnectionCustomEditor.propertyRadio.text"));
        }
        return _rbProperty;
    }
    
    /** Returns operator of "Property:" text field.
     * @return  JTextFieldOperator instance of "Property:" text field
     */
    public JTextFieldOperator txtProperty() {
        if(_txtProperty == null) {
            _txtProperty = new JTextFieldOperator(this, 0);
        }
        return _txtProperty;
    }
    
    /** Returns operator of "..." button for "Property:" field.
     * @return  JButtonOperator instance of "..." button
     */
    public JButtonOperator btSelectProperty() {
        if(_btSelectProperty == null) {
            _btSelectProperty = new JButtonOperator(this, "...", 0); // NOI18N
        }
        return _btSelectProperty;
    }
    
    /** Returns operator of "Method Call:" radio button.
     * @return  JRadioButtonOperator instance of "Method Call:" radio button
     */
    public JRadioButtonOperator rbMethodCall() {
        if(_rbMethodCall == null) {
            _rbMethodCall = new JRadioButtonOperator(this, 
                            Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                                                    "ConnectionCustomEditor.methodRadio.text"));
        }
        return _rbMethodCall;
    }
    
    /** Returns operator of "Method Call:" text field.
     * @return  JTextFieldOperator instance of "Method Call:" text field
     */
    public JTextFieldOperator txtMethodCall() {
        if(_txtMethodCall==null) {
            _txtMethodCall = new JTextFieldOperator(this, 1);
        }
        return _txtMethodCall;
    }
    
    /** Returns operator of "..." button for "Method Call:" field.
     * @return  JButtonOperator instance of "..." button
     */
    public JButtonOperator btSelectMethod() {
        if(_btSelectMethod == null) {
            _btSelectMethod = new JButtonOperator(this, "...", 1); // NOI18N
        }
        return _btSelectMethod;
    }
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** Pushes "Component:" radio button. */
    public void component() {
        rbComponent().push();
    }
    
    /** Selects specified item from "Component:" combo box.
     * @param item item to be selected
     */
    public void setComponent(String item) {
        cboComponent().setSelectedItem(item);
    }
    
    /** Pushes "Property:" radio button. */
    public void property() {
        rbProperty().push();
    }
    
    /** Clicks on ... JButton in Property field. It invokes "Select Property"
     * dialog. Use {@link PropertyPickerOperator} to test the dialog. */
    public void selectProperty() {
        btSelectProperty().pushNoBlock();
    }
    
    /** Pushes "Method Call:" radio button. */
    public void methodCall() {
        rbMethodCall().push();
    }
    
    /** Clicks on ... JButton in Method Call field. It invokes "Select Method"
     * dialog. Use {@link MethodPickerOperator} to test the dialog. */
    public void selectMethod() {
        btSelectMethod().pushNoBlock();
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        lblGetParameterFrom();
        txtMethodCall();
        txtProperty();
        rbComponent();
        rbMethodCall();
        rbProperty();
        btSelectMethod();
        btSelectProperty();
        cboComponent();
        super.verify();
    }

}
