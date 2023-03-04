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
package org.netbeans.modules.javascript.nodejs.ui.actions;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.exec.NodeProcesses;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferencesValidator;
import org.netbeans.modules.javascript.nodejs.util.RunInfo;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;

final class RunProjectCommand extends ProjectCommand {

    public RunProjectCommand(Project project) {
        super(project, false);
    }

    @Override
    public String getCommandId() {
        return ActionProvider.COMMAND_RUN;
    }

    @Override
    public boolean isEnabledInternal(Lookup context) {
        return true;
    }

    @Override
    ValidationResult validateRunInfo(RunInfo runInfo) {
        return new NodeJsPreferencesValidator()
                .validateRun(runInfo.getStartFile(), runInfo.getStartArgs())
                .getResult();
    }

    @Override
    protected NodeProcesses.RunInfo runNodeInternal(NodeExecutable node, RunInfo runInfo) {
        return NodeProcesses.RunInfo.run(node.run(new File(runInfo.getStartFile()), runInfo.getStartArgs()));
    }

}
