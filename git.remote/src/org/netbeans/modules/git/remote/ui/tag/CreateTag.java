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
package org.netbeans.modules.git.remote.ui.tag;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitTag;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 */
class CreateTag implements DocumentListener, ActionListener {
    private final CreateTagPanel panel;
    private final RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;
    private boolean revisionValid = true;
    private Boolean nameValid = false;
    private final Task branchCheckTask;
    private String tagName;
    private final VCSFileProxy repository;
    private final Icon ICON_INFO = new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/remote/resources/icons/info.png")); //NOI18N

    CreateTag (VCSFileProxy repository, String initialRevision, String initialTagName) {
        this.repository = repository;
        this.branchCheckTask = Git.getInstance().getRequestProcessor(repository).create(new TagNameCheckWorker());
        revisionPicker = new RevisionDialogController(repository, new VCSFileProxy[] { repository }, initialRevision);
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
