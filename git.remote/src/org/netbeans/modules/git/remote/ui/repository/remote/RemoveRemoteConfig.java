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

package org.netbeans.modules.git.remote.ui.repository.remote;

import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 */
public class RemoveRemoteConfig {

    public void removeRemote (VCSFileProxy repository, final String remoteName) {
        if (DialogDisplayer.getDefault().notify(new NotifyDescriptor(NbBundle.getMessage(RemoveRemoteConfig.class, "MSG_RemoveRemoteConfig.confirmation", remoteName), //NOI18N
                NbBundle.getMessage(RemoveRemoteConfig.class, "LBL_RemoveRemoteConfig.confirmation"), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE, //NOI18N
                new Object[] { NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION }, NotifyDescriptor.OK_OPTION)) == NotifyDescriptor.OK_OPTION) {
            GitProgressSupport supp = new GitProgressSupport() {
                @Override
                protected void perform () {
                    try {
                        GitClient client = getClient();
                        client.removeRemote(remoteName, getProgressMonitor());
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    }
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(RemoveRemoteConfig.class, "LBL_RemoveRemoteConfig.progressName")); //NOI18N
        }
    }
}
