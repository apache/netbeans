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

package org.netbeans.modules.git.remote.ui.conflicts;

import java.util.EnumSet;
import java.util.Set;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.git.remote.utils.GitUtils;
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
@ActionID(id = "org.netbeans.modules.git.remote.ui.conflicts.ResolveAllConflictsAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_ResolveAllConflictsAction_Name")
@NbBundle.Messages({
    "LBL_ResolveAllConflictsAction_Name=Resolve All Conflicts - Repository"
})
public class ResolveAllConflictsAction extends MultipleRepositoryAction {

    private static final String ICON_RESOURCE = "org/netbeans/modules/git/remote/resources/icons/conflict-resolve.png"; //NOI18N

    public ResolveAllConflictsAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected boolean enable (Node[] activatedNodes) {
        VCSContext context = getCurrentContext(activatedNodes);
        Set<VCSFileProxy> roots = GitUtils.getRepositoryRoots(context);
        return Git.getInstance().getFileStatusCache().containsFiles(roots, EnumSet.of(Status.IN_CONFLICT), false);
    }

    @Override
    protected Task performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        return SystemAction.get(ResolveConflictsAction.class).performAction(repository, new VCSFileProxy[] { repository }, GitUtils.getContextForFile(repository));
    }
}
