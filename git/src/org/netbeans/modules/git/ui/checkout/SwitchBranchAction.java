/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.checkout;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.AbstractMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.LifecycleManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.checkout.SwitchBranchAction", category = "Git")
@ActionRegistration(displayName = "#LBL_SwitchBranchAction_Name")
public class SwitchBranchAction extends AbstractCheckoutAction {

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        if (canCheckout(info)) {
            checkoutRevision(repository, info.getActiveBranch().getName().equals(GitBranch.NO_BRANCH) ? GitUtils.HEAD : info.getActiveBranch().getName());
        }
    }

    public void checkoutRevision (final File repository, String preselectedRevision) {
        checkoutRevision(repository, new SwitchBranch(repository, RepositoryInfo.getInstance(repository), preselectedRevision), "LBL_SwitchBranchAction.progressName", //NOI18N
                new HelpCtx("org.netbeans.modules.git.ui.checkout.SwitchBranchAction")); //NOI18N
    }
    
    private static class SwitchBranch extends AbstractCheckoutRevision {
        public SwitchBranch (File repository, RepositoryInfo info, String initialRevision) {
            super(info, new RevisionDialogController(repository, new File[] { repository }, info.getBranches(), null));
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
            final AbstractMap.SimpleImmutableEntry<File, File[]> roots = GitUtils.getActionRoots(ctx);
            if (roots != null) {
                final File root = roots.getKey();
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
