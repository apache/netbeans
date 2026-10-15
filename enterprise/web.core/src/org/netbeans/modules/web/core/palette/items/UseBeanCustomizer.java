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

package org.netbeans.modules.web.core.palette.items;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JspPaletteUtilities;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author  Libor Kotouc
 */
public class UseBeanCustomizer extends javax.swing.JPanel {

    private static final ResourceBundle bundle = NbBundle.getBundle(UseBeanCustomizer.class);

    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport statusLine;
    private boolean dialogOK = false;
    PageInfo.BeanData[] beanData = null;
    UseBean useBean;
    JTextComponent target;
            
    /** Creates new form TABLECustomizerPanel */
    public UseBeanCustomizer(UseBean useBean, JTextComponent target) {
        this.useBean = useBean;
        this.target = target;
        
        initComponents();
        
        jComboBox2.setModel(new DefaultComboBoxModel<>(UseBean.scopes));
        jComboBox2.setSelectedIndex(useBean.getScopeIndex());
        jTextField2.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent arg0) {
                validateInput();
            }
            public void removeUpdate(DocumentEvent arg0) {
                validateInput();
            }
            public void changedUpdate(DocumentEvent arg0) {
                validateInput();
            }
        });
        jTextField1.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent arg0) {
                validateInput();
            }
            public void removeUpdate(DocumentEvent arg0) {
                validateInput();
            }
            public void changedUpdate(DocumentEvent arg0) {
                validateInput();
            }
        });
    }
    
    public boolean showDialog(PageInfo.BeanData[] beanData) {
        this.beanData = beanData;
        dialogOK = false;
        
        String displayName = ""; // NOI18N
        try {
            displayName = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.resources.Bundle").getString("NAME_jsp-UseBean"); // NOI18N
        }
        catch (Exception e) {}
        
        descriptor = new DialogDescriptor
                (this, bundle.getString("LBL_Customizer_InsertPrefix") + " " + displayName, true,   // NOI18N
                 DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                 new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                            collectInput();
                            dialogOK = true;
                        }
                        dialog.dispose();
                     }
                  }
        );
        statusLine = descriptor.createNotificationLineSupport();
        validateInput();
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();
        
        return dialogOK;
    }
    
    private void collectInput() {
        useBean.setBean(jTextField2.getText());
        
        String clazz = jTextField1.getText();
        useBean.setClazz(clazz);
        
        int scopeIndex = jComboBox2.getSelectedIndex();
        useBean.setScopeIndex(scopeIndex);
    }
    
    private void validateInput() {
        if (jTextField2.getText().length() < 1) {
            statusLine.setInformationMessage(bundle.getString("Error_Empty_ID")); // NOI18N
            descriptor.setValid(false);
            return;
        }
        if (JspPaletteUtilities.idExists(jTextField2.getText(), beanData)) {
            statusLine.setErrorMessage(bundle.getString("Error_not_uniq_ID")); // NOI18N
            descriptor.setValid(false);
            return;
        }
        if (jTextField1.getText().length() < 1) {
            statusLine.setInformationMessage(bundle.getString("Error_Empty_class")); // NOI18N
            descriptor.setValid(false);
            return;
        }
        if (!JspPaletteUtilities.typeExists(target, jTextField1.getText())) {
            statusLine.setErrorMessage(bundle.getString("Error_No_Such_class")); // NOI18N
            descriptor.setValid(false);
            return;
        }

        statusLine.clearMessages();
        descriptor.setValid(true);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jFileChooser1 = new javax.swing.JFileChooser();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();

        jFileChooser1.setCurrentDirectory(null);

        setLayout(new java.awt.GridBagLayout());

        jLabel4.setLabelFor(jComboBox2);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "LBL_UseBean_Scope")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "ACSN_UseBean_Scope")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "ACSD_UseBean_Scope")); // NOI18N

        jTextField1.setColumns(35);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(jTextField1, gridBagConstraints);

        jLabel1.setLabelFor(jTextField2);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "LBL_UseBean_ID")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "ACSN_UseBean_ID")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "ACSD_UseBean_ID")); // NOI18N

        jLabel2.setLabelFor(jTextField1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "LBL_UseBean_Class")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "ACSN_UseBean_Class")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "ACSD_UseBean_Class")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(jComboBox2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(jTextField2, gridBagConstraints);

        jLabel3.setForeground(new java.awt.Color(255, 0, 0));
        add(jLabel3, new java.awt.GridBagConstraints());

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UseBeanCustomizer.class, "ACSD_UseBean_Dialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
    
}
