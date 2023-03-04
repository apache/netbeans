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
package org.netbeans.modules.git.ui.revert;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class RevertCommit implements ActionListener, DocumentListener {
    private RevertCommitPanel panel;
    private RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;
    private boolean valid = true;

    RevertCommit (File repository, File[] roots, String initialRevision) {
        revisionPicker = new RevisionDialogController(repository, roots, initialRevision);
        panel = new RevertCommitPanel(revisionPicker.getPanel());
        attachListeners();
    }

    String getRevision () {
        return revisionPicker.getRevision().getRevision();
    }

    String getMessage () {
        return panel.txtCommitMessage.getText().trim();
    }

    boolean isCommitEnabled () {
        return panel.cbCommit.isSelected();
    }

    boolean show() {
        okButton = new JButton(NbBundle.getMessage(RevertCommit.class, "LBL_RevertCommit.OKButton.text")); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        dd = new DialogDescriptor(panel, NbBundle.getMessage(RevertCommit.class, "LBL_RevertCommit.title"), true,  //NOI18N
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(RevertCommit.class), null);
        enableRevisionPanel();
        revisionPicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                    setValid(Boolean.TRUE.equals(evt.getNewValue()));
                }
            }
        });
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        return okButton == dd.getValue();
    }

    private void enableRevisionPanel () {
        setValid(valid);
    }

    private void setValid (boolean flag) {
        this.valid = flag;
        okButton.setEnabled(flag);
        dd.setValid(flag);
    }

    private void attachListeners () {
        panel.cbCommit.addActionListener(this);
        panel.txtCommitMessage.getDocument().addDocumentListener(this);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.cbCommit) {
            panel.commitMessagePanel.setVisible(panel.cbCommit.isSelected());
        }
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        messageChanged();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        messageChanged();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
        messageChanged();
    }

    private void messageChanged () {
        panel.lblMessageWarning.setVisible(getMessage().isEmpty());
    }
}
