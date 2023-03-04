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

package org.netbeans.modules.git.ui.branch;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import javax.swing.JButton;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.ui.repository.SelectBranchPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@NbBundle.Messages({
    "# {0} - branch name", "SelectTrackedBranchPanel.error.noselection=Select tracked branch for branch \"{0}\"",
    "# {0} - branch name", "SelectTrackedBranchPanel.error.samerevision=Select branch other than \"{0}\"",
    "# {0} - branch name", "# {1} - tracked branch name", "SelectTrackedBranchPanel.info=Branch \"{0}\" will start tracking branch \"{1}\""
})
public final class SelectTrackedBranch {
    private final SelectTrackedBranchPanel panel;
    private final String branchName;
    protected final SelectBranchPanel selectBranchPanel;
    private final RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;

    public SelectTrackedBranch (File repository, String branchName, String preselectedBranch) {
        this.revisionPicker = new RevisionDialogController(repository, new File[0],
                Collections.<String, GitBranch>emptyMap(), preselectedBranch);
        this.branchName = branchName;
        selectBranchPanel = new SelectBranchPanel(revisionPicker.getPanel());
        panel = new SelectTrackedBranchPanel(selectBranchPanel);
        panel.errorLabel.setText(Bundle.SelectTrackedBranchPanel_error_noselection(branchName));
    }

    public String getSelectedBranch () {
        return revisionPicker.getRevision().getRevision();
    }
    
    @NbBundle.Messages({
        "BranchSelector.okButton.text=&OK",
        "BranchSelector.title=Select Branch"
    })
    public boolean open () {
        okButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(okButton, Bundle.BranchSelector_okButton_text());
        dd = new DialogDescriptor(panel, Bundle.BranchSelector_title(), true,
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton,
                DialogDescriptor.DEFAULT_ALIGN, null, null);
        revisionPicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                    boolean valid = Boolean.TRUE.equals(evt.getNewValue());
                    if (!valid) {
                        panel.errorLabel.setText(Bundle.SelectTrackedBranchPanel_error_noselection(branchName));
                    } else if (branchName.equals(revisionPicker.getRevision().getRevision())) {
                        valid = false;
                        panel.errorLabel.setText(Bundle.SelectTrackedBranchPanel_error_samerevision(branchName));
                    }
                    if (valid) {
                        panel.errorLabel.setText(Bundle.SelectTrackedBranchPanel_info(branchName, revisionPicker.getRevision().getRevision()));
                    }
                    setRevisionValid(valid);
                } else if (evt.getPropertyName() == RevisionDialogController.PROP_REVISION_ACCEPTED) {
                    if (dd.isValid()) {
                        okButton.doClick();
                    }
                }
            }
        });
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        okButton.setEnabled(false);
        d.setVisible(true);
        return okButton == dd.getValue();
    }
    
    private void setRevisionValid (boolean flag) {
        okButton.setEnabled(flag);
        dd.setValid(flag);
    }

}
