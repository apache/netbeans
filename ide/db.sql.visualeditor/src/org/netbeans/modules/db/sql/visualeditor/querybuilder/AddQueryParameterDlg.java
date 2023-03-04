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
package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx ;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;

/**
 *
 * @author  jhoff, Sanjay Dhamankar
 */
public class AddQueryParameterDlg extends JPanel {

    // Variables

    /** A return status code - returned if Cancel button has been pressed */
    public static final int     RET_CANCEL = 0;

    /** A return status code - returned if OK button has been pressed */
    public static final int     RET_OK = 1;

    private String              _columnName = "<Column Name>"; // NOI18N

    private Dialog              dialog;
    private final int WIDTH = 400;
    private final int HEIGHT = 280;


    // Constructors

    /**
     * Default constructor
     */
    public AddQueryParameterDlg() {
        this(true, "<Column Name>");           // NOI18N
    }

//     /** Creates new form AddQueryParameterDlg */
//     public AddQueryParameterDlg(boolean modal) {
//      this(modal, "<Column Name>");
//     }

    /** Creates new form AddQueryParameterDlg */
    public AddQueryParameterDlg(boolean modal, String columnName) {

        initComponents();
        // try to make it so that the default information in the field is pre-selected so that the user
        // can simply type over it without having to select it.
        valueTxtField.setSelectionEnd(valueTxtField.getText().length());
        parmTxtField.setSelectionStart(0);
        parmTxtField.setSelectionEnd(parmTxtField.getText().length());

        ActionListener listener = new ActionListener () {

                public void actionPerformed (ActionEvent evt) {
                    Object o = evt.getSource();
                    if (o == NotifyDescriptor.CANCEL_OPTION) {
                        returnStatus = RET_CANCEL;
                    } else if (o == NotifyDescriptor.OK_OPTION) {
                        // do something useful
                        returnStatus = RET_OK;
                    } // else if HELP ...
                }
            };

        // Note - we may want to use the version that also has help (check with Jeff)
        DialogDescriptor dlg =
            new DialogDescriptor(this,
                    NbBundle.getMessage(AddQueryParameterDlg.class,
                                 "ADD_QUERY_CRITERIA_TITLE"),     // NOI18N
                                 modal, listener);

        dlg.setHelpCtx (
            new HelpCtx( "projrave_ui_elements_dialogs_add_query_criteria" ) );        // NOI18N

        setColumnName(columnName);
        dialog = DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setVisible(true);
    }

    public void setColumnName (String columnName) {
        _columnName = columnName;
        dispColumnNameLbl.setText(_columnName);
    }

