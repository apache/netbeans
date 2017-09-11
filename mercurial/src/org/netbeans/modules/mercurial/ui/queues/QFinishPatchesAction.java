/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
