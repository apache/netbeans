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

package org.netbeans.modules.projectimport.eclipse.core;

import java.awt.Dialog;
import java.io.File;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.ProjectInformation;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;

/**
 *
 */
class UpdateEclipseReferencePanel extends javax.swing.JPanel implements DocumentListener {

    private DialogDescriptor dd;
    private String projName;
    
    /** Creates new form UpdateEclipseReferencePanel */
    private UpdateEclipseReferencePanel(EclipseProjectReference reference) {
        projName = reference.getProject().getLookup().lookup(ProjectInformation.class).getDisplayName();
        initComponents();
        eclipseProjectTextField.setText(reference.getEclipseProjectLocation().getPath());
        boolean enabled = !(reference.getEclipseProjectLocation().exists() && EclipseUtils.isRegularProject(reference.getEclipseProjectLocation()));
        eclipseProjectTextField.setEnabled(enabled);
        browseProjectButton.setEnabled(enabled);
        enabled = !(reference.getEclipseWorkspaceLocation().exists() && EclipseUtils.isRegularWorkSpace(reference.getEclipseWorkspaceLocation()));
        eclipseWorkspaceTextField.setText(reference.getEclipseWorkspaceLocation().getPath());
        eclipseWorkspaceTextField.setEnabled(enabled);
        browseWorkspaceButton.setEnabled(enabled);
    }

    public void setDialogDescriptor(DialogDescriptor dd) {
        this.dd = dd;
        eclipseProjectTextField.getDocument().addDocumentListener(this);
        eclipseWorkspaceTextField.getDocument().addDocumentListener(this);
        updateStatus();
    }

