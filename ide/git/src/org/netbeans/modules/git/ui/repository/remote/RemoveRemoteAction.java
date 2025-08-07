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
import java.util.Map;
import javax.swing.JComboBox;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Christian Lenz
 */
@ActionID(id = "org.netbeans.modules.git.ui.repository.remote.RemoveRemoteAction", category = "Git")
@ActionRegistration(displayName = "#LBL_RemoveRemoteAction_Name")
public class RemoveRemoteAction extends SingleRepositoryAction {

    @Override
    public void performAction(File repository, File[] roots, VCSContext context) {
        GitRemoteConfig remote = context != null ? context.getElements().lookup(GitRemoteConfig.class) : null;
        String remoteName = remote != null ? remote.getRemoteName() : null;
        if (remoteName == null) {
            Map<String, GitRemoteConfig> remotes = RepositoryInfo.getInstance(repository).getRemotes();
            if (remotes.isEmpty()) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(RemoteRepositoryPanel.class, "LBL_RemoveRemoteAction_noRemotes")));
                return;
            }
            var combo = new JComboBox<String>(remotes.keySet().toArray(new String[0]));
            NotifyDescriptor nd = new NotifyDescriptor(combo, NbBundle.getMessage(RemoteRepositoryPanel.class, "LBL_RemoveRemoteAction_Select"), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null);
            if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                return;
            }
            remoteName = (String) combo.getSelectedItem();
        }

        new RemoveRemoteConfig().removeRemote(repository, remoteName);
    }
}
