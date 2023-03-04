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
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.runconfigs.RunConfigScript;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigScriptValidator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Action implementation for SCRIPT configuration.
 * It means running and debugging scripts.
 * @author Tomas Mysik
 */
class ConfigActionScript extends ConfigAction {
    private final FileObject sourceRoot;

    protected ConfigActionScript(PhpProject project) {
        super(project);
        sourceRoot = ProjectPropertiesSupport.getSourcesDirectory(project);
        assert sourceRoot != null;
    }

    @Override
    public boolean isProjectValid() {
        return isValid(RunConfigScriptValidator.validateConfigAction(RunConfigScript.forProject(project), true) == null);
    }

    @Override
    public boolean isFileValid() {
        return isValid(RunConfigScriptValidator.validateConfigAction(RunConfigScript.forProject(project), false) == null);
    }

    private boolean isValid(boolean valid) {
        if (!valid) {
            showCustomizer();
        }
        return valid;
    }

    @Override
    public boolean isRunFileEnabled(Lookup context) {
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context, sourceRoot);
        return file != null && FileUtils.isPhpFile(file);
    }

    @Override
    public boolean isDebugFileEnabled(Lookup context) {
        if (DebugStarterFactory.getInstance() == null) {
            return false;
        }
        return isRunFileEnabled(context);
    }

    @Override
    public void runProject() {
        createFileRunner(null).run();
    }

    @Override
    public void debugProject() {
        createFileRunner(null).debug();
    }

    @Override
    public void runFile(Lookup context) {
        createFileRunner(context).run();
    }

    @Override
    public void debugFile(Lookup context) {
        createFileRunner(context).debug();
    }

    private File getStartFile(Lookup context) {
        FileObject file;
        if (context == null) {
            file = FileUtil.toFileObject(RunConfigScript.forProject(project).getIndexFile());
        } else {
            file = CommandUtils.fileForContextOrSelectedNodes(context, sourceRoot);
        }
        assert file != null : "Start file must be found";
        return FileUtil.toFile(file);
    }

    private FileRunner createFileRunner(Lookup context) {
        RunConfigScript configScript = RunConfigScript.forProject(project);
        return new FileRunner(getStartFile(context))
                .project(project)
                .command(configScript.getInterpreter())
                .workDir(configScript.getWorkDir())
                .phpArgs(configScript.getOptions())
                .fileArgs(configScript.getArguments());
    }

}
