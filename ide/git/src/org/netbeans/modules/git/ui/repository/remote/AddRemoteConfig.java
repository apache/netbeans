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
package org.netbeans.modules.git.ui.repository.remote;

import java.io.File;
import java.util.Collections;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Christian Lenz
 *
 * Adds a new remote configuration to a repository.
 */
public class AddRemoteConfig {

    public void addRemote(File repository) {
        AddRemotePanel panel = new AddRemotePanel();
        DialogDescriptor dd = new DialogDescriptor(panel,
            NbBundle.getMessage(RemoteRepositoryPanel.class, "LBL_AddRemoteConfig.title"));
        if (DialogDisplayer.getDefault().notify(dd) != NotifyDescriptor.OK_OPTION) {
            return;
        }

        final String remoteName = panel.getRemoteName();
        final String remoteUrl = panel.getRemoteURL();
        if (remoteName.isEmpty() || remoteUrl.isEmpty()) {
            return;
        }

        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            protected void perform() {
                try {
                    GitClient client = getClient();
                    GitRemoteConfig cfg = new GitRemoteConfig(
                        remoteName,
                        Collections.singletonList(remoteUrl),
                        Collections.<String>emptyList(),
                        Collections.singletonList("+refs/heads/*:refs/remotes/" + remoteName + "/*"),
                        Collections.<String>emptyList());
                    client.setRemote(cfg, getProgressMonitor());
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository,
            NbBundle.getMessage(AddRemoteConfig.class, "LBL_AddRemoteConfig.progressName"));
    }
}
