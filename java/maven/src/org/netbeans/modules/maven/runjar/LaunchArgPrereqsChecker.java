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
package org.netbeans.modules.maven.runjar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Performs default processing to provide {@code exec.vmArgs} and {@code exec.appArgs} properties. Unless the 
 * arguments are processed already (see {@link MavenExecuteUtils#RUN_EXPLICIT_PROCESSED}), the Checker consults
 * {@link StartupExtender}s in the default Lookup and {@link ExplicitProcessParameters} in the action context Lookup
 * to build {@code exec.vmArgs} and {@code exec.appArgs} properties.
 * 
 * @author sdedic
 */
@ProjectServiceProvider(service=LateBoundPrerequisitesChecker.class, projectType="org-netbeans-modules-maven/_any")
public class LaunchArgPrereqsChecker implements LateBoundPrerequisitesChecker {

    @Override
    public boolean checkRunConfig(RunConfig config, ExecutionContext con) {
        StartupExtender.StartMode mode;
        
        String actionName = config.getActionName();
        if (ActionProvider.COMMAND_RUN.equals(actionName) || ActionProviderImpl.COMMAND_RUN_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.NORMAL;
        } else if (ActionProvider.COMMAND_DEBUG.equals(actionName) || ActionProviderImpl.COMMAND_DEBUG_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.DEBUG;
        } else if (ActionProvider.COMMAND_PROFILE.equals(actionName) || ActionProvider.COMMAND_PROFILE_SINGLE.equals(actionName) || ActionProviderImpl.COMMAND_PROFILE_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.PROFILE;
        } else if (ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName)) {
            mode = StartupExtender.StartMode.TEST_PROFILE;
        } else {
            return true;
        }

        Map<? extends String, ? extends String> props = config.getProperties();
        
        if ("true".equals(props.get(MavenExecuteUtils.RUN_EXPLICIT_PROCESSED))) {
            return true;
        }
        
        boolean vmArgsPresent = props.containsKey(MavenExecuteUtils.RUN_VM_PARAMS);
        boolean appArgsPresent = props.containsKey(MavenExecuteUtils.RUN_APP_PARAMS);
        boolean execArgsPresent = props.containsKey(MavenExecuteUtils.RUN_PARAMS);

        List<String> fixedArgs = new ArrayList<String>();
        if (execArgsPresent || vmArgsPresent || appArgsPresent) {
            // define exec.vmArgs and exec.appArgs
            InstanceContent ic = new InstanceContent();
            Project p = config.getProject();
            if (p != null) {
                ic.add(p);
                ActiveJ2SEPlatformProvider pp = p.getLookup().lookup(ActiveJ2SEPlatformProvider.class);
                if (pp != null) {
                    ic.add(pp.getJavaPlatform());
                }
            }
            for (StartupExtender group : StartupExtender.getExtenders(new AbstractLookup(ic), mode)) {
                fixedArgs.addAll(group.getRawArguments());
            }
        }

        ExplicitProcessParameters changedParams = null;
        List<String> vmArgsValue;
        List<String> appArgsValue;

        vmArgsValue = new ArrayList<>(Arrays.asList(MavenExecuteUtils.splitCommandLine(props.get(MavenExecuteUtils.RUN_VM_PARAMS))));
        appArgsValue = new ArrayList<>(Arrays.asList(MavenExecuteUtils.splitCommandLine(props.get(MavenExecuteUtils.RUN_APP_PARAMS))));
        
        ExplicitProcessParameters injectParams = ExplicitProcessParameters.buildExplicitParameters(config.getActionContext());

        List<String> vmArgs = new ArrayList<>(fixedArgs);
        if (!(fixedArgs.isEmpty() && injectParams.isEmpty())) {
            changedParams = ExplicitProcessParameters.
                builder().launcherArgs(vmArgsValue).
                // include user arguments, if any
                args(appArgsValue).
                // allow to append or override from context injectors.
                combine(
                    injectParams
                ).build();
            
            vmArgs.addAll(changedParams.getLauncherArguments());
            config.setProperty(MavenExecuteUtils.RUN_VM_PARAMS, 
                    MavenExecuteUtils.joinParameters(vmArgs));
            config.setProperty(MavenExecuteUtils.RUN_APP_PARAMS, 
                    MavenExecuteUtils.joinParameters(changedParams.getArguments()));
        } else {
            vmArgs.addAll(vmArgsValue);
            config.setProperty(MavenExecuteUtils.RUN_VM_PARAMS, 
                    MavenExecuteUtils.joinParameters(vmArgs));
            config.setProperty(MavenExecuteUtils.RUN_APP_PARAMS, 
                    MavenExecuteUtils.joinParameters(appArgsValue));
        }
        File workingDirectory = injectParams.getWorkingDirectory();
        if (workingDirectory != null) {
            config.setProperty(MavenExecuteUtils.RUN_WORKDIR,
                    workingDirectory.getAbsolutePath());
        }
        Map<String, String> environmentVariables = injectParams.getEnvironmentVariables();
        for (Map.Entry<String, String> env : environmentVariables.entrySet()) {
            String value = env.getValue();
            if (value == null) {
                // The environment variable is to be removed when the value is null
                value = MavenExecuteUtils.ENV_REMOVED;
            }
            config.setProperty(MavenExecuteUtils.ENV_PREFIX + env.getKey(), value);
        }
        return true;
    }
}
