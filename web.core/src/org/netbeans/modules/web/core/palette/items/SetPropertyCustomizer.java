/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.openide.util.NbBundle;

/**
 *
 * @author  Libor Kotouc
 */
public class SetPropertyCustomizer extends javax.swing.JPanel {

    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private boolean dialogOK = false;
    SetProperty setProperty;
    JTextComponent target;
    
    public SetPropertyCustomizer(SetProperty setProperty, JTextComponent target) {
        this.setProperty = setProperty;
        this.target = target;

        initComponents();

        beanNameCb.setModel(new DefaultComboBoxModel(setProperty.getAllBeans().toArray()));
        beanNameCb.setSelectedIndex(setProperty.getBeanIndex());
        addDocumentListener(beanNameCb);
    }
            
    public boolean showDialog() {
        dialogOK = false;
        
        String displayName = "";  // NOI18N
        try {
            displayName = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.resources.Bundle").getString("NAME_jsp-SetProperty"); // NOI18N
        }
        catch (Exception e) {}
        
        descriptor = new DialogDescriptor
                (this, NbBundle.getMessage(SetPropertyCustomizer.class, "LBL_Customizer_InsertPrefix") + " " + displayName, true,  // NOI18N
                 DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                 new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                            evaluateInput();
                            dialogOK = true;
                        }
                        dialog.dispose();
		     }
		 } 
                );
        
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
    
    private void updateProperties() {
        List<GetProperty.BeanDescr> beans = setProperty.getAllBeans();
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
            String[] pref = {"set"};  // NOI18N
            propertyNameCb.setModel(new DefaultComboBoxModel(JspPaletteUtilities.getTypeProperties(target, currentBean.getFqcn(), pref).toArray()));
        }
    }
    
    private void evaluateInput() {
        int beanIndex = beanNameCb.getSelectedIndex();
        setProperty.setBeanIndex(beanIndex);
        if (beanIndex == -1) { // new or no value selected
            Object item = beanNameCb.getSelectedItem();
            if (item != null)
                setProperty.setBean(item.toString());
        }

        String property = ((JTextComponent) (propertyNameCb.getEditor().getEditorComponent())).getText();
        setProperty.setProperty(property);
        
        String value = propertyValueTf.getText();
        setProperty.setValue(value);
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
        jLabel4 = new javax.swing.JLabel();
        propertyValueTf = new javax.swing.JTextField();
        propertyNameCb = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(beanNameCb);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SetPropertyCustomizer.class, "LBL_SetProperty_Bean")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SetPropertyCustomizer.class, "ACSN_SetProperty_Bean")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SetPropertyCustomizer.class, "ACSD_SetProperty_Bean")); // NOI18N

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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SetPropertyCustomizer.class, "LBL_SetProperty_PropertyName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SetPropertyCustomizer.class, "ACSN_SetProperty_PropertyName")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SetPropertyCustomizer.class, "ACSD_SetProperty_PropertyName")); // NOI18N

        jLabel4.setLabelFor(propertyValueTf);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SetPropertyCustomizer.class, "LBL_SetProperty_PropertyValue")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 0);
        add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SetPropertyCustomizer.class, "ACSN_SetProperty_PropertyValue")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SetPropertyCustomizer.class, "ACSD_SetProperty_PropertyValue")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(propertyValueTf, gridBagConstraints);

        propertyNameCb.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(propertyNameCb, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SetPropertyCustomizer.class, "ACSD_SetProperty_Dialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void beanNameCbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_beanNameCbActionPerformed
        updateProperties();
}//GEN-LAST:event_beanNameCbActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox beanNameCb;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JComboBox propertyNameCb;
    private javax.swing.JTextField propertyValueTf;
    // End of variables declaration//GEN-END:variables
    
}
