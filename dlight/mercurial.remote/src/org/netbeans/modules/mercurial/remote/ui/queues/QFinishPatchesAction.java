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
package org.netbeans.modules.mercurial.remote.ui.queues;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.hooks.HgHookContext.LogEntry;
import org.netbeans.modules.remotefs.versioning.hooks.HgQueueHook;
import org.netbeans.modules.remotefs.versioning.hooks.HgQueueHookContext;
import org.netbeans.modules.remotefs.versioning.hooks.VCSHooks;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * 
 */
@ActionID(id = "org.netbeans.modules.mercurial.remote.ui.queues.QFinishPatchesAction", category = "MercurialRemote/Queues")
@ActionRegistration(displayName = "#CTL_MenuItem_QFinishPatches")
@Messages({
    "CTL_MenuItem_QFinishPatches=&Finish Patches...",
    "CTL_PopupMenuItem_QFinishPatches=Finish Patches..."
})
public class QFinishPatchesAction extends ContextAction {

    @Override
    protected boolean enable (Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName (Node[] nodes) {
        return "CTL_MenuItem_QFinishPatches"; //NOI18N
    }

    @Override
    protected void performContextAction (Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final VCSFileProxy roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) {
            return;
        }
        final VCSFileProxy root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        Utils.post(new Runnable() {
            @Override
            public void run () {
                if (QUtils.isMQEnabledExtension(root)) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            FinishPatch finishPatch = new FinishPatch(root);
                            if (finishPatch.showDialog()) {
                                String patchName = finishPatch.getSelectedPatch();
                                if (patchName != null) {
                                    finishPatch(root, roots, patchName);
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    public void finishPatch (final VCSFileProxy root, final VCSFileProxy[] roots, final String patchName) {
        new HgProgressSupport() {
            @Override
            protected void perform () {
                OutputLogger logger = getLogger();
                try {
                    logger.outputInRed(NbBundle.getMessage(QFinishPatchesAction.class, "MSG_FINISH_TITLE")); //NOI18N
                    logger.outputInRed(NbBundle.getMessage(QFinishPatchesAction.class, "MSG_FINISH_TITLE_SEP")); //NOI18N
                    List<QPatch> toFinish = getPatchesToFinish(patchName, root);
                    if (toFinish.isEmpty()) {
                        logger.output(NbBundle.getMessage(QFinishPatchesAction.class, "MSG_FINISH_NOTHING_TO_FINISH")); //NOI18N
                        return;
                    }
                    Collection<HgQueueHook> hooks = VCSHooks.getInstance().getHooks(HgQueueHook.class);
                    for (QPatch patch : toFinish) {
                        if (isCanceled()) {
                            return;
                        }
                        setDisplayName(patch.getId());
                        logger.output(NbBundle.getMessage(QFinishPatchesAction.class, "MSG_FINISH_INFO_SEP", patch.getId(), root.getPath())); //NOI18N
                        logger.output(""); // NOI18N
                        HgQueueHookContext hooksCtx = new HgQueueHookContext(roots, null, patch.getId());
                        for (HgQueueHook hgHook : hooks) {
                            try {
                                // XXX handle returned context
                                hgHook.beforePatchFinish(hooksCtx);
                            } catch (IOException ex) {
                                // XXX handle veto
                            }
                        }
                        HgLogMessage message = getRevision(patch.getId());
                        if (message == null) {
                            logger.outputInRed(NbBundle.getMessage(QFinishPatchesAction.class, "MSG_FINISH_ERR_NOSUCHREVISION", patch.getId())); //NOI18N
                            break;
                        }
                        HgCommand.qFinishPatches(root, patch.getId(), logger);
                        HgModuleConfig.getDefault(root).setLastUsedQPatchMessage(patch.getId(), null); // cleanup preferences
                        message = getRevision(message.getRevisionNumber());
                        if (message == null) {
                            logger.outputInRed(NbBundle.getMessage(QFinishPatchesAction.class, "MSG_FINISH_ERR_NOSUCHREVISION", patch.getId())); //NOI18N
                            break;
                        } else {
                            HgUtils.logHgLog(message, logger);
                            hooksCtx = new HgQueueHookContext(roots, null, patch.getId(), 
                                    new LogEntry(message.getMessage(), message.getAuthor(), message.getCSetShortID(), message.getDate()));
                            for (HgQueueHook hgHook : hooks) {
                                setDisplayName(NbBundle.getMessage(QFinishPatchesAction.class, "MSG_QFinishPatchesAction.progress.hooks", patch.getId())); //NOI18N
                                hgHook.afterPatchFinish(hooksCtx);
                            }
                        }
                    }
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                } finally {
                    logger.outputInRed(NbBundle.getMessage(QFinishPatchesAction.class, "MSG_FINISH_DONE")); // NOI18N
                    logger.output(""); // NOI18N
                }
            }

            private List<QPatch> getPatchesToFinish (String patchName, VCSFileProxy root) throws HgException {
                QPatch[] patches = HgCommand.qListSeries(root);
                List<QPatch> toFinish = new LinkedList<>();
                boolean containsTarget = false;
                for (QPatch patch : patches) {
                    if (patch.isApplied()) {
                        toFinish.add(patch);
                    } else {
                        break;
                    }
                    if (patchName.contains(patch.getId())) {
                        containsTarget = true;
                        break;
                    }
                }
                return containsTarget ? toFinish : Collections.<QPatch>emptyList();
            }

            private HgLogMessage getRevision (String revisionId) {
                HgLogMessage[] messages = HgCommand.getLogMessagesNoFileInfo(root, null, revisionId, revisionId, true, -1, Collections.<String>emptyList(), getLogger());
                return messages.length == 0 ? null : messages[0];
            }
        }.start(Mercurial.getInstance().getRequestProcessor(root), root, NbBundle.getMessage(QFinishPatchesAction.class, "LBL_QFinishPatchAction.progress")); //NOI18N
    }
    
}
