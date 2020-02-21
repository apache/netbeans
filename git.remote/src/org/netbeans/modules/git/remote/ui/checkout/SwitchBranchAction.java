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

package org.netbeans.modules.git.remote.ui.checkout;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.AbstractMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.LifecycleManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.checkout.SwitchBranchAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_SwitchBranchAction_Name")
public class SwitchBranchAction extends AbstractCheckoutAction {

    @Override
    protected void performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        checkoutRevision(repository, info.getActiveBranch().getName().equals(GitBranch.NO_BRANCH) ? GitUtils.HEAD : info.getActiveBranch().getName());
    }

    public void checkoutRevision (final VCSFileProxy repository, String preselectedRevision) {
        checkoutRevision(repository, new SwitchBranch(repository, RepositoryInfo.getInstance(repository), preselectedRevision), "LBL_SwitchBranchAction.progressName", //NOI18N
                new HelpCtx("org.netbeans.modules.git.remote.ui.checkout.SwitchBranchAction")); //NOI18N
    }
    
    private static class SwitchBranch extends AbstractCheckoutRevision {
        public SwitchBranch (VCSFileProxy repository, RepositoryInfo info, String initialRevision) {
            super(info, new RevisionDialogController(repository, new VCSFileProxy[] { repository }, info.getBranches(), null));
            panel.jLabel1.setText(NbBundle.getMessage(CheckoutRevisionAction.class, "SwitchBranch.jLabel1.text")); //NOI18N
        }

        @Override
        protected String getOkButtonLabel () {
            return NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_SwitchBranch.OKButton.text"); //NOI18N
        }

        @Override
        protected String getDialogTitle () {
            return NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_SwitchBranch.title"); //NOI18N
        }
    }

    @NbBundle.Messages({
        "# {0} - branch name", "SwitchBranchAction.KnownBranchAction.name=Switch to {0}",
        "# {0} - branch name", "SwitchBranchAction.KnownBranchAction.progress=Switching to {0}"
    })
    public static class KnownBranchAction extends AbstractAction {
        private final VCSContext ctx;
        private final String branchName;

        public KnownBranchAction (String recentBranch, VCSContext ctx) {
            super(Bundle.SwitchBranchAction_KnownBranchAction_name(recentBranch));
            this.branchName = recentBranch;
            this.ctx = ctx;
        }

        @Override
        @NbBundle.Messages({
            "MSG_GitAction.savingFiles.progress=Preparing Git action"
        })
        public void actionPerformed (ActionEvent e) {
            final AbstractMap.SimpleImmutableEntry<VCSFileProxy, VCSFileProxy[]> roots = GitUtils.getActionRoots(ctx);
            if (roots != null) {
                final VCSFileProxy root = roots.getKey();
                final AtomicBoolean canceled = new AtomicBoolean(false);
                Runnable run = new Runnable() {

                    @Override
                    public void run () {
                        LifecycleManager.getDefault().saveAll();
                        Utils.logVCSActionEvent("Git"); //NOI18N
                        if (!canceled.get()) {
                            EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run () {
                                    SystemAction.get(SwitchBranchAction.class).checkoutRevision(root, branchName, null,
                                            Bundle.SwitchBranchAction_KnownBranchAction_progress(branchName));
                                }
                            });
                        }
                    }
                };
                ProgressUtils.runOffEventDispatchThread(run, Bundle.MSG_GitAction_savingFiles_progress(), canceled, false);
            }
        }
    }
}
