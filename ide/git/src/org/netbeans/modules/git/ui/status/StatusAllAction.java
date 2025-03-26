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

package org.netbeans.modules.git.ui.status;

import java.io.File;
import java.util.Set;
import org.netbeans.modules.git.ui.actions.GitAction;
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
@ActionID(id = "org.netbeans.modules.git.ui.status.StatusAllAction", category = "Git")
@ActionRegistration(displayName = "#LBL_StatusAllAction_Name")
@NbBundle.Messages({
    "LBL_StatusAllAction_Name=Show All Changes - Repository"
})
public class StatusAllAction extends GitAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/show_changes.png"; //NOI18N
    
    public StatusAllAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected final void performContextAction (Node[] nodes) {
        VCSContext context = getCurrentContext(nodes);
        Set<File> roots = GitUtils.getRepositoryRoots(context);
        SystemAction.get(StatusAction.class).performContextAction(GitUtils.getContextForFiles(roots.toArray(new File[0])));
    }

}
