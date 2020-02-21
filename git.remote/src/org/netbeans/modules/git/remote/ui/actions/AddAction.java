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

package org.netbeans.modules.git.remote.ui.actions;

import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.FileInformation;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.actions.AddAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_AddAction_Name")
public class AddAction extends SingleRepositoryAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/remote/resources/icons/add.png"; //NOI18N
    
    public AddAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected void performAction (VCSFileProxy repository, final VCSFileProxy[] roots, VCSContext context) {
        GitProgressSupport supp = new GitProgressSupport () {
            @Override
            protected void perform() {
                GitClient client;
                VCSFileProxy[] actionRoots = GitUtils.listFiles(roots, FileInformation.STATUS_LOCAL_CHANGES);
                if (actionRoots.length == 0) {
                    return;
                }
                try {
                    client = getClient();
                    client.addNotificationListener(new DefaultFileListener(actionRoots));
                    client.add(actionRoots, getProgressMonitor());
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                    Git.getInstance().getFileStatusCache().refreshAllRoots(actionRoots);                    
                }               
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(AddAction.class, "LBL_AddProgress"));
    }
    
}
