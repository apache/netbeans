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

package org.netbeans.modules.mercurial.ui.queues;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import org.netbeans.modules.versioning.util.common.VCSCommitParameters.DefaultCommitParameters;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class QCreatePatchParameters extends DefaultCommitParameters implements ItemListener, DocumentListener,
        ActionListener {
    private CommitPanel panel;
    private final String commitMessage;
    private final QPatch patch;
    private String errorMessage;
    private boolean userValid;
    private String user;
    private final List<String> recentUsers;

    public QCreatePatchParameters (Preferences preferences, String commitMessage, QPatch patch, List<String> recentUsers) {
        super(preferences);
        this.commitMessage = commitMessage;
        this.patch = patch;
        this.recentUsers = recentUsers;
    }

    @Override
    public CommitPanel getPanel () {
        if(panel == null) {
            panel = createPanel();   
            panel.txtPatchName.getDocument().addDocumentListener(this);
            panel.cbAuthor.addActionListener(this);
            ((JTextComponent) panel.cmbAuthor.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        }
        return panel;
    }

    public JLabel getMessagesTemplateLink (JTextArea text) {
        return super.getMessagesTemplateLink(text, "org.netbeans.modules.mercurial.ui.queues.TemplatePanel"); //NOI18N
    }

    @Override
    public JLabel getRecentMessagesLink (JTextArea text) {
        return super.getRecentMessagesLink(text);
    }
    
    @Override
    public Preferences getPreferences () {
        return super.getPreferences();
    }

    @Override
    public String getLastCanceledCommitMessage () {
        return HgModuleConfig.getDefault().getLastCanceledCommitMessage(QCreatePatchAction.KEY_CANCELED_MESSAGE);
    }

    List<String> getCommitMessages () {
        return getRecentCommitMessages(getPreferences());
    }
    
    @Override
    protected CommitPanel createPanel () {
        return new CommitPanel(this, commitMessage, patch == null ? null : patch.getId());
    }

    @Override
    public String getCommitMessage () {
        return ((CommitPanel) getPanel()).messageTextArea.getText();
    }
    
    public String getPatchName () {
        return getPanel().txtPatchName.getText().trim();
    }
    
    QPatch getPatch () {
        return patch;
    }

    @Override
    @NbBundle.Messages({
        "MSG_QPatchForm_ErrorInvalidAuthor=Invalid author"
    })
    public boolean isCommitable () {            
        if (getPatchName().isEmpty()) {
            errorMessage = NbBundle.getMessage(QCreatePatchParameters.class, "MSG_WARNING_EMPTY_PATCH_NAME"); //NOI18N
            return false;
        } else if (!isUserValid()) {
            errorMessage = Bundle.MSG_QPatchForm_ErrorInvalidAuthor();
            return false;            
        }
        errorMessage = null;
        return true;
    }

    @Override
    public String getErrorMessage () {
        return errorMessage;
    }    

    @Override
    public void itemStateChanged(ItemEvent e) {
        fireChange();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if (e.getDocument() == ((JTextComponent) panel.cmbAuthor.getEditor().getEditorComponent()).getDocument()) {
            boolean oldUserValid = userValid;
            validateUser();
            if (userValid != oldUserValid && panel.cbAuthor.isSelected()) {
                fireChange();
            }
        } else {
            fireChange();
        }
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.cbAuthor) {
            panel.cmbAuthor.setEnabled(panel.cbAuthor.isSelected());
            validateUser();
            fireChange();
        }
    }

    String getUser () {
        return panel.cbAuthor.isSelected() && isUserValid() ? user: null;
    }

    ComboBoxModel createRecentUsersModel () {
        return new DefaultComboBoxModel(recentUsers.toArray(new String[0]));
    }

    private boolean isUserValid () {
        return userValid || !panel.cbAuthor.isSelected();
    }

    private void validateUser () {
        user = panel.cmbAuthor.getEditor().getItem().toString().trim();
        userValid = !user.isEmpty();
    }

}
