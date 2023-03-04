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
package org.netbeans.modules.mercurial.ui.branch;

import java.awt.EventQueue;
import java.io.File;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.commit.CommitAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

@ActionID(id = "org.netbeans.modules.mercurial.ui.branch.CloseBranchAction", category = "Mercurial")
@ActionRegistration(displayName = "#CTL_MenuItem_CloseBranch")
@NbBundle.Messages({
    "CTL_MenuItem_CloseBranch=C&lose Branch...",
    "CTL_PopupMenuItem_CloseBranch=Close Branch..."
})
public class CloseBranchAction extends ContextAction {
    
    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_CloseBranch"; //NOI18N
    }

    @NbBundle.Messages({
        "# {0} - repository name", "MSG_CloseBranchAction.interruptedRebase.error=Repository {0} is in the middle of an interrupted rebase.\n"
            + "Finish the rebase before closing this branch."
    })
    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);

        final RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                setDisplayName(NbBundle.getMessage(CloseBranchAction.class, "MSG_CloseBranch.Progress.preparing")); //NOI18N
                try {
                    final String branchName = HgCommand.getBranch(root);
                    if (branchName.equals(HgBranch.DEFAULT_NAME)) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(CloseBranchAction.class, "MSG_CloseBranch.error.defaultBranch"), NotifyDescriptor.ERROR_MESSAGE); //NOI18N
                        DialogDisplayer.getDefault().notifyLater(nd);
                        return;
                    }
                    int numberOfHeads = getBranchHeadCount(branchName);
                    if (numberOfHeads == 0) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(CloseBranchAction.class, "MSG_CloseBranch.error.branchClosed", //NOI18N
                                new Object[] { branchName }), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    } else if (numberOfHeads > 1) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(CloseBranchAction.class, "MSG_CloseBranch.error.moreHeads", //NOI18N
                                new Object[] { branchName, numberOfHeads }), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                    } else if (HgUtils.isRebasing(root)) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                Bundle.MSG_CloseBranchAction_interruptedRebase_error(root.getName()),
                                NotifyDescriptor.ERROR_MESSAGE));
                    } else if (!isCanceled()) {
                        final VCSContext ctx = VCSContext.forNodes(new Node[] { new AbstractNode(Children.LEAF, Lookups.fixed((Object[]) roots)) {
                            @Override
                            public String getDisplayName () {
                                return root.getName();
                            }
                        }});
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                SystemAction.get(CommitAction.class).closeBranch(branchName, ctx, 
                                        NbBundle.getMessage(CloseBranchAction.class, "MSG_CloseBranch.commit.title", new Object[] { branchName, Utils.getContextDisplayName(ctx) })); //NOI18N
                            }
                        });
                    }
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }
            }

            private int getBranchHeadCount (String branchName) throws HgException {
                HgLogMessage[] messages = HgCommand.getHeadRevisionsInfo(root, false, getLogger());
                int headsInBranch = 0;
                for (HgLogMessage message : messages) {
                    if (message.getBranches().length == 0 && branchName.equals(HgBranch.DEFAULT_NAME)) {
                        ++headsInBranch;
                    }
                    for (String b : message.getBranches()) {
                        if (b.equals(branchName)) {
                            ++headsInBranch;
                        }
                    }
                }
                return headsInBranch;
            }
        };
        support.start(rp, root, NbBundle.getMessage(CloseBranchAction.class, "MSG_CloseBranch.Progress")); //NOI18N
    }
}
