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

package org.netbeans.modules.git.ui.stash;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.annotations.common.StaticResource;
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
 * Git stash action, currently only repo wide.
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
        
    // TODO pick/create better icon
    @StaticResource
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/get_clean.png"; //NOI18N

    public SaveStashAction() {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource() {
        return ICON_RESOURCE;
    }

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
                NotifyDescriptor.OK_OPTION
            );
            DialogDisplayer.getDefault().notifyLater(nd);
            return;
        }
        Mutex.EVENT.readAccess(() -> {
            SaveStash saveStash = new SaveStash(repository, roots, RepositoryInfo.getInstance(repository).getActiveBranch());
            if (saveStash.show()) {
                start(repository, modifications, saveStash);
            }
        });
    }

    private void start (final File repository, final File[] modifications, final SaveStash saveStash) {
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            protected void perform () {
                try {
                    final GitClient client = getClient();
                    GitUtils.runWithoutIndexing(() -> {
                        client.stashSave(saveStash.getMessage(), saveStash.isIncludeUncommitted(), getProgressMonitor());
                        return null;
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
