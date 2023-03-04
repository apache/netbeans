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
package org.netbeans.modules.git.ui.tag;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitTag;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
class CreateTag implements DocumentListener, ActionListener {
    private CreateTagPanel panel;
    private RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;
    private boolean revisionValid = true;
    private Boolean nameValid = false;
    private final Task branchCheckTask;
    private String tagName;
    private final File repository;
    private final Icon ICON_INFO = org.openide.util.ImageUtilities.loadImageIcon("/org/netbeans/modules/git/resources/icons/info.png", false); //NOI18N

    CreateTag (File repository, String initialRevision, String initialTagName) {
        this.repository = repository;
        this.branchCheckTask = Git.getInstance().getRequestProcessor(repository).create(new TagNameCheckWorker());
        revisionPicker = new RevisionDialogController(repository, new File[] { repository }, initialRevision);
        panel = new CreateTagPanel(revisionPicker.getPanel());
        panel.tagNameField.setText(initialTagName);
    }

    String getRevision () {
        return revisionPicker.getRevision().getRevision();
    }
    
    String getTagName () {
        return panel.tagNameField.getText().trim();
    }
    
    String getTagMessage () {
        return panel.tagMessageField.getText().trim();
    }

    boolean show() {
        okButton = new JButton(NbBundle.getMessage(CreateTag.class, "LBL_CreateTag.OKButton.text")); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        dd = new DialogDescriptor(panel, NbBundle.getMessage(CreateTag.class, "LBL_CreateTag.title"), true,  //NOI18N
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(CreateTag.class), null);
        validate();
        revisionPicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                    setRevisionValid(Boolean.TRUE.equals(evt.getNewValue()));
                }
            }
        });
        panel.tagNameField.getDocument().addDocumentListener(this);
        panel.tagMessageField.getDocument().addDocumentListener(this);
        panel.cbForceUpdate.addActionListener(this);
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        return okButton == dd.getValue();
    }
    
    private void setRevisionValid (boolean flag) {
        this.revisionValid = flag;
        if (!flag) {
            setInfoMessage(NbBundle.getMessage(CreateTag.class, "MSG_CreateTag.errorRevision")); //NOI18N
        }
        validate();
    }

    private void validate () {
        boolean flag = revisionValid && (Boolean.TRUE.equals(nameValid)
                || panel.cbForceUpdate.isEnabled() && panel.cbForceUpdate.isSelected());
        if (!flag && revisionValid) {
            String tName = getTagName();
            if (tName.isEmpty() || !GitUtils.isValidTagName(tName)) {
                setInfoMessage(NbBundle.getMessage(CreateTag.class, "MSG_CreateTag.errorTagNameInvalid")); //NOI18N
            } else {
                setInfoMessage(NbBundle.getMessage(CreateTag.class, "MSG_CreateTag.errorTagExists")); //NOI18N
            }
        }
        if (flag) {
            if (getTagMessage().isEmpty()) {
                setInfoMessage(NbBundle.getMessage(CreateTag.class, "MSG_CreateTag.infoLightWeightTag")); //NOI18N
            } else {
                setInfoMessage(null);
            }
        }
        okButton.setEnabled(flag);
        dd.setValid(flag);
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        if (e.getDocument() == panel.tagMessageField.getDocument()) {
            validate();
        } else {
            validateName();
        }
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        if (e.getDocument() == panel.tagMessageField.getDocument()) {
            validate();
        } else {
            validateName();
        }
    }

    @Override
    public void changedUpdate (DocumentEvent e) { }

    @Override
    public void actionPerformed (ActionEvent e) {
        validate();
    }

    private void validateName () {
        nameValid = false;
        tagName = panel.tagNameField.getText();
        if (!tagName.isEmpty() && GitUtils.isValidTagName(tagName)) {
            nameValid = null;
            branchCheckTask.cancel();
            branchCheckTask.schedule(500);
        }
        validate();
    }

    boolean isForceUpdate () {
        return panel.cbForceUpdate.isEnabled() && panel.cbForceUpdate.isSelected();
    }

    private class TagNameCheckWorker implements Runnable {
        @Override
        public void run () {
            final String tagName = CreateTag.this.tagName;
            GitClient client = null;
            try {
                client = Git.getInstance().getClient(repository);
                final Map<String, GitTag> tags = client.getTags(GitUtils.NULL_PROGRESS_MONITOR, true);
                EventQueue.invokeLater(new Runnable () {
                    @Override
                    public void run () {
                        if (tagName.equals(panel.tagNameField.getText())) {
                            nameValid = !tags.containsKey(tagName);
                            if (!panel.cbForceUpdate.isEnabled()) {
                                panel.cbForceUpdate.setSelected(false);
                            }
                            panel.cbForceUpdate.setEnabled(!nameValid);
                            validate();
                        }
                    }
                });
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, true);
            } finally {
                if (client != null) {
                    client.release();
                }
            }
        }
    }

    private void setInfoMessage (String message) {
        panel.lblInfo.setText(message);
        if (message == null || message.isEmpty()) {
            panel.lblInfo.setIcon(null);
        } else {
            panel.lblInfo.setIcon(ICON_INFO);
        }
    }    
}
