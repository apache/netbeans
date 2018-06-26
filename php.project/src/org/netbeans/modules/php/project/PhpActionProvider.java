/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.php.project;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.Action;
import org.netbeans.modules.php.project.ui.actions.BuildToolsCommand;
import org.netbeans.modules.php.project.ui.actions.Command;
import org.netbeans.modules.php.project.ui.actions.CopyCommand;
import org.netbeans.modules.php.project.ui.actions.DebugFileCommand;
import org.netbeans.modules.php.project.ui.actions.DebugProjectCommand;
import org.netbeans.modules.php.project.ui.actions.DebugTestMethodCommand;
import org.netbeans.modules.php.project.ui.actions.DeleteCommand;
import org.netbeans.modules.php.project.ui.actions.DownloadCommand;
import org.netbeans.modules.php.project.ui.actions.MoveCommand;
import org.netbeans.modules.php.project.ui.actions.RenameCommand;
import org.netbeans.modules.php.project.ui.actions.RunFileCommand;
import org.netbeans.modules.php.project.ui.actions.RunProjectCommand;
import org.netbeans.modules.php.project.ui.actions.RunTestCommand;
import org.netbeans.modules.php.project.ui.actions.RunTestMethodCommand;
import org.netbeans.modules.php.project.ui.actions.RunTestsCommand;
import org.netbeans.modules.php.project.ui.actions.SyncCommand;
import org.netbeans.modules.php.project.ui.actions.TestProjectCommand;
import org.netbeans.modules.php.project.ui.actions.UploadCommand;
import org.netbeans.modules.php.project.ui.actions.support.Displayable;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.LifecycleManager;
import org.openide.util.Lookup;

/**
 * @author Radek Matous
 */
public class PhpActionProvider implements ActionProvider {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final PhpProject project;
    private final Map<String, Command> commands;

    PhpActionProvider(PhpProject project) {
        assert project != null;
        this.project = project;

        commands = new LinkedHashMap<>();
        Command[] commandArray = new Command[] {
            // project sensitive actions
            new RunProjectCommand(project),
            new DebugProjectCommand(project),
            new TestProjectCommand(project),
            new RunFileCommand(project),
            new DebugFileCommand(project),
            new RunTestCommand(project),
            new RunTestsCommand(project),
            new RunTestMethodCommand(project),
            new DebugTestMethodCommand(project),
            new DeleteCommand(project),
            new CopyCommand(project),
            new MoveCommand(project),
            new RenameCommand(project),
            new BuildToolsCommand(project, COMMAND_BUILD),
            new BuildToolsCommand(project, COMMAND_CLEAN),
            new BuildToolsCommand(project, COMMAND_REBUILD),

            // file sensitive actions
            new DownloadCommand(project),
            new UploadCommand(project),
            new SyncCommand(project),
        };
        for (Command command : commandArray) {
            commands.put(command.getCommandId(), command);
        }
    }

    @Override
    public String[] getSupportedActions() {
        Set<String> commandIds = commands.keySet();
        return commandIds.toArray(new String[commandIds.size()]);
    }

    @Override
    public void invokeAction(final String commandId, final Lookup lookup) {
        final Command command = getCommand(commandId);
        if (!command.getProject().getCopySupport().waitFinished()) {
            return;
        }
        if (command.saveRequired()) {
            LifecycleManager.getDefault().saveAll();
        }
        if (!command.asyncCallRequired()) {
            runCommand(command, lookup);
        } else {
            submitTask(new Runnable() {
                @Override
                public void run() {
                    runCommand(command, lookup);
                }
            });
        }
    }

    public static void submitTask(Runnable runnable) {
        assert runnable != null;
        EXECUTOR.submit(runnable);
    }

    void runCommand(Command command, Lookup lookup) {
        tryBuildTools(command.getCommandId());
        command.invokeAction(lookup);
    }

    @Override
    public boolean isActionEnabled(String commandId, Lookup lookup) {
        return getCommand(commandId).isActionEnabled(lookup);
    }

    public Command getCommand(String commandId) {
        Command retval = commands.get(commandId);
        assert retval != null : commandId;
        return retval;
    }

    public Action getAction(String commandId) {
        Command command = getCommand(commandId);
        assert command != null : commandId;
        assert command instanceof Displayable;
        if (command.isFileSensitive()) {
            return FileSensitiveActions.fileCommandAction(command.getCommandId(), ((Displayable) command).getDisplayName(), null);
        }
        return ProjectSensitiveActions.projectCommandAction(command.getCommandId(), ((Displayable) command).getDisplayName(), null);
    }

    void tryBuildTools(String commandId) {
        if (getCommand(commandId) instanceof BuildToolsCommand) {
            return;
        }
        new BuildToolsCommand(project, commandId)
                .tryBuild(false, true);
    }

}
