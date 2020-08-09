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
package org.netbeans.modules.cpplite.project;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cpplite.debugger.api.Debugger;
import org.netbeans.modules.cpplite.project.runner.Runner;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;

/**
 *
 * @author lahvac
 */
public class ActionProviderImpl implements ActionProvider {
    private static final String[] SUPPORTED_ACTIONS = {
        COMMAND_BUILD,
        COMMAND_REBUILD,
        COMMAND_RUN,
        COMMAND_DEBUG,
    };

    private final CPPLiteProject prj;

    public ActionProviderImpl(CPPLiteProject prj) {
        this.prj = prj;
    }
    
    @Override
    public String[] getSupportedActions() {
        return SUPPORTED_ACTIONS;
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        BuildConfiguration config = prj.getActiveBuildConfiguration();
        File module = InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-cpplite-project.jar", "org.netbeans.modules.cpplite.project", false);
        ExecutionService.newService(() -> {
            if (COMMAND_DEBUG.equals(command)) {
                List<List<String>> executablesFor = config.executablesFor(COMMAND_RUN);
                return Debugger.startInDebugger(executablesFor.get(0));
            }
            List<List<String>> executablesFor = config.executablesFor(command);
            String arg = executablesFor.stream().map(c -> quote(c.stream().map(p -> quote(p)).collect(Collectors.joining(" ")))).collect(Collectors.joining(" "));
            return new ProcessBuilder("java", "-classpath", module.getAbsolutePath(), Runner.class.getName(), arg).directory(FileUtil.toFile(prj.getProjectDirectory())).start();
        }, new ExecutionDescriptor(), ProjectUtils.getInformation(prj).getDisplayName() + " - " + command).run();
    }

    private String quote(String s) {
        return s.replace("_", "_u_").replace(" ", "_s_");
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (COMMAND_DEBUG.equals(command)) {
            command = COMMAND_RUN;
        }
        return prj.getActiveBuildConfiguration().executablesFor(command) != null;
    }
    
}
