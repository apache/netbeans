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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.fetch.FetchFromUpstreamAction", category = "Git")
@ActionRegistration(displayName = "#LBL_FetchFromUpstreamAction_Name")
@Messages("LBL_FetchFromUpstreamAction_Name=&Fetch from Upstream")
public class FetchFromUpstreamAction extends MultipleRepositoryAction {

    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/fetch.png"; //NOI18N
    
    public FetchFromUpstreamAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected RequestProcessor.Task performAction (File repository, File[] roots, VCSContext context) {
        return fetch(repository);
    }
    
    @Messages({
        "LBL_Fetch.fetchFromUpstreamFailed=Fetch from Upstream Failed",
        "LBL_FetchFromUpstreamAction.preparing=Preparing Fetch...",
        "# {0} - repository name", "MSG_FetchFromUpstreamAction.error.noRemote=No remote configuration found for repository \"{0}\"."
    })
    private Task fetch (final File repository) {
        final Task[] t = new Task[1];
        GitProgressSupport supp = new GitProgressSupport.NoOutputLogging() {
            @Override
            protected void perform () {
                RepositoryInfo info = RepositoryInfo.getInstance(repository);
                try {
                    info.refreshRemotes();
                } catch (GitException ex) {
                    Logger.getLogger(FetchFromUpstreamAction.class.getName()).log(Level.INFO, null, ex);
                }
                GitBranch trackedBranch = GitUtils.getTrackedBranch(info, null);
                GitRemoteConfig cfg;
                String errorLabel = Bundle.LBL_Fetch_fetchFromUpstreamFailed();
                if (trackedBranch == null) {
                    // is there a default?
                    cfg = info.getRemotes().get(GitUtils.ORIGIN);
                    if (cfg == null) {
                        GitUtils.notifyError(errorLabel, Bundle.MSG_FetchFromUpstreamAction_error_noRemote(repository.getName()));
                    }
                } else {
                    cfg = FetchUtils.getRemoteConfigForActiveBranch(trackedBranch, info, errorLabel);
                }
                if (cfg == null) {
                    return;
                }
                String uri = cfg.getUris().get(0);
                Utils.logVCSExternalRepository("GIT", uri); //NOI18N
                if (!isCanceled()) {
                    t[0] = SystemAction.get(FetchAction.class).fetch(repository, uri, cfg.getFetchRefSpecs(), null);
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.LBL_FetchFromUpstreamAction_preparing()).waitFinished();
        return t[0];
    }

}
