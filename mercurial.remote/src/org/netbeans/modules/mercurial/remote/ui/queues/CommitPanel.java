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

package org.netbeans.modules.mercurial.remote.ui.queues;

import java.awt.Dimension;
import java.util.List;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.netbeans.modules.versioning.util.StringSelector;
import org.netbeans.modules.versioning.util.TemplateSelector;
import org.netbeans.modules.versioning.util.UndoRedoSupport;
import org.netbeans.modules.versioning.util.common.CommitMessageMouseAdapter;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class CommitPanel extends javax.swing.JPanel {
    private final QCreatePatchParameters parameters;
    private UndoRedoSupport um;

    /** Creates new form CommitPanel */
    public CommitPanel(QCreatePatchParameters parameters, String commitMessage, String patchName) {
        this.parameters = parameters;
        
        initComponents();
        boolean skipTemplates = false;
        if (patchName != null && !patchName.isEmpty()) {
            txtPatchName.setText(patchName);
            txtPatchName.setEditable(false);
            skipTemplates = true;
        }
        
        messageTextArea.setColumns(60);    //this determines the preferred width of the whole dialog
        messageTextArea.setLineWrap(true);
        messageTextArea.setRows(4);
        messageTextArea.setTabSize(4);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setMinimumSize(new Dimension(100, 18));
        
        messageTextArea.getAccessibleContext().setAccessibleName(getMessage("ACSN_CommitForm_Message")); // NOI18N
        messageTextArea.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CommitForm_Message")); // NOI18N
        messageTextArea.addMouseListener(new CommitMessageMouseAdapter());
        
        Spellchecker.register (messageTextArea);  
        initCommitMessage(commitMessage, skipTemplates);
        
        cmbAuthor.setModel(parameters.createRecentUsersModel());
    }
    
    private void initCommitMessage (String commitMessage, boolean skipTemplates) {
        TemplateSelector ts = new TemplateSelector(parameters.getPreferences());
        if (commitMessage == null) {
            commitMessage = ""; //NOI18N
        }
        messageTextArea.setText(commitMessage);
        if (ts.isAutofill() && !skipTemplates) {
            messageTextArea.setText(ts.getTemplate());
        } else {
            String lastCommitMessage = parameters.getLastCanceledCommitMessage();
            if (lastCommitMessage.isEmpty() && new StringSelector.RecentMessageSelector(parameters.getPreferences()).isAutoFill()) {
                List<String> messages = parameters.getCommitMessages();
                if (messages.size() > 0) {
                    lastCommitMessage = messages.get(0);
                }
            }
            if (commitMessage.isEmpty() && !lastCommitMessage.isEmpty()) {
                messageTextArea.setText(lastCommitMessage);
            }
        }
        messageTextArea.selectAll();
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        if (um == null) {
            um = UndoRedoSupport.register(messageTextArea);
        }
    }

    @Override
    public void removeNotify() {
        // kind of a work-around, removeNotify is called even when a diff view is opened in the commit dialog
        // we may unregister only when the whole dialog is shut down
        if (getParent() == null || !getParent().isShowing()) {
            if (um != null) {
                um.unregister();
                um = null;
            }
        }
        super.removeNotify();
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
        templatesLabel = parameters.getMessagesTemplateLink(messageTextArea);
        recentLabel = parameters.getRecentMessagesLink(messageTextArea);
        jLabel1 = new javax.swing.JLabel();

        messageLabel.setLabelFor(messageTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.messageLabel.text")); // NOI18N

        messageTextArea.setColumns(20);
        messageTextArea.setRows(5);
        jScrollPane1.setViewportView(messageTextArea);

        org.openide.awt.Mnemonics.setLocalizedText(templatesLabel, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.templatesLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(recentLabel, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.recentLabel.text")); // NOI18N

        jLabel1.setLabelFor(txtPatchName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.jLabel1.TTtext")); // NOI18N

        txtPatchName.setText(org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.txtPatchName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbAuthor, org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.cbAuthor.text")); // NOI18N
        cbAuthor.setToolTipText(org.openide.util.NbBundle.getMessage(CommitPanel.class, "CommitPanel.cbAuthor.TTtext")); // NOI18N

        cmbAuthor.setEditable(true);
        cmbAuthor.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbAuthor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPatchName, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(messageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 382, Short.MAX_VALUE)
                        .addComponent(recentLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(templatesLabel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtPatchName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageLabel)
                    .addComponent(templatesLabel)
                    .addComponent(recentLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbAuthor)
                    .addComponent(cmbAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JCheckBox cbAuthor = new javax.swing.JCheckBox();
    final javax.swing.JComboBox cmbAuthor = new javax.swing.JComboBox();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel messageLabel;
    final javax.swing.JTextArea messageTextArea = new javax.swing.JTextArea();
    private javax.swing.JLabel recentLabel;
    private javax.swing.JLabel templatesLabel;
    final javax.swing.JTextField txtPatchName = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables

    private String getMessage(String msgKey) {
        return NbBundle.getMessage(CommitPanel.class, msgKey);
    }

}
