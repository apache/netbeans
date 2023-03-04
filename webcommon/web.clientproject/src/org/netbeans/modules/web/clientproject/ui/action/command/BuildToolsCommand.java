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
package org.netbeans.modules.web.clientproject.ui.action.command;

import java.util.logging.Logger;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.api.build.BuildTools;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class BuildToolsCommand extends Command {

    private static final Logger LOGGER = Logger.getLogger(BuildToolsCommand.class.getName());

    private final String commandId;

    private volatile boolean showCustomizer = true;


    public BuildToolsCommand(ClientSideProject project, String commandId) {
        super(project);
        assert commandId != null;
        this.commandId = commandId;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }

    @Override
    boolean isActionEnabledInternal(Lookup context) {
        return true;
    }

    @NbBundle.Messages("BuildToolsCommand.buildTool.none=No build tool (e.g. Grunt) used in project.")
    @Override
    void invokeActionInternal(Lookup context) {
        if (!tryBuild(showCustomizer, false)
                && !ClientSideProjectUtilities.isCordovaProject(project)) {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.BuildToolsCommand_buildTool_none()));
        }
        showCustomizer = false;
    }

    public boolean tryBuild(boolean showCustomizer, boolean waitFinished) {
        return BuildTools.getDefault().run(project, commandId, waitFinished,
                showCustomizer && !ClientSideProjectUtilities.isCordovaProject(project));
    }

}
