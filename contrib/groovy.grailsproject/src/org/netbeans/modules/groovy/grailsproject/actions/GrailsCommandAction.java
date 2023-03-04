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

package org.netbeans.modules.groovy.grailsproject.actions;

import java.net.URL;
import java.util.concurrent.Callable;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.GrailsServerState;
import static org.netbeans.modules.groovy.grailsproject.actions.Bundle.*;
import org.netbeans.modules.groovy.grailsproject.commands.GrailsCommandChooser;
import org.netbeans.modules.groovy.grailsproject.commands.GrailsCommandSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author Petr Hejl
 */
@Messages("CTL_GrailsCommandAction=Run/Debug Grails Command...")
@ActionID(id = "org.netbeans.modules.groovy.grailsproject.actions.GrailsCommandAction", category = "Groovy")
@ActionRegistration(lazy = false, displayName = "#CTL_GrailsCommandAction")
public class GrailsCommandAction extends CallableSystemAction {

    @Override
    public void performAction() {
        final GrailsPlatform runtime = GrailsPlatform.getDefault();
        if (!runtime.isConfigured()) {
            ConfigurationSupport.showConfigurationWarning(runtime);
            return;
        }

        final GrailsProject project = inferGrailsProject();
        if (project == null) {
            return;
        }

        GrailsCommandChooser.CommandDescriptor commandDescriptor = GrailsCommandChooser.select(project);
        if (commandDescriptor == null) {
            return;
        }

        ProjectInformation inf = ProjectUtils.getInformation(project);
        String displayName = inf.getDisplayName() + " (" + commandDescriptor.getGrailsCommand().getCommand() + ")"; // NOI18N


        final String[] params;
        // FIXME all parameters in one String should we split it ?
        if (commandDescriptor.getCommandParams() != null && !"".equals(commandDescriptor.getCommandParams().trim())) {
            params = new String[] {commandDescriptor.getCommandParams()};
        } else {
            params = new String[] {};
        }


        Callable<Process> callable;
        ExecutionDescriptor descriptor;

        final boolean debug = commandDescriptor.isDebug();

        if (GrailsPlatform.IDE_RUN_COMMAND.equals(commandDescriptor.getGrailsCommand().getCommand())) {
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
            callable = new Callable<Process>() {
                public Process call() throws Exception {
                    if (oldProcess != null) {
                        oldProcess.waitFor();
                    }
                    Callable<Process> inner = ExecutionSupport.getInstance().createRunApp(
                            GrailsProjectConfig.forProject(project), debug, params);
                    Process process = inner.call();
                    final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
                    if (serverState != null) {
                        serverState.setProcess(process);
                        serverState.setDebug(debug);
                    }
                    return process;
                }
            };

            descriptor = project.getCommandSupport().getRunDescriptor(debug);
        } else {
            callable = ExecutionSupport.getInstance().createSimpleCommand(
                    commandDescriptor.getGrailsCommand().getCommand(), debug,
                    GrailsProjectConfig.forProject(project), params);
            descriptor = project.getCommandSupport().getDescriptor(
                    commandDescriptor.getGrailsCommand().getCommand(), debug);
        }
        ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
        service.run();
    }
    
    private static GrailsProject inferGrailsProject() {
        // try current context firstly
        Node[] activatedNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();

        if (activatedNodes != null) {
            for (Node n : activatedNodes) {
                GrailsProject result = lookupGrailsProject(n.getLookup());
                if (result != null) {
                    return result;
                }
            }
        }

        Lookup globalContext = Utilities.actionsGlobalContext();
        GrailsProject result = lookupGrailsProject(globalContext);
        if (result != null) {
            return result;
        }
        FileObject fo = globalContext.lookup(FileObject.class);
        if (fo != null) {
            result = lookupGrailsProject(FileOwnerQuery.getOwner(fo));
            if (result != null) {
                return result;
            }
        }

        // next try main project
        OpenProjects projects = OpenProjects.getDefault();
        result = lookupGrailsProject(projects.getMainProject());
        if (result != null) {
            return result;
        }

        // next try other opened projects
        for (Project project : projects.getOpenProjects()) {
            result = lookupGrailsProject(project);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static GrailsProject lookupGrailsProject(Project project) {
        if (project != null) {
            return lookupGrailsProject(project.getLookup());
        }
        return null;
    }

    private static GrailsProject lookupGrailsProject(Lookup lookup) {
        // try directly
        GrailsProject result = lookup.lookup(GrailsProject.class);
        if (result != null) {
            return result;
        }
        // try through Project instance
        Project project = lookup.lookup(Project.class);
        if (project != null) {
            result = project.getLookup().lookup(GrailsProject.class);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    public String getName() {
        return CTL_GrailsCommandAction();
    }

}
