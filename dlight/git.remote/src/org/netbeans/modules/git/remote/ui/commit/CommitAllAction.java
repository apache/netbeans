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

import org.netbeans.modules.git.remote.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.commit.CommitAllAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_CommitAllAction_Name")
@NbBundle.Messages({
    "LBL_CommitAllAction_Name=Commit All - Repository..."
})
public class CommitAllAction extends SingleRepositoryAction {

    private static final String ICON_RESOURCE = "org/netbeans/modules/git/remote/resources/icons/commit.png"; //NOI18N
    
    public CommitAllAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    @Override
    protected void performAction (final VCSFileProxy repository, final VCSFileProxy[] roots, final VCSContext context) {
        SystemAction.get(CommitAction.class).performAction(repository, new VCSFileProxy[] { repository }, GitUtils.getContextForFile(repository));
    }

}
