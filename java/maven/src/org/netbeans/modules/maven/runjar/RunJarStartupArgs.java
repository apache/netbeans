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

package org.netbeans.modules.maven.runjar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.ActionProviderImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.execute.ModelRunConfig;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

@ProjectServiceProvider(service=LateBoundPrerequisitesChecker.class, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_JAR)
public class RunJarStartupArgs implements LateBoundPrerequisitesChecker {
    /**
     * Marker that separates VM args main class name from 'user specified arguments', whatever that
     * means for a selected goal.
     * If not present, then any injected arguments will be appended at the end of 'exec.args' property. If
     * present, the action context can <b>replace</b> the arguments.
     * @since 2.143
     */
    public static final String USER_PROGRAM_ARGS_MARKER = "%args"; // NOI18N
    
    /**
     * Splits a command line, pays respect to quoting and newlines.
     * @param line original line
     * @return line split into individual arguments.
     */
    private static String[] splitCommandLine(String line) {
        if (line == null) {
            return new String[0];
        }
        String l = line.trim();
        if (l.isEmpty()) {
            return new String[0];
        }
        List<String> result = new ArrayList<>();
        for (String part : MavenExecuteUtils.propertySplitter(l, true)) {
            result.add(part);
        }
        return result.toArray(new String[0]);
    }
    
    @Override public boolean checkRunConfig(RunConfig config, ExecutionContext con) {
        String actionName = config.getActionName();
        StartupExtender.StartMode mode;
        if (ActionProvider.COMMAND_RUN.equals(actionName) || ActionProviderImpl.COMMAND_RUN_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.NORMAL;
        } else if (ActionProvider.COMMAND_DEBUG.equals(actionName) || ActionProviderImpl.COMMAND_DEBUG_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.DEBUG;
        } else if (ActionProvider.COMMAND_PROFILE.equals(actionName) || ActionProvider.COMMAND_PROFILE_SINGLE.equals(actionName) || ActionProviderImpl.COMMAND_PROFILE_MAIN.equals(actionName)) {
            mode = StartupExtender.StartMode.PROFILE;
        } else if (ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName)) {
            mode = StartupExtender.StartMode.TEST_PROFILE;
        } else {
            // XXX could also set argLine for COMMAND_TEST and relatives (StartMode.TEST_*); need not be specific to TYPE_JAR
            return true;
        }
        boolean isTestScope = false;
        Map<? extends String, ? extends String> props = config.getProperties();
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
        
        boolean splitParameters = vmArgsPresent || appArgsPresent;
        List<String> joinedArgs = new ArrayList<>(fixedArgs);
        
        ExplicitProcessParameters changedParams = null;
        List<String> vmArgsValue;
        List<String> appArgsValue;
        
        ExplicitProcessParameters injectParams = ExplicitProcessParameters.buildExplicitParameters(config.getActionContext());

        if (splitParameters) {
            vmArgsValue = new ArrayList<>(Arrays.asList(splitCommandLine(props.get(MavenExecuteUtils.RUN_VM_PARAMS))));
            appArgsValue = new ArrayList<>(Arrays.asList(splitCommandLine(props.get(MavenExecuteUtils.RUN_APP_PARAMS))));
        } else {
            String val = props.get(MavenExecuteUtils.RUN_PARAMS);
            // split the 'exec.args' property to main and user arguments; userArgs will be null
            // if no user arguments are present or the marker is not found
            String[] argParts = MavenExecuteUtils.splitAll(val, false);

            vmArgsValue = new ArrayList<>(Arrays.asList(splitCommandLine(argParts[0])));
            String[] mainClass = splitCommandLine(argParts[1]);
            appArgsValue = new ArrayList<>(Arrays.asList(splitCommandLine(argParts[2])));

            if (mainClass.length == 0) {
                // accept userargs, since we don't know where the division is, make it fixed in the processing.
                joinedArgs.addAll(appArgsValue);
                appArgsValue = Collections.emptyList();
            } else {
                joinedArgs.addAll(Arrays.asList(mainClass));
            }

            // patch: if there's -classpath %classpath in the vmArgsValue, move it at the start of fixedArgs
            int at = vmArgsValue.indexOf("-classpath");
            if (at >= 0 && vmArgsValue.size() > at + 1 && "%classpath".equals(vmArgsValue.get(at + 1))) {
                List<String> toMove = vmArgsValue.subList(at, at + 2);
                joinedArgs.subList(0, 0).addAll(toMove);
                toMove.clear();
            }
        }

        // RunConfig may have merged in POM VM args and command, but indicated that using "exec.args.merged" property
        // there can be prefix (= vm args) and suffix (app args) that may need to be replaced.
        // Also run the scan when there's no apparent vmArg or appArg - maybe they are configured directly into the 
        // exec.args property by the user - but recognize only the basic pattern; action mapping must be adjusted for more complex setups.
        if ("true".equals(config.getProperties().get(ModelRunConfig.EXEC_MERGED)) ||
            (vmArgsValue.isEmpty() && appArgsValue.isEmpty())) {
            String cmdLine = props.get(MavenExecuteUtils.RUN_PARAMS);
            if (cmdLine != null) {
                String template = MavenExecuteUtils.doesNotSpecifyCustomExecArgs(false, config.getProperties());
                // the template should be never null, as EXEC_MERGED can be set only if doesNotSpecifyCustomExecArgs already returned true...
                int templateIndex = template == null ? -1 : cmdLine.indexOf(template);
                if (templateIndex > 0) {
                    String prefix = cmdLine.substring(0, templateIndex);
                    String suffix = cmdLine.substring(templateIndex + template.length());

                    // for !splitParameters, vmArgs & args were populated above
                    if (splitParameters) {
                        vmArgsValue.addAll(0, Arrays.asList(splitCommandLine(prefix.trim())));
                        appArgsValue.addAll(0, Arrays.asList(splitCommandLine(suffix.trim())));
                    }
                    // replace back the template, since we handle the VM (prefix) and app (postfix)
                    config.setProperty(MavenExecuteUtils.RUN_PARAMS, template);
                }
            }
        }
        
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
            
            if (!splitParameters) {
                // the original cmdline was split between vmArgsValue appArgsValue and joinedArgs, so
                // any customizations shoudl remain preserved.
                String newParams = String.join(" ", changedParams.getAllArguments(joinedArgs));
                config.setProperty(MavenExecuteUtils.RUN_PARAMS, newParams);
            }
            
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

        if ("test".equals(props.get("exec.classpathScope"))) {
            isTestScope = true;
        }
        if (isTestScope) { //#230190
            String[] goals = config.getGoals().toArray(new String[0]);
            for (int i = 0; i < goals.length;i++) {
                if ("process-classes".equals(goals[i])) {
                    goals[i] = "process-test-classes";
                }
            }
            ((BeanRunConfig)config).setGoals(new ArrayList<String>(Arrays.asList(goals)));
        }
        config.setProperty(MavenExecuteUtils.RUN_EXPLICIT_PROCESSED, "true");
        return true;
    }

}