    public String getCriteria () {

        StringBuffer criteriaStringBuffer = new StringBuffer ();

        String comboBoxString = (String) comparisonComboBox.getSelectedItem();

        if (comboBoxString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "EQUALS") ) )  {       // NOI18N
            criteriaStringBuffer.append ("="); // NOI18N
        }
        else if (comboBoxString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "LESS_THAN") ) )  {       // NOI18N
            criteriaStringBuffer.append ("<"); // NOI18N
        }
        else if (comboBoxString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "LESS_THAN_EQUALS") ) )  {       // NOI18N
            criteriaStringBuffer.append ("<="); // NOI18N
        }
        else if (comboBoxString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "GREATER_THAN") ) )  {       // NOI18N
            criteriaStringBuffer.append (">"); // NOI18N
        }
        else if (comboBoxString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "GREATER_THAN_EQUALS") ) )  {       // NOI18N
            criteriaStringBuffer.append (">="); // NOI18N
        }
        else if (comboBoxString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "NOT_EQUALS") ) )  {       // NOI18N
            criteriaStringBuffer.append ("<>"); // NOI18N
        }
        else if (comboBoxString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "LIKE") ) )  {       // NOI18N
            criteriaStringBuffer.append ("LIKE"); // NOI18N
        }
        else if (comboBoxString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "IN") ) )  {       // NOI18N
            criteriaStringBuffer.append ("IN"); // NOI18N
        }
        // Get operator plus trailing blank
        criteriaStringBuffer.append (" "); // NOI18N

        // Append criterion value or parameter
        if ( parmRadioBtn.isSelected() ) {
            criteriaStringBuffer.append(parmTxtField.getText());
        } else if ( valueRadioBtn.isSelected() )
            criteriaStringBuffer.append(valueTxtField.getText());

        /* Ask Jeff how to handle this ?
           criteriaStringBuffer.append ((String) defaultValTxtField.getText());
        */

        return ( criteriaStringBuffer.toString() );
    }
    
    
    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }
    
    private void updateLabel ( String comparisonString ) {
        /*
        //    "= Equals", 
        if (comparisonString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "EQUALS") ) ) {     // NOI18N
            columnNameHintLbl.setText (
                NbBundle.getMessage(AddQueryParameterDlg.class, 
                    "HINT_EQUALS") );       // NOI18N
        }
        //    "< Less Than", 
        else if (comparisonString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "LESS_THAN") ) )  {     // NOI18N
            columnNameHintLbl.setText (
                NbBundle.getMessage(AddQueryParameterDlg.class, 
                    "HINT_LESS_THAN") );       // NOI18N
        }
        //    "<= Less Than Equals", 
        else if (comparisonString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "LESS_THAN_EQUALS") ) ) {       // NOI18N
            columnNameHintLbl.setText (
                NbBundle.getMessage(AddQueryParameterDlg.class, 
                    "HINT_LESS_THAN_EQUALS") );       // NOI18N
        }
        //    "> Greater Than",
        else if (comparisonString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "GREATER_THAN") ) ) {       // NOI18N
            columnNameHintLbl.setText (
                NbBundle.getMessage(AddQueryParameterDlg.class, 
                    "HINT_GREATER_THAN") );       // NOI18N
        }
        //    ">= Greater Than Equals" 
        else if (comparisonString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "GREATER_THAN_EQUALS") ) ) {        // NOI18N
            columnNameHintLbl.setText (
                NbBundle.getMessage(AddQueryParameterDlg.class, 
                    "HINT_GREATER_THAN_EQUALS") );       // NOI18N
        }
        //    "<> Not Equals" 
        else if (comparisonString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "NOT_EQUALS") ) )  {       // NOI18N
            columnNameHintLbl.setText (
                NbBundle.getMessage(AddQueryParameterDlg.class, 
                    "HINT_NOT_EQUALS") );       // NOI18N
        }
        //    "LIKE" 
        else 
            */
            
        if (comparisonString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "LIKE") ) )  {       // NOI18N
            comparisonHintLbl.setText (
                NbBundle.getMessage(AddQueryParameterDlg.class, 
                    "HINT_LIKE") );       // NOI18N
        }
        //    "IN" 
        else if (comparisonString.equals (NbBundle.getMessage(AddQueryParameterDlg.class, "IN") ) )  {       // NOI18N
            comparisonHintLbl.setText (
                NbBundle.getMessage(AddQueryParameterDlg.class, 
                    "HINT_IN") );       // NOI18N
        }
        else {
            comparisonHintLbl.setText ( "        " );  // NOI18N
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new ButtonGroup();
        mainPanel = new JPanel();
        columnNameLbl = new JLabel();
        dispColumnNameLbl = new JLabel();
        comparisonLbl = new JLabel();
        comparisonHintLbl = new JLabel();
        comparisonComboBox = new JComboBox();
        radioButtonPanel = new JPanel();
        valueRadioBtn = new JRadioButton();
        parmRadioBtn = new JRadioButton();
        valueTxtField = new JTextField();
        valueTxtFieldLbl = new JLabel();
        valueTxtFieldLbl.setLabelFor(valueTxtField);
        parmTxtField = new JTextField();
        parmTxtFieldLbl = new JLabel();
        parmTxtFieldLbl.setLabelFor(parmTxtFieldLbl);
        
// Default are disabled until we restore parameterized execution, which requires ParameterMetaData
//         defaultValTxtField = new JTextField();
//         defaultValueLbl = new JLabel();
        fillerLbl = new JLabel();
       
        setLayout(new java.awt.GridBagLayout());

        mainPanel.setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AddQueryParameterDlg.class, "ADD_QUERY_CRITERIA_a11yName"));
        getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AddQueryParameterDlg.class, "ADD_QUERY_CRITERIA_a11yDescription"));

        // do we need to make it a11y compliant ?
        instructions = new JTextArea(
            NbBundle.getMessage(AddQueryParameterDlg.class, "QUERY_INSTRUCTIONS") );        // NOI18N
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setEditable(false);
        instructions.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        instructions.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
