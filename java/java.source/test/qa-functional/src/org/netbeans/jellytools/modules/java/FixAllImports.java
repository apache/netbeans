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
package org.netbeans.jellytools.modules.java;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JDialog;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Fix All Imports" NbDialog.
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 * @version 1.0
 */
public class FixAllImports extends JDialogOperator {

    /** Creates new FixAllImports that can handle it.
     */
    public FixAllImports() {
        super(waitFixImportDialog());
    }
    
    private static JDialog waitFixImportDialog() {
        JDialogOperator candidate = new JDialogOperator(TITLE);        
        return (JDialog)candidate.getSource();
    }
    
    public static final String TITLE= "Fix All Imports";
    private JLabelOperator _lblHtmlSelectTheFullyQualifiedNameToUseInTheImportStatementHtml;
    private JComboBoxOperator _cbo;
    public static final String ITEM_COMSUNXMLINTERNALBINDV2SCHEMAGENXMLSCHEMALIST = "com.sun.xml.internal.bind.v2.schemagen.xmlschema.List";
    public static final String ITEM_JAVAAWTLIST = "java.awt.List";
    public static final String ITEM_JAVAUTILLIST = "java.util.List";
    private JLabelOperator _lblVector;
    private JComboBoxOperator _cboVector;
    public static final String ITEM_JAVAUTILVECTOR = "java.util.Vector";
    private JButtonOperator _btMetalScrollButton;
    private JButtonOperator _btMetalScrollButton2;
    private JButtonOperator _btMetalScrollButton3;
    private JButtonOperator _btMetalScrollButton4;
    private JCheckBoxOperator _cbRemoveUnusedImports;
    private JLabelOperator _lblImportStatements;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JLabelOperator _lblNothingToFix;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "<html>Select the fully qualified name to use in the import statement.</html>" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblHtmlSelectTheFullyQualifiedNameToUseInTheImportStatementHtml() {
        if (_lblHtmlSelectTheFullyQualifiedNameToUseInTheImportStatementHtml==null) {
            _lblHtmlSelectTheFullyQualifiedNameToUseInTheImportStatementHtml = new JLabelOperator(this, "<html>Select the fully qualified name to use in the import statement.</html>");
        }
        return _lblHtmlSelectTheFullyQualifiedNameToUseInTheImportStatementHtml;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    
    
    private Map<Integer,JComboBoxOperator> operators = new HashMap<Integer, JComboBoxOperator>();
    
    public JComboBoxOperator cbo(int index) {
        Integer i = new Integer(index);
        JComboBoxOperator _cbo = operators.get(i);
        if (_cbo==null) {
            _cbo = new JComboBoxOperator(this,index);
            operators.put(i, _cbo);
        }
        return _cbo;
    }

    public JComboBoxOperator get(int key) {
        return operators.get(key);
    }
    
    private Map<Integer,JLabelOperator> labelOperators = new HashMap<Integer, JLabelOperator>();
    
    public JLabelOperator lo(int index) {
        Integer i = new Integer(index);
        JLabelOperator _lo = labelOperators.get(i);
        if (_lo==null) {
            _lo = new JLabelOperator(this,index+2);
            labelOperators.put(i, _lo);
        }
        return _lo;
    }

    public JLabelOperator getLo(int key) {
        return labelOperators.get(key);
    }
                                
    /** Tries to find "Remove unused imports" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbRemoveUnusedImports() {
        if (_cbRemoveUnusedImports==null) {
            _cbRemoveUnusedImports = new JCheckBoxOperator(this, "Remove unused imports");
        }
        return _cbRemoveUnusedImports;
    }

    /** Tries to find "Import Statements:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblImportStatements() {
        if (_lblImportStatements==null) {
            _lblImportStatements = new JLabelOperator(this, "Import Statements:");
        }
        return _lblImportStatements;
    }
    
    public JLabelOperator lblNothingToFix() {
        if (_lblNothingToFix==null) {
            _lblNothingToFix = new JLabelOperator(this, "<nothing to fix>");
        }
        return _lblNothingToFix;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK");
        }
        return _btOK;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** returns selected item for cboList
     * @return String item
     */
    public String getSelectedList(int index) {
        return cbo(index).getSelectedItem().toString();
    }      

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkRemoveUnusedImports(boolean state) {
        if (cbRemoveUnusedImports().isSelected()!=state) {
            cbRemoveUnusedImports().push();
        }
    }

    /** clicks on "OK" JButton
     */
    public void ok() {
        btOK().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of FixAllImports by accessing all its components.
     */
    public void verify() {
        lblHtmlSelectTheFullyQualifiedNameToUseInTheImportStatementHtml();
        cbRemoveUnusedImports();
        lblImportStatements();
        btOK();
        btCancel();
    }

    /** Performs simple test of FixAllImports
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new FixAllImports().verify();
        System.out.println("FixAllImports verification finished.");
    }
}

