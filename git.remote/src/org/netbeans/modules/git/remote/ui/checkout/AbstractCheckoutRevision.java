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

package org.netbeans.modules.git.remote.ui.checkout;

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
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
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
    private final Icon ICON_ERROR = new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/remote/resources/icons/info.png")); //NOI18N

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
        return panel.branchNameField.getText();
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
        branchName = panel.branchNameField.getText();
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
        
        //#229555: automatically fill in local branch name based on the remote branch name
        if (b != null && b.isRemote() && panel.cbCheckoutAsNewBranch.isSelected()) {
            //extract "branch_X" from "origin/branch_X" to be the default local branch name
            final String localBranch = rev.substring(rev.indexOf('/')+1);
            final boolean localBranchExists = branches.containsKey(localBranch);
            if (!localBranchExists) {
                panel.branchNameField.setText(localBranch);
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
