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

package org.netbeans.modules.git.ui.stash;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondra Vrabec
 */
@ActionID(id = "org.netbeans.modules.git.ui.stash.SaveStashAction", category = "Git")
@ActionRegistration(displayName = "#LBL_SaveStashAction_Name")
@NbBundle.Messages({
    "LBL_SaveStashAction_Name=&Stash Changes",
    "LBL_SaveStashAction_PopupName=Stash Changes",
    "MSG_SaveStashAction.progressName=Stashing local changes",
    "LBL_SaveStashAction.noModifications=No Local Modifications",
    "MSG_SaveStashAction.noModifications=There are no uncommitted changes to stash."
})
public class SaveStashAction extends SingleRepositoryAction {
    
    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        saveStash(repository, roots);
    }

    public void saveStash (final File repository) {
        saveStash(repository, new File[0]);
    }
    
    public void saveStash (final File repository, final File[] roots) {
        final File[] modifications = Git.getInstance().getFileStatusCache().listFiles(new File[] { repository }, FileInformation.STATUS_LOCAL_CHANGES);
        if (modifications.length == 0) {
            NotifyDescriptor nd = new NotifyDescriptor(
                Bundle.MSG_SaveStashAction_noModifications(),
                Bundle.LBL_SaveStashAction_noModifications(),
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.INFORMATION_MESSAGE,
                new Object[]{NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
            DialogDisplayer.getDefault().notifyLater(nd);
            return;
        }
        Mutex.EVENT.readAccess(new Runnable () {

            @Override
            public void run () {
                SaveStash saveStash = new SaveStash(repository, roots, RepositoryInfo.getInstance(repository).getActiveBranch());
                if (saveStash.show()) {
                    start(repository, modifications, saveStash);
                }
            }
            
        });
    }

    private void start (final File repository, final File[] modifications, final SaveStash saveStash) {
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            protected void perform () {
                try {
                    final GitClient client = getClient();
                    GitUtils.runWithoutIndexing(new Callable<Void>() {

                        @Override
                        public Void call () throws Exception {
                            client.stashSave(saveStash.getMessage(), saveStash.isIncludeUncommitted(), getProgressMonitor());
                            return null;
                        }
                    }, new File[] { repository });
                    RepositoryInfo.getInstance(repository).refreshStashes();
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    if (modifications.length > 0) {
                        setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                        Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repository, Arrays.asList(modifications)));
                    }
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.MSG_SaveStashAction_progressName());
    }
}
