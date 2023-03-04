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

package org.netbeans.modules.git.ui.fetch;

import java.awt.EventQueue;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.fetch.FetchAction", category = "Git")
@ActionRegistration(displayName = "#LBL_FetchAction_Name")
@NbBundle.Messages("LBL_FetchAction_Name=F&etch...")
public class FetchAction extends SingleRepositoryAction {

    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/fetch-setting.png"; //NOI18N
    
    public FetchAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        fetch(repository);
    }
    
    public void fetch (final File repository, GitRemoteConfig remote) {
        if (remote.getUris().size() != 1) {
            Utils.post(new Runnable () {
                @Override
                public void run () {
                    fetch(repository);
                }
            });
        } else {
            fetch(repository, remote.getUris().get(0), remote.getFetchRefSpecs(), null);
        }
    }
    
    private void fetch (final File repository) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        try {
            info.refreshRemotes();
        } catch (GitException ex) {
            GitClientExceptionHandler.notifyException(ex, true);
        }
        final Map<String, GitRemoteConfig> remotes = info.getRemotes();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                FetchWizard wiz = new FetchWizard(repository, remotes);
                if (wiz.show()) {
                    Utils.logVCSExternalRepository("GIT", wiz.getFetchUri()); //NOI18N
                    fetch(repository, wiz.getFetchUri(), wiz.getFetchRefSpecs(), wiz.getRemoteToPersist());
                }
            }
        });
    }
    
    @NbBundle.Messages({
        "# {0} - repository name", "LBL_FetchAction.progressName=Fetching - {0}",
        "# {0} - branch name", "MSG_FetchAction.branchDeleted=Branch {0} deleted.",
        "MSG_FetchAction.progress.syncBranches=Synchronizing tracking branches"
    })
    public Task fetch (final File repository, final String target, final List<String> fetchRefSpecs, final String remoteNameToUpdate) {
        final List<String> fetchRefSpecsList = new ArrayList<>(fetchRefSpecs);
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            protected void perform () {
                try {
                    Set<String> toDelete = new HashSet<>();
                    for(ListIterator<String> it = fetchRefSpecsList.listIterator(); it.hasNext(); ) {
                        String refSpec = it.next();
                        if (refSpec.startsWith(GitUtils.REF_SPEC_DEL_PREFIX)) {
                            // branches are deleted separately
                            it.remove();
                            toDelete.add(refSpec.substring(GitUtils.REF_SPEC_DEL_PREFIX.length()));
                        }
                    }
                    GitClient client = getClient();
                    for (String branch : toDelete) {
                        client.deleteBranch(branch, true, getProgressMonitor());
                        getLogger().outputLine(Bundle.MSG_FetchAction_branchDeleted(branch));
                    }
                    if (!fetchRefSpecsList.isEmpty() && !isCanceled()) {
                        if (remoteNameToUpdate != null) {
                            GitRemoteConfig config = client.getRemote(remoteNameToUpdate, getProgressMonitor());
                            if (isCanceled()) {
                                return;
                            }
                            config = GitUtils.prepareConfig(config, remoteNameToUpdate, target, fetchRefSpecsList);
                            client.setRemote(config, getProgressMonitor());
                            if (isCanceled()) {
                                return;
                            }
                        }
                        Map<String, GitTransportUpdate> updates = fetchRepeatedly(client,
                                getProgressMonitor(), target, fetchRefSpecs);
                        if (!isCanceled()) {
                            FetchUtils.log(repository, updates, getLogger());
                        }
                        if (!isCanceled()) {
                            setDisplayName(Bundle.MSG_FetchAction_progress_syncBranches());
                            FetchUtils.syncTrackingBranches(repository, updates, this, getProgress(), true);
                        }
                    }
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                }
            }
        };
        return supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.LBL_FetchAction_progressName(repository.getName()));
    }

    static Map<String, GitTransportUpdate> fetchRepeatedly (GitClient client, ProgressMonitor monitor,
            String target, List<String> fetchRefSpecs) throws GitException {
        List<String> fetchRefSpecsList = new ArrayList<>(fetchRefSpecs);
        Map<String, GitTransportUpdate> result = null;
        while (result == null && !fetchRefSpecsList.isEmpty() && !monitor.isCanceled()) {
            try {
                result = client.fetch(target, fetchRefSpecsList, monitor);
            } catch (GitException ex) {
                boolean found = false;
                if (fetchRefSpecsList.size() > 1) {
                    for (ListIterator<String> it = fetchRefSpecsList.listIterator(); it.hasNext(); ) {
                        String refSpec = it.next();
                        String remoteHead = GitUtils.parseRemoteHeadFromFetch(refSpec);
                        if (refSpec != null && ex.getMessage().toLowerCase().matches(
                                MessageFormat.format(".*remote does not have {0} available for fetch.*", remoteHead))) { //NOI18N
                            it.remove();
                            found = true;
                            Logger.getLogger(FetchAction.class.getName()).log(Level.INFO,
                                    "Remote does not have head according to spec {0}, trying fetching without the spec.", refSpec); //NOI18N
                            break;
                        }
                    }
                }
                if (!found) {
                    throw ex;
                }
            }
        }
        return result;
    }
}