    private void updateStatus() {
        String errorMsg = null;
        if (eclipseProjectTextField.isEnabled()) {
            if (!EclipseUtils.isRegularProject(eclipseProjectTextField.getText())) {
                errorMsg = org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "MSG_SelectProject");
            }
        }
        if (errorMsg == null && eclipseWorkspaceTextField.isEnabled()) {
            String d = eclipseWorkspaceTextField.getText();
            if (d == null || d.trim().length() == 0) {
                errorMsg = org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "MSG_SelectWorkspace");
            } else {
                if (!EclipseUtils.isRegularWorkSpace(FileUtil.normalizeFile(new File(d.trim())))) {
                    errorMsg = org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "MSG_SelectWorkspace");
                }
            }
        }
        dd.setValid(errorMsg == null);
        error.setText(errorMsg == null ? " " : errorMsg); //NOI18N
    }


    public static boolean showEclipseReferenceResolver(@NonNull EclipseProjectReference ref, @NonNull Map<String,String> resolvedEntries) {
        File workspace = ref.getEclipseWorkspaceLocation();
        if (workspace != null && !workspace.exists() && resolvedEntries.get(workspace.getPath()) != null) {
            ref.updateReference(null, resolvedEntries.get(workspace.getPath()));
        }
        if (!ref.getEclipseProjectLocation().exists() && resolvedEntries.get(ref.getEclipseProjectLocation().getParent()) != null) {
            File f = new File(resolvedEntries.get(ref.getEclipseProjectLocation().getParent()));
            f = new File(f, ref.getEclipseProjectLocation().getName());
            if (f.exists()) {
                ref.updateReference(f.getPath(), null);
            }
        }
        if (ref.isEclipseProjectReachable()) {
            return true;
        }
        UpdateEclipseReferencePanel p = new UpdateEclipseReferencePanel(ref);
        DialogDescriptor dd = new DialogDescriptor (p, org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "TITLE_Synchronize_with_Eclipse"),
            true, DialogDescriptor.OK_CANCEL_OPTION, null, null);
        p.setDialogDescriptor(dd);
        Dialog dlg = DialogDisplayer.getDefault().createDialog (dd);
        dlg.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            if (p.eclipseProjectTextField.isEnabled()) {
                resolvedEntries.put(ref.getEclipseProjectLocation().getParent(), p.eclipseProjectTextField.getText());
            }
            if (workspace != null && p.eclipseWorkspaceTextField.isEnabled()) {
                resolvedEntries.put(workspace.getPath(), p.eclipseWorkspaceTextField.getText());
            }
            ref.updateReference(
                    p.eclipseProjectTextField.isEnabled() ? p.eclipseProjectTextField.getText() : null,
                    p.eclipseWorkspaceTextField.isEnabled() ? p.eclipseWorkspaceTextField.getText() : null);
            return true;
        }
        return false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        eclipseProjectTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        eclipseWorkspaceTextField = new javax.swing.JTextField();
        browseProjectButton = new javax.swing.JButton();
        browseWorkspaceButton = new javax.swing.JButton();
        error = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        jLabel1.setLabelFor(eclipseProjectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.jLabel1.text")); // NOI18N

        eclipseProjectTextField.setText(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.eclipseProjectTextField.text")); // NOI18N

        jLabel2.setLabelFor(eclipseWorkspaceTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.jLabel2.text")); // NOI18N

        eclipseWorkspaceTextField.setText(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.eclipseWorkspaceTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseProjectButton, org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.browseProjectButton.text")); // NOI18N
        browseProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(browseWorkspaceButton, org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.browseWorkspaceButton.text")); // NOI18N
        browseWorkspaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseWorkspaceButtonActionPerformed(evt);
            }
        });

        error.setForeground(java.awt.Color.red);
        org.openide.awt.Mnemonics.setLocalizedText(error, " ");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.jLabel3.text", projName)); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "UpdateEclipseReferencePanel.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(eclipseProjectTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseProjectButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(eclipseWorkspaceTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseWorkspaceButton))))
                    .addComponent(error, javax.swing.GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(eclipseProjectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseProjectButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eclipseWorkspaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(browseWorkspaceButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(error)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "ACSD_UpdateEclipseReferencePanel_NA")); // NOI18N
        eclipseProjectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "ACSD_UpdateEclipseReferencePanel_NA")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "ACSD_UpdateEclipseReferencePanel_NA")); // NOI18N
        eclipseWorkspaceTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "ACSD_UpdateEclipseReferencePanel_NA")); // NOI18N
        browseProjectButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "ACSD_UpdateEclipseReferencePanel_NA")); // NOI18N
        browseWorkspaceButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "ACSD_UpdateEclipseReferencePanel_NA")); // NOI18N
        error.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "ACSD_UpdateEclipseReferencePanel_NA")); // NOI18N
        error.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "ACSD_UpdateEclipseReferencePanel_NA")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "ACSD_UpdateEclipseReferencePanel_NA")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "ACSD_UpdateEclipseReferencePanel_NA")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "ACSD_UpdateEclipseReferencePanel_NA")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "ACSD_UpdateEclipseReferencePanel_NA")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void browseProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectButtonActionPerformed
    JFileChooser chooser = new JFileChooser();
    FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
    chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    chooser.setDialogTitle(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "TITLE_Select_Eclipse_Project"));
    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        File file = FileUtil.normalizeFile(chooser.getSelectedFile());
        eclipseProjectTextField.setText(file.getAbsolutePath());
    }
}//GEN-LAST:event_browseProjectButtonActionPerformed

private void browseWorkspaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseWorkspaceButtonActionPerformed
    JFileChooser chooser = new JFileChooser();
    FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
    chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    chooser.setDialogTitle(org.openide.util.NbBundle.getMessage(UpdateEclipseReferencePanel.class, "TITLE_Select_Eclipse_Workspace"));
    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        File file = FileUtil.normalizeFile(chooser.getSelectedFile());
        eclipseWorkspaceTextField.setText(file.getAbsolutePath());
    }
}//GEN-LAST:event_browseWorkspaceButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseProjectButton;
    private javax.swing.JButton browseWorkspaceButton;
    private javax.swing.JTextField eclipseProjectTextField;
    private javax.swing.JTextField eclipseWorkspaceTextField;
    private javax.swing.JLabel error;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    // End of variables declaration//GEN-END:variables

    public void insertUpdate(DocumentEvent arg0) {
        updateStatus();
    }

    public void removeUpdate(DocumentEvent arg0) {
        updateStatus();
    }

    public void changedUpdate(DocumentEvent arg0) {
        
    }

}