//      gridBagConstraints.gridx = 0;
//      gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        mainPanel.add(instructions, gridBagConstraints);

        columnNameLbl.setText (
            NbBundle.getMessage(AddQueryParameterDlg.class, "COLUMN_NAME") );       // NOI18N
        columnNameLbl.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AddQueryParameterDlg.class, "COLUMN_NAME_a11yDescription"));
        columnNameLbl.setDisplayedMnemonic(NbBundle.getMessage(AddQueryParameterDlg.class, "COLUMN_NAME_Mnemonic").charAt(0));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        mainPanel.add(columnNameLbl, gridBagConstraints);

//        dispColumnNameLbl.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        // No a11y ?
        dispColumnNameLbl.setText(_columnName);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        mainPanel.add(dispColumnNameLbl, gridBagConstraints);

        comparisonLbl.setLabelFor(comparisonComboBox);
        comparisonLbl.setText(
            NbBundle.getMessage(AddQueryParameterDlg.class, "COMPARISON") );        // NOI18N
        comparisonLbl.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AddQueryParameterDlg.class, "COMPARISON_a11yDescription"));
        comparisonLbl.setDisplayedMnemonic(NbBundle.getMessage(AddQueryParameterDlg.class, "COMPARISON_Mnemonic").charAt(0));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        mainPanel.add(comparisonLbl, gridBagConstraints);

        comparisonComboBox.addActionListener ( 
                new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                    JComboBox cb = (JComboBox)evt.getSource();
                    String comparisonString = (String)cb.getSelectedItem();
                    updateLabel(comparisonString);
                }
            });

        comparisonComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddQueryParameterDlg.class, "COMPARISON_a11yDescription"));

        comparisonComboBox.setModel(
            new javax.swing.DefaultComboBoxModel(
                new String[] { 
                //    "= Equals", 
                NbBundle.getMessage(AddQueryParameterDlg.class, "EQUALS"),      // NOI18N
                //    "< Less Than", 
                NbBundle.getMessage(AddQueryParameterDlg.class, "LESS_THAN"),       // NOI18N
                //    "<= Less Than Equals", 
                NbBundle.getMessage(AddQueryParameterDlg.class, "LESS_THAN_EQUALS"),        // NOI18N
                //    "> Greater Than",
                NbBundle.getMessage(AddQueryParameterDlg.class, "GREATER_THAN"),        // NOI18N
                //    ">= Greater Than Equals" 
                NbBundle.getMessage(AddQueryParameterDlg.class, "GREATER_THAN_EQUALS"),         // NOI18N
                //    "<> Not Equals" 
                NbBundle.getMessage(AddQueryParameterDlg.class, "NOT_EQUALS"),         // NOI18N
                //    "LIKE" 
                NbBundle.getMessage(AddQueryParameterDlg.class, "LIKE"),         // NOI18N
                //    "IN" 
                NbBundle.getMessage(AddQueryParameterDlg.class, "IN"),         // NOI18N
                 }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        mainPanel.add(comparisonComboBox, gridBagConstraints);

        radioButtonPanel.setLayout(new java.awt.GridBagLayout());

        valueRadioBtn.setSelected(true);
        valueRadioBtn.setMnemonic (NbBundle.getMessage(AddQueryParameterDlg.class, "VALUE_Mnemonic").charAt(0));
        valueRadioBtn.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AddQueryParameterDlg.class, "VALUE_RADIO_BUTTON_a11yDescription"));
        valueRadioBtn.setText(
            NbBundle.getMessage(AddQueryParameterDlg.class, "VALUE") );     // NOI18N
        buttonGroup1.add(valueRadioBtn);
        valueRadioBtn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    valueRadioBtnActionPerformed(evt);
                }
            });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        radioButtonPanel.add(valueRadioBtn, gridBagConstraints);

        parmRadioBtn.setMnemonic (NbBundle.getMessage(AddQueryParameterDlg.class, "PARAMETER_Mnemonic").charAt(0));
        parmRadioBtn.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddQueryParameterDlg.class, "PARAMETER_RADIO_BUTTON_a11yDescription"));
        parmRadioBtn.setText(
            NbBundle.getMessage(AddQueryParameterDlg.class, "PARAMETER") );     // NOI18N
        buttonGroup1.add(parmRadioBtn);
        parmRadioBtn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    parmRadioBtnActionPerformed(evt);
                }
            });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        radioButtonPanel.add(parmRadioBtn, gridBagConstraints);

        valueTxtField.setHorizontalAlignment(JTextField.TRAILING);
        valueTxtField.setText("0"); // NOI18N
        valueTxtField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AddQueryParameterDlg.class, "VALUE_a11yName"));
        valueTxtField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AddQueryParameterDlg.class, "VALUE_a11yDescription"));
        valueTxtFieldLbl.setText(
            NbBundle.getMessage(AddQueryParameterDlg.class, "VALUE_label"));
        valueTxtFieldLbl.setLabelFor(valueTxtField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        radioButtonPanel.add(valueTxtField, gridBagConstraints);

        comparisonHintLbl.setHorizontalAlignment(JTextField.LEADING);
        comparisonHintLbl.setText("        "); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        radioButtonPanel.add(comparisonHintLbl, gridBagConstraints);

        parmTxtField.setText("?"); // NOI18N
        parmTxtField.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(AddQueryParameterDlg.class, "PARAMETER_a11yName"));
        parmTxtField.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(AddQueryParameterDlg.class, "PARAMETER_a11yDescription"));
        parmTxtFieldLbl.setText(
            NbBundle.getMessage(AddQueryParameterDlg.class, "PARAMETER_label"));
        parmTxtFieldLbl.setLabelFor(parmTxtField);
        // what happens if the criteria is like "IN ( ?, ? )" ?
        // parmTxtField.setEnabled(false);
        // Per JDBC restrictions, parameter markers must be "?", so disable editing
        // parmTxtField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        radioButtonPanel.add(parmTxtField, gridBagConstraints);

