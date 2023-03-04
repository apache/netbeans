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
package org.netbeans.modules.mercurial.ui.queues;

import java.io.File;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.queues.CreateRefreshAction.Cmd.CreateRefreshPatchCmd;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.queues.QCreatePatchAction", category = "Mercurial/Queues")
@ActionRegistration(displayName = "#CTL_MenuItem_QCreatePatch")
@NbBundle.Messages({
    "CTL_MenuItem_QCreatePatch=&Create Patch...",
    "CTL_PopupMenuItem_QCreatePatch=Create Patch..."
})
public class QCreatePatchAction extends CreateRefreshAction {

    static final String KEY_CANCELED_MESSAGE = "qcreate"; //NOI18N

    public QCreatePatchAction () {
        super("create"); //NOI18N
    }
    
    @Override
    protected String getBaseName (Node[] nodes) {
        return "CTL_MenuItem_QCreatePatch"; //NOI18N
    }

    @Override
    CreateRefreshPatchCmd createHgCommand (File root, List<File> commitCandidates, OutputLogger logger, String message,
            String patchName, String user, String bundleKeyPostfix,
            List<File> roots, Set<File> excludedFiles, Set<File> filesToRefresh) {
        return new Cmd.CreateRefreshPatchCmd(root, commitCandidates, logger, message, patchName, user, bundleKeyPostfix,
                roots, excludedFiles, filesToRefresh) {
            @Override
            protected void runHgCommand (File repository, List<File> candidates, Set<File> excludedFiles,
                    String patchId, String msg, String user, OutputLogger logger) throws HgException {
                HgCommand.qCreatePatch(repository, candidates, excludedFiles, patchId, msg, user, logger);
            }
        };
    }

    @Override
    QCommitPanel createPanel (File root, File[] roots) {
        return QCommitPanel.createNewPanel(roots, root, HgModuleConfig.getDefault().getLastCanceledCommitMessage(KEY_CANCELED_MESSAGE), QCreatePatchAction.class.getName());
    }

    @Override
    void persistCanceledCommitMessage (QCreatePatchParameters params, String canceledCommitMessage) {
        HgModuleConfig.getDefault().setLastCanceledCommitMessage(KEY_CANCELED_MESSAGE, canceledCommitMessage);
    }
}
