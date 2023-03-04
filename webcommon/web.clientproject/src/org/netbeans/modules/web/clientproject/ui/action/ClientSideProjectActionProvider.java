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
package org.netbeans.modules.web.clientproject.ui.action;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ui.action.command.BrowserCommand;
import org.netbeans.modules.web.clientproject.ui.action.command.BuildToolsCommand;
import org.netbeans.modules.web.clientproject.ui.action.command.Command;
import org.netbeans.modules.web.clientproject.ui.action.command.CopyCommand;
import org.netbeans.modules.web.clientproject.ui.action.command.DeleteCommand;
import org.netbeans.modules.web.clientproject.ui.action.command.MoveCommand;
import org.netbeans.modules.web.clientproject.ui.action.command.PlatformCommand;
import org.netbeans.modules.web.clientproject.ui.action.command.RenameCommand;
import org.netbeans.modules.web.clientproject.ui.action.command.TestProjectCommand;
import org.netbeans.spi.project.ActionProvider;
import org.openide.LifecycleManager;
import org.openide.util.Lookup;

public class ClientSideProjectActionProvider implements ActionProvider {

    private static final Logger LOGGER = Logger.getLogger(ClientSideProjectActionProvider.class.getName());

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final ClientSideProject project;
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    private final List<String> supportedActions;


    public ClientSideProjectActionProvider(ClientSideProject project) {
        this.project = project;
        fillCommands();
        assert !commands.isEmpty();
        supportedActions = new ArrayList<>(commands.keySet());
        addDebugCommands();
    }

    private void fillCommands() {
        Command[] allCommands = new Command[] {
            new BrowserCommand(project, COMMAND_RUN_SINGLE),
            new BrowserCommand(project, COMMAND_RUN),
            new TestProjectCommand(project),

            new BuildToolsCommand(project, COMMAND_BUILD),
            new BuildToolsCommand(project, COMMAND_REBUILD),
            new BuildToolsCommand(project, COMMAND_CLEAN),
            new BuildToolsCommand(project, COMMAND_TEST_SINGLE),

            new CopyCommand(project),
            new MoveCommand(project),
            new RenameCommand(project),
            new DeleteCommand(project),
        };
        for (Command command : allCommands) {
            Command old = commands.put(command.getCommandId(), command);
            assert old == null : "Command already set for " + command.getCommandId();
        }
    }

    private void addDebugCommands() {
        // allow also debug project/file (ActionProvider of browser/platform provider can provide their supported actions),
        // behave like run project/file
        Command old = commands.put(COMMAND_DEBUG, getCommand(COMMAND_RUN));
        assert old == null : "Command already set for " + COMMAND_RUN;
        old = commands.put(COMMAND_DEBUG_SINGLE, getCommand(COMMAND_RUN_SINGLE));
        assert old == null : "Command already set for " + COMMAND_RUN_SINGLE;
    }

    private boolean isDebugCommand(String commandId) {
        return COMMAND_DEBUG.equals(commandId)
                || COMMAND_DEBUG_SINGLE.equals(commandId);
    }

    @Override
    public String[] getSupportedActions() {
        LinkedHashSet<String> actions = new LinkedHashSet<>();
        // project
        actions.addAll(supportedActions);
        // browser
        actions.addAll(new BrowserCommand(project, "dummy").getSupportedActions()); // NOI18N
        // platform providers
        actions.addAll(new PlatformCommand(project, "dummy").getSupportedActions()); // NOI18N
        return new ArrayList<>(actions).toArray(new String[actions.size()]);
    }

    @Override
    public boolean isActionEnabled(String commandId, Lookup lookup) {
        if (isCommandEnabled(new PlatformCommand(project, commandId), lookup)) {
            return true;
        }
        if (isCommandEnabled(new BrowserCommand(project, commandId), lookup)) {
            return true;
        }
        if (isDebugCommand(commandId)) {
            // debug can be supported only by browser or platform command (but see #addDebugCommands())
            return false;
        }
        Command cmd = commands.get(commandId);
        return cmd != null && cmd.isActionEnabled(lookup);
    }

    @Override
    public void invokeAction(final String commandId, final Lookup lookup) {
        final Command command = getCommand(commandId);
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                LifecycleManager.getDefault().saveAll();
                AtomicBoolean warnUser = new AtomicBoolean(true);
                tryBuildTools(commandId);
                tryPlatform(commandId, lookup, warnUser);
                command.invokeAction(lookup, warnUser);
                tryBrowser(commandId, lookup, warnUser);
            }
        });
    }

    private Command getCommand(String commandId) {
        Command retval = commands.get(commandId);
        assert retval != null : commandId;
        return retval;
    }

    private boolean isCommandEnabled(Command command, Lookup context) {
        return command.isActionEnabled(context);
    }

    void tryBuildTools(String commandId) {
        if (getCommand(commandId) instanceof BuildToolsCommand) {
            return;
        }
        new BuildToolsCommand(project, commandId)
                .tryBuild(false, true);
    }

    void tryPlatform(String commandId, Lookup context, AtomicBoolean warnUser) {
        PlatformCommand platformCommand = new PlatformCommand(project, commandId);
        if (isCommandEnabled(platformCommand, context)) {
            platformCommand.invokeAction(context, warnUser);
        }
    }

    void tryBrowser(String commandId, Lookup context, AtomicBoolean warnUser) {
        if (getCommand(commandId) instanceof BrowserCommand) {
            return;
        }
        BrowserCommand browserCommand = new BrowserCommand(project, commandId);
        if (isCommandEnabled(browserCommand, context)) {
            browserCommand.invokeAction(context, warnUser);
        }
    }

}