//         defaultValTxtField.setText(" ");
//         defaultValTxtField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
//        radioButtonPanel.add(defaultValTxtField, gridBagConstraints);

//         defaultValueLbl.setText("Default Value");
//         defaultValueLbl.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        radioButtonPanel.add(defaultValueLbl, gridBagConstraints);

        fillerLbl.setText("          ");  // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        radioButtonPanel.add(fillerLbl, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(radioButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT)); // needed to get good initial size

        add(mainPanel, gridBagConstraints);
    }//GEN-END:initComponents


    private void parmRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parmRadioBtnActionPerformed
        // Add your handling code here:
        enableDlgControls();
    }//GEN-LAST:event_parmRadioBtnActionPerformed


    private void valueRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueRadioBtnActionPerformed
        // Add your handling code here:
        enableDlgControls();
    }//GEN-LAST:event_valueRadioBtnActionPerformed
    

    private void enableDlgControls() {
        boolean parmSelected = parmRadioBtn.isSelected();
        parmTxtField.setEnabled(parmSelected);
        parmTxtField.repaint();
//        defaultValTxtField.enable(parmSelected);
//        defaultValueLbl.enable(parmSelected);
//        defaultValueLbl.repaint();
        valueTxtField.setEnabled(!parmSelected);
        valueTxtField.repaint();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ButtonGroup         buttonGroup1;
    private JLabel              columnNameLbl;
    private JComboBox           comparisonComboBox;
    private JLabel              comparisonLbl;
    private JLabel              comparisonHintLbl;
//    private JTextField defaultValTxtField;
//    private JLabel defaultValueLbl;
    private JLabel              dispColumnNameLbl;
    private JLabel              fillerLbl;
    private JPanel              mainPanel;
    private JRadioButton        parmRadioBtn;
    private JPanel              radioButtonPanel;
    private JRadioButton        valueRadioBtn;
    private JTextField          valueTxtField;
    private JLabel              valueTxtFieldLbl;
    private JTextField          parmTxtField;
    private JLabel              parmTxtFieldLbl;

    private JTextArea instructions;

    // End of variables declaration//GEN-END:variables
    
    private int returnStatus = RET_CANCEL;
}
