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

package org.netbeans.modules.git.ui.diff;

import java.io.File;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.diff.DiffToRevisionAction", category = "Git")
@ActionRegistration(displayName = "#LBL_DiffToRevisionAction_Name")
@NbBundle.Messages({
    "LBL_DiffToRevisionAction_Name=Diff &To...",
    "LBL_DiffToRevisionAction_PopupName=Diff To..."
})
public class DiffToRevisionAction extends GitAction {
    
    @Override
    protected boolean enableFull (Node[] activatedNodes) {
        VCSContext context = getCurrentContext(activatedNodes);
        return GitUtils.getRepositoryRoots(context).size() == 1;
    }

    @Override
    protected void performContextAction (Node[] nodes) {
        VCSContext context = getCurrentContext(nodes);
        if (GitUtils.getRepositoryRoots(context).size() == 1) {
            File repository = GitUtils.getRootFile(context);
            RepositoryInfo info = RepositoryInfo.getInstance(repository);
            DiffToRevision panel = new DiffToRevision(repository, info.getActiveBranch());
            if (panel.showDialog()) {
                SystemAction.get(DiffAction.class).diff(context, panel.getSelectedTreeFirst(), panel.getSelectedTreeSecond());
            }
        }
    }
}
