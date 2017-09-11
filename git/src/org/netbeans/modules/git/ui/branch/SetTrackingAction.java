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

package org.netbeans.modules.git.ui.branch;

import org.netbeans.modules.git.client.GitClientExceptionHandler;
import java.io.File;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.branch.SetTrackingAction", category = "Git")
@ActionRegistration(displayName = "#LBL_SetTrackingAction_Name", lazy = true)
@NbBundle.Messages("LBL_SetTrackingAction_Name=Set Tra&cked Branch...")
public class SetTrackingAction extends SingleRepositoryAction {

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        GitBranch activeBranch = info.getActiveBranch();
        if (activeBranch.getName() != GitBranch.NO_BRANCH) {
            setupTrackedBranch(repository, activeBranch.getName(),
                    activeBranch.getTrackedBranch() == null ? null : activeBranch.getTrackedBranch().getName());
        }
    }

    public void setupTrackedBranch (final File repository, final String branchName, String currentTrackedBranch) {
        SelectTrackedBranch selectBranch = new SelectTrackedBranch(repository, branchName, currentTrackedBranch);
        if (selectBranch.open()) {
            setupTrackedBranchImmediately(repository, branchName, selectBranch.getSelectedBranch());
        }
    }
    
    @NbBundle.Messages({
        "LBL_SetTrackingAction.progressName=Setting Tracked Branch",
        "# {0} - branch name", "# {1} - tracked branch name",
        "MSG_SetTrackingAction.result=Branch \"{0}\" marked to track branch \"{1}\""
    })
    public void setupTrackedBranchImmediately (final File repository, final String branchName, final String targetBranch) {
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            protected void perform () {
                try {
                    GitClient client = getClient();
                    client.updateTracking(branchName, targetBranch, getProgressMonitor());
                    log(branchName, targetBranch);
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                }
            }

            private void log (String branchName, String targetBranchName) {
                OutputLogger logger = getLogger();
                logger.outputLine(Bundle.MSG_SetTrackingAction_result(branchName, targetBranchName));
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.LBL_SetTrackingAction_progressName());
    }

}
