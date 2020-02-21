/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git.remote.ui.stash;

import java.awt.Dialog;
import java.awt.EventQueue;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.ui.shelve.ShelveChangesAction;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 */
@NbBundle.Messages({
    "CTL_SaveStash.okButton.text=&Stash Changes",
    "CTL_SaveStash.shelveButton.text=Shel&ve Changes",
    "MSG_SaveStash.emptyMessage=No stash message",
    "MSG_SaveStash.shelveMessage=You may shelve changes instead using git stash.",
    "# {0} - repository name", "LBL_SaveStash.title=Stash Changes [{0}]",
    "# {0} - branch name", "# {1} - branch commit id", "MSG_SaveStash.defaultMessage=WIP on {0}: {1} - MESSAGE"
})
class SaveStash implements DocumentListener {
    private final SaveStashPanel panel;
    private JButton okButton;
    private DialogDescriptor dd;
    private final VCSFileProxy repository;
    private final Icon ICON_INFO = new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/remote/resources/icons/info.png")); //NOI18N
    private final VCSFileProxy[] roots;

    SaveStash (VCSFileProxy repository, VCSFileProxy[] roots, GitBranch activeBranch) {
        this.repository = repository;
        this.roots = roots;
        panel = new SaveStashPanel();
        panel.txtMessage.setText(Bundle.MSG_SaveStash_defaultMessage(activeBranch.getName(), activeBranch.getId().substring(0, 7)));
    }

    String getMessage () {
        return panel.txtMessage.getText().trim();
    }
    
    boolean isIncludeUncommitted () {
        return panel.cbIncludeUncommitted.isSelected();
    }

    boolean show () {
        okButton = new JButton(Bundle.CTL_SaveStash_okButton_text());
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        JButton shelveButton = new JButton(Bundle.CTL_SaveStash_shelveButton_text());
        org.openide.awt.Mnemonics.setLocalizedText(shelveButton, shelveButton.getText());
        Object[] buttons;
        if (roots.length == 0) {
            buttons = new Object[] { okButton, DialogDescriptor.CANCEL_OPTION };
        } else {
            buttons = new Object[] { okButton, shelveButton, DialogDescriptor.CANCEL_OPTION };
        }
        dd = new DialogDescriptor(panel, Bundle.LBL_SaveStash_title(repository.getName()), true,
                buttons, okButton, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx("org.netbeans.modules.git.remote.ui.stash.SaveStash"), //NOI18N
                null);
        validate();
        panel.txtMessage.getDocument().addDocumentListener(this);
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        if (shelveButton == dd.getValue()) {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run () {
                    SystemAction.get(ShelveChangesAction.class).shelve(repository, roots);
                }
            });
        }
        return okButton == dd.getValue();
    }
    
    private void validate () {
        boolean flag = true;
        if (panel.txtMessage.getText().trim().isEmpty()) {
            setInfoMessage(Bundle.MSG_SaveStash_emptyMessage());
            flag = false;
        } else if (roots.length > 0) {
            setInfoMessage(Bundle.MSG_SaveStash_shelveMessage());
        } else {
            setInfoMessage(null);
        }
        okButton.setEnabled(flag);
        dd.setValid(flag);
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        validate();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        validate();
    }

    @Override
    public void changedUpdate (DocumentEvent e) { }

    private void setInfoMessage (String message) {
        panel.lblInfo.setText(message);
        if (message == null || message.isEmpty()) {
            panel.lblInfo.setIcon(null);
        } else {
            panel.lblInfo.setIcon(ICON_INFO);
        }
    }    
}
