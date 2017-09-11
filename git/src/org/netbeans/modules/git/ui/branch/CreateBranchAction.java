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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.checkout.SwitchBranchAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.branch.CreateBranchAction", category = "Git")
@ActionRegistration(displayName = "#LBL_CreateBranchAction_Name")
@NbBundle.Messages("LBL_CreateBranchAction_Name=Create &Branch...")
public class CreateBranchAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(CreateBranchAction.class.getName());

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        createBranch(repository, info.getActiveBranch().getName().equals(GitBranch.NO_BRANCH) ? GitUtils.HEAD : info.getActiveBranch().getName());
    }

    @NbBundle.Messages("LBL_CreateBranchAction.progressName=Create Branch")
    public void createBranch (final File repository, String preselectedRevision) {
        final CreateBranch createBranch = new CreateBranch(repository, preselectedRevision, getBranches(repository));
        if (createBranch.show()) {
            if (createBranch.isCheckoutSelected()) {
                // create and switch to branch
                SystemAction.get(SwitchBranchAction.class).checkoutRevision(repository, createBranch.getRevision(), 
                        createBranch.getBranchName(), Bundle.LBL_CreateBranchAction_progressName());
            } else {
                GitProgressSupport supp = new GitProgressSupport() {
                    @Override
                    protected void perform () {
                        try {
                            GitClient client = getClient();
                            String revision = createBranch.getRevision();
                            LOG.log(Level.FINE, "Creating a branch: {0}", revision); //NOI18N
                            GitBranch branch = client.createBranch(createBranch.getBranchName(), createBranch.getRevision(), getProgressMonitor());
                            log(revision, branch);
                        } catch (GitException ex) {
                            GitClientExceptionHandler.notifyException(ex, true);
                        }
                    }

                    private void log (String revision, GitBranch branch) {
                        OutputLogger logger = getLogger();
                        logger.outputLine(NbBundle.getMessage(CreateBranchAction.class, "MSG_CreateBranchAction.branchCreated", new Object[] { branch.getName(), revision, branch.getId() })); //NOI18N
                    }
                };
                supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.LBL_CreateBranchAction_progressName());
            }
        }
    }

    private Map<String, GitBranch> getBranches (File repository) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        info.refresh();
        return info.getBranches();
    }

}
