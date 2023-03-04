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

package org.netbeans.modules.git.ui.repository.remote;

import java.io.File;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class RemoveRemoteConfig {

    public void removeRemote (File repository, final String remoteName) {
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
