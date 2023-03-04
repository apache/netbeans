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

import java.io.File;
import java.util.Set;
import org.netbeans.modules.git.ui.actions.*;
import java.util.logging.Logger;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.git.ui.repository.RepositoryBrowserAction", category = "Git")
@ActionRegistration(displayName = "#LBL_RepositoryBrowserAction_Name")
@NbBundle.Messages({
    "LBL_RepositoryBrowserAction_Name=Repositor&y Browser",
    "LBL_RepositoryBrowserAction_PopupName=Repository Browser"
})
public class RepositoryBrowserAction extends GitAction {

    private static final Logger LOG = Logger.getLogger(RepositoryBrowserAction.class.getName());

    @Override
    protected boolean enableFull (Node[] activatedNodes) {
        return true;
    }

    @Override
    protected void performContextAction (final Node[] nodes) {
        final GitRepositoryTopComponent rtc = GitRepositoryTopComponent.findInstance();
        rtc.open();
        rtc.requestActive();
        Utils.postParallel(new Runnable () {
            @Override
            public void run() {
                VCSContext context = getCurrentContext(nodes);
                Set<File> repositories = GitUtils.getRepositoryRoots(context);
                if (repositories.size() == 1) {
                    File repository = repositories.iterator().next();
                    GitUtils.logRemoteRepositoryAccess(repository);
                    rtc.selectRepository(repository);
                }
            }
        }, 0);
    }
    
}
