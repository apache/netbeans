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

import org.netbeans.tax.TreeAttribute;
import org.netbeans.tax.TreeElement;
import org.netbeans.tax.TreeException;

import org.netbeans.modules.xml.tax.util.TAXUtil;
import org.netbeans.modules.xml.tax.beans.Lib;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeAttributeCustomizer extends AbstractTreeCustomizer {

    /** Serial Version UID */
    private static final long serialVersionUID = 7976099790445909386L;

    /** */
    private volatile boolean askingDialog = false;
    
    
    //
    // init
    //
    
    /** Creates new TreeAttributeCustomizer. */
    public TreeAttributeCustomizer () {
        super ();
        
        initComponents ();
        nameLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_xmlName")); // NOI18N
        valueLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_xmlValue")); // NOI18N
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeAttribute getAttribute () {
        return (TreeAttribute)getTreeObject ();
    }
    
    /**
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName ().equals (TreeAttribute.PROP_NAME)) {
            updateNameComponent ();
        } else if (pche.getPropertyName ().equals (TreeAttribute.PROP_VALUE)) {
            updateValueComponent ();
        }
    }
    
    /**
     */
    protected final void updateAttributeName () {
        if ( askingDialog ) {
            return;
        }
        
        try {
            String attrName = nameField.getText ();
            
            boolean toSet = true;
            TreeElement ownerElement = getAttribute ().getOwnerElement ();
            if ( ownerElement != null ) { // if it is not new attribute (has owner element)
                TreeAttribute oldAttribute = ownerElement.getAttribute (attrName);
                if ( getAttribute () != oldAttribute ) {
                    if ( oldAttribute != null ) {
                        askingDialog = true;
                        toSet = Lib.confirmAction (Util.THIS.getString ("MSG_replace_attribute", attrName));
                        askingDialog = false;
                    }
                }
            }
            
            if ( toSet ) {
                getAttribute ().setQName (attrName);
            } else {
                updateNameComponent ();
            }
        } catch (TreeException exc) {
            updateNameComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateNameComponent () {
        nameField.setText (getAttribute ().getQName ());
    }
    
    /**
     */
    protected final void updateAttributeValue () {
        try {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\nTreeAttributeCustomizer::updateAttributeValue: valueField.getText() = " + valueField.getText ());//, new RuntimeException()); // NOI18N
            
            getAttribute ().setValue (valueField.getText ());
            //            TAXUtil.setAttributeValue (getAttribute(), valueField.getText());
        } catch (TreeException ex) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("                       ::updateAttributeValue: ex = " + ex + "\n"); // NOI18N
            
            updateValueComponent ();
            TAXUtil.notifyTreeException (ex);
        }
    }
    
    /**
     */
    protected final void updateValueComponent () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\nTreeAttributeCustomizer::updateValueComponent: getAttribute().getValue() = " + getAttribute ().getValue ());//, new RuntimeException()); // NOI18N
        
        valueField.setText (getAttribute ().getValue ());
    }
    
    /**
     */
    protected void initComponentValues () {
        updateNameComponent ();
        updateValueComponent ();
    }
    
    
    /**
     */
    protected final void updateReadOnlyStatus (boolean editable) {
        nameField.setEditable (editable);
        valueField.setEditable (editable);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        valueLabel = new javax.swing.JLabel();
        valueField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setText(Util.THIS.getString ("PROP_xmlName"));
        nameLabel.setLabelFor(nameField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
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
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(nameField, gridBagConstraints);

        valueLabel.setText(Util.THIS.getString ("PROP_xmlValue"));
        valueLabel.setLabelFor(valueField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(valueLabel, gridBagConstraints);

        valueField.setColumns(20);
        valueField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueFieldActionPerformed(evt);
            }
        });

        valueField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                valueFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                valueFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(valueField, gridBagConstraints);

    }//GEN-END:initComponents

    private void valueFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_valueFieldFocusGained
        if ("new".equals(getClientProperty("xml-edit-mode"))) {  // NOI18N
            valueField.selectAll();
        }
    }//GEN-LAST:event_valueFieldFocusGained

    private void nameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusGained
        if ("new".equals(getClientProperty("xml-edit-mode"))) {  // NOI18N
            nameField.selectAll();
        }
    }//GEN-LAST:event_nameFieldFocusGained
    
    /**
     */
    private void valueFieldFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_valueFieldFocusLost
        // Add your handling code here:
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeAttributeCustomizer::valueFieldFocusLost"); // NOI18N
        
        updateAttributeValue ();        
    }//GEN-LAST:event_valueFieldFocusLost
    
    /**
     */
    private void nameFieldFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusLost
        // Add your handling code here:
        updateAttributeName ();
    }//GEN-LAST:event_nameFieldFocusLost
    
    /**
     */
    private void valueFieldActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueFieldActionPerformed
        // Add your handling code here:
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeAttributeCustomizer::valueFieldActionPerformed"); // NOI18N
        
        updateAttributeValue ();
    }//GEN-LAST:event_valueFieldActionPerformed
    
    /**
     */
    private void nameFieldActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // Add your handling code here:
        updateAttributeName ();
    }//GEN-LAST:event_nameFieldActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JTextField valueField;
    // End of variables declaration//GEN-END:variables
    
}
