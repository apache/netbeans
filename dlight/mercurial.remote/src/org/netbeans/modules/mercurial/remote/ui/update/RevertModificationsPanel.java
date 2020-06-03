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
package org.netbeans.modules.mercurial.remote.ui.update;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.ui.repository.ChangesetPickerPanel;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class RevertModificationsPanel extends ChangesetPickerPanel {

    private JCheckBox doBackupChxBox;
    private JCheckBox doPurgeChxBox;

    /** Creates new form ReverModificationsPanel */
    public RevertModificationsPanel (VCSFileProxy repo, VCSFileProxy[] files) {
        super(repo, files);
        initComponents();
        loadRevisions();
    }
    
    public boolean isBackupRequested() {
        return doBackupChxBox.isSelected();
    }

    boolean isPurgeRequested () {
        return doPurgeChxBox.isSelected();
    }

    @Override
    protected String getRefreshLabel() {
        return NbBundle.getMessage(RevertModificationsPanel.class, "MSG_Refreshing_Revert_Versions"); //NOI18N
    }

    private void initComponents() {
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RevertModificationsPanel.class, "RevertModificationsPanel.infoLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RevertModificationsPanel.class, "RevertModificationsPanel.infoLabel2.text")); // NOI18N

        doBackupChxBox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(doBackupChxBox, org.openide.util.NbBundle.getMessage(RevertModificationsPanel.class, "RevertModificationsPanel.doBackupChxBox.text")); // NOI18N
        boolean doBackup = HgModuleConfig.getDefault(getRepository()).getBackupOnRevertModifications();
        doBackupChxBox.setSelected(doBackup);
        doPurgeChxBox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(doPurgeChxBox, org.openide.util.NbBundle.getMessage(RevertModificationsPanel.class, "RevertModificationsPanel.doPurgeChxBox.text")); // NOI18N
        doPurgeChxBox.setToolTipText(org.openide.util.NbBundle.getMessage(RevertModificationsPanel.class, "RevertModificationsPanel.doPurgeChxBox.desc")); // NOI18N
        doPurgeChxBox.getAccessibleContext().setAccessibleDescription(doPurgeChxBox.getToolTipText());
        boolean doPurge = HgModuleConfig.getDefault(getRepository()).isRemoveNewFilesOnRevertModifications();
        doPurgeChxBox.setSelected(doPurge);
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.add(doBackupChxBox);
        optionsPanel.add(doPurgeChxBox);
        optionsPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        setOptionsPanel(optionsPanel, null);
    }
}
