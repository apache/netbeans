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

import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeEntityDecl;
import org.netbeans.tax.TreeException;

import org.netbeans.modules.xml.tax.util.TAXUtil;

/**
 *
 * @author  Libor Kramolis, Vladimir Zboril
 * @version 0.1
 */
public class TreeEntityDeclCustomizer extends AbstractTreeCustomizer {

    /** Serial Version UID */
    private static final long serialVersionUID = -4905667144375255810L;


    /** */
    private static final String TYPE_GENERAL   = "General"; // NOI18N
    /** */
    private static final String TYPE_PARAMETER = "Parameter"; // NOI18N
    /** */
    private static final String[] typeItems = { TYPE_GENERAL, TYPE_PARAMETER };
    
    
    
    /** */
    public TreeEntityDeclCustomizer () {
        super ();
        
        initComponents ();
        nameLabel.setDisplayedMnemonic (Util.THIS.getChar ("LAB_ElementName_mn")); // NOI18N
        typeLabel.setDisplayedMnemonic (Util.THIS.getChar ("LAB_EntityType_mn")); // NOI18N
        internalRadio.setMnemonic (Util.THIS.getChar ("RAD_Internal_mn")); // NOI18N
        externalRadio.setMnemonic (Util.THIS.getChar ("RAD_External_mn")); // NOI18N
        unparsedRadio.setMnemonic (Util.THIS.getChar ("RAD_Unparsed_mn")); // NOI18N

        internValueLabel.setDisplayedMnemonic (Util.THIS.getChar ("LAB_internValue_mn")); // NOI18N
        externPublicLabel.setDisplayedMnemonic (Util.THIS.getChar ("LAB_externPublic_mn")); // NOI18N
        externSystemLabel.setDisplayedMnemonic (Util.THIS.getChar ("LAB_externSystem_mn")); // NOI18N
        unparsedPublicLabel.setDisplayedMnemonic (Util.THIS.getChar ("LAB_unparsedPublic_mn")); // NOI18N
        unparsedSystemLabel.setDisplayedMnemonic (Util.THIS.getChar ("LAB_unparsedSystem_mn")); // NOI18N
        unparsedNotationLabel.setDisplayedMnemonic (Util.THIS.getChar ("LAB_unparsedNotation_mn")); // NOI18N
        
        initAccessibility ();
    }
    
    
    /**
     */
    protected final TreeEntityDecl getEntityDecl () {
        return (TreeEntityDecl)getTreeObject ();
    }
    
    
    /**
     */
    protected void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName ().equals (TreeEntityDecl.PROP_PARAMETER)) {
            updateParameterComponent ();
        } else if (pche.getPropertyName ().equals (TreeEntityDecl.PROP_NAME)) {
            updateNameComponent ();
        } else if (pche.getPropertyName ().equals (TreeEntityDecl.PROP_INTERNAL_TEXT)) {
            updateInternalTextComponent ();
        } else if (pche.getPropertyName ().equals (TreeEntityDecl.PROP_PUBLIC_ID)) {
            updatePublicIdComponent ();
        } else if (pche.getPropertyName ().equals (TreeEntityDecl.PROP_SYSTEM_ID)) {
            updateSystemIdComponent ();
        } else if (pche.getPropertyName ().equals (TreeEntityDecl.PROP_NOTATION_NAME)) {
            updateNotationComponent ();
        } else if (pche.getPropertyName ().equals (TreeEntityDecl.PROP_TYPE)) {
            updateTypeComponent ();
        }
    }
    
    /**
     */
    protected final void updateEntityDeclParameter () {
        if ( typeCombo.getSelectedItem () == null ) {
            return;
        }
        
        try {
            getEntityDecl ().setParameter (typeCombo.getSelectedItem () == TYPE_PARAMETER);
        } catch (TreeException exc) {
            updateParameterComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateParameterComponent () {
        if (getEntityDecl ().isParameter ()) {
            typeCombo.setSelectedItem (TYPE_PARAMETER);
        } else {
            typeCombo.setSelectedItem (TYPE_GENERAL);
        }
    }
    
    /**
     */
    protected final void updateEntityDeclName () {
        try {
            getEntityDecl ().setName (nameField.getText ());
        } catch (TreeException exc) {
            updateNameComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateNameComponent () {
        nameField.setText (getEntityDecl ().getName ());
    }
    
    
    
    /**
     */
    protected final void updateEntityDeclInternalText () {
        try {
            getEntityDecl ().setInternalText (text2null (internValueField.getText ()));
        } catch (TreeException exc) {
            updateInternalTextComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateInternalTextComponent () {
        internValueField.setText (null2text (getEntityDecl ().getInternalText ()));
    }
    
    /**
     */
    protected final void updateEntityDeclPublicId () {
        try {
            if ( externalRadio.isSelected () ) {
                getEntityDecl ().setPublicId (text2null (externPublicField.getText ()));
            } else if ( unparsedRadio.isSelected () ) {
                getEntityDecl ().setPublicId (text2null (unparsedPublicField.getText ()));
            }
        } catch (TreeException exc) {
            updatePublicIdComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updatePublicIdComponent () {
        externPublicField.setText (null2text (getEntityDecl ().getPublicId ()));
        unparsedPublicField.setText (null2text (getEntityDecl ().getPublicId ()));
    }
    
    /**
     */
    protected final void updateEntityDeclSystemId () {
        try {
            if ( externalRadio.isSelected () ) {
                getEntityDecl ().setSystemId (text2null (externSystemField.getText ()));
            } else if ( unparsedRadio.isSelected () ) {
                getEntityDecl ().setSystemId (text2null (unparsedSystemField.getText ()));
            }
        } catch (TreeException exc) {
            updateSystemIdComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateSystemIdComponent () {
        externSystemField.setText (null2text (getEntityDecl ().getSystemId ()));
        unparsedSystemField.setText (null2text (getEntityDecl ().getSystemId ()));
    }
    
    
    /**
     */
    protected final void updateEntityDeclNotationName () {
        try {
            getEntityDecl ().setNotationName (text2null (unparsedNotationField.getText ()));
        } catch (TreeException exc) {
            updateNotationComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateNotationComponent () {
        unparsedNotationField.setText (null2text (getEntityDecl ().getNotationName ()));
    }
    
    /**
     */
    protected final void updateTypeComponent () {
        CardLayout cl = (CardLayout)typeCardPanel.getLayout ();
        if ( getEntityDecl ().getType () == TreeEntityDecl.TYPE_INTERNAL ) {
            internalRadio.setSelected (true);
            cl.show (typeCardPanel, "internalPanel"); // NOI18N
        } else if ( getEntityDecl ().getType () == TreeEntityDecl.TYPE_EXTERNAL ) {
            externalRadio.setSelected (true);
            cl.show (typeCardPanel, "externalPanel"); // NOI18N
        } else {
            unparsedRadio.setSelected (true);
            cl.show (typeCardPanel, "unparsedPanel"); // NOI18N
        }
    }
    
    
    /**
     */
    protected final void initComponentValues () {
        updateParameterComponent ();
        updateNameComponent ();
        updateInternalTextComponent ();
        updatePublicIdComponent ();
        updateSystemIdComponent ();
        updateNotationComponent ();
        updateTypeComponent ();
    }
    
    
    /**
     */
    protected void updateReadOnlyStatus (boolean editable) {
        nameField.setEditable (editable);
        typeCombo.setEnabled (editable);
        internalRadio.setEnabled (editable);
        externalRadio.setEnabled (editable);
        unparsedRadio.setEnabled (editable);
        internValueField.setEditable (editable);
        externPublicField.setEditable (editable);
        externSystemField.setEditable (editable);
        unparsedPublicField.setEditable (editable);
        unparsedSystemField.setEditable (editable);
        unparsedNotationField.setEditable (editable);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typeCombo = new javax.swing.JComboBox(typeItems);
        entityTypePanel = new javax.swing.JPanel();
        internalRadio = new javax.swing.JRadioButton();
        externalRadio = new javax.swing.JRadioButton();
        unparsedRadio = new javax.swing.JRadioButton();
        typeCardPanel = new javax.swing.JPanel();
        internalPanel = new javax.swing.JPanel();
        internValueLabel = new javax.swing.JLabel();
        internValueField = new javax.swing.JTextField();
        externalPanel = new javax.swing.JPanel();
        externPublicLabel = new javax.swing.JLabel();
        externPublicField = new javax.swing.JTextField();
        externSystemLabel = new javax.swing.JLabel();
        externSystemField = new javax.swing.JTextField();
        unparsedPanel = new javax.swing.JPanel();
        unparsedPublicLabel = new javax.swing.JLabel();
        unparsedPublicField = new javax.swing.JTextField();
        unparsedSystemLabel = new javax.swing.JLabel();
        unparsedSystemField = new javax.swing.JTextField();
        unparsedNotationLabel = new javax.swing.JLabel();
        unparsedNotationField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setText(Util.THIS.getString ("LAB_ElementName"));
        nameLabel.setLabelFor(nameField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
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
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(nameField, gridBagConstraints);

        typeLabel.setText(Util.THIS.getString ("LAB_EntityType"));
        typeLabel.setLabelFor(typeCombo);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(typeLabel, gridBagConstraints);

        typeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(typeCombo, gridBagConstraints);

        entityTypePanel.setLayout(new java.awt.GridBagLayout());

        entityTypePanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0))));
        internalRadio.setSelected(true);
        internalRadio.setText(Util.THIS.getString ("RAD_Internal"));
        buttonGroup.add(internalRadio);
        internalRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                internalRadioActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        entityTypePanel.add(internalRadio, gridBagConstraints);

        externalRadio.setText(Util.THIS.getString ("RAD_External"));
        buttonGroup.add(externalRadio);
        externalRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                externalRadioActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        entityTypePanel.add(externalRadio, gridBagConstraints);

        unparsedRadio.setText(Util.THIS.getString ("RAD_Unparsed"));
        buttonGroup.add(unparsedRadio);
        unparsedRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unparsedRadioActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        entityTypePanel.add(unparsedRadio, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(entityTypePanel, gridBagConstraints);

        typeCardPanel.setLayout(new java.awt.CardLayout());

        internalPanel.setLayout(new java.awt.GridBagLayout());

        internValueLabel.setText(Util.THIS.getString ("LAB_Internal_Text"));
        internValueLabel.setLabelFor(internValueField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        internalPanel.add(internValueLabel, gridBagConstraints);

        internValueField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                internValueFieldActionPerformed(evt);
            }
        });

        internValueField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                internValueFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                internValueFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        internalPanel.add(internValueField, gridBagConstraints);

        typeCardPanel.add(internalPanel, "internalPanel");

        externalPanel.setLayout(new java.awt.GridBagLayout());

        externPublicLabel.setText(Util.THIS.getString ("LAB_External_PublicId"));
        externPublicLabel.setLabelFor(externPublicField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        externalPanel.add(externPublicLabel, gridBagConstraints);

        externPublicField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                externPublicFieldActionPerformed(evt);
            }
        });

        externPublicField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                externPublicFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        externalPanel.add(externPublicField, gridBagConstraints);

        externSystemLabel.setText(Util.THIS.getString ("LAB_External_SystemId"));
        externSystemLabel.setLabelFor(externSystemField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        externalPanel.add(externSystemLabel, gridBagConstraints);

        externSystemField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                externSystemFieldActionPerformed(evt);
            }
        });

        externSystemField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                externSystemFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        externalPanel.add(externSystemField, gridBagConstraints);

        typeCardPanel.add(externalPanel, "externalPanel");

        unparsedPanel.setLayout(new java.awt.GridBagLayout());

        unparsedPublicLabel.setText(Util.THIS.getString ("LAB_Unparsed_PublicId"));
        unparsedPublicLabel.setLabelFor(unparsedPublicField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        unparsedPanel.add(unparsedPublicLabel, gridBagConstraints);

        unparsedPublicField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unparsedPublicFieldActionPerformed(evt);
            }
        });

        unparsedPublicField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                unparsedPublicFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        unparsedPanel.add(unparsedPublicField, gridBagConstraints);

        unparsedSystemLabel.setText(Util.THIS.getString ("LAB_Unparsed_SystemId"));
        unparsedSystemLabel.setLabelFor(unparsedSystemField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        unparsedPanel.add(unparsedSystemLabel, gridBagConstraints);

        unparsedSystemField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unparsedSystemFieldActionPerformed(evt);
            }
        });

        unparsedSystemField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                unparsedSystemFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        unparsedPanel.add(unparsedSystemField, gridBagConstraints);

        unparsedNotationLabel.setText(Util.THIS.getString ("LAB_Unparsed_NotationName"));
        unparsedNotationLabel.setLabelFor(unparsedNotationField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        unparsedPanel.add(unparsedNotationLabel, gridBagConstraints);

        unparsedNotationField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unparsedNotationFieldActionPerformed(evt);
            }
        });

        unparsedNotationField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                unparsedNotationFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        unparsedPanel.add(unparsedNotationField, gridBagConstraints);

        typeCardPanel.add(unparsedPanel, "unparsedPanel");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(typeCardPanel, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void internValueFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_internValueFieldFocusGained
        // Accessibility:
        internValueField.selectAll ();
    }//GEN-LAST:event_internValueFieldFocusGained
    
    private void nameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusGained
        // Accessibility:
        nameField.selectAll ();
    }//GEN-LAST:event_nameFieldFocusGained
    
    private void unparsedNotationFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_unparsedNotationFieldFocusLost
        // Add your handling code here:
        updateEntityDeclNotationName ();
    }//GEN-LAST:event_unparsedNotationFieldFocusLost
    
    private void unparsedNotationFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unparsedNotationFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclNotationName ();
    }//GEN-LAST:event_unparsedNotationFieldActionPerformed
    
    private void unparsedSystemFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_unparsedSystemFieldFocusLost
        // Add your handling code here:
        updateEntityDeclSystemId ();
    }//GEN-LAST:event_unparsedSystemFieldFocusLost
    
    private void unparsedSystemFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unparsedSystemFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclSystemId ();
    }//GEN-LAST:event_unparsedSystemFieldActionPerformed
    
    private void unparsedPublicFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_unparsedPublicFieldFocusLost
        // Add your handling code here:
        updateEntityDeclPublicId ();
    }//GEN-LAST:event_unparsedPublicFieldFocusLost
    
    private void unparsedPublicFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unparsedPublicFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclPublicId ();
    }//GEN-LAST:event_unparsedPublicFieldActionPerformed
    
    private void externSystemFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_externSystemFieldFocusLost
        // Add your handling code here:
        updateEntityDeclSystemId ();
    }//GEN-LAST:event_externSystemFieldFocusLost
    
    private void externSystemFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_externSystemFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclSystemId ();
    }//GEN-LAST:event_externSystemFieldActionPerformed
    
    private void externPublicFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_externPublicFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclPublicId ();
    }//GEN-LAST:event_externPublicFieldActionPerformed
    
    private void externPublicFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_externPublicFieldFocusLost
        // Add your handling code here:
        updateEntityDeclPublicId ();
    }//GEN-LAST:event_externPublicFieldFocusLost
    
    private void internValueFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_internValueFieldFocusLost
        // Add your handling code here:
        updateEntityDeclInternalText ();
    }//GEN-LAST:event_internValueFieldFocusLost
    
    private void internValueFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_internValueFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclInternalText ();
    }//GEN-LAST:event_internValueFieldActionPerformed
    
    private void nameFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusLost
        // Add your handling code here:
        updateEntityDeclName ();
    }//GEN-LAST:event_nameFieldFocusLost
    
    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // Add your handling code here:
        updateEntityDeclName ();
    }//GEN-LAST:event_nameFieldActionPerformed
    
    private void unparsedRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unparsedRadioActionPerformed
        // Add your handling code here:
        CardLayout cl = (CardLayout)typeCardPanel.getLayout ();
        cl.show (typeCardPanel, "unparsedPanel"); // NOI18N
    }//GEN-LAST:event_unparsedRadioActionPerformed
    
    private void externalRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_externalRadioActionPerformed
        // Add your handling code here:
        CardLayout cl = (CardLayout)typeCardPanel.getLayout ();
        cl.show (typeCardPanel, "externalPanel"); // NOI18N
    }//GEN-LAST:event_externalRadioActionPerformed
    
    private void internalRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_internalRadioActionPerformed
        // Add your handling code here:
        CardLayout cl = (CardLayout)typeCardPanel.getLayout ();
        cl.show (typeCardPanel, "internalPanel"); // NOI18N
    }//GEN-LAST:event_internalRadioActionPerformed
    
    private void typeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboActionPerformed
        unparsedRadio.setEnabled (typeCombo.getSelectedIndex () != 1);
        if (unparsedRadio.isSelected ()) {
            internalRadio.setSelected (true);
            internalRadioActionPerformed (evt);
        }
        updateEntityDeclParameter ();
    }//GEN-LAST:event_typeComboActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField unparsedSystemField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel externSystemLabel;
    private javax.swing.JRadioButton internalRadio;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField externSystemField;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JPanel internalPanel;
    private javax.swing.JLabel unparsedPublicLabel;
    private javax.swing.JLabel internValueLabel;
    private javax.swing.JTextField unparsedPublicField;
    private javax.swing.JLabel externPublicLabel;
    private javax.swing.JRadioButton unparsedRadio;
    private javax.swing.JTextField internValueField;
    private javax.swing.JTextField externPublicField;
    private javax.swing.JPanel entityTypePanel;
    private javax.swing.JLabel unparsedNotationLabel;
    private javax.swing.JPanel unparsedPanel;
    private javax.swing.JRadioButton externalRadio;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JTextField unparsedNotationField;
    private javax.swing.JPanel typeCardPanel;
    private javax.swing.JLabel unparsedSystemLabel;
    private javax.swing.JPanel externalPanel;
    private javax.swing.JComboBox typeCombo;
    // End of variables declaration//GEN-END:variables
    
    /** Initialize accesibility
     */
    public void initAccessibility (){
        
        this.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_TreeEntityDeclCustomizer"));
        
        nameField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_nameField2"));
        typeCombo.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_typeCombo"));
        
        internValueField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_internValueField"));
        internValueField.selectAll ();
        
        externPublicField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_externPublicField"));
        externSystemField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_externSystemField"));
        
        unparsedPublicField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_unparsedPublicField"));
        unparsedSystemField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_unparsedSystemField"));
        unparsedNotationField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_unparsedNotationField"));
        
    }
}
