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
package org.netbeans.modules.git.remote.ui.tag;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitTag;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.remote.ui.output.OutputLogger;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.tag.CreateTagAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_CreateTagAction_Name")
@NbBundle.Messages("LBL_CreateTagAction_Name=Crea&te Tag...")
public class CreateTagAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(CreateTagAction.class.getName());

    @Override
    protected void performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        createTag(repository, info.getActiveBranch().getName().equals(GitBranch.NO_BRANCH) ? GitUtils.HEAD : info.getActiveBranch().getName());
    }

    public void createTag (final VCSFileProxy repository, String preselectedRevision) {
        final CreateTag createTag = new CreateTag(repository, preselectedRevision, "");
        if (createTag.show()) {
            GitProgressSupport supp = new GitProgressSupport() {
                @Override
                protected void perform () {
                    try {
                        new CreateTagProcess(createTag, this, getClient()).call();
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    }
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(CreateTagAction.class, "LBL_CreateTagAction.progressName")); //NOI18N
        }
    }
    
    static class CreateTagProcess implements Callable<GitTag> {
        
        private final GitProgressSupport supp;
        private final CreateTag createTag;
        private final GitClient client;

        public CreateTagProcess (CreateTag createTag, GitProgressSupport supp, GitClient client) {
            this.supp = supp;
            this.createTag = createTag;
            this.client = client;
        }
        
        @Override
        public GitTag call () {
            try {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Creating a tag: {0}/{1}", new Object[] { createTag.getTagName(), createTag.getRevision() }); //NOI18N
                }
                GitTag tag = client.createTag(createTag.getTagName(), createTag.getRevision(),
                        createTag.getTagMessage(), false, createTag.isForceUpdate(), supp.getProgressMonitor());
                log(tag);
                return tag;
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, true);
            }
            return null;
        }

        private void log (GitTag tag) {
            OutputLogger logger = supp.getLogger();
            logger.outputLine(NbBundle.getMessage(CreateTagAction.class, "MSG_CreateTagAction.tagCreated", new Object[] { tag.getTagName(), //NOI18N
                tag.getTaggedObjectId(),
                tag.getTagId(),
                tag.getTagger().toString(),
                tag.getMessage() }));
        }
        
    }
}
