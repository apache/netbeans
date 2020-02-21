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
package org.netbeans.modules.mercurial.remote.ui.branch;

import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.WorkingCopyInfo;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

@ActionID(id = "org.netbeans.modules.mercurial.remote.ui.branch.CreateBranchAction", category = "MercurialRemote")
@ActionRegistration(displayName = "#CTL_MenuItem_CreateBranch")
@Messages({
    "CTL_MenuItem_CreateBranch=Create &Branch...",
    "CTL_PopupMenuItem_CreateBranch=Create Branch..."
})
public class CreateBranchAction extends ContextAction {
    
    @Override
    protected boolean enable(Node[] nodes) {
        return HgUtils.isFromHgRepository(HgUtils.getCurrentContext(nodes));
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_CreateBranch"; // NOI18N
    }

    @Override
    @Messages({
        "# {0} - branch name", "MSG_CREATE_WC_MARKED=Working copy marked as branch {0}.\nDo not forget to commit to make the branch permanent."
    })
    protected void performContextAction(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final VCSFileProxy roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) {
            return;
        }
        final VCSFileProxy root = Mercurial.getInstance().getRepositoryRoot(roots[0]);

        CreateBranch createBranch = new CreateBranch();
        if (!createBranch.showDialog()) {
            return;
        }
        final String branchName = createBranch.getBranchName();
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                OutputLogger logger = getLogger();
                try {
                    logger.outputInRed(NbBundle.getMessage(CreateBranchAction.class, "MSG_CREATE_TITLE")); //NOI18N
                    logger.outputInRed(NbBundle.getMessage(CreateBranchAction.class, "MSG_CREATE_TITLE_SEP")); //NOI18N
                    logger.output(NbBundle.getMessage(CreateBranchAction.class, "MSG_CREATE_INFO_SEP", branchName, root.getPath())); //NOI18N
                    HgCommand.markBranch(root, branchName, logger);
                    WorkingCopyInfo.refreshAsync(root);
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                    Bundle.MSG_CREATE_WC_MARKED(branchName),
                                    NotifyDescriptor.INFORMATION_MESSAGE));
                    logger.output(Bundle.MSG_CREATE_WC_MARKED(branchName));
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }
                logger.outputInRed(NbBundle.getMessage(CreateBranchAction.class, "MSG_CREATE_DONE")); // NOI18N
                logger.output(""); // NOI18N
            }
        };
        support.start(rp, root, org.openide.util.NbBundle.getMessage(CreateBranchAction.class, "MSG_CreateBranch_Progress", branchName)); //NOI18N
    }
}
