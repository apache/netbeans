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
package org.netbeans.modules.mercurial.ui.merge;

import java.util.Set;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.update.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.ui.repository.ChangesetPickerPanel;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.NbBundle;

/**
 *
 * @author  Padraig O'Briain
 */
public class MergeRevisionsPanel extends ChangesetPickerPanel {

    public MergeRevisionsPanel (File repo,File [] roots) {
        super(repo, roots);
        initComponents();
        setInitMessageInfoFetcher(new HeadsInfoFetcher());
    }

    @Override
    protected String getRefreshLabel() {
        return NbBundle.getMessage(UpdatePanel.class, "MSG_Refreshing_Update_Versions"); //NOI18N
    }

    @Override
    protected boolean acceptSelection (HgLogMessage rev) {
        boolean accepted = rev != null;
        HgRevision parentRevision;
        if (accepted && (parentRevision = getParentRevision()) != null) {
            accepted = !rev.getCSetShortID().equals(parentRevision.getChangesetId());
        }
        return accepted;
    }

    @Override
    protected void loadRevisions () {
        super.loadRevisions();
    }

    private void initComponents() {
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MergeRevisionsPanel.class, "infoLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(MergeRevisionsPanel.class, "infoLabel2.text")); // NOI18N
    }

    private static final class HeadsInfoFetcher extends MessageInfoFetcher {
        @Override
        protected HgLogMessage[] getMessageInfo(File repository, Set<File> setRoots, int fetchRevisionLimit, OutputLogger logger) {
            HgLogMessage[] messages;
            try {
                messages = HgCommand.getHeadRevisionsInfo(repository, false, logger);
            } catch (HgException ex) {
                Logger.getLogger(MergeRevisionsPanel.class.getName()).log(Level.INFO, null, ex);
                messages = new HgLogMessage[0];
            }
            return messages;
        }
    }
}
