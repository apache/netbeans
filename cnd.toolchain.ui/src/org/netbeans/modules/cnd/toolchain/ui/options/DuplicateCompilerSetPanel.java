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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.toolchain.ui.options;

import java.awt.Dimension;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.toolchain.support.ToolchainUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

/**
 *
 */
/*package-local*/ final class DuplicateCompilerSetPanel extends javax.swing.JPanel implements DocumentListener {
    private DialogDescriptor dialogDescriptor = null;
    private CompilerSetManager csm;
    
    /** Creates new form AddCompilerSetPanel */
    public DuplicateCompilerSetPanel(CompilerSetManager csm, CompilerSet cs) {
        initComponents();
        this.csm = csm;
        
        List<CompilerFlavor> list = CompilerFlavor.getFlavors(csm.getPlatform());
        for (CompilerFlavor cf : list) {
            cbFamily.addItem(cf);
        }
        tfBaseDirectory.setText(cs.getDirectory());
        cbFamily.setSelectedItem(cs.getCompilerFlavor());
        updateDataFamily();
                
        taInfo.setBackground(getBackground());
        validateData();
        
        setPreferredSize(new Dimension(600, 300));
        
        tfName.getDocument().addDocumentListener(this);
    }

    private static String getString(String key) {
        return NbBundle.getMessage(DuplicateCompilerSetPanel.class, key);
    }

    public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
        this.dialogDescriptor = dialogDescriptor;
        //dialogDescriptor.setValid(false);
        validateData();
    }
    
    private void updateDataFamily() {
        CompilerFlavor flavor = (CompilerFlavor)cbFamily.getSelectedItem();
        String suggestedName = ToolchainUtilities.getUniqueCompilerSetName(csm, flavor.toString());
        tfName.setText(suggestedName);
        updateDataName();
    }
    
    private void updateDataName() {
        validateData();
    }
    
    private void validateData() {
        boolean valid = true;
        lbError.setText(""); // NOI18N
        
        String compilerSetName = CndPathUtilities.replaceOddCharacters(tfName.getText().trim(), '_');
        if (valid && compilerSetName.length() == 0 || compilerSetName.contains("|")) { // NOI18N
            valid = false;
            lbError.setText(getString("NAME_INVALID"));
        }
        
        if (valid && csm.getCompilerSet(compilerSetName.trim()) != null) {
            valid = false;
            lbError.setText(getString("TOOLNAME_ALREADY_EXISTS"));
        }
        
        if (dialogDescriptor != null) {
            dialogDescriptor.setValid(valid);
        }
    }
    
    private void handleUpdate(DocumentEvent e) {
        updateDataName();
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        handleUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        handleUpdate(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        //validateData();
    }
    
    public String getBaseDirectory() {
        return tfBaseDirectory.getText();
    }
    
    public CompilerFlavor getFamily() {
        return (CompilerFlavor)cbFamily.getSelectedItem();
    }
    
    public String getCompilerSetName() {
        return CndPathUtilities.replaceOddCharacters(tfName.getText().trim(), '_');
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lbBaseDirectory = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        lbFamily = new javax.swing.JLabel();
        cbFamily = new javax.swing.JComboBox();
        lbName = new javax.swing.JLabel();
        tfBaseDirectory = new javax.swing.JTextField();
        lbError = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taInfo = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        lbBaseDirectory.setLabelFor(tfBaseDirectory);
        org.openide.awt.Mnemonics.setLocalizedText(lbBaseDirectory, org.openide.util.NbBundle.getMessage(DuplicateCompilerSetPanel.class, "AddCompilerSetPanel.lbBaseDirectory.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 16, 0, 0);
        add(lbBaseDirectory, gridBagConstraints);

        tfName.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 16, 16);
        add(tfName, gridBagConstraints);
        tfName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DuplicateCompilerSetPanel.class, "AddCompilerSetPanel.tfName.AccessibleContext.accessibleDescription")); // NOI18N

        lbFamily.setLabelFor(cbFamily);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lbFamily, bundle.getString("AddCompilerSetPanel.lbFamily.text")); // NOI18N
        lbFamily.setToolTipText(bundle.getString("AddCompilerSetPanel.lbFamily.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 16, 0, 0);
        add(lbFamily, gridBagConstraints);

        cbFamily.setEnabled(false);
        cbFamily.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbFamilyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 16);
        add(cbFamily, gridBagConstraints);

        lbName.setLabelFor(tfName);
        org.openide.awt.Mnemonics.setLocalizedText(lbName, org.openide.util.NbBundle.getMessage(DuplicateCompilerSetPanel.class, "AddCompilerSetPanel.lbName.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(9, 16, 16, 0);
        add(lbName, gridBagConstraints);

        tfBaseDirectory.setColumns(40);
        tfBaseDirectory.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(16, 4, 0, 16);
        add(tfBaseDirectory, gridBagConstraints);
        tfBaseDirectory.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DuplicateCompilerSetPanel.class, "AddCompilerSetPanel.tfBaseDirectory.AccessibleContext.accessibleDescription")); // NOI18N

        lbError.setForeground(new java.awt.Color(255, 51, 51));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 16, 16);
        add(lbError, gridBagConstraints);

        jScrollPane1.setBorder(null);

        taInfo.setColumns(20);
        taInfo.setEditable(false);
        taInfo.setLineWrap(true);
        taInfo.setText(org.openide.util.NbBundle.getMessage(DuplicateCompilerSetPanel.class, "CopyCompilerSetPanel.taInfo.text")); // NOI18N
        taInfo.setWrapStyleWord(true);
        taInfo.setBorder(null);
        jScrollPane1.setViewportView(taInfo);
        taInfo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DuplicateCompilerSetPanel.class, "AddCompilerSetPanel.taInfo.AccessibleContext.accessibleName")); // NOI18N
        taInfo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DuplicateCompilerSetPanel.class, "AddCompilerSetPanel.taInfo.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(16, 16, 0, 16);
        add(jScrollPane1, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DuplicateCompilerSetPanel.class, "AddCompilerSetPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void cbFamilyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbFamilyActionPerformed
    updateDataFamily();
}//GEN-LAST:event_cbFamilyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbFamily;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbBaseDirectory;
    private javax.swing.JLabel lbError;
    private javax.swing.JLabel lbFamily;
    private javax.swing.JLabel lbName;
    private javax.swing.JTextArea taInfo;
    private javax.swing.JTextField tfBaseDirectory;
    private javax.swing.JTextField tfName;
    // End of variables declaration//GEN-END:variables
}
