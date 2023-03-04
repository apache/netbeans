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
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.queues.CreateRefreshAction.Cmd.CreateRefreshPatchCmd;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.queues.QRefreshPatchAction", category = "Mercurial/Queues")
@ActionRegistration(displayName = "#CTL_MenuItem_QRefreshPatch")
@NbBundle.Messages({
    "CTL_MenuItem_QRefreshPatch=&Refresh Patch...",
    "CTL_PopupMenuItem_QRefreshPatch=Refresh Patch..."
})
public class QRefreshPatchAction extends CreateRefreshAction {

    static final String KEY_CANCELED_MESSAGE = "qrefresh."; //NOI18N

    public QRefreshPatchAction () {
        super("refresh"); //NOI18N
    }

    @Override
    protected String getBaseName (Node[] nodes) {
        return "CTL_MenuItem_QRefreshPatch"; //NOI18N
    }

    @Override
    @NbBundle.Messages({
        "# {0} - repository name", "MSG_QRefreshPatchAction.err.noPatchApplied=Cannot refresh patch.\n"
            + "No patch applied in repository \"{0}\"."
    })
    QCommitPanel createPanel (final File root, final File[] roots) {
        QPatch currentPatch = null;
        try {
            for (QPatch p : HgCommand.qListSeries(root)) {
                if (p.isApplied()) {
                    currentPatch = p;
                } else {
                    break;
                }
            }
            if (currentPatch == null) {
                NotifyDescriptor.Message e = new NotifyDescriptor.Message(Bundle.MSG_QRefreshPatchAction_err_noPatchApplied(root.getName()));
                DialogDisplayer.getDefault().notifyLater(e);
            } else {
                final HgLogMessage.HgRevision parent = HgCommand.getParent(root, null, currentPatch.getId());
                String commitMessage = HgModuleConfig.getDefault().getLastCanceledCommitMessage(KEY_CANCELED_MESSAGE + currentPatch.getId());
                if (commitMessage.isEmpty()) {
                    commitMessage = HgModuleConfig.getDefault().getLastUsedQPatchMessage(currentPatch.getId());
                    if (commitMessage.isEmpty()) {
                        List<HgLogMessage> msgs = HgCommand.getParents(root, null, null);
                        if (!msgs.isEmpty()) {
                            commitMessage = msgs.get(0).getMessage();
                        }
                    }
                }
                return QCommitPanel.createRefreshPanel(roots, root, commitMessage, currentPatch, parent, QRefreshPatchAction.class.getName());
            }
        } catch (HgException.HgCommandCanceledException ex) {
            // canceled by user, do nothing
        } catch (HgException ex) {
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(ex.getMessage());
            DialogDisplayer.getDefault().notifyLater(e);
        }
        return null;
    }

    @Override
    CreateRefreshPatchCmd createHgCommand (File root, List<File> candidates, OutputLogger logger,
            String message, String patchName, String user, String bundleKeyPostfix,
            List<File> roots, Set<File> excludedFiles, Set<File> filesToRefresh) {
        return new CreateRefreshPatchCmd(root, candidates, logger, message, patchName, user, bundleKeyPostfix,
                roots, excludedFiles, filesToRefresh) {
            @Override
            protected void runHgCommand (File repository, List<File> candidates, Set<File> excludedFiles,
                    String patchId, String msg, String user, OutputLogger logger) throws HgException {
                HgCommand.qRefreshPatch(repository, candidates, excludedFiles, msg, user, logger);
            }
        };
    }

    @Override
    void persistCanceledCommitMessage (QCreatePatchParameters params, String canceledCommitMessage) {
        HgModuleConfig.getDefault().setLastCanceledCommitMessage(KEY_CANCELED_MESSAGE + params.getPatch().getId(), canceledCommitMessage);
    }
    
}
