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
package org.netbeans.modules.mercurial.ui.rollback;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JPanel;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.repository.ChangesetPickerPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author  Padraig O'Briain
 */
public class BackoutPanel extends ChangesetPickerPanel {

    private javax.swing.JLabel commitLabel;
    private javax.swing.JTextField commitMsgField;
    private final HgLogMessage repoRev;

    public BackoutPanel(File repo, HgLogMessage repoRev) {
        super(repo, null);
        this.repoRev = repoRev;
        initComponents();
    }

    public String getCommitMessage() {
        return commitMsgField.getText();
    }

    @Override
    protected String getRefreshLabel() {
        return NbBundle.getMessage(Backout.class, "MSG_Refreshing_Backout_Versions"); //NOI18N
    }

    @Override
    protected HgLogMessage getDisplayedRevision() {
        return repoRev;
    }

    @Override
    protected void loadRevisions () {
        super.loadRevisions();
    }

    private void initComponents() {
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BackoutPanel.class, "BackoutPanel.infoLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(BackoutPanel.class, "BackoutPanel.infoLabel2.text")); // NOI18N
        if(repoRev != null){
            org.openide.awt.Mnemonics.setLocalizedText(revisionsLabel,
                    org.openide.util.NbBundle.getMessage(BackoutPanel.class, "CTL_ChoosenRevision")); // NOI18N
        }
        commitMsgField = new javax.swing.JTextField();
        commitLabel = new javax.swing.JLabel();
        commitLabel.setLabelFor(commitMsgField);
        commitMsgField.setText(NbBundle.getMessage(BackoutPanel.class, "BackoutPanel.commitMsgField.text", BackoutAction.HG_BACKOUT_REVISION)); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(commitLabel, org.openide.util.NbBundle.getMessage(BackoutPanel.class, "BackoutPanel.commitLabel.text")); // NOI18N
        commitMsgField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BackoutPanel.class, "ACSD_commitMsgField")); // NOI18N

        JPanel optionsPanel = new JPanel(new BorderLayout(10, 0));
        optionsPanel.add(commitLabel, BorderLayout.WEST);
        optionsPanel.add(commitMsgField, BorderLayout.CENTER);
        optionsPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        setOptionsPanel(optionsPanel, null);
    }
}
