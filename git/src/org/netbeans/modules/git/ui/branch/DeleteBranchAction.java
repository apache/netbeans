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

import org.netbeans.libs.git.GitException.NotMergedException;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.branch.DeleteBranchAction", category = "Git")
@ActionRegistration(displayName = "#LBL_DeleteBranchAction_Name")
public class DeleteBranchAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(DeleteBranchAction.class.getName());

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        throw new UnsupportedOperationException();
    }

    public void deleteBranch (final File repository, final String branchName) {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(DeleteBranchAction.class, "MSG_DeleteBranchAction.confirmation", branchName), //NOI18N
                NbBundle.getMessage(DeleteBranchAction.class, "LBL_DeleteBranchAction.confirmation"), //NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);

        if (NotifyDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(nd)) {
            GitProgressSupport supp = new GitProgressSupport() {
                @Override
                protected void perform () {
                    try {
                        GitClient client = getClient();
                        boolean forceDelete = false;
                        boolean cont;
                        do {
                            try {
                                LOG.log(Level.FINE, "Deleting a branch: {0}/{1}", new Object[] { branchName, forceDelete }); //NOI18N
                                client.deleteBranch(branchName, forceDelete, getProgressMonitor());
                                cont = false;
                            } catch (GitException.NotMergedException ex) {
                                cont = forceDelete = handleException(ex);
                            }
                        } while (cont);
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    }
                }

                private boolean handleException (NotMergedException ex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(DeleteBranchAction.class, "MSG_DeleteBranchAction.notMerged", ex.getUnmergedRevision()), //NOI18N
                            NbBundle.getMessage(DeleteBranchAction.class, "LBL_DeleteBranchAction.notMerged"), //NOI18N
                            NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
                    return NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(nd);
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(DeleteBranchAction.class, "LBL_DeleteBranchAction.progressName", branchName)); //NOI18N
        }
    }
}
