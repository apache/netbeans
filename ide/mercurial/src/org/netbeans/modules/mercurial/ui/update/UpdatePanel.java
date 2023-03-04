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
package org.netbeans.modules.mercurial.ui.update;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.repository.ChangesetPickerPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author  Padraig O'Briain
 */
public class UpdatePanel extends ChangesetPickerPanel {

    private JCheckBox forcedUpdateChxBox;
    private final HgLogMessage fixedRevision;

    /** Creates new form ReverModificationsPanel */
    public UpdatePanel(File repo, HgLogMessage fixedRevision) {
        super(repo, null);
        this.fixedRevision = fixedRevision;
        initComponents();
        loadRevisions();
    }

    public boolean isForcedUpdateRequested() {
        return forcedUpdateChxBox.isSelected();
    }

    @Override
    protected HgLogMessage getDisplayedRevision () {
        return fixedRevision;
    }

    @Override
    protected String getRefreshLabel() {
        return NbBundle.getMessage(UpdatePanel.class, "MSG_Refreshing_Update_Versions"); //NOI18N
    }

    private void initComponents() {
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RevertModificationsPanel.class, "UpdatePanel.infoLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RevertModificationsPanel.class, "UpdatePanel.infoLabel2.text")); // NOI18N
        forcedUpdateChxBox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(forcedUpdateChxBox, org.openide.util.NbBundle.getMessage(UpdatePanel.class, "UpdatePanel.forcedUpdateChxBox.text")); // NOI18N
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.add(forcedUpdateChxBox, BorderLayout.NORTH);
        optionsPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        setOptionsPanel(optionsPanel, null);
    }
}
