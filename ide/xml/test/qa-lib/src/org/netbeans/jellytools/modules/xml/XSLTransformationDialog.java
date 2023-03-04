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
package org.netbeans.jellytools.modules.xml;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "XSL Transformation" NbDialog.
 *
 * @author ms113234
 * @version 1.0
 */
public class XSLTransformationDialog extends JDialogOperator {

    /** Creates new XSLTransformationDialog that can handle it.
     * @throws TimeoutExpiredException when NbDialog not found
     */
    public XSLTransformationDialog() {
        super( "XSL Transformation" );
    }

    private JCheckBoxOperator _cbOverwriteOutput;
    private JLabelOperator _lblOutput;
    private JButtonOperator _btBrowseSource;
    private JComboBoxOperator _cboXMLSource;
    private JLabelOperator _lblXSLTScript;
    private JComboBoxOperator _cboJComboBox;
    public static final String ITEM_DONOTHING = "Do Nothing";
    public static final String ITEM_APPLYDEFAULTACTION = "Apply Default Action";
    public static final String ITEM_OPENINBROWSER = "Open in Browser";
    private JButtonOperator _btBrowseScript;
    private JButtonOperator _btCancel;
    private JComboBoxOperator _cboXSLTScript;
    private JButtonOperator _btHelp;
    private JButtonOperator _btOK;
    private JComboBoxOperator _cboOutput;
    public static final String ITEM_BOOKSHTML = "Books.html";
    public static final String ITEM_PREVIEWOPENINBROWSER = "Preview (open in browser)";
    private JLabelOperator _lblProcessOutput;
    private JLabelOperator _lblXMLSource;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Overwrite Output" JCheckBox in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbOverwriteOutput() {
        if (_cbOverwriteOutput==null) {
            _cbOverwriteOutput = new JCheckBoxOperator( this, "Overwrite Output", 0 );
        }
        return _cbOverwriteOutput;
    }
    
    /** Tries to find "Output:" JLabel in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JLabelOperator
     */
    public JLabelOperator lblOutput() {
        if (_lblOutput==null) {
            _lblOutput = new JLabelOperator( this, "Output:", 0 );
        }
        return _lblOutput;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseSource() {
        if (_btBrowseSource==null) {
            _btBrowseSource = new JButtonOperator( this, "Browse...", 0 );
        }
        return _btBrowseSource;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboXMLSource() {
        if (_cboXMLSource==null) {
            _cboXMLSource = new JComboBoxOperator(this, 0);
        }
        return _cboXMLSource;
    }
    
    /** Tries to find "XSLT Script:" JLabel in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JLabelOperator
     */
    public JLabelOperator lblXSLTScript() {
        if (_lblXSLTScript==null) {
            _lblXSLTScript = new JLabelOperator( this, "XSLT Script:", 0 );
        }
        return _lblXSLTScript;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJComboBox() {
        if (_cboJComboBox==null) {
            _cboJComboBox = new JComboBoxOperator(this, 3);
        }
        return _cboJComboBox;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseScript() {
        if (_btBrowseScript==null) {
            _btBrowseScript = new JButtonOperator( this, "Browse...", 1 );
        }
        return _btBrowseScript;
    }
    
    /** Tries to find "Cancel" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator( this, "Cancel", 0 );
        }
        return _btCancel;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboXSLTScript() {
        if (_cboXSLTScript==null) {
            _cboXSLTScript = new JComboBoxOperator(this, 1);
        }
        return _cboXSLTScript;
    }
    
    /** Tries to find "Help" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator( this, "Help", 0 );
        }
        return _btHelp;
    }
    
    /** Tries to find "OK" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator( this, "OK", 0 );
        }
        return _btOK;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboOutput() {
        if (_cboOutput==null) {
            _cboOutput = new JComboBoxOperator(this, 2);
        }
        return _cboOutput;
    }
    
    /** Tries to find "Process Output:" JLabel in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JLabelOperator
     */
    public JLabelOperator lblProcessOutput() {
        if (_lblProcessOutput==null) {
            _lblProcessOutput = new JLabelOperator( this, "Process Output:", 0 );
        }
        return _lblProcessOutput;
    }
    
    /** Tries to find "XML Source:" JLabel in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JLabelOperator
     */
    public JLabelOperator lblXMLSource() {
        if (_lblXMLSource==null) {
            _lblXMLSource = new JLabelOperator( this, "XML Source:", 0 );
        }
        return _lblXMLSource;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkOverwriteOutput( boolean state ) {
        if (cbOverwriteOutput().isSelected()!=state) {
            cbOverwriteOutput().push();
        }
    }
    
    /** clicks on "Browse..." JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void browseSource() {
        btBrowseSource().push();
    }
    
    /** tries to find cboXMLSource and select item
     * @param item String item
     */
    public void setXMLSource( String item ) {
        cboXMLSource().selectItem(item, true, true);
    }
    
    /** tries to find cboJComboBox and select item
     * @param item String item
     */
    public void setJComboBox( String item ) {
        cboJComboBox().selectItem(item, true, true);
    }
    
    /** clicks on "Browse..." JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void browseScript() {
        btBrowseScript().push();
    }
    
    /** clicks on "Cancel" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void cancel() {
        btCancel().push();
    }
    
    /** tries to find cboXSLTScript and select item
     * @param item String item
     */
    public void setXSLTScript( String item ) {
        cboXSLTScript().selectItem(item, true, true);
    }
    
    /** clicks on "Help" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void help() {
        btHelp().push();
    }
    
    /** clicks on "OK" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void oK() {
        btOK().push();
    }
    
    /** tries to find cboOutput and select item
     * @param item String item
     */
    public void setOutput( String item ) {
        cboOutput().selectItem(item, true, true);
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of XSLTransformationDialog by accessing all its components.
     * @throws TimeoutExpiredException when any component not found
     */
    public void verify() {
        cbOverwriteOutput();
        lblOutput();
        btBrowseSource();
        cboXMLSource();
        lblXSLTScript();
        cboJComboBox();
        btBrowseScript();
        btCancel();
        cboXSLTScript();
        btHelp();
        btOK();
        cboOutput();
        lblProcessOutput();
        lblXMLSource();
    }
    
    /** Performs simple test of XSLTransformationDialog
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new XSLTransformationDialog().verify();
        System.out.println("XSLTransformationDialog verification finished.");
    }
}
