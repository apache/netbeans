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

package org.netbeans.modules.groovy.grailsproject;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.actions.ConfigurationSupport;
import org.netbeans.modules.groovy.grailsproject.commands.GrailsCommandSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.LifecycleManager;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class GrailsActionProvider implements ActionProvider {

    public static final String COMMAND_GRAILS_SHELL = "grails-shell"; // NOI18N
    public static final String COMMAND_COMPILE = "compile"; // NOI18N
    public static final String COMMAND_UPGRADE = "upgrade"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(GrailsActionProvider.class.getName());

    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_RUN,
        COMMAND_DEBUG,
        COMMAND_TEST,
        COMMAND_CLEAN,
        COMMAND_DELETE,
        COMMAND_GRAILS_SHELL,
        COMMAND_COMPILE,
        COMMAND_UPGRADE
    };

    private final GrailsProject project;

    public GrailsActionProvider(GrailsProject project) {
        this.project = project;
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions.clone();
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        final GrailsPlatform runtime = GrailsPlatform.getDefault();

        // If the runtime is not configured and we are performing anything except delete show warning
        if (!runtime.isConfigured() && !COMMAND_DELETE.equals(command)) {
            ConfigurationSupport.showConfigurationWarning(runtime);
            return;
        }
        switch (command) {
            case COMMAND_RUN:
                LifecycleManager.getDefault().saveAll();
                executeRunAction();
                break;
            case COMMAND_DEBUG:
                LifecycleManager.getDefault().saveAll();
                executeRunAction(true);
                break;
            case COMMAND_GRAILS_SHELL:
                executeSimpleAction("shell"); // NOI18N
                break;
            case COMMAND_TEST:
                executeSimpleAction("test-app"); // NOI18N
                break;
            case COMMAND_CLEAN:
                executeSimpleAction("clean"); // NOI18N
                break;
            case COMMAND_COMPILE:
                executeSimpleAction("compile"); // NOI18N
                break;
            case COMMAND_UPGRADE:
                executeSimpleAction("upgrade"); // NOI18N
                break;
            case COMMAND_DELETE:
                DefaultProjectOperations.performDefaultDeleteOperation(project);
                break;
            case COMMAND_BUILD:
                executeSimpleAction("war"); // NOI18N
                break;
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return true;
    }

    private void executeRunAction() {
        executeRunAction(false);
    }

    private void executeRunAction(final boolean debug) {
        final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
        Process process = null;
        if (serverState != null && serverState.isRunning()) {
            if (!debug /*|| debug == serverState.isDebug()*/) {
                URL url = serverState.getRunningUrl();
                if (url != null) {
                    GrailsCommandSupport.showURL(url, debug, project);
                }
                return;
            } else {
                process = serverState.getProcess();
                if (process != null) {
                    process.destroy();
                }
            }
        }

        final Process oldProcess = process;
        Callable<Process> callable = new Callable<Process>() {

            @Override
            public Process call() throws Exception {
                if (oldProcess != null) {
                    oldProcess.waitFor();
                }
                Callable<Process> inner = ExecutionSupport.getInstance().createRunApp(
                        GrailsProjectConfig.forProject(project), debug);
                Process process = inner.call();
                final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
                if (serverState != null) {
                    serverState.setProcess(process);
                    serverState.setDebug(debug);
                }
                return process;
            }
        };

        ProjectInformation inf = ProjectUtils.getInformation(project);
        String displayName = inf.getDisplayName() + " (run-app)"; // NOI18N

        ExecutionDescriptor descriptor = project.getCommandSupport().getRunDescriptor(debug);

        ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
        service.run();
    }

    private void executeSimpleAction(String command) {
        ProjectInformation inf = ProjectUtils.getInformation(project);
        String displayName = inf.getDisplayName() + " (" + command + ")"; // NOI18N

        Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand(
                command, GrailsProjectConfig.forProject(project));

        ExecutionDescriptor descriptor = project.getCommandSupport().getDescriptor(command);

        ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
        service.run();
    }

}
