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
package org.netbeans.modules.git.ui.stash;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.ui.shelve.ShelveChangesAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
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
    private SaveStashPanel panel;
    private JButton okButton;
    private DialogDescriptor dd;
    private final File repository;
    private final Icon ICON_INFO = org.openide.util.ImageUtilities.loadIcon("org/netbeans/modules/git/resources/icons/info.png"); //NOI18N
    private final File[] roots;

    SaveStash (File repository, File[] roots, GitBranch activeBranch) {
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
                new HelpCtx("org.netbeans.modules.git.ui.stash.SaveStash"), //NOI18N
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
