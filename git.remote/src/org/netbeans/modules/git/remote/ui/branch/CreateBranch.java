/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.remote.ui.branch;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class CreateBranch implements DocumentListener {
    private final CreateBranchPanel panel;
    private final RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;
    private boolean revisionValid = false;
    private String msgInvalidName;
    private String branchName;
    private final Icon ICON_ERROR = new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/remote/resources/icons/info.png")); //NOI18N
    private final Map<String, GitBranch> existingBranches;
    private final Set<String> localBranchNames;
    private boolean internalChange;
    private boolean nameModifiedByUser;

    CreateBranch (VCSFileProxy repository, String initialRevision, Map<String, GitBranch> existingBranches) {
        this.existingBranches = existingBranches;
        this.localBranchNames = getLocalBranches(existingBranches);
        revisionPicker = new RevisionDialogController(repository, new VCSFileProxy[] { repository }, initialRevision);
        panel = new CreateBranchPanel(revisionPicker.getPanel());
    }

    String getRevision () {
        return revisionPicker.getRevision().getRevision();
    }
    
    String getBranchName () {
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
                new HelpCtx("org.netbeans.modules.git.remote.ui.branch.CreateBranch"), null); //NOI18N
        validate();
        revisionPicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                    setRevisionValid(Boolean.TRUE.equals(evt.getNewValue()));
                }
            }
        });
        panel.branchNameField.getDocument().addDocumentListener(this);
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
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
                    String localBranch = revision.substring(revision.indexOf('/') + 1); //NOI18N
                    panel.branchNameField.setText(localBranch);
                }
            }
            internalChange = false;
        }
    }
}
