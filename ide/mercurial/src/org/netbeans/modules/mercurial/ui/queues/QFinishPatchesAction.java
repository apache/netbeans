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

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.hooks.HgHookContext.LogEntry;
import org.netbeans.modules.versioning.hooks.HgQueueHook;
import org.netbeans.modules.versioning.hooks.HgQueueHookContext;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.queues.QFinishPatchesAction", category = "Mercurial/Queues")
@ActionRegistration(displayName = "#CTL_MenuItem_QFinishPatches")
@NbBundle.Messages({
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
        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
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

    public void finishPatch (final File root, final File[] roots, final String patchName) {
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
                        logger.output(NbBundle.getMessage(QFinishPatchesAction.class, "MSG_FINISH_INFO_SEP", patch.getId(), root.getAbsolutePath())); //NOI18N
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
                        HgModuleConfig.getDefault().setLastUsedQPatchMessage(patch.getId(), null); // cleanup preferences
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

            private List<QPatch> getPatchesToFinish (String patchName, File root) throws HgException {
                QPatch[] patches = HgCommand.qListSeries(root);
                List<QPatch> toFinish = new LinkedList<QPatch>();
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
