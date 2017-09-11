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
