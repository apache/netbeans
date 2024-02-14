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
package org.netbeans.modules.javascript.nodejs.ui.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

public final class NodeJsActionProvider implements ActionProvider {

    private static final RequestProcessor RP = new RequestProcessor(NodeJsActionProvider.class);

    private final Project project;
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    private final List<String> supportedActions;


    public NodeJsActionProvider(Project project) {
        assert project != null;
        this.project = project;
        fillCommands();
        supportedActions = new ArrayList<>(commands.keySet());
    }

    private void fillCommands() {
        Command[] allCommands = new Command[] {
            new RunProjectCommand(project),
            new RunFileCommand(project),
            new DebugProjectCommand(project),
            new DebugFileCommand(project),
        };
        for (Command command : allCommands) {
            Command old = commands.put(command.getCommandId(), command);
            assert old == null : "Command already set for " + command.getCommandId();
        }
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions.toArray(new String[0]);
    }

    @Override
    public void invokeAction(String command, final Lookup context) {
        final Command runCommand = commands.get(command);
        assert runCommand != null : command;
        RP.post(new Runnable() {
            @Override
            public void run() {
                runCommand.run(context);
            }
        });
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) {
        Command runCommand = commands.get(command);
        if (runCommand == null) {
            return false;
        }
        return runCommand.isEnabled(context);
    }

}
