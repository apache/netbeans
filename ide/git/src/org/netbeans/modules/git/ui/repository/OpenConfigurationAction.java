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
import org.netbeans.modules.git.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ondrej Vrabec
 */
@ActionID(id = "org.netbeans.modules.git.ui.repository.OpenConfigurationAction", category = "Git")
@ActionRegistration(lazy = false, displayName = "#LBL_OpenConfigurationAction_Name")
@NbBundle.Messages({
    "LBL_OpenConfigurationAction_Name=Open Confi&guration",
    "LBL_OpenConfigurationAction_PopupName=Open Configuration"
})
public class OpenConfigurationAction extends MultipleRepositoryAction {

    @Override
    protected boolean enable (Node[] activatedNodes) {
        return true;
    }

    @Override
    protected RequestProcessor.Task performAction (File repository, File[] roots, VCSContext context) {
        File config = new File(GitUtils.getGitFolderForRoot(repository), "config"); //NOI18N
        if (config.canRead()) {
            Utils.openFile(config);
        }
        return null;
    }

    
}
