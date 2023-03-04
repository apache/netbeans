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

package org.netbeans.modules.git.ui.conflicts;

import java.io.File;
import org.netbeans.modules.git.ui.actions.*;
import java.util.EnumSet;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.git.ui.conflicts.ResolveConflictsAction", category = "Git")
@ActionRegistration(displayName = "#LBL_ResolveConflictsAction_Name")
@NbBundle.Messages({
    "LBL_ResolveConflictsAction_Name=Resolve Con&flicts"
})
public class ResolveConflictsAction extends MultipleRepositoryAction {

    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/conflict-resolve.png"; //NOI18N

    public ResolveConflictsAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected boolean enableFull (Node[] activatedNodes) {
        VCSContext context = getCurrentContext(activatedNodes);
        return Git.getInstance().getFileStatusCache().containsFiles(context, EnumSet.of(Status.IN_CONFLICT), false);
    }

    @Override
    protected Task performAction (File repository, File[] roots, VCSContext context) {
        final File[] files = Git.getInstance().getFileStatusCache().listFiles(roots, EnumSet.of(Status.IN_CONFLICT));
        if (files.length > 0) {
            GitProgressSupport supp = new ResolveConflictsExecutor(files);
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(ResolveConflictsAction.class, "MSG_PreparingMerge")); //NOI18N
            return supp.getTask();
        } else {
            return null;
        }
    }
}
