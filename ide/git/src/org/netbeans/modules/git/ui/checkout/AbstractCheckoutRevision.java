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

package org.netbeans.modules.git.ui.checkout;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author ondra
 */
public abstract class AbstractCheckoutRevision implements DocumentListener, ActionListener, PropertyChangeListener {
    protected final CheckoutRevisionPanel panel;
    private final RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;
    private boolean revisionValid = false;
    private String msgInvalidName;
    private boolean branchNameRecommended = true;
    private String branchName;
    private final Map<String, GitBranch> branches;
    private final Icon ICON_ERROR = org.openide.util.ImageUtilities.loadIcon("org/netbeans/modules/git/resources/icons/info.png"); //NOI18N
    private boolean autoSelectedCreateBranch = true;

    protected AbstractCheckoutRevision (RepositoryInfo info, RevisionDialogController revisionPicker) {
        this.revisionPicker = revisionPicker;
        panel = new CheckoutRevisionPanel(revisionPicker.getPanel());
        info.addPropertyChangeListener(WeakListeners.propertyChange(this, info));
        this.branches = info.getBranches();
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
    
    boolean isCreateBranchSelected () {
        return panel.cbCheckoutAsNewBranch.isSelected();
    }
    
    protected abstract String getOkButtonLabel ();
    
    protected abstract String getDialogTitle ();

    boolean show (HelpCtx helpCtx) {
        okButton = new JButton(getOkButtonLabel());
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        dd = new DialogDescriptor(panel, getDialogTitle(), true, new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, helpCtx, null);
        validateBranchCB();
        revisionPicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                    setRevisionValid(Boolean.TRUE.equals(evt.getNewValue()));
                } else if (evt.getPropertyName() == RevisionDialogController.PROP_REVISION_ACCEPTED) {
                    if (dd.isValid()) {
                        okButton.doClick();
                    }
                }
            }
        });
        panel.branchNameField.getDocument().addDocumentListener(this);
        panel.cbCheckoutAsNewBranch.addActionListener(this);
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        return okButton == dd.getValue();
    }
    
    private void setRevisionValid (boolean flag) {
        this.revisionValid = flag;
        if (flag) {
            validateBranchCB();
        } else {
            setErrorMessage(NbBundle.getMessage(AbstractCheckoutRevision.class, "MSG_CheckoutRevision.errorRevision")); //NOI18N
            validate();
        }
    }

    private void validate () {
        boolean flag = revisionValid;
        boolean messageSet = false;
        if (flag) {
            if (panel.cbCheckoutAsNewBranch.isSelected() && msgInvalidName != null) {
                setErrorMessage(msgInvalidName);
                flag = false;
                messageSet = true;
            } else if (!panel.cbCheckoutAsNewBranch.isSelected() && branchNameRecommended) {
                setErrorMessage(NbBundle.getMessage(AbstractCheckoutRevision.class, "MSG_CheckoutRevision.warningDetachedHead")); //NOI18N
                messageSet = true;
            }
        }
        if (!messageSet) {
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

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.cbCheckoutAsNewBranch) {
            autoSelectedCreateBranch = false;
            panel.branchNameField.setEnabled(panel.cbCheckoutAsNewBranch.isSelected());
            //#229555: automatically fill in local branch name based on the remote branch name
            validateBranchCB();
            validate();
        }
    }

    @NbBundle.Messages({
        "MSG_CheckoutRevision.errorBranchNameEmpty=No branch name entered",
        "MSG_CheckoutRevision.errorInvalidBranchName=Invalid branch name",
        "MSG_CheckoutRevision.errorBranchExists=A branch with the given name already exists",
        "# {0} - branch name",
        "MSG_CheckoutRevision.errorParentExists=Cannot create branch under already existing \"{0}\""
    })
    private void validateName () {
        msgInvalidName = null;
        branchName = getBranchName();
        if (branchName.isEmpty()) {
            msgInvalidName = Bundle.MSG_CheckoutRevision_errorBranchNameEmpty();
        } else if (!GitUtils.isValidBranchName(branchName)) {
            msgInvalidName = Bundle.MSG_CheckoutRevision_errorInvalidBranchName();
        } else if (branches.containsKey(branchName)) {
            msgInvalidName = Bundle.MSG_CheckoutRevision_errorBranchExists();
        } else {
            for (String branch : branches.keySet()) {
                if (branchName.startsWith(branch + "/") || branch.startsWith(branchName + "/")) {
                    msgInvalidName = Bundle.MSG_CheckoutRevision_errorParentExists(branch);
                    break;
                }
            }
        }
        validate();
    }

    private void validateBranchCB () {
        String rev = revisionPicker.getRevision().getRevision();
        if (rev.startsWith(GitUtils.PREFIX_R_HEADS)) {
            rev = rev.substring(GitUtils.PREFIX_R_HEADS.length());
        } else if (rev.startsWith(GitUtils.PREFIX_R_REMOTES)) {
            rev = rev.substring(GitUtils.PREFIX_R_REMOTES.length());
        } else if (rev.startsWith("remotes/")) { //NOI18N
            rev = rev.substring(8);
        }
        GitBranch b = branches.get(rev);
        if (b != null && !b.isRemote()) {
            branchNameRecommended = false;
        } else {
            branchNameRecommended = true;
        }
        
        if (b != null) {
            if (b.isRemote()) {
                if (autoSelectedCreateBranch) {
                    panel.cbCheckoutAsNewBranch.setSelected(true);
                    panel.branchNameField.setEnabled(true);
                }
                //#229555: automatically fill in local branch name based on the remote branch name
                if (panel.cbCheckoutAsNewBranch.isSelected()) {
                    //extract "branch_X" from "origin/branch_X" to be the default local branch name
                    final String localBranch = rev.substring(rev.indexOf("/")+1);
                    final boolean localBranchExists = branches.containsKey(localBranch);
                    if (localBranchExists) {
                        panel.branchNameField.setText("");
                    } else {
                        panel.branchNameField.setText(localBranch);
                    }
                }
            } else if (autoSelectedCreateBranch) {
                panel.cbCheckoutAsNewBranch.setSelected(false);
                panel.branchNameField.setEnabled(false);
                panel.branchNameField.setText("");
            }
        }
        
        validate();
    }

    @Override
    public void propertyChange (final PropertyChangeEvent evt) {
        if (RepositoryInfo.PROPERTY_BRANCHES.equals(evt.getPropertyName())) {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                @SuppressWarnings("unchecked")
                public void run () {
                    branches.clear();
                    branches.putAll((Map<String, GitBranch>) evt.getNewValue());
                    validateName();
                    validateBranchCB();
                }
            });
        }
    }

    private void setErrorMessage (String message) {
        panel.lblError.setText(message);
        if (message == null || message.isEmpty()) {
            panel.lblError.setIcon(null);
        } else {
            panel.lblError.setIcon(ICON_ERROR);
        }
    }
}
