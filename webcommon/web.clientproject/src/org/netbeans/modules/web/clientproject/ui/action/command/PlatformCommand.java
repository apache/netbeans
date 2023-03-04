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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.api.platform.PlatformProvider;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;

public class PlatformCommand extends Command {

    private final String commandId;


    public PlatformCommand(ClientSideProject project, String commandId) {
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
        for (PlatformProvider provider : project.getPlatformProviders()) {
            ActionProvider actionProvider = provider.getActionProvider(project);
            if (actionProvider != null
                    && isSupportedAction(commandId, actionProvider)
                    && actionProvider.isActionEnabled(commandId, context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    void invokeActionInternal(final Lookup context) {
        for (PlatformProvider provider : project.getPlatformProviders()) {
            final ActionProvider actionProvider = provider.getActionProvider(project);
            if (actionProvider != null
                    && isSupportedAction(commandId, actionProvider)
                    && actionProvider.isActionEnabled(commandId, context)) {
                runInEventThread(new Runnable() {
                    @Override
                    public void run() {
                        actionProvider.invokeAction(commandId, context);
                    }
                });
            }
        }
    }

    public List<String> getSupportedActions() {
        List<String> supportedActions = new ArrayList<>();
        for (PlatformProvider provider : project.getPlatformProviders()) {
            ActionProvider actionProvider = provider.getActionProvider(project);
            if (actionProvider != null) {
                supportedActions.addAll(Arrays.asList(actionProvider.getSupportedActions()));
            }
        }
        return supportedActions;
    }

}
