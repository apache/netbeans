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

package org.netbeans.modules.php.project.ui.actions.support;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpInterpreter;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

// heavily inspired from ruby :)
@ServiceProvider(service = ActionProvider.class)
public class RunFileActionProvider implements ActionProvider {

    // store for one session
    private static final Map<File, RunFileArgs> ARGS_FOR_FILE = new HashMap<>();

    @Override
    public String[] getSupportedActions() {
        return new String[] {ActionProvider.COMMAND_RUN_SINGLE, ActionProvider.COMMAND_DEBUG_SINGLE};
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) {
        Project p = context.lookup(Project.class);
        if (p != null) {
            return false;
        }
        Collection<? extends DataObject> files = context.lookupAll(DataObject.class);
        if (files.isEmpty()) {
            return false;
        }
        for (DataObject d : files) {
            if (!FileUtils.isPhpFile(d.getPrimaryFile())) {
                return false;
            }
        }
        if (ActionProvider.COMMAND_DEBUG_SINGLE.equals(command)
                && DebugStarterFactory.getInstance() == null) {
            return false;
        }
        return true;
    }

    @Override
    public void invokeAction(String command, Lookup context) {
        boolean debug = ActionProvider.COMMAND_DEBUG_SINGLE.equals(command);
        for (DataObject dataObject : context.lookupAll(DataObject.class)) {
            File file = FileUtil.toFile(dataObject.getPrimaryFile());
            if (file != null) {
                FileUtils.saveFile(dataObject);
                runFile(file, debug);
            }
        }
    }

    private void runFile(File file, boolean debug) {
        RunFileArgs existing = ARGS_FOR_FILE.get(file);
        if (existing != null && !existing.displayDialog) {
            doRun(file, existing, debug);
            return;
        }
        if (existing == null) {
            // init work dir
            existing = new RunFileArgs(null, file.getParent(), null, true);
        }

        RunFileArgs runFileArgs = RunFilePanel.open(existing, file, debug);
        if (runFileArgs == null) {
            return;
        }
        ARGS_FOR_FILE.put(file, runFileArgs);
        doRun(file, runFileArgs, debug);
    }

    private void doRun(File file, RunFileArgs args, boolean debug) {
        FileRunner fileRunner = createFileRunner(file, args);
        if (fileRunner == null) {
            return;
        }
        if (debug) {
            fileRunner.debug();
        } else {
            fileRunner.run();
        }
    }

    private FileRunner createFileRunner(File file, RunFileArgs args) {
        PhpInterpreter phpInterpreter;
        try {
            phpInterpreter = PhpInterpreter.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(ex.getLocalizedMessage());
            return null;
        }
        return new FileRunner(file)
                .command(phpInterpreter.getInterpreter())
                .workDir(args.getWorkDir())
                .phpArgs(args.getPhpOpts())
                .fileArgs(args.getRunArgs());
    }

    /**
     * Holds the args last given for running (file specific).
     */
    static final class RunFileArgs {
        private final String runArgs;
        private final String workDir;
        private final String phpOpts;
        private final boolean displayDialog;

        public RunFileArgs(String runArgs, String workDir, String phpOpts, boolean displayDialog) {
            this.runArgs = runArgs;
            this.workDir = workDir;
            this.phpOpts = phpOpts;
            this.displayDialog = displayDialog;
        }

        public String getRunArgs() {
            return runArgs;
        }

        public boolean displayDialog() {
            return displayDialog;
        }

        public String getWorkDir() {
            return workDir;
        }

        public String getPhpOpts() {
            return phpOpts;
        }
    }
}
