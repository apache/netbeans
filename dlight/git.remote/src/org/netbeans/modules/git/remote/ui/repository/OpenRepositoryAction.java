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

package org.netbeans.modules.git.remote.ui.repository;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.GitRepositories;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.repository.OpenRepositoryAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_OpenRepositoryAction_Name")
@NbBundle.Messages({
    "LBL_OpenRepositoryAction_Name=&Open Repository",
    "LBL_OpenRepositoryAction_PopupName=Open Repository",
    "CTL_OpenRepository_okButton=Open",
    "CTL_OpenRepository_ACSD=Choose Git repository to open in the Repository Browser"
})
public class OpenRepositoryAction extends AbstractAction {

    public OpenRepositoryAction () {
        super(Bundle.LBL_OpenRepositoryAction_PopupName());
    }

    @Override
    public void actionPerformed (ActionEvent event) {
        //TODO: provide file system chooser
        //FileSystem[] fileSystems = VCSFileProxySupport.getConnectedFileSystems();
        //if (fileSystems.length == 0) {
        //    return;
        //}
        // Now use default FS
        FileSystem defaultFileSystem = VCSFileProxySupport.getDefaultFileSystem();
        if (defaultFileSystem == null) {
            return;
        }
        VCSFileProxy root = VCSFileProxy.createFileProxy(defaultFileSystem.getRoot());
        JFileChooser fileChooser = VCSFileProxySupport.createFileChooser(root);
        fileChooser.setDialogTitle(Bundle.CTL_OpenRepository_ACSD());
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(null, Bundle.CTL_OpenRepository_okButton());
        final VCSFileProxy f = VCSFileProxySupport.getSelectedFile(fileChooser);
        if (f == null) {
            return;
        }
        Utils.postParallel(new Runnable () {
            @Override
            public void run() {
                final VCSFileProxy repository = Git.getInstance().getRepositoryRoot(f);
                if (repository != null) {
                    GitRepositories.getInstance().add(repository, true);
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run () {
                            GitRepositoryTopComponent rtc = GitRepositoryTopComponent.findInstance();
                            rtc.open();
                            rtc.requestActive();
                            rtc.selectRepository(repository);
                        }
                    });
                }
            }
        }, 0);
    }
    
}
