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

package org.netbeans.modules.git.ui.fetch;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.ActionProgress;
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

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.fetch.PullFromUpstreamAction", category = "Git")
@ActionRegistration(displayName = "#LBL_PullFromUpstreamAction_Name")
@Messages("LBL_PullFromUpstreamAction_Name=&Pull from Upstream")
public class PullFromUpstreamAction extends MultipleRepositoryAction {

    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/pull.png"; //NOI18N
    
    public PullFromUpstreamAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected RequestProcessor.Task performAction (File repository, File[] roots, VCSContext context) {
        ActionProgress p = pull(repository);
        return p == null ? null : p.getActionTask();
    }
    
    @Messages({"LBL_Pull.pullFromUpstreamFailed=Pull from Upstream Failed", "LBL_PullFromUpstreamAction.preparing=Preparing Pull..."})
    public ActionProgress pull (final File repository) {
        final ActionProgress[] t = new ActionProgress[1];
        GitProgressSupport supp = new GitProgressSupport.NoOutputLogging() {
            @Override
            protected void perform () {
                RepositoryInfo info = RepositoryInfo.getInstance(repository);
                try {
                    info.refreshRemotes();
                } catch (GitException ex) {
                    Logger.getLogger(FetchFromUpstreamAction.class.getName()).log(Level.INFO, null, ex);
                }
                String errorLabel = Bundle.LBL_Pull_pullFromUpstreamFailed();
                GitBranch activeBranch = info.getActiveBranch();
                if (activeBranch == null) {
                    return;
                }
                GitBranch trackedBranch = GitUtils.getTrackedBranch(info, null);

                // If no tracked branch and multiple remotes, use full pull UI
                if (trackedBranch == null && info.getRemotes().size() > 1) {
                    SystemAction.get(PullAction.class).pull(repository, activeBranch);
                    return;
                }

                if (trackedBranch == null) {
                    return;
                }

                GitRemoteConfig cfg = FetchUtils.getRemoteConfigForActiveBranch(trackedBranch, info, errorLabel);
                if (cfg == null) {
                    return;
                }
                String uri = cfg.getUris().get(0);
                Utils.logVCSExternalRepository("GIT", uri); //NOI18N
                if (!isCanceled()) {
                    t[0] = SystemAction.get(PullAction.class).pull(repository, uri, cfg.getFetchRefSpecs(), trackedBranch.getName(), null);
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.LBL_PullFromUpstreamAction_preparing()).waitFinished();
        return t[0];
    }

}
