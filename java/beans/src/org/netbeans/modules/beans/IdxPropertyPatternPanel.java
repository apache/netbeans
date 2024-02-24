/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.beans;

import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.StringTokenizer;
import java.util.List;
import java.util.LinkedList;
import javax.swing.border.TitledBorder;

import org.openide.DialogDisplayer;

import org.openide.util.Utilities;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import static org.netbeans.modules.beans.BeanUtils.*;

/** Customizer for newIndexed Property Pattern
 *
 * @author Petr Hrebejk
 */
public final class IdxPropertyPatternPanel extends javax.swing.JPanel
    implements java.awt.event.ActionListener {

    /** Dialog for displaiyng this panel */
    private Dialog dialog;
    /** Group node under which the new pattern will below */
    private PatternGroupNode groupNode;
    /** Geneartion for interface/class */
    private boolean forInterface = false;

    /** Standard types */
    private static String[] TYPES = null;

    /** Human representable form of properties modes */
    private static final String[] MODES = new String[] {
                                       getString( "LAB_ReadWriteMODE" ),
                                       getString( "LAB_ReadOnlyMODE" ),
                                       getString( "LAB_WriteOnlyMODE" )
                                   };

    static final long serialVersionUID =8551245035767258531L;
    /** Initializes the Form */
    public IdxPropertyPatternPanel() {
        initComponents ();
        initAccessibility();

        String[] types = getTypes();
        // Customize type checkbox
        for ( int i = 0; i < types.length; i++ ) {
            typeComboBox.addItem( types[i] );
        }
        typeComboBox.setSelectedItem(
                NbBundle.getMessage(IdxPropertyPatternPanel.class, "IdxPropertyPatternPanel_SelectedType")); // NOI18N

        // Customize mode checkbox
        for ( int i = 0; i < MODES.length; i++ ) {
            modeComboBox.addItem( MODES[i] );
        }
        modeComboBox.setSelectedItem( MODES[0] );

        // i18n

        ((TitledBorder)propertyPanel.getBorder()).setTitle(
            getString( "CTL_IdxPropertyPanel_propertyPanel" ) );
        ((TitledBorder)optionsPanel.getBorder()).setTitle(
            getString( "CTL_IdxPropertyPanel_optionsPanel" ) );
        ((TitledBorder)nonIndexOptionsPanel.getBorder()).setTitle(
            getString( "CTL_IdxPropertyPanel_niOptionsPanel" ) );
        nameLabel.setText( getString( "CTL_IdxPropertyPanel_nameLabel" ) );
        nameLabel.setDisplayedMnemonic(getString("CTL_IdxPropertyPanel_nameLabel_Mnemonic").charAt(0));
        nameLabel.setLabelFor(nameTextField);
        nameTextField.setToolTipText(getString("ACS_IdxPropertyPanel_nameTextFieldA11yDesc"));
        typeLabel.setText( getString( "CTL_IdxPropertyPanel_typeLabel" ) );
        typeLabel.setDisplayedMnemonic(getString("CTL_IdxPropertyPanel_typeLabel_Mnemonic").charAt(0));
        typeLabel.setLabelFor(typeComboBox);
        typeComboBox.setToolTipText(getString("ACS_IdxPropertyPanel_typeComboBoxA11yDesc"));
        modeLabel.setText(getString( "CTL_IdxPropertyPanel_modeLabel" ) );
        modeLabel.setDisplayedMnemonic(getString("CTL_IdxPropertyPanel_modeLabel_Mnemonic").charAt(0));
        modeLabel.setLabelFor(modeComboBox);
        modeComboBox.setToolTipText(getString("ACS_IdxPropertyPanel_modeComboBoxA11yDesc"));
        boundCheckBox.setText(getString( "CTL_IdxPropertyPanel_boundCheckBox" ) );
        boundCheckBox.setMnemonic(getString("CTL_IdxPropertyPanel_boundCheckBox_Mnemonic").charAt(0));
        boundCheckBox.setToolTipText(getString("ACS_IdxPropertyPanel_boundCheckBoxA11yDesc"));
        constrainedCheckBox.setText(getString( "CTL_IdxPropertyPanel_constrainedCheckBox" ) );
        constrainedCheckBox.setMnemonic(getString("CTL_IdxPropertyPanel_constrainedCheckBox_Mnemonic").charAt(0));
        constrainedCheckBox.setToolTipText(getString("ACS_IdxPropertyPanel_constrainedCheckBoxA11yDesc"));
        fieldCheckBox.setText(getString( "CTL_IdxPropertyPanel_fieldCheckBox" ) );
        fieldCheckBox.setMnemonic(getString("CTL_IdxPropertyPanel_fieldCheckBox_Mnemonic").charAt(0));
        fieldCheckBox.setToolTipText(getString("ACS_IdxPropertyPanel_fieldCheckBoxA11yDesc"));
        returnCheckBox.setText(getString( "CTL_IdxPropertyPanel_returnCheckBox" ) );
        returnCheckBox.setMnemonic(getString("CTL_IdxPropertyPanel_returnCheckBox_Mnemonic").charAt(0));
        returnCheckBox.setToolTipText(getString("ACS_IdxPropertyPanel_returnCheckBoxA11yDesc"));
        setCheckBox.setText(getString( "CTL_IdxPropertyPanel_setCheckBox" ) );
        setCheckBox.setMnemonic(getString("CTL_IdxPropertyPanel_setCheckBox_Mnemonic").charAt(0));
        setCheckBox.setToolTipText(getString("ACS_IdxPropertyPanel_setCheckBoxA11yDesc"));
        supportCheckBox.setText(getString( "CTL_IdxPropertyPanel_supportCheckBox" ) );
        supportCheckBox.setMnemonic(getString("CTL_IdxPropertyPanel_supportCheckBox_Mnemonic").charAt(0));
        supportCheckBox.setToolTipText(getString("ACS_IdxPropertyPanel_supportCheckBoxA11yDesc"));
        niGetterCheckBox.setText(getString( "CTL_IdxPropertyPanel_niGetterCheckBox" ) );
        niGetterCheckBox.setMnemonic(getString("CTL_IdxPropertyPanel_niGetterCheckBox_Mnemonic").charAt(0));
        niGetterCheckBox.setToolTipText(getString("ACS_IdxPropertyPanel_niGetterCheckBoxA11yDesc"));
        niReturnCheckBox.setText(getString( "CTL_IdxPropertyPanel_niReturnCheckBox" ) );
        niReturnCheckBox.setMnemonic(getString("CTL_IdxPropertyPanel_niReturnCheckBox_Mnemonic").charAt(0));
        niReturnCheckBox.setToolTipText(getString("ACS_IdxPropertyPanel_niReturnCheckBoxA11yDesc"));
        niSetterCheckBox.setText(getString( "CTL_IdxPropertyPanel_niSetterCheckBox" ) );
        niSetterCheckBox.setMnemonic(getString("ACS_IdxPropertyPanel_niSetterCheckBoxA11yDesc").charAt(0));
        niSetterCheckBox.setToolTipText(getString("ACS_IdxPropertyPanel_boundCheckBoxA11yDesc"));
        niSetCheckBox.setText(getString( "CTL_IdxPropertyPanel_niSetCheckBox" ) );
        niSetCheckBox.setMnemonic(getString("CTL_IdxPropertyPanel_niSetCheckBox_Mnemonic").charAt(0));
        niSetCheckBox.setToolTipText(getString("ACS_IdxPropertyPanel_niSetCheckBoxA11yDesc"));
        HelpCtx.setHelpIDString(this, HelpCtxKeys.BEAN_PROPERTIES_HELP); //NOI18N

    }


    private void initAccessibility()
    {
        this.getAccessibleContext().setAccessibleDescription(getString("ACSD_PropertyPanelDialog"));
        nameLabel.getAccessibleContext().setAccessibleDescription(getString("ACS_IdxPropertyPanel_nameLabelA11yDesc"));
        nameTextField.getAccessibleContext().setAccessibleName(getString("ACS_IdxPropertyPanel_nameTextFieldA11yName"));
        typeLabel.getAccessibleContext().setAccessibleDescription(getString("ACS_IdxPropertyPanel_typeLabelA11yDesc"));
        typeComboBox.getAccessibleContext().setAccessibleName(getString("ACS_IdxPropertyPanel_typeComboBoxA11yName"));
        modeLabel.getAccessibleContext().setAccessibleDescription(getString("ACS_IdxPropertyPanel_modeLabelA11yDesc"));
        modeComboBox.getAccessibleContext().setAccessibleName(getString("ACS_IdxPropertyPanel_modeComboBoxA11yName"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        propertyPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        modeLabel = new javax.swing.JLabel();
        modeComboBox = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        boundCheckBox = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        constrainedCheckBox = new javax.swing.JCheckBox();
        optionsPanel = new javax.swing.JPanel();
        fieldCheckBox = new javax.swing.JCheckBox();
        returnCheckBox = new javax.swing.JCheckBox();
        setCheckBox = new javax.swing.JCheckBox();
        supportCheckBox = new javax.swing.JCheckBox();
        nonIndexOptionsPanel = new javax.swing.JPanel();
        niGetterCheckBox = new javax.swing.JCheckBox();
        niReturnCheckBox = new javax.swing.JCheckBox();
        niSetterCheckBox = new javax.swing.JCheckBox();
        niSetCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.GridBagLayout());

        mainPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        propertyPanel.setLayout(new java.awt.GridBagLayout());

        propertyPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(null, new java.awt.Color(149, 142, 130)), "propertyPanel"));
        nameLabel.setText("nameLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 2);
        propertyPanel.add(nameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        propertyPanel.add(nameTextField, gridBagConstraints);

        typeLabel.setText("typeLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 2);
        propertyPanel.add(typeLabel, gridBagConstraints);

        typeComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        propertyPanel.add(typeComboBox, gridBagConstraints);

        modeLabel.setText("modeLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 2);
        propertyPanel.add(modeLabel, gridBagConstraints);

        modeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modeComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        propertyPanel.add(modeComboBox, gridBagConstraints);

        propertyPanel.add(jPanel3, new java.awt.GridBagConstraints());

        boundCheckBox.setText("boundCheckBox");
        boundCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boundCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        propertyPanel.add(boundCheckBox, gridBagConstraints);

        propertyPanel.add(jPanel4, new java.awt.GridBagConstraints());

        constrainedCheckBox.setText("constrainedCheckBox");
        constrainedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                constrainedCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        propertyPanel.add(constrainedCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(propertyPanel, gridBagConstraints);

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        optionsPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(null, new java.awt.Color(149, 142, 130)), "optionsPanel"));
        fieldCheckBox.setSelected(true);
        fieldCheckBox.setText("fieldCheckBox");
        fieldCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 4);
        optionsPanel.add(fieldCheckBox, gridBagConstraints);

        returnCheckBox.setSelected(true);
        returnCheckBox.setText("returnCheckBox");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 4);
        optionsPanel.add(returnCheckBox, gridBagConstraints);

        setCheckBox.setSelected(true);
        setCheckBox.setText("setCheckBox");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 4);
        optionsPanel.add(setCheckBox, gridBagConstraints);

        supportCheckBox.setText("supportCheckBox");
        supportCheckBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 4);
        optionsPanel.add(supportCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(optionsPanel, gridBagConstraints);

        nonIndexOptionsPanel.setLayout(new java.awt.GridBagLayout());

        nonIndexOptionsPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(null, new java.awt.Color(149, 142, 130)), "nonIndexOptionsPanel"));
        niGetterCheckBox.setText("niGetterCheckBox");
        niGetterCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                niGetterCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        nonIndexOptionsPanel.add(niGetterCheckBox, gridBagConstraints);

        niReturnCheckBox.setText("niReturnCheckBox");
        niReturnCheckBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        nonIndexOptionsPanel.add(niReturnCheckBox, gridBagConstraints);

        niSetterCheckBox.setText("niSetterCheckBox");
        niSetterCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                niSetterCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        nonIndexOptionsPanel.add(niSetterCheckBox, gridBagConstraints);

        niSetCheckBox.setText("niSetCheckBox");
        niSetCheckBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        nonIndexOptionsPanel.add(niSetCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(nonIndexOptionsPanel, gridBagConstraints);

        add(mainPanel, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

    private void niSetterCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_niSetterCheckBoxActionPerformed
        protectControls();
    }//GEN-LAST:event_niSetterCheckBoxActionPerformed

    private void niGetterCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_niGetterCheckBoxActionPerformed
        protectControls();
    }//GEN-LAST:event_niGetterCheckBoxActionPerformed

    private void fieldCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldCheckBoxActionPerformed
        protectControls();
    }//GEN-LAST:event_fieldCheckBoxActionPerformed

    private void constrainedCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_constrainedCheckBoxActionPerformed
        protectControls();
    }//GEN-LAST:event_constrainedCheckBoxActionPerformed

    private void boundCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boundCheckBoxActionPerformed
        protectControls();
    }//GEN-LAST:event_boundCheckBoxActionPerformed

    private void modeComboBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modeComboBoxActionPerformed
        protectControls();
    }//GEN-LAST:event_modeComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox boundCheckBox;
    private javax.swing.JCheckBox constrainedCheckBox;
    private javax.swing.JCheckBox fieldCheckBox;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JComboBox modeComboBox;
    private javax.swing.JLabel modeLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JCheckBox niGetterCheckBox;
    private javax.swing.JCheckBox niReturnCheckBox;
    private javax.swing.JCheckBox niSetCheckBox;
    private javax.swing.JCheckBox niSetterCheckBox;
    private javax.swing.JPanel nonIndexOptionsPanel;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JPanel propertyPanel;
    private javax.swing.JCheckBox returnCheckBox;
    private javax.swing.JCheckBox setCheckBox;
    private javax.swing.JCheckBox supportCheckBox;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables

    class Result  {
        String name;
        String type;
        int mode = PropertyPattern.READ_WRITE;
        boolean bound = false;
        boolean constrained = false;
        boolean withField = false;
        boolean withReturn = false;
        boolean withSet = false;
        boolean withSupport = false;

        boolean niGetter = false;
        boolean niWithReturn = false;
        boolean niSetter = false;
        boolean niWithSet = false;
    }

    IdxPropertyPatternPanel.Result getResult( ) {
        Result result = new Result();

        result.name = nameTextField.getText();
        result.type = typeComboBox.getEditor().getItem().toString();
        if ( modeComboBox.getSelectedItem().toString().equals( MODES[1] ) )
            result.mode = PropertyPattern.READ_ONLY;
        else if ( modeComboBox.getSelectedItem().toString().equals( MODES[2] ) )
            result.mode = PropertyPattern.WRITE_ONLY;
        else
            result.mode = PropertyPattern.READ_WRITE;

        if ( boundCheckBox.isSelected() )
            result.bound = true;

        if ( constrainedCheckBox.isSelected() )
            result.constrained = true;

        if ( fieldCheckBox.isSelected() )
            result.withField = true;

        if ( returnCheckBox.isSelected() && fieldCheckBox.isSelected())
            result.withReturn = true;

        if ( setCheckBox.isSelected() && fieldCheckBox.isSelected() )
            result.withSet = true;

        if ( supportCheckBox.isSelected() )
            result.withSupport = true;

        if ( niGetterCheckBox.isSelected() )
            result.niGetter = true;

        if ( niReturnCheckBox.isSelected() && fieldCheckBox.isSelected() )
            result.niWithReturn = true;

        if ( niSetterCheckBox.isSelected() )
            result.niSetter = true;

        if ( niSetCheckBox.isSelected() && fieldCheckBox.isSelected() )
            result.niWithSet = true;

        return result;
    }

    /** This method is called when ocuures the possibilty that any
    * xontrol should be enabled or disabled.
    */
    private void protectControls() {
        Result result = getResult();

        final boolean forClass = !forInterface;
        fieldCheckBox.setEnabled( forClass );

        returnCheckBox.setEnabled(
                  ( result.mode == PropertyPattern.READ_WRITE ||
                    result.mode == PropertyPattern.READ_ONLY ) &&
                  result.withField && forClass );

        setCheckBox.setEnabled(
            ( result.mode == PropertyPattern.READ_WRITE ||
              result.mode == PropertyPattern.WRITE_ONLY ) &&
            result.withField && forClass );

        supportCheckBox.setEnabled( ( result.bound || result.constrained ) && forClass );

        niReturnCheckBox.setEnabled( fieldCheckBox.isSelected() && result.niGetter && forClass );
        niSetCheckBox.setEnabled( fieldCheckBox.isSelected() && result.niSetter && forClass );
        
        boundCheckBox.setEnabled(forClass);
        constrainedCheckBox.setEnabled(forClass);
    }

    void setDialog( Dialog dialog ) {
        this.dialog = dialog;
    }

    void setForInterface( boolean forInterface ) {
        this.forInterface = forInterface;
        protectControls();
        fieldCheckBox.setSelected(!forInterface);
        returnCheckBox.setSelected(!forInterface);
        setCheckBox.setSelected(!forInterface);
    }

    void setGroupNode( PatternGroupNode groupNode ) {
        this.groupNode = groupNode;
    }

    public void actionPerformed( java.awt.event.ActionEvent e ) {
//        if ( dialog != null ) {
//
////            if ( e.getActionCommand().equals( "OK" ) ) { // NOI18N
//
//            if ( e.getSource() == org.openide.DialogDescriptor.OK_OPTION ) {
//
//                //Test wether the string is empty
//                String typeName = typeComboBox.getEditor().getItem().toString().trim();
//                if ( typeName.length() <= 0) {
//                    DialogDisplayer.getDefault().notify(
//                        new NotifyDescriptor.Message(
//                            getString("MSG_Not_Valid_Type"),
//                            NotifyDescriptor.ERROR_MESSAGE) );
//                    typeComboBox.requestFocus();
//                    return;
//                }
//
//                String name = nameTextField.getText().trim();
//                if ( !Utilities.isJavaIdentifier( name ) ) {
//                    DialogDisplayer.getDefault().notify(
//                        new NotifyDescriptor.Message(
//                            getString("MSG_Not_Valid_Identifier"),
//                            NotifyDescriptor.ERROR_MESSAGE) );
//                    nameTextField.requestFocus();
//                    return;
//                }
//
//                // Test wheter property with this name already exists
//                if ( groupNode.propertyExists( name ) ) {
//                    String msg = MessageFormat.format( getString("MSG_Property_Exists"),
//                                                       new Object[] { name } );
//                    DialogDisplayer.getDefault().notify(
//                        new NotifyDescriptor.Message( msg, NotifyDescriptor.ERROR_MESSAGE) );
//
//                    nameTextField.requestFocus();
//                    return;
//                }
//                
//                PatternAnalyser patternAnalyser = (PatternAnalyser) groupNode.getLookup().lookup(PatternAnalyser.class);
//                boolean isWrongType = true;
//                try {
//                    BeanUtils.beginTrans(false);
//                    try  finally {
//                        BeanUtils.endTrans();
//                    }
//                } catch (JmiException ex) {
//                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
//                }
//                if (isWrongType) {
//                    DialogDisplayer.getDefault().notify(
//                        new NotifyDescriptor.Message(
//                            getString("MSG_Not_Valid_Type"),
//                            NotifyDescriptor.ERROR_MESSAGE) );
//                    typeComboBox.requestFocus();
//                    return;
//                }
//
//            }
//            dialog.setVisible( false );
//            dialog.dispose();
//        }
    }
    
    public static String[] getTypes() {
        if (TYPES == null) {
            String typeList = NbBundle.getMessage(PropertyPatternPanel.class, "PropertyPatternPanel_Types"); // NOI18N
            StringTokenizer st = new StringTokenizer(typeList, "|"); // NOI18N
            List<String> l = new LinkedList<>();
            while (st.hasMoreTokens()) {
                String type = st.nextToken().trim();
                l.add(type);
            }
            TYPES = l.toArray(new String[0]);
        }
        return TYPES;
    }

}
