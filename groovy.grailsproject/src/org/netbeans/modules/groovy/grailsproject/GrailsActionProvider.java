/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
