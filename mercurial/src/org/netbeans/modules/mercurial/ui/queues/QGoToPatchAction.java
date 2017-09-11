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
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
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
@ActionID(id = "org.netbeans.modules.mercurial.ui.queues.QGoToPatchAction", category = "Mercurial/Queues")
@ActionRegistration(displayName = "#CTL_MenuItem_QGoToPatch")
@NbBundle.Messages({
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

    @NbBundle.Messages({
        "# {0} - repository name",
        "# {1} - queue name",
        "MSG_SwitchingQueue=Switching current queue to {1} in: {0}"
    })
    public void goToPatch (final File root, final String queueName, final String patchName) {
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
                                logger.output(Bundle.MSG_SwitchingQueue(root.getAbsolutePath(), queueName));
                                HgCommand.qSwitchQueue(root, queueName, logger);
                            }
                            if (isCanceled()) {
                                return false;
                            }
                            if (patchName != null) {
                                logger.output(NbBundle.getMessage(QGoToPatchAction.class, "MSG_GOTO_INFO_SEP", patchName, root.getAbsolutePath())); //NOI18N
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

    public void applyPatch (File repository, String patchName, OutputLogger logger) throws HgException {
        List<String> output = HgCommand.qGoToPatch(repository, patchName, logger);
        FailedPatchResolver resolver = new FailedPatchResolver(repository, output, logger);
        resolver.resolveFailure();
        logger.output(output);
    }

    public void popAllPatches (File repository, OutputLogger logger) throws HgException, MissingResourceException {
        logger.output(NbBundle.getMessage(QGoToPatchAction.class, "MSG_GOTO_EMPTY_INFO_SEP", repository.getAbsolutePath())); //NOI18N
        HgCommand.qPopPatches(repository, null, logger);
    }
    
}
