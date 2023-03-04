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
package org.netbeans.modules.mercurial.ui.update;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import java.io.File;
import org.netbeans.modules.mercurial.ui.repository.ChangesetPickerPanel;

/**
 *
 * @author Padraig O'Briain
 */
public class RevertModifications implements PropertyChangeListener {

    private RevertModificationsPanel panel;
    private JButton okButton;
    private JButton cancelButton;
    private final File repository;
    
    /** Creates a new instance of RevertModifications */
    public RevertModifications(File repository, File[] files) {
        this (repository, files, null);
    }

    public RevertModifications(File repository, File[] files, String defaultRevision) {
        this.repository = repository;
        panel = new RevertModificationsPanel(repository, files);
        okButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(okButton, org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertForm_Action_Revert")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertForm_Action_Revert")); // NOI18N
        okButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSN_RevertForm_Action_Revert")); // NOI18N
        cancelButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertForm_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertForm_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSN_RevertForm_Action_Cancel")); // NOI18N
        okButton.setEnabled(false);
        panel.addPropertyChangeListener(this);
    } 
    
    public boolean showDialog() {
        File[] revertFiles = panel.getRootFiles();
        if (revertFiles == null) {
            revertFiles = new File[] { repository };
        }
        DialogDescriptor dialogDescriptor;

        String title;
        if (revertFiles.length == 1) {
            title = org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertDialog", revertFiles[0].getName()); // NOI18N
        } else {
            title = org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_MultiRevertDialog"); // NOI18N
        }
        dialogDescriptor =
            new DialogDescriptor(panel,
                title,
                true,
                new Object[] {okButton, cancelButton},
                okButton, 
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(this.getClass()),
                null);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        if (revertFiles.length == 1) {
            dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertDialog", revertFiles[0].getName())); // NOI18N
        } else {
            dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_MultiRevertDialog")); // NOI18N
        }
        dialog.setVisible(true);
        dialog.setResizable(false);
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;       
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ChangesetPickerPanel.PROP_VALID.equals(evt.getPropertyName()) && okButton != null) {
            boolean valid = ((Boolean)evt.getNewValue()).booleanValue();
            okButton.setEnabled(valid);
        }       
    }

    public String getSelectionRevision() {
        if (panel == null) return null;
        return panel.getSelectedRevisionCSetId();
    }
    
    public boolean isBackupRequested() {
        if (panel == null) return false;
        return panel.isBackupRequested();
    }

    boolean isRemoveNewFilesRequested () {
        if (panel == null) return false;
        return panel.isPurgeRequested();
    }

}
