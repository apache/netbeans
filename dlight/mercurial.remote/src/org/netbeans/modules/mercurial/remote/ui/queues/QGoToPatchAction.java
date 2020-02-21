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
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
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
@ActionID(id = "org.netbeans.modules.mercurial.remote.ui.queues.QGoToPatchAction", category = "MercurialRemote/Queues")
@ActionRegistration(displayName = "#CTL_MenuItem_QGoToPatch")
@Messages({
    "CTL_MenuItem_QGoToPatch=&Go To Patch...",
    "CTL_PopupMenuItem_QGoToPatch=Go To Patch..."
})
public class QGoToPatchAction extends ContextAction {

    @Override
    protected boolean enable (Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName (Node[] nodes) {
        return "CTL_MenuItem_QGoToPatch"; //NOI18N
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
                            GoToPatch goToPatch = new GoToPatch(root);
                            if (goToPatch.showDialog()) {
                                if (goToPatch.isPopAllSelected()) {
                                    goToPatch(root, null, null);
                                } else if (goToPatch.getSelectedQueue() != null) {
                                    Queue q = goToPatch.getSelectedQueue();
                                    goToPatch(root, q.getName(), null);
                                } else if (goToPatch.getSelectedPatch() != null) {
                                    QPatch patch = goToPatch.getSelectedPatch();
                                    goToPatch(root, patch.getQueue().isActive() ? null : patch.getQueue().getName(), patch.getId());
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    @Messages({
        "# {0} - repository name",
        "# {1} - queue name",
        "MSG_SwitchingQueue=Switching current queue to {1} in: {0}"
    })
    public void goToPatch (final VCSFileProxy root, final String queueName, final String patchName) {
        new HgProgressSupport() {
            @Override
            protected void perform () {
                final OutputLogger logger = getLogger();
                try {
                    logger.outputInRed(NbBundle.getMessage(QGoToPatchAction.class, "MSG_GOTO_TITLE")); //NOI18N
                    logger.outputInRed(NbBundle.getMessage(QGoToPatchAction.class, "MSG_GOTO_TITLE_SEP")); //NOI18N
                    if (!HgUtils.runWithoutIndexing(new Callable<Boolean>() {
                        @Override
                        public Boolean call () throws Exception {
                            if (patchName == null || queueName != null) {
                                popAllPatches(root, logger);
                            }
                            if (isCanceled()) {
                                return false;
                            }
                            if (queueName != null) {
                                logger.output(Bundle.MSG_SwitchingQueue(root.getPath(), queueName));
                                HgCommand.qSwitchQueue(root, queueName, logger);
                            }
                            if (isCanceled()) {
                                return false;
                            }
                            if (patchName != null) {
                                logger.output(NbBundle.getMessage(QGoToPatchAction.class, "MSG_GOTO_INFO_SEP", patchName, root.getPath())); //NOI18N
                                applyPatch(root, patchName, logger);
                            }
                            return true;
                        }
                    }, root)) {
                        // do not continue, canceled
                        logger.outputInRed(NbBundle.getMessage(QGoToPatchAction.class, "MSG_GOTO_DONE")); // NOI18N
                        logger.output(""); // NOI18N
                        return;
                    }
                    Mercurial.getInstance().historyChanged(root);
                    HgLogMessage parent = HgCommand.getParents(root, null, null).get(0);
                    logger.output(""); // NOI18N
                    HgUtils.logHgLog(parent, logger);
                    logger.outputInRed(NbBundle.getMessage(QGoToPatchAction.class, "MSG_GOTO_DONE")); // NOI18N
                    logger.output(""); // NOI18N
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }
            }
        }.start(Mercurial.getInstance().getRequestProcessor(root), root, NbBundle.getMessage(QGoToPatchAction.class, "LBL_QGoToPatchAction.progress")); //NOI18N
    }

    public void applyPatch (VCSFileProxy repository, String patchName, OutputLogger logger) throws HgException {
        List<String> output = HgCommand.qGoToPatch(repository, patchName, logger);
        FailedPatchResolver resolver = new FailedPatchResolver(repository, output, logger);
        resolver.resolveFailure();
        logger.output(output);
    }

    public void popAllPatches (VCSFileProxy repository, OutputLogger logger) throws HgException, MissingResourceException {
        logger.output(NbBundle.getMessage(QGoToPatchAction.class, "MSG_GOTO_EMPTY_INFO_SEP", repository.getPath())); //NOI18N
        HgCommand.qPopPatches(repository, null, logger);
    }
    
}
