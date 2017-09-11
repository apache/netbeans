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
