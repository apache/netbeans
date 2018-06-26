/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
        return getCommand(commandId).isActionEnabled(lookup);
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
