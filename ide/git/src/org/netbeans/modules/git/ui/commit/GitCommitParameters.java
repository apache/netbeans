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

package org.netbeans.modules.git.ui.commit;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.regex.Matcher;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import org.netbeans.modules.versioning.util.common.VCSCommitParameters.DefaultCommitParameters;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.GitModuleConfig;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class GitCommitParameters extends DefaultCommitParameters implements ItemListener, DocumentListener {
    private CommitPanel panel;
    private String commitMessage;
    private GitUser user;
    private String errorMessage;
    private final boolean preferredCommitMessage;

    GitCommitParameters(Preferences preferences, String commitMessage, GitUser user) {
        this(preferences, commitMessage, false, user);
    }

    GitCommitParameters(Preferences preferences, String commitMessage, boolean preferredCommitMessage, GitUser user) {
        super(preferences);
        this.commitMessage = commitMessage;
        this.preferredCommitMessage = preferredCommitMessage;
        this.user = user;
    }

    @Override
    public CommitPanel getPanel() {
        if(panel == null) {
            panel = createPanel();   
            
            ((JTextField) panel.authorComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
            ((JTextField) panel.commiterComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
            panel.amendCheckBox.addItemListener(this);
            panel.authorComboBox.addItemListener(this);
            panel.commiterComboBox.addItemListener(this);
        }
        return panel;
    }

    public JLabel getMessagesTemplateLink(JTextArea text) {
        return super.getMessagesTemplateLink(text, "org.netbeans.modules.git.ui.commit.TemplatePanel"); //NOI18N
    }

    @Override
    public JLabel getRecentMessagesLink(JTextArea text) {
        return super.getRecentMessagesLink(text);
    }
    
    @Override
    public Preferences getPreferences() {
        return super.getPreferences();
    }

    @Override
    public String getLastCanceledCommitMessage() {
        return GitModuleConfig.getDefault().getLastCanceledCommitMessage();
    }

    List<String> getCommitMessages() {
        return getRecentCommitMessages(getPreferences());
    }
    
    @Override
    protected CommitPanel createPanel() {
        return new CommitPanel(this, commitMessage, preferredCommitMessage, getUserString(user));
    }

    @Override
    public String getCommitMessage() {
        return ((CommitPanel) getPanel()).messageTextArea.getText();
    }

    public boolean isAmend() {
        return getPanel().amendCheckBox.isSelected();
    }
    
    public GitUser getAuthor() {
        return getUser(getPanel().authorComboBox);
    }
    
    public GitUser getCommiter() {
        return getUser(getPanel().commiterComboBox);        
    }
    
    static String getUserString(GitUser user) {
        if(user == null) return "";                                             // NOI18N
        String name = user.getName();
        String mail = user.getEmailAddress();
        return name + 
               (mail != null && !mail.isEmpty() ? 
                     " <" + user.getEmailAddress() + ">" :                      // NOI18N
                     "");                                                       // NOI18N
    }
        
    @Override
    public boolean isCommitable() {            
        if(getAuthor() != null && getCommiter() != null) {
            errorMessage = null;
            return true;
        }
        errorMessage = NbBundle.getMessage(GitCommitParameters.class, "MSG_WARNING_WRONG_USER");  // NOI18N
        return false;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }    
    
    private Pattern validUserFormat = Pattern.compile("(.+)\\<(.*)\\>");        // NOI18N
    
    // cli rejects "<>", "<bla>", "bla>" or ">"
    private Pattern invalidUserFormat = Pattern.compile("(\\<)?(.*)\\>");       // NOI18N
    
    /** package private to support testing */
    GitUser getUser(JComboBox combo) {
        String str = (String) combo.getEditor().getItem();
        if(str == null || str.trim().isEmpty()) {
            return null;
        }        
        Matcher m = validUserFormat.matcher(str.trim());
        if(m.matches()) {
            String name = m.group(1).trim();
            String mail = m.groupCount() > 1 ? (m.group(2) != null ? m.group(2) : "") : ""; // NOI18N
            mail = mail.trim();
            return new GitUser(name, mail);
        }        
        m = invalidUserFormat.matcher(str.trim());
        if(m.matches()) {            
            return null;
        }
        return new GitUser(str, "");
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        fireChange();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        fireChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        fireChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        fireChange();
    }
    
}
