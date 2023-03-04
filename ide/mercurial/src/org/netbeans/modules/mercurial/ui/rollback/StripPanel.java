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
import org.netbeans.modules.mercurial.ui.repository.ChangesetPickerPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author  Padraig O'Briain
 */
public class StripPanel extends ChangesetPickerPanel {

    private javax.swing.JCheckBox doBackupChxBox;

    /** Creates new form ReverModificationsPanel */
    public StripPanel(File repo) {
        super(repo, null);
        initComponents();
        loadRevisions();
    }

    public boolean isBackupRequested() {
        return doBackupChxBox.isSelected();
    }

    private void initComponents() {
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(StripPanel.class, "StripPanel.infoLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(StripPanel.class, "StripPanel.infoLabel2.text")); // NOI18N

        doBackupChxBox = new javax.swing.JCheckBox();
        doBackupChxBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(doBackupChxBox, org.openide.util.NbBundle.getMessage(StripPanel.class, "StripPanel.doBackupChxBox.text")); // NOI18N
        doBackupChxBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(StripPanel.class, "ACSD_doBackupChxBox")); // NOI18N
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.add(doBackupChxBox, BorderLayout.NORTH);
        optionsPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        setOptionsPanel(optionsPanel, null);
    }

    @Override
    protected String getRefreshLabel() {
        return NbBundle.getMessage(StripPanel.class, "MSG_Refreshing_Strip_Versions"); //NOI18N
    }
}
