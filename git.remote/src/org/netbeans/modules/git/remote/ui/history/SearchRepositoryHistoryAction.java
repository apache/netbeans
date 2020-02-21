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

package org.netbeans.modules.git.remote.ui.history;

import org.netbeans.modules.git.remote.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.SystemAction;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.history.SearchRepositoryHistoryAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_SearchRepositoryHistoryAction_Name")
@NbBundle.Messages({
    "LBL_SearchRepositoryHistoryAction_Name=Show Repository History"
})
public class SearchRepositoryHistoryAction extends MultipleRepositoryAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/remote/resources/icons/search_history.png"; //NOI18N

    public SearchRepositoryHistoryAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected Task performAction (final VCSFileProxy repository, final VCSFileProxy[] roots, final VCSContext context) {
        return SystemAction.get(SearchHistoryAction.class).performAction(repository, new VCSFileProxy[] { repository }, GitUtils.getContextForFile(repository));
    }

}
