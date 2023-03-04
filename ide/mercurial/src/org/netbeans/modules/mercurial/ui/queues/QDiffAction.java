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
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.diff.DiffAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.mercurial.ui.queues.QDiffAction", category = "Mercurial/Queues")
@ActionRegistration(displayName = "#CTL_MenuItem_QDiff")
@NbBundle.Messages({
    "CTL_MenuItem_QDiff=&Diff",
    "CTL_PopupMenuItem_QDiff=Diff"
})
public class QDiffAction extends ContextAction {

    @Override
    protected boolean enable (Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName (Node[] nodes) {
        return "CTL_MenuItem_QDiff"; //NOI18N
    }

    @Override
    @NbBundle.Messages({
        "# {0} - number of selected files",
        "LBL_DiffView.name.files={0} files",
        "# {0} - label for the selected context",
        "LBL_DiffView.name=QDiff - {0}"
    })
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
                try {
                    List<HgLogMessage> parents = HgCommand.getParents(root, null, null);
                    if (parents.size() != 1 || !Arrays.asList(parents.get(0).getTags()).contains(QPatch.TAG_QTIP)) {
                        NotifyDescriptor.Message e = new NotifyDescriptor.Message(NbBundle.getMessage(QDiffAction.class, "MSG_DiffAction.error.notAtTip"), NotifyDescriptor.ERROR_MESSAGE); //NOI18N
                        DialogDisplayer.getDefault().notifyLater(e);
                    } else {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                SystemAction.get(DiffAction.class).diff(roots, HgLogMessage.HgRevision.QDIFF_BASE,
                                        HgLogMessage.HgRevision.CURRENT,
                                        Bundle.LBL_DiffView_name(roots.length == 1 
                                        ? roots[0].getName() 
                                        : Bundle.LBL_DiffView_name_files(roots.length)),
                                        true, false);
                            }
                        });
                    }
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }
            }
            
        }.start(Mercurial.getInstance().getRequestProcessor(root), root, NbBundle.getMessage(QDiffAction.class, "LBL_DiffAction.progress")); //NOI18N
    }

}
