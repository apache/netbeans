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

package org.netbeans.modules.git.ui.branch;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;

/**
 *
 * @author ondra
 */
public class CreateBranch implements DocumentListener {
    private final CreateBranchPanel panel;
    private final RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;
    private boolean revisionValid = false;
    private String msgInvalidName;
    private String branchName;
    private final Icon ICON_ERROR = org.openide.util.ImageUtilities.loadIcon("org/netbeans/modules/git/resources/icons/info.png"); //NOI18N
    private final Map<String, GitBranch> existingBranches;
    private final Set<String> localBranchNames;
    private boolean internalChange;
    private boolean nameModifiedByUser;

    CreateBranch (File repository, String initialRevision, Map<String, GitBranch> existingBranches) {
        this.existingBranches = existingBranches;
        this.localBranchNames = getLocalBranches(existingBranches);
        revisionPicker = new RevisionDialogController(repository, new File[] { repository }, initialRevision);
        panel = new CreateBranchPanel(revisionPicker.getPanel());
    }

    String getRevision () {
        return revisionPicker.getRevision().getRevision();
    }
    
    String getBranchName () {
        if (GitModuleConfig.getDefault().getAutoReplaceInvalidBranchNameCharacters()) {
            return GitUtils.normalizeBranchName(panel.branchNameField.getText());
        }
        return panel.branchNameField.getText().trim();
    }

    boolean isCheckoutSelected () {
        return panel.cbCheckoutBranch.isSelected();
    }

    boolean show() {
        okButton = new JButton(NbBundle.getMessage(CreateBranch.class, "LBL_CreateBranch.OKButton.text")); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        dd = new DialogDescriptor(panel, NbBundle.getMessage(CreateBranch.class, "LBL_CreateBranch.title"), true,  //NOI18N
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx("org.netbeans.modules.git.ui.branch.CreateBranch"), null); //NOI18N
        validate();
        revisionPicker.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                setRevisionValid(Boolean.TRUE.equals(evt.getNewValue()));
            }
        });
        panel.branchNameField.getDocument().addDocumentListener(this);
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        DialogBoundsPreserver.preserveAndRestore(d, GitModuleConfig.getDefault().getPreferences(), this.getClass().getName());
        d.setVisible(true);
        return okButton == dd.getValue();
    }
    
    private void setRevisionValid (boolean flag) {
        this.revisionValid = flag;
        updateBranchName();
        if (!flag) {
            setErrorMessage(NbBundle.getMessage(CreateBranch.class, "MSG_CreateBranch.errorRevision")); //NOI18N
        }
        validate();
    }

    private void validate () {
        boolean flag = revisionValid && msgInvalidName == null;
        if (revisionValid && msgInvalidName != null) {
            setErrorMessage(msgInvalidName);
        }
        if (flag) {
            setErrorMessage(null);
        }
        okButton.setEnabled(flag);
        dd.setValid(flag);
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        validateName();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        validateName();
    }

    @Override
    public void changedUpdate (DocumentEvent e) { }

    @NbBundle.Messages({
        "MSG_CreateBranch.errorBranchNameEmpty=Branch name cannot be empty",
        "MSG_CreateBranch.errorInvalidBranchName=Invalid branch name",
        "MSG_CreateBranch.errorBranchExists=A branch with the given name already exists.",
        "# {0} - branch name",
        "MSG_CreateBranch.errorParentExists=Cannot create branch under already existing \"{0}\""
    })
    private void validateName () {
        if (!internalChange) {
            nameModifiedByUser = true;
        }
        msgInvalidName = null;
        branchName = getBranchName();
        if (branchName.isEmpty()) {
            msgInvalidName = Bundle.MSG_CreateBranch_errorBranchNameEmpty();
        } else if (!GitUtils.isValidBranchName(branchName)) {
            msgInvalidName = Bundle.MSG_CreateBranch_errorInvalidBranchName();
        } else if (localBranchNames.contains(branchName)) {
            msgInvalidName = Bundle.MSG_CreateBranch_errorBranchExists();
        } else {
            for (String branch : localBranchNames) {
                if (branchName.startsWith(branch + "/") || branch.startsWith(branchName + "/")) {
                    msgInvalidName = Bundle.MSG_CreateBranch_errorParentExists(branch);
                    break;
                }
            }
        }
        validate();
    }

    private void setErrorMessage (String message) {
        panel.lblError.setText(message);
        if (message == null || message.isEmpty()) {
            panel.lblError.setIcon(null);
        } else {
            panel.lblError.setIcon(ICON_ERROR);
        }
    }

    private static Set<String> getLocalBranches (Map<String, GitBranch> existingBranches) {
        Set<String> branchNames = new HashSet<>();
        for (Map.Entry<String, GitBranch> e : existingBranches.entrySet()) {
            GitBranch branch = e.getValue();
            if (!branch.isRemote() && !GitBranch.NO_BRANCH.equals(branch.getName())) {
                branchNames.add(e.getKey());
            }
        }
        return branchNames;
    }

    private void updateBranchName () {
        if (!nameModifiedByUser) {
            internalChange = true;
            String revision = revisionPicker.getRevision().getRevision();
            if (revision.startsWith(GitUtils.PREFIX_R_REMOTES)) {
                revision = revision.substring(GitUtils.PREFIX_R_REMOTES.length());
            } else if (revision.startsWith("remotes/")) { //NOI18N
                revision = revision.substring(8);
            }
            for (Map.Entry<String, GitBranch> e : existingBranches.entrySet()) {
                if (e.getValue().isRemote() && e.getKey().equals(revision)) {
                    // selected revision is a remote branch
                    // offer the usual local branch name
                    String localBranch = revision.substring(revision.indexOf("/") + 1); //NOI18N
                    panel.branchNameField.setText(localBranch);
                }
            }
            internalChange = false;
        }
    }
}
