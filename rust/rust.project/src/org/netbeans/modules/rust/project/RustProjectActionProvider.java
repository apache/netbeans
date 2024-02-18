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
package org.netbeans.modules.rust.project;

import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.rust.cargo.api.CargoCLICommand;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import org.netbeans.modules.rust.project.ui.actions.CargoExecutionCommand;
import org.netbeans.modules.rust.project.ui.actions.Command;
import org.netbeans.modules.rust.project.ui.actions.CopyCommand;
import org.netbeans.modules.rust.project.ui.actions.DeleteCommand;
import org.netbeans.modules.rust.project.ui.actions.MoveCommand;
import org.netbeans.modules.rust.project.ui.actions.RenameCommand;
import org.openide.LifecycleManager;
import org.openide.util.RequestProcessor;

/**
 * An ActionProvider for Rust projects.
 */
public final class RustProjectActionProvider implements ActionProvider {

    private static final RequestProcessor REQUEST_PROCESSOR = new RequestProcessor("Rust Executor");

    private final Map<String,Command> commands;

    public RustProjectActionProvider(RustProject project) {
        commands = new LinkedHashMap<>();

        commands.put(COMMAND_BUILD, new CargoExecutionCommand(project, COMMAND_BUILD, new CargoCLICommand[]{CargoCLICommand.CARGO_BUILD}));
        commands.put(COMMAND_CLEAN, new CargoExecutionCommand(project, COMMAND_BUILD, new CargoCLICommand[]{CargoCLICommand.CARGO_CLEAN}));
        commands.put(COMMAND_REBUILD, new CargoExecutionCommand(project, COMMAND_BUILD, new CargoCLICommand[]{CargoCLICommand.CARGO_CLEAN, CargoCLICommand.CARGO_BUILD}));
        commands.put(COMMAND_RUN, new CargoExecutionCommand(project, COMMAND_BUILD, new CargoCLICommand[]{CargoCLICommand.CARGO_RUN}));
        commands.put(COMMAND_TEST, new CargoExecutionCommand(project, COMMAND_TEST, new CargoCLICommand[]{CargoCLICommand.CARGO_TEST}));

        commands.put(COMMAND_COPY, new CopyCommand(project));
        commands.put(COMMAND_DELETE, new DeleteCommand(project));
        commands.put(COMMAND_MOVE, new MoveCommand(project));
        commands.put(COMMAND_RENAME, new RenameCommand(project));
    }

    @Override
    public String[] getSupportedActions() {
        return commands.keySet().toArray(new String[0]);
    }

    @Override
    public void invokeAction(String commandId, Lookup lookup) throws IllegalArgumentException {
        final Command command = getCommand(commandId);
        if (command.saveRequired()) {
            LifecycleManager.getDefault().saveAll();
        }
        if (!command.asyncCallRequired()) {
            command.invokeAction(lookup);
        } else {
            REQUEST_PROCESSOR.submit(() -> command.invokeAction(lookup));
        }
    }

    @Override
    public boolean isActionEnabled(String commandId, Lookup lookup) throws IllegalArgumentException {
        return getCommand(commandId).isActionEnabled(lookup);
    }

    public Command getCommand(String commandId) {
        Command retval = commands.get(commandId);
        assert retval != null : commandId;
        return retval;
    }
}
