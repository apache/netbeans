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

import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.exec.NodeProcesses;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.util.RunInfo;
import org.openide.util.Lookup;

abstract class ProjectCommand extends Command {

    private final boolean debug;


    public ProjectCommand(Project project, boolean debug) {
        super(project);
        this.debug = debug;
    }

    protected abstract boolean isEnabledInternal(Lookup context);
    protected abstract NodeProcesses.RunInfo runNodeInternal(NodeExecutable node, RunInfo runInfo);

    @Override
    public final boolean isEnabled(Lookup context) {
        if (!NodeJsSupport.forProject(project).getPreferences().isRunEnabled()) {
            return false;
        }
        return isEnabledInternal(context);
    }

    @Override
    final void runInternal(Lookup context) {
        NodeProcesses nodeProcesses = NodeProcesses.forProject(project);
        NodeProcesses.RunInfo currentNodeInfo = nodeProcesses.getProjectRun();
        if (!currentNodeInfo.isRunning()) {
            // run it
            nodeProcesses.setProjectRun(runNode());
            return;
        }
        // node is running
        assert currentNodeInfo.isRunning();
        NodeJsSupport nodeJsSupport = NodeJsSupport.forProject(project);
        // check type + restart option
        if (currentNodeInfo.isDebug() != debug
                || nodeJsSupport.getPreferences().isRunRestart()) {
            // force restart
            currentNodeInfo.stop();
            nodeProcesses.setProjectRun(runNode());
        }
    }

    private NodeProcesses.RunInfo runNode() {
        NodeExecutable node = getNode();
        if (node == null) {
            return NodeProcesses.RunInfo.none();
        }
        RunInfo runInfo = getRunInfo();
        if (runInfo == null) {
            return NodeProcesses.RunInfo.none();
        }
        return runNodeInternal(node, runInfo);
    }

}
