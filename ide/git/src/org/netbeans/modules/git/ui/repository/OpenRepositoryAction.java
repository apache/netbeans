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

package org.netbeans.modules.git.ui.repository;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitRepositories;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondra Vrabec
 */
@ActionID(id = "org.netbeans.modules.git.ui.repository.OpenRepositoryAction", category = "Git")
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
        final File f = new FileChooserBuilder(OpenRepositoryAction.class).setDirectoriesOnly(true)
                .setApproveText(Bundle.CTL_OpenRepository_okButton())
                .setAccessibleDescription(Bundle.CTL_OpenRepository_ACSD())
                .showOpenDialog();
        if (f == null) {
            return;
        }
        Utils.postParallel(new Runnable () {
            @Override
            public void run() {
                final File repository = Git.getInstance().getRepositoryRoot(f);
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
