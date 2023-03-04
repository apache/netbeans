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

package org.netbeans.modules.git.ui.actions;

import java.io.File;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.git.ui.actions.AddAction", category = "Git")
@ActionRegistration(displayName = "#LBL_AddAction_Name")
public class AddAction extends SingleRepositoryAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/add.png"; //NOI18N
    
    public AddAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected void performAction (File repository, final File[] roots, VCSContext context) {
        GitProgressSupport supp = new GitProgressSupport () {
            @Override
            protected void perform() {
                GitClient client;
                File[] actionRoots = GitUtils.listFiles(roots, FileInformation.STATUS_LOCAL_CHANGES);
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
