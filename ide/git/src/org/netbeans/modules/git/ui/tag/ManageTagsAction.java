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
package org.netbeans.modules.git.ui.tag;

import java.awt.EventQueue;
import java.io.File;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitTag;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
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
@ActionID(id = "org.netbeans.modules.git.ui.tag.ManageTagsAction", category = "Git")
@ActionRegistration(displayName = "#LBL_ManageTagsAction_Name")
@NbBundle.Messages("LBL_ManageTagsAction_Name=Mana&ge Tags...")
public class ManageTagsAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(ManageTagsAction.class.getName());
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/tags.png"; //NOI18N

    public ManageTagsAction() {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource() {
        return ICON_RESOURCE;
    }

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        showTagManager(repository, info.getActiveBranch().getName().equals(GitBranch.NO_BRANCH) ? GitUtils.HEAD : info.getActiveBranch().getName());
    }

    public void showTagManager (final File repository, final String preselectedTag) {
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            protected void perform () {
                try {
                    GitClient client = getClient();
                    Map<String, GitTag> tags = client.getTags(getProgressMonitor(), true);
                    final ManageTags createTag = new ManageTags(repository, tags, preselectedTag);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            createTag.show();
                        }
                    });
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(ManageTagsAction.class, "LBL_ManageTagsAction.listingTags.progressName")); //NOI18N
    }    
}
