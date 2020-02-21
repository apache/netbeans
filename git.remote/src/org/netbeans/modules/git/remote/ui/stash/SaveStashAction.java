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

package org.netbeans.modules.git.remote.ui.stash;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.FileInformation;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.GitAction;
import org.netbeans.modules.git.remote.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.stash.SaveStashAction", category = "GitRemote")
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
    protected void performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        saveStash(repository, roots);
    }

    public void saveStash (final VCSFileProxy repository) {
        saveStash(repository, new VCSFileProxy[0]);
    }
    
    public void saveStash (final VCSFileProxy repository, final VCSFileProxy[] roots) {
        final VCSFileProxy[] modifications = Git.getInstance().getFileStatusCache().listFiles(new VCSFileProxy[] { repository }, FileInformation.STATUS_LOCAL_CHANGES);
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

    private void start (final VCSFileProxy repository, final VCSFileProxy[] modifications, final SaveStash saveStash) {
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
                    }, new VCSFileProxy[] { repository });
                    RepositoryInfo.getInstance(repository).refreshStashes();
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    if (modifications.length > 0) {
                        setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                        Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<VCSFileProxy, Collection<VCSFileProxy>>singletonMap(repository, Arrays.asList(modifications)));
                    }
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.MSG_ApplyStashAction_progressName());
    }
}
