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
package org.netbeans.modules.git.ui.commit;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.commit.ExcludeFromCommitAction", category = "Git")
@ActionRegistration(displayName = "#LBL_ExcludeFromCommitAction_Name")
@NbBundle.Messages({
    "LBL_ExcludeFromCommitAction_Name=Ex&clude From Commit"
})
public class ExcludeFromCommitAction extends MultipleRepositoryAction {

    @Override
    protected Task performAction (File repository, File[] roots, VCSContext context) {
        return exclude(repository, roots);
    }

    @Override
    protected boolean enableFull (Node[] activatedNodes) {
        boolean enabled = super.enableFull(activatedNodes);
        if (enabled) {
            enabled = false;
            GitModuleConfig config = GitModuleConfig.getDefault();
            for (File root : getCurrentContext(activatedNodes).getRootFiles()) {
                if (!config.isExcludedFromCommit(root.getAbsolutePath())) {
                    enabled = true;
                    break;
                }
            }
        }
        return enabled;
    }

    public Task exclude (File repository, File[] roots) {
        List<String> toExclude = filterRoots(roots);
        GitModuleConfig config = GitModuleConfig.getDefault();
        config.addExclusionPaths(toExclude);
        SystemAction.get(IncludeInCommitAction.class).setEnabled(false);
        SystemAction.get(ExcludeFromCommitAction.class).setEnabled(false);
        return null;
    }
    
    private static List<String> filterRoots (File[] roots) {
        List<String> toExclude = new LinkedList<String>();
        GitModuleConfig config = GitModuleConfig.getDefault();
        for (File root : roots) {
            String path = root.getAbsolutePath();
            if (!config.isExcludedFromCommit(path)) {
                toExclude.add(path);
            }
        }
        return toExclude;
    }
    
}
