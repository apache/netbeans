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

import java.io.File;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.WorkingCopyInfo;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

@ActionID(id = "org.netbeans.modules.mercurial.ui.branch.CreateBranchAction", category = "Mercurial")
@ActionRegistration(displayName = "#CTL_MenuItem_CreateBranch")
@NbBundle.Messages({
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
    @NbBundle.Messages({
        "# {0} - branch name", "MSG_CREATE_WC_MARKED=Working copy marked as branch {0}.\nDo not forget to commit to make the branch permanent."
    })
    protected void performContextAction(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        final File roots[] = HgUtils.getActionRoots(ctx);
        if (roots == null || roots.length == 0) return;
        final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);

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
                    logger.output(NbBundle.getMessage(CreateBranchAction.class, "MSG_CREATE_INFO_SEP", branchName, root.getAbsolutePath())); //NOI18N
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
