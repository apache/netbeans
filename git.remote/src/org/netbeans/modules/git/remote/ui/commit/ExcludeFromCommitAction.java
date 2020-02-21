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
package org.netbeans.modules.git.remote.ui.commit;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.git.remote.GitModuleConfig;
import org.netbeans.modules.git.remote.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.SystemAction;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.commit.ExcludeFromCommitAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_ExcludeFromCommitAction_Name")
@NbBundle.Messages({
    "LBL_ExcludeFromCommitAction_Name=Ex&clude From Commit"
})
public class ExcludeFromCommitAction extends MultipleRepositoryAction {

    @Override
    protected Task performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        return exclude(repository, roots);
    }

    @Override
    protected boolean enable (Node[] activatedNodes) {
        boolean enabled = super.enable(activatedNodes);
        if (enabled) {
            enabled = false;
            GitModuleConfig config = GitModuleConfig.getDefault();
            for (VCSFileProxy root : getCurrentContext(activatedNodes).getRootFiles()) {
                if (!config.isExcludedFromCommit(root.getPath())) {
                    enabled = true;
                    break;
                }
            }
        }
        return enabled;
    }

    public Task exclude (VCSFileProxy repository, VCSFileProxy[] roots) {
        List<String> toExclude = filterRoots(roots);
        GitModuleConfig config = GitModuleConfig.getDefault();
        config.addExclusionPaths(toExclude);
        SystemAction.get(IncludeInCommitAction.class).setEnabled(false);
        SystemAction.get(ExcludeFromCommitAction.class).setEnabled(false);
        return null;
    }
    
    private static List<String> filterRoots (VCSFileProxy[] roots) {
        List<String> toExclude = new LinkedList<>();
        GitModuleConfig config = GitModuleConfig.getDefault();
        for (VCSFileProxy root : roots) {
            String path = root.getPath();
            if (!config.isExcludedFromCommit(path)) {
                toExclude.add(path);
            }
        }
        return toExclude;
    }
    
}
