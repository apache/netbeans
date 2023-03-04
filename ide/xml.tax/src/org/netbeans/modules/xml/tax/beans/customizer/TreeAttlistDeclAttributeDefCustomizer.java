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
package org.netbeans.modules.xml.tax.beans.customizer;

import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeAttlistDeclAttributeDef;

import org.netbeans.modules.xml.tax.util.TAXUtil;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeAttlistDeclAttributeDefCustomizer extends AbstractTreeCustomizer {

    /** Serial Version UID */
    private static final long serialVersionUID =2877621716093964464L;


    //
    // init
    //

    /** */
    public TreeAttlistDeclAttributeDefCustomizer () {
        super ();
        
        initComponents ();
        
        elementNameLabel.setDisplayedMnemonic (Util.THIS.getChar ("TreeAttributeDeclCustomizer.elemNameLabel.mne")); // NOI18N
        nameLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_xmlName")); // NOI18N
        typeLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_dtdAttDefType")); // NOI18N
        enumeratedLabel.setDisplayedMnemonic (Util.THIS.getChar ("DTDAttDefNode.enumeratedLabel.mne")); // NOI18N
        defaultTypeLabel.setDisplayedMnemonic (Util.THIS.getChar ("DTDAttDefNode.defaultTypeLabel.mne")); // NOI18N
        defaultValueLabel.setDisplayedMnemonic (Util.THIS.getChar ("DTDAttDefNode.defaultValueLabel.mne")); // NOI18N
        
        initAccessibility ();
    }
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeAttlistDeclAttributeDef getAttributeDef () {
        return (TreeAttlistDeclAttributeDef)getTreeObject ();
    }
    
    /**
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName ().equals (TreeAttlistDeclAttributeDef.PROP_NAME)) {
            updateNameComponent ();
        } else if (pche.getPropertyName ().equals (TreeAttlistDeclAttributeDef.PROP_TYPE)) {
            updateTypeComponent ();
        } else if (pche.getPropertyName ().equals (TreeAttlistDeclAttributeDef.PROP_ENUMERATED_TYPE)) {
            updateEnumeratedTypeComponent ();
        } else if (pche.getPropertyName ().equals (TreeAttlistDeclAttributeDef.PROP_DEFAULT_TYPE)) {
            updateDefaultTypeComponent ();
        } else if (pche.getPropertyName ().equals (TreeAttlistDeclAttributeDef.PROP_DEFAULT_VALUE)) {
            updateDefaultValueComponent ();
        }
    }
    
    
    /**
     */
    protected final void updateElementNameComponent () {
        elementNameField.setText (getAttributeDef ().getElementName ());
    }
    
    /**
     */
    protected final void updateNameComponent () {
        nameField.setText (getAttributeDef ().getName ());
    }
    
    /**
     */
    protected final void updateAttributeDefName () {
        try {
            getAttributeDef ().setName (nameField.getText ());
        } catch (TreeException exc) {
            updateNameComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateTypeComponent () {
        typeField.setText (getAttributeDef ().getTypeName ());
    }
    
    /**
     */
    protected final void updateAttributeDefType () {
        try {
            getAttributeDef ().setType
                (TreeAttlistDeclAttributeDef.findType (text2null (typeField.getText ())),
                 getAttributeDef ().getEnumeratedType ());
        } catch (TreeException exc) {
            updateTypeComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateEnumeratedTypeComponent () {
        enumeratedField.setText (null2text (getAttributeDef ().getEnumeratedTypeString ()));
    }
    
    /**
     */
    protected final void updateAttributeDefEnumeratedType () {
        try {
            getAttributeDef ().setType
                (getAttributeDef ().getType (),
                 TreeAttlistDeclAttributeDef.createEnumeratedType (text2null (enumeratedField.getText ())));
        } catch (TreeException exc) {
            updateEnumeratedTypeComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateDefaultTypeComponent () {
        defaultTypeField.setText (null2text (getAttributeDef ().getDefaultTypeName ()));
    }
    
    /**
     */
    protected final void updateAttributeDefDefaultType () {
        try {
            getAttributeDef ().setDefaultType
                (TreeAttlistDeclAttributeDef.findDefaultType (text2null (defaultTypeField.getText ())),
                 getAttributeDef ().getDefaultValue ());
        } catch (TreeException exc) {
            updateDefaultTypeComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateDefaultValueComponent () {
        defaultValueField.setText (null2text (getAttributeDef ().getDefaultValue ()));
    }
    
    /**
     */
    protected final void updateAttributeDefDefaultValue () {
        try {
            short defaultType = getAttributeDef ().getDefaultType ();
            String defaultValue = text2null (defaultValueField.getText ());
            if ( defaultValue == null ) {
                if ( ( defaultType == TreeAttlistDeclAttributeDef.DEFAULT_TYPE_NULL ) ||
                     ( defaultType == TreeAttlistDeclAttributeDef.DEFAULT_TYPE_FIXED ) ) {
                    defaultValue = "";
                }
            }
            getAttributeDef ().setDefaultType (defaultType, defaultValue);
        } catch (TreeException exc) {
            updateDefaultValueComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    
    /**
     */
    protected final void initComponentValues () {
        updateElementNameComponent ();
        updateNameComponent ();
        updateTypeComponent ();
        updateEnumeratedTypeComponent ();
        updateDefaultTypeComponent ();
        updateDefaultValueComponent ();
    }
    
    
    /**
     */
    protected final void updateReadOnlyStatus (boolean editable) {
        nameField.setEditable (editable);
        typeField.setEditable (editable);
        enumeratedField.setEditable (editable);
        defaultTypeField.setEditable (editable);
        defaultValueField.setEditable (editable);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        elementNameLabel = new javax.swing.JLabel();
        elementNameField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typeField = new javax.swing.JTextField();
        enumeratedLabel = new javax.swing.JLabel();
        enumeratedField = new javax.swing.JTextField();
        defaultTypeLabel = new javax.swing.JLabel();
        defaultTypeField = new javax.swing.JTextField();
        defaultValueLabel = new javax.swing.JLabel();
        defaultValueField = new javax.swing.JTextField();
        fillPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        elementNameLabel.setText(Util.THIS.getString ("TreeAttributeDeclCustomizer.elemNameLabel.text"));
        elementNameLabel.setLabelFor(elementNameField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(elementNameLabel, gridBagConstraints);

        elementNameField.setEditable(false);
        elementNameField.setColumns(20);
        elementNameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                elementNameFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(elementNameField, gridBagConstraints);

        nameLabel.setText(Util.THIS.getString ("PROP_xmlName"));
        nameLabel.setLabelFor(nameField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(nameLabel, gridBagConstraints);

        nameField.setColumns(20);
        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });

        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                nameFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(nameField, gridBagConstraints);

        typeLabel.setText(Util.THIS.getString ("PROP_dtdAttDefType"));
        typeLabel.setLabelFor(typeField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(typeLabel, gridBagConstraints);

        typeField.setColumns(20);
        typeField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeFieldActionPerformed(evt);
            }
        });

        typeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                typeFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                typeFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(typeField, gridBagConstraints);

        enumeratedLabel.setText(Util.THIS.getString ("DTDAttDefNode.enumeratedLabel.text"));
        enumeratedLabel.setLabelFor(enumeratedField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(enumeratedLabel, gridBagConstraints);

        enumeratedField.setColumns(20);
        enumeratedField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enumeratedFieldActionPerformed(evt);
            }
        });

        enumeratedField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                enumeratedFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                enumeratedFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(enumeratedField, gridBagConstraints);

        defaultTypeLabel.setText(Util.THIS.getString ("DTDAttDefNode.defaultTypeLabel.text"));
        defaultTypeLabel.setLabelFor(defaultTypeField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(defaultTypeLabel, gridBagConstraints);

        defaultTypeField.setColumns(20);
        defaultTypeField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultTypeFieldActionPerformed(evt);
            }
        });

        defaultTypeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                defaultTypeFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                defaultTypeFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(defaultTypeField, gridBagConstraints);

        defaultValueLabel.setText(Util.THIS.getString ("DTDAttDefNode.defaultValueLabel.text"));
        defaultValueLabel.setLabelFor(defaultValueField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(defaultValueLabel, gridBagConstraints);

        defaultValueField.setColumns(20);
        defaultValueField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultValueFieldActionPerformed(evt);
            }
        });

        defaultValueField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                defaultValueFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                defaultValueFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(defaultValueField, gridBagConstraints);

        fillPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(fillPanel, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void defaultValueFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_defaultValueFieldFocusGained
        // Accessibility:
        defaultValueField.selectAll ();
    }//GEN-LAST:event_defaultValueFieldFocusGained
    
    private void defaultTypeFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_defaultTypeFieldFocusGained
        // Accessibility:
        defaultTypeField.selectAll ();
    }//GEN-LAST:event_defaultTypeFieldFocusGained
    
    private void enumeratedFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_enumeratedFieldFocusGained
        // Accessibility:
        enumeratedField.selectAll ();
    }//GEN-LAST:event_enumeratedFieldFocusGained
    
    private void typeFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_typeFieldFocusGained
        // Accessibility:
        typeField.selectAll ();
    }//GEN-LAST:event_typeFieldFocusGained
    
    private void nameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusGained
        // Accessibility:
        nameField.selectAll ();
    }//GEN-LAST:event_nameFieldFocusGained
    
    private void elementNameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_elementNameFieldFocusGained
        // Accessibility:
        elementNameField.selectAll ();
    }//GEN-LAST:event_elementNameFieldFocusGained
    
    private void defaultValueFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultValueFieldActionPerformed
        // Add your handling code here:
        updateAttributeDefDefaultValue ();
    }//GEN-LAST:event_defaultValueFieldActionPerformed
    
    private void defaultValueFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_defaultValueFieldFocusLost
        // Add your handling code here:
        updateAttributeDefDefaultValue ();
    }//GEN-LAST:event_defaultValueFieldFocusLost
    
    private void defaultTypeFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_defaultTypeFieldFocusLost
        // Add your handling code here:
        updateAttributeDefDefaultType ();
    }//GEN-LAST:event_defaultTypeFieldFocusLost
    
    private void defaultTypeFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultTypeFieldActionPerformed
        // Add your handling code here:
        updateAttributeDefDefaultType ();
    }//GEN-LAST:event_defaultTypeFieldActionPerformed
    
    private void enumeratedFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enumeratedFieldActionPerformed
        // Add your handling code here:
        updateAttributeDefEnumeratedType ();
    }//GEN-LAST:event_enumeratedFieldActionPerformed
    
    private void enumeratedFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_enumeratedFieldFocusLost
        // Add your handling code here:
        updateAttributeDefEnumeratedType ();
    }//GEN-LAST:event_enumeratedFieldFocusLost
    
    private void typeFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_typeFieldFocusLost
        // Add your handling code here:
        updateAttributeDefType ();
    }//GEN-LAST:event_typeFieldFocusLost
    
    private void typeFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeFieldActionPerformed
        // Add your handling code here:
        updateAttributeDefType ();
    }//GEN-LAST:event_typeFieldActionPerformed
    
    private void nameFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusLost
        // Add your handling code here:
        updateAttributeDefName ();
    }//GEN-LAST:event_nameFieldFocusLost
    
    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // Add your handling code here:
        updateAttributeDefName ();
    }//GEN-LAST:event_nameFieldActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel enumeratedLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel defaultTypeLabel;
    private javax.swing.JTextField enumeratedField;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JTextField defaultTypeField;
    private javax.swing.JTextField typeField;
    private javax.swing.JLabel defaultValueLabel;
    private javax.swing.JTextField defaultValueField;
    private javax.swing.JLabel elementNameLabel;
    private javax.swing.JTextField elementNameField;
    private javax.swing.JPanel fillPanel;
    // End of variables declaration//GEN-END:variables
    
    
    /** Initialize accesibility
     */
    public void initAccessibility (){
        
        elementNameField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_elementNameField"));
        elementNameField.selectAll ();
        
        nameField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_nameField1"));
        nameField.selectAll ();
        
        typeField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_typeField"));
        typeField.selectAll ();
        
        enumeratedField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_enumeratedField"));
        enumeratedField.selectAll ();
        
        defaultTypeField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_defaultTypeField"));
        defaultTypeField.selectAll ();
        
        defaultValueField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_defaultValueField"));
        defaultValueField.selectAll ();
        
        this.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_TreeAttlistDeclAttributeDefCustomizer"));
    }
}
