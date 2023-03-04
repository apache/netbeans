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

package org.netbeans.modules.web.core.palette.items;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JspPaletteUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 *
 * @author  Libor Kotouc
 */
public class GetPropertyCustomizer extends javax.swing.JPanel {

    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private boolean dialogOK = false;
    GetProperty getProperty;
    JTextComponent target;

    public GetPropertyCustomizer(GetProperty getProperty, JTextComponent target) {
        this.getProperty = getProperty;
        this.target = target;

        initComponents();

        beanNameCb.setModel(new DefaultComboBoxModel(getProperty.getAllBeans().toArray()));
        beanNameCb.setSelectedIndex(getProperty.getBeanIndex());
        addDocumentListener(beanNameCb);
    }

    public boolean showDialog() {
        dialogOK = false;

        String displayName = "";  // NOI18N
        try {
            displayName = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.resources.Bundle").getString("NAME_jsp-GetProperty"); // NOI18N
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        descriptor = new DialogDescriptor(this, NbBundle.getMessage(GetPropertyCustomizer.class, "LBL_Customizer_InsertPrefix") + " " + displayName, true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, new ActionListener() {   // NOI18N

            public void actionPerformed(ActionEvent e) {
                if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                    evaluateInput();
                    dialogOK = true;
                }
                dialog.dispose();
            }
        });

        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();

        return dialogOK;
    }

    private void addDocumentListener(JComboBox beanNameCb) {
        JTextComponent com = (JTextComponent) beanNameCb.getEditor().getEditorComponent();
        com.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent arg0) {
                updateProperties();
            }
            public void removeUpdate(DocumentEvent arg0) {
                updateProperties();
            }
            public void changedUpdate(DocumentEvent arg0) {
                updateProperties();
            }
        });
    }

    private void evaluateInput() {
        int beanIndex = beanNameCb.getSelectedIndex();
        getProperty.setBeanIndex(beanIndex);
        if (beanIndex == -1) {
            // new or no value selected
            Object item = beanNameCb.getSelectedItem();
            if (item != null) {
                getProperty.setBean(item.toString());
            }
        }
        String property = ((JTextComponent) (propertyNameCb.getEditor().getEditorComponent())).getText();
        getProperty.setProperty(property);
    }

    private void updateProperties() {
        List<GetProperty.BeanDescr> beans = getProperty.getAllBeans();
        GetProperty.BeanDescr currentBean = null;
        for (Iterator<GetProperty.BeanDescr> it = beans.iterator(); it.hasNext();) {
            GetProperty.BeanDescr beanDescr = it.next();
            final String item = ((JTextComponent) (beanNameCb.getEditor().getEditorComponent())).getText();
            if (beanDescr.getId().equals(item)) {
                currentBean = beanDescr;
                break;
            }
        }

        if (currentBean == null) {
            propertyNameCb.setModel(new DefaultComboBoxModel());
        } else {
            String[] pref = {"get", "is"};  // NOI18N
            propertyNameCb.setModel(new DefaultComboBoxModel(JspPaletteUtilities.getTypeProperties(target, currentBean.getFqcn(), pref).toArray()));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        beanNameCb = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        propertyNameCb = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(beanNameCb);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GetPropertyCustomizer.class, "LBL_GetProperty_Bean")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GetPropertyCustomizer.class, "ACSN_GetProperty_Bean")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetPropertyCustomizer.class, "ACSD_GetProperty_Bean")); // NOI18N

        beanNameCb.setEditable(true);
        beanNameCb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                beanNameCbActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(beanNameCb, gridBagConstraints);

        jLabel3.setLabelFor(propertyNameCb);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(GetPropertyCustomizer.class, "LBL_GetProperty_Property")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 0);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GetPropertyCustomizer.class, "ACSN_GetProperty_Property")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetPropertyCustomizer.class, "ACSD_GetProperty_Property")); // NOI18N

        propertyNameCb.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(propertyNameCb, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GetPropertyCustomizer.class, "ACSD_GetProperty_Dialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void beanNameCbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_beanNameCbActionPerformed
        updateProperties();
}//GEN-LAST:event_beanNameCbActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox beanNameCb;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JComboBox propertyNameCb;
    // End of variables declaration//GEN-END:variables
}
