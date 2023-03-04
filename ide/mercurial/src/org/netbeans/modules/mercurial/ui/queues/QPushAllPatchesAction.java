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
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.queues.QPushAllPatchesAction", category = "Mercurial/Queues")
@ActionRegistration(displayName = "#CTL_MenuItem_QPushAllPatches")
@NbBundle.Messages({
    "CTL_MenuItem_QPushAllPatches=Pu&sh All Patches",
    "CTL_PopupMenuItem_QPushAllPatches=Push All Patches"
})
public class QPushAllPatchesAction extends ContextAction {

    @Override
    protected boolean enable (Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName (Node[] nodes) {
        return "CTL_MenuItem_QPushAllPatches"; //NOI18N
    }

    @Override
    protected void performContextAction (Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);
        new HgProgressSupport() {
            @Override
            protected void perform () {
                if (!QUtils.isMQEnabledExtension(root)) {
                    return;
                }
                final OutputLogger logger = getLogger();
                try {
                    logger.outputInRed(NbBundle.getMessage(QPushAllPatchesAction.class, "MSG_PUSH_TITLE")); //NOI18N
                    logger.outputInRed(NbBundle.getMessage(QPushAllPatchesAction.class, "MSG_PUSH_TITLE_SEP")); //NOI18N
                    logger.output(NbBundle.getMessage(QPushAllPatchesAction.class, "MSG_PUSH_INFO_SEP", root.getAbsolutePath())); //NOI18N
                    List<String> output = HgUtils.runWithoutIndexing(new Callable<List<String>>() {
                        @Override
                        public List<String> call () throws Exception {
                            return HgCommand.qPushPatches(root, null, logger);
                        }
                    }, roots);
                    FailedPatchResolver resolver = new FailedPatchResolver(root, output, logger);
                    resolver.resolveFailure();
                    logger.output(output);
                    HgLogMessage parent = HgCommand.getParents(root, null, null).get(0);
                    Mercurial.getInstance().historyChanged(root);
                    logger.output(""); // NOI18N
                    HgUtils.logHgLog(parent, logger);
                    logger.outputInRed(NbBundle.getMessage(QPushAllPatchesAction.class, "MSG_PUSH_DONE")); // NOI18N
                    logger.output(""); // NOI18N
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }
            }
        }.start(Mercurial.getInstance().getRequestProcessor(root), root, NbBundle.getMessage(QPushAllPatchesAction.class, "LBL_QPushAllPatchesAction.progress")); //NOI18N
    }
    
}
