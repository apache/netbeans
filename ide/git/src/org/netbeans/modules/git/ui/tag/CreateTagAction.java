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
package org.netbeans.modules.git.ui.tag;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitTag;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.tag.CreateTagAction", category = "Git")
@ActionRegistration(displayName = "#LBL_CreateTagAction_Name")
@NbBundle.Messages("LBL_CreateTagAction_Name=Crea&te Tag...")
public class CreateTagAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(CreateTagAction.class.getName());
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/tag.png"; //NOI18N

    public CreateTagAction() {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource() {
        return ICON_RESOURCE;
    }

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        createTag(repository, info.getActiveBranch().getName().equals(GitBranch.NO_BRANCH) ? GitUtils.HEAD : info.getActiveBranch().getName());
    }

    public void createTag (final File repository, String preselectedRevision) {
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
                LOG.log(Level.FINE, "Creating a tag: {0}/{1}", new Object[] { createTag.getTagName(), createTag.getRevision() }); //NOI18N
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
