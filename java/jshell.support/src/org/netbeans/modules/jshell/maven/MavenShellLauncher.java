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
package org.netbeans.modules.jshell.maven;

import java.io.IOException;
import java.util.List;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.launch.ShellAgent;
import org.netbeans.modules.jshell.launch.ShellLaunchManager;
import org.netbeans.modules.jshell.launch.PropertyNames;
import org.netbeans.modules.jshell.project.LaunchedProjectOpener;
import org.netbeans.modules.jshell.project.ShellProjectUtils;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.ExecutionResultChecker;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(
    projectType = "org-netbeans-modules-maven",
    service = {
        LateBoundPrerequisitesChecker.class,
        PrerequisitesChecker.class,
        ExecutionResultChecker.class
    }
)
public class MavenShellLauncher implements PrerequisitesChecker, LateBoundPrerequisitesChecker, ExecutionResultChecker {
    private static final String PLACEHOLDER_JSHELL_ARGS = "${jshell.args}"; // NOI18N
    private static final String PROPERTY_EXEC_ARGS = "exec.args"; // NOI18N
    private static final String PROPERTY_JSHELL_KEY = "jshell.authKey"; // NOI18N
    private static final String PROPERTY_JSHELL_AGENT = "jshell.agent"; // NOI18N
    
    @Override
    public boolean checkRunConfig(RunConfig config) {
        return true;
    }

    @Override
    public void executionResult(RunConfig config, ExecutionContext res, int resultCode) {
        String s = config.getProperties().get(PROPERTY_JSHELL_KEY);
        if (s == null) {
            return;
        }
        RequestProcessor.getDefault().post(() -> {
            ShellLaunchManager.getInstance().destroyAgent(s);
        });
    }

    @Override
    public boolean checkRunConfig(RunConfig config, ExecutionContext con) {
        boolean enabled = Boolean.parseBoolean(config.getProperties().get(PropertyNames.JSHELL_ENABLED));
        if (!enabled) {
            return true;
        }
        LaunchedProjectOpener.init();
        
        Project project = config.getProject();
        boolean isDebug = config.getActionName().equals("debug");
        boolean ok = isDebug ||
                config.getActionName().equals("run");
        
        if (!ok) {
            return true;
        }
        ShellAgent agent;
        ShellLaunchManager mgr = ShellLaunchManager.getInstance();
        try {
            agent = mgr.openForProject(project, isDebug);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
        agent.setIO(con.getInputOutput(), config.getTaskDisplayName());
        JavaPlatform platform = ShellProjectUtils.findPlatform(project);
        List<String> args = ShellLaunchManager.buildLocalJVMAgentArgs(
                platform,
                agent, 
                config.getProperties()::get
        );
        String agentString = args.get(args.size() -1);
        args.addAll(ShellProjectUtils.launchVMOptions(project));
        String execArgs = config.getProperties().get(PROPERTY_EXEC_ARGS);
        if (execArgs != null) {
            StringBuilder sb = new StringBuilder();
            for (String a : args) {
                if (sb.length() > 0) {
                    sb.append(" "); // NOI18N
                }
                sb.append(ShellProjectUtils.quoteCmdArg(a));
            }
            String newArgs;

            if (execArgs.contains(PLACEHOLDER_JSHELL_ARGS)) {
                newArgs = execArgs.replace(PLACEHOLDER_JSHELL_ARGS, sb.toString());
            } else {
                newArgs = sb.append(" ").append(execArgs).toString(); // NOI18N
            }
            config.setProperty(PROPERTY_EXEC_ARGS, newArgs);
        }        
        config.setProperty(PROPERTY_JSHELL_AGENT, agentString);
        config.setProperty(PROPERTY_JSHELL_KEY, agent.getAuthorizationKey());
        return true;
    }
    
}
