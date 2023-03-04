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

/*
 * EnterAbbreviation.java
 *
 * Created on 8/28/02 11:08 AM
 */
package org.netbeans.jellytools.modules.editor;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Enter Abbreviation" NbDialog.
 *
 * @author jl105142
 * @version 1.0
 */
public class EnterAbbreviation extends JDialogOperator {

    /** Creates new EnterAbbreviation that can handle it.
     */
    public EnterAbbreviation() {
        super(java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("AEP_EnterAbbrev"));
    }
    
    private JLabelOperator _lblAbbreviation;
    private JTextFieldOperator _txtAbbreviation;
    private JLabelOperator _lblExpansion;
    private JTextAreaOperator _txtExpansion;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Abbreviation:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblAbbreviation() {
        if (_lblAbbreviation==null) {
            _lblAbbreviation = new JLabelOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("AIP_Abbrev"));
        }
        return _lblAbbreviation;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtAbbreviation() {
        if (_txtAbbreviation==null) {
            _txtAbbreviation = new JTextFieldOperator(this);
        }
        return _txtAbbreviation;
    }
    
    /** Tries to find "Expansion:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblExpansion() {
        if (_lblExpansion==null) {
            _lblExpansion = new JLabelOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("AIP_Expand"));
        }
        return _lblExpansion;
    }
    
    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtExpansion() {
        if (_txtExpansion==null) {
            _txtExpansion = new JTextAreaOperator(this);
        }
        return _txtExpansion;
    }
    
    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("KBEP_OK_LABEL"));
        }
        return _btOK;
    }
    
    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.openide.explorer.propertysheet.Bundle").getString("CTL_Cancel"));
        }
        return _btCancel;
    }
    
    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.openide.explorer.propertysheet.Bundle").getString("CTL_Help"));
        }
        return _btHelp;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** gets text for txtAbbreviation
     * @return String text
     */
    public String getAbbreviation() {
        return txtAbbreviation().getText();
    }
    
    /** sets text for txtAbbreviation
     * @param text String text
     */
    public void setAbbreviation(String text) {
        txtAbbreviation().setText(text);
    }
    
    /** types text for txtAbbreviation
     * @param text String text
     */
    public void typeAbbreviation(String text) {
        txtAbbreviation().typeText(text);
    }
    
    /** gets text for txtExpansion
     * @return String text
     */
    public String getExpansion() {
        return txtExpansion().getText();
    }
    
    /** sets text for txtExpansion
     * @param text String text
     */
    public void setExpansion(String text) {
        txtExpansion().setText(text);
    }
    
    /** types text for txtExpansion
     * @param text String text
     */
    public void typeExpansion(String text) {
        txtExpansion().typeText(text);
    }
    
    /** clicks on "OK" JButton
     */
    public void oK() {
        btOK().push();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }
    
    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }
    
    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of EnterAbbreviation by accessing all its components.
     */
    public void verify() {
        lblAbbreviation();
        txtAbbreviation();
        lblExpansion();
        txtExpansion();
        btOK();
        btCancel();
        btHelp();
    }
    
    public void fillAbbreviation(String abbreviation, String expansion) {
        if (!"".equals(txtAbbreviation().getText()))
            txtAbbreviation().clearText();
        typeAbbreviation(abbreviation);
        
        if (!"".equals(txtExpansion().getText()))
            txtExpansion().clearText();
        typeExpansion(expansion);
    }
    
    /** Performs simple test of EnterAbbreviation
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new EnterAbbreviation().verify();
        System.out.println("EnterAbbreviation verification finished.");
    }
}

