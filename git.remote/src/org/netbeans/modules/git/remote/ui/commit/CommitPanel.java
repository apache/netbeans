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

/*
 * CommitPanel.java
 *
 * Created on Nov 9, 2010, 5:18:15 PM
 */

package org.netbeans.modules.git.remote.ui.commit;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.git.remote.GitModuleConfig;
import org.netbeans.modules.versioning.util.StringSelector;
import org.netbeans.modules.versioning.util.TemplateSelector;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 */
public class CommitPanel extends javax.swing.JPanel {
    private final GitCommitParameters parameters;
    private String headCommitMessage;
    private boolean commitMessageEdited;
    private static final int TITLE_WIDTH;
    private static final int MESSAGE_WIDTH;
    static {
        int width = Integer.getInteger("versioning.git.commitMessageWidth", 72); //NOI18N
        if (width < 0) {
            // 72 is a good practise according to
            // http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html
            width = 72;
        }
        MESSAGE_WIDTH = width;
        width = Integer.getInteger("versioning.git.commitMessageTitleWidth", 0); //NOI18N
        if (width < 0) {
            // 50 is a good practise according to
            // http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html
            width = 50;
        }
        TITLE_WIDTH = width;
    }
    private boolean opened;

    /** Creates new form CommitPanel */
    public CommitPanel(GitCommitParameters parameters, String commitMessage, boolean preferredMessage, String user) {
        this.parameters = parameters;
        
        initComponents();
        Mnemonics.setLocalizedText(messageLabel, getMessage("CTL_CommitForm_Message")); // NOI18N
        
        authorComboBox.setModel(prepareUserModel(GitModuleConfig.getDefault().getRecentCommitAuthors(), user));
        setCaretPosition(authorComboBox);
        
        commiterComboBox.setModel(prepareUserModel(GitModuleConfig.getDefault().getRecentCommiters(), user));
        setCaretPosition(commiterComboBox);
        
        initCommitMessage(commitMessage, preferredMessage);
        attacheMessageListener();
    }

    private void setCaretPosition(JComboBox cbo) {
        Component cmp = cbo.getEditor().getEditorComponent();
        if(cmp instanceof JTextComponent) {
            ((JTextComponent)cmp).setCaretPosition(0);
        }
    }
    
    private void initCommitMessage (String commitMessage, boolean preferred) {
        TemplateSelector ts = new TemplateSelector(parameters.getPreferences());
        if(commitMessage != null) {
            messageTextArea.setText(commitMessage);
        }
        if (!preferred) {
            if (ts.isAutofill()) {
                messageTextArea.setText(ts.getTemplate());
            } else {
                String lastCommitMessage = parameters.getLastCanceledCommitMessage();
                if (lastCommitMessage.isEmpty() && new StringSelector.RecentMessageSelector(parameters.getPreferences()).isAutoFill()) {
                    List<String> messages = parameters.getCommitMessages();
                    if (messages.size() > 0) {
                        lastCommitMessage = messages.get(0);
                    }
                }
                if (!lastCommitMessage.isEmpty()) {
                    messageTextArea.setText(lastCommitMessage);
                }
            }
        }
        messageTextArea.selectAll();
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        if (!opened) {
            opened = true;
            messageTextArea.open();
        }
    }

    @Override
    public void removeNotify() {
        // kind of a work-around, removeNotify is called even when a diff view is opened in the commit dialog
        // we may unregister only when the whole dialog is shut down
        if (getParent() == null || !getParent().isShowing() && opened) {
            messageTextArea.close();
        }
        super.removeNotify();
    }

    private void attacheMessageListener () {
        messageTextArea.getDocument().addDocumentListener(new DocumentListener() {
            
            @Override
            public void insertUpdate (DocumentEvent e) {
                modified();
            }

            @Override
            public void removeUpdate (DocumentEvent e) {
                modified();
            }

            @Override
            public void changedUpdate (DocumentEvent e) {
            }
            
            private void modified () {
                commitMessageEdited = true;
                messageTextArea.getDocument().removeDocumentListener(this);
            }
        });
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messageLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        templatesLabel = parameters.getMessagesTemplateLink(messageTextArea);
        recentLabel = parameters.getRecentMessagesLink(messageTextArea);
        amendCheckBox = new javax.swing.JCheckBox();

        messageLabel.setLabelFor(messageTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.messageLabel.text")); // NOI18N

        messageTextArea.setColumns(20);
        messageTextArea.setRows(5);
        jScrollPane1.setViewportView(messageTextArea);

        jLabel2.setLabelFor(authorComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.jLabel2.text")); // NOI18N

        authorComboBox.setEditable(true);

        commiterComboBox.setEditable(true);

        jLabel3.setLabelFor(commiterComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(templatesLabel, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.templatesLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(recentLabel, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.recentLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(amendCheckBox, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.amendCheckBox.text")); // NOI18N
        amendCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.amendCheckBox.TTtext")); // NOI18N
        amendCheckBox.setEnabled(false);
        amendCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                amendCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(messageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 355, Short.MAX_VALUE)
                        .addComponent(recentLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(templatesLabel)
                        .addGap(57, 57, 57))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(authorComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(commiterComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(amendCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageLabel)
                    .addComponent(templatesLabel)
                    .addComponent(recentLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(authorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(commiterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(amendCheckBox)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void amendCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_amendCheckBoxActionPerformed
        if (amendCheckBox.isSelected() && !commitMessageEdited) {
            this.messageTextArea.setText(headCommitMessage);
            commitMessageEdited = true;
        }
    }//GEN-LAST:event_amendCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JCheckBox amendCheckBox;
    final javax.swing.JComboBox authorComboBox = new javax.swing.JComboBox();
    final javax.swing.JComboBox commiterComboBox = new javax.swing.JComboBox();
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel messageLabel;
    final org.netbeans.modules.git.remote.ui.commit.MessageArea messageTextArea = new org.netbeans.modules.git.remote.ui.commit.MessageAreaBuilder()
    .setWraplineHint(MESSAGE_WIDTH)
    .setTitleHint(TITLE_WIDTH)
    .setAccessibleName(getMessage("ACSN_CommitForm_Message"))
    .setAccessibleDescription(getMessage("ACSD_CommitForm_Message"))
    .build()
    ;
    private javax.swing.JLabel recentLabel;
    private javax.swing.JLabel templatesLabel;
    // End of variables declaration//GEN-END:variables

    private String getMessage(String msgKey) {
        return NbBundle.getMessage(CommitPanel.class, msgKey);
    }

    public String getHeadCommitMessage() {
        return headCommitMessage;
    }

    public void setHeadCommitMessage(String headCommitMessage) {
        this.headCommitMessage = headCommitMessage;
        this.amendCheckBox.setEnabled(true);
    }

    private ComboBoxModel prepareUserModel (List<String> authors, String user) {
        DefaultComboBoxModel model;
        if (authors == null) {
            authors = new LinkedList<>();
        }
        authors.remove(user);
        authors.add(0, user);
        model = new DefaultComboBoxModel(authors.toArray(new String[authors.size()]));
        return model;
    }
    
    
}
