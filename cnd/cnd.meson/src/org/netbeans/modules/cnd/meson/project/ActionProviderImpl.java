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
package org.netbeans.modules.cnd.meson.project;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class ActionProviderImpl implements ActionProvider {
    public static final String COMMAND_SETUP = "setup"; // NOI18N
    public static final String COMMAND_DEBUG_TEST = "debug.test"; // NOI18N
    public static final String COMMAND_DEBUG_STEP_INTO_TEST = "debug.stepinto.test"; // NOI18N

    private static final String[] SUPPORTED_ACTIONS = {
        COMMAND_SETUP,
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_RUN,
        COMMAND_DEBUG,
        COMMAND_DEBUG_STEP_INTO,
        COMMAND_DEBUG_SINGLE,
        COMMAND_TEST,
        COMMAND_DEBUG_TEST,
        COMMAND_DEBUG_STEP_INTO_TEST,
    };

    private final MesonProject project;

    public ActionProviderImpl(MesonProject project) {
        this.project = project;
    }

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public String[] getSupportedActions() {
        return SUPPORTED_ACTIONS;
    }

    @Override
    public void invokeAction(String action, Lookup context) throws IllegalArgumentException {
        File module = InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-cnd-meson.jar", "org.netbeans.modules.cnd.meson", false);

        ExecutionDescriptor executionDescriptor =
            new ExecutionDescriptor()
                .showProgress(true)
                .showSuspended(true)
                .frontWindowOnError(true)
                .controllable(true)
                .errConvertorFactory(new LineConvertorFactoryImpl())
                .outConvertorFactory(new LineConvertorFactoryImpl());

        ExecutionService.newService(
            () -> {
                LifecycleManager.getDefault().saveAll();

                List<List<String>> commandsToExecute = new ArrayList<>();
                Configuration cfg = project.getActiveConfiguration();
                File runDir = FileUtil.toFile(project.getProjectDirectory());
                File buildDir = Paths.get(project.getProjectDirectory().getPath(), cfg.getBuildDirectory()).toFile();

                if (!buildDir.exists() && !action.equals(COMMAND_SETUP)) {
                    commandsToExecute.add(getMesonCommandLineFor(COMMAND_SETUP));
                }

                switch (action)
                {
                    case COMMAND_REBUILD:
                        if (buildDir.exists()) {
                            commandsToExecute.add(getMesonCommandLineFor(COMMAND_CLEAN));
                        }
                        commandsToExecute.add(getMesonCommandLineFor(COMMAND_BUILD));
                        break;
                    case COMMAND_RUN:
                        if (!buildDir.exists()) {
                            commandsToExecute.add(getMesonCommandLineFor(COMMAND_BUILD));
                        }
                        commandsToExecute.add(Arrays.asList(cfg.getRunExecutable(), cfg.getRunArguments()));
                        if ((cfg.getRunDirectory() != null) && !cfg.getRunDirectory().isEmpty()) {
                            runDir = Paths.get(project.getProjectDirectory().getPath(), cfg.getRunDirectory()).toFile();
                        }
                        break;
                    case COMMAND_DEBUG:
                        // TODO
                        break;
                    default:
                        List<String> command = getMesonCommandLineFor(action);
                        if (command != null) {
                            commandsToExecute.add(command);
                        }   break;
                }
                String arg = Utils.encode(commandsToExecute);
                return new ProcessBuilder("java", "-classpath", module.getAbsolutePath(), Runner.class.getName(), arg).directory(runDir).start();
            },
            executionDescriptor,
            ProjectUtils.getInformation(project).getDisplayName() + " - " + action
        ).run();
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (null != command) switch (command)
        {
            case COMMAND_DEBUG:
                return false; //isActionEnabled(COMMAND_RUN, context);
            case COMMAND_REBUILD:
                return isActionEnabled(COMMAND_CLEAN, context) && isActionEnabled(COMMAND_BUILD, context);
            case COMMAND_RUN:
                Configuration cfg = project.getActiveConfiguration();
                return !Utils.isEmpty(cfg.getRunExecutable()) && Paths.get(project.getProjectDirectory().getPath(), cfg.getRunExecutable()).toFile().isFile();
            default:
                break;
        }
        return true;
    }

    private List<String> getMesonCommandLineFor(String action) {
        List<String> command = null;
        Configuration config = project.getActiveConfiguration();

        switch (action) {
            case COMMAND_BUILD:
                command = new ArrayList<>();
                command.add("meson"); //NOI18N
                command.add("compile"); //NOI18N
                command.add("-C"); //NOI18N
                command.add(config.getBuildDirectory());
                command.add(config.getAdditionalArgumentsFor(action));
                break;

            case COMMAND_CLEAN:
                command = new ArrayList<>();
                command.add("meson"); //NOI18N
                command.add("compile"); //NOI18N
                command.add("-C"); //NOI18N
                command.add(config.getBuildDirectory());
                command.add("--clean"); //NOI18N
                break;

            case COMMAND_SETUP:
                command = new ArrayList<>();
                command.add("meson"); //NOI18N
                command.add("setup"); //NOI18N
                command.add("--wipe"); //NOI18N

                if (!Utils.isEmpty(config.getBuildType())) {
                    command.add("--buildtype"); //NOI18N
                    command.add(config.getBuildType());
                }

                if (!Utils.isEmpty(config.getWrapMode())) {
                    command.add("--wrap-mode"); //NOI18N
                    command.add(config.getWrapMode());
                }

                command.add(config.getAdditionalArgumentsFor(action));
                command.add(config.getBuildDirectory());
                break;

            case COMMAND_TEST:
                command = new ArrayList<>();
                command.add("meson"); //NOI18N
                command.add("test"); //NOI18N
                command.add("-C"); //NOI18N
                command.add(config.getBuildDirectory());
                command.add(config.getAdditionalArgumentsFor(action));
                break;
        }

        return command;
    }

    public static final class Runner {
        /**
         * @param args the command line arguments
         */
        public static void main(String[] args) throws Exception {
            for (List<String> command : Utils.decode(args[0])) {
                int result = new ProcessBuilder(command).inheritIO().start().waitFor();
                if (result != 0) {
                    System.exit(result);
                }
            }
        }
    }

    public static Action createSetupAction() {
        return
            ProjectSensitiveActions.projectCommandAction(
                COMMAND_SETUP,
                NbBundle.getMessage(ActionProviderImpl.class, "LBL_MesonSetupCommand"),
                MesonProject.getIcon());
    }
}
