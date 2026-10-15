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

/*
 * CommitPanel.java
 *
 * Created on Nov 9, 2010, 5:18:15 PM
 */

package org.netbeans.modules.git.ui.commit;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.versioning.util.StringSelector;
import org.netbeans.modules.versioning.util.TemplateSelector;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
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
        initActions();
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
        templatesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.templatesLabel.TTtext")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(recentLabel, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.recentLabel.text")); // NOI18N
        recentLabel.setToolTipText(org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.recentLabel.TTtext")); // NOI18N

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
    final org.netbeans.modules.git.ui.commit.MessageArea messageTextArea = new org.netbeans.modules.git.ui.commit.MessageAreaBuilder()
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
            authors = new LinkedList<String>();
        }
        authors.remove(user);
        authors.add(0, user);
        model = new DefaultComboBoxModel(authors.toArray(new String[0]));
        return model;
    }

    private void initActions () {
        InputMap inputMap = getInputMap( WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        ActionMap actionMap = getActionMap();
        Object action = recentLabel.getClientProperty("openAction");
        if (action instanceof Action) {
            inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_R, KeyEvent.ALT_DOWN_MASK, false ), "messageHistory" ); //NOI18N
            actionMap.put("messageHistory", (Action) action); //NOI18N
        }
        action = templatesLabel.getClientProperty("openAction");
        if (action instanceof Action) {
            inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_L, KeyEvent.ALT_DOWN_MASK, false ), "messageTemplate" ); //NOI18N
            actionMap.put("messageTemplate", (Action) action); //NOI18N
        }
    }
    
    
}
