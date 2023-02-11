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
package org.netbeans.modules.rust.cargo.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.rust.cargo.api.CargoBuild;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author antonio
 */
@ServiceProvider(service = CargoBuild.class)
public class CargoBuildImpl implements CargoBuild {

    private static final Logger LOG = Logger.getLogger(CargoBuildImpl.class.getName());

    /**
     * A Callable used to invoke one Cargo command (such as "cargo build", for
     * instance.
     */
    private static class CargoProcess implements Callable<Process> {

        private final Project project;
        private final CargoBuildCommand command;
        private final InputOutput console;

        CargoProcess(Project project, CargoBuildCommand command, InputOutput console) {
            this.project = project;
            this.command = command;
            this.console = console;
        }

        @Override
        public Process call() throws Exception {
            org.netbeans.api.extexecution.base.ProcessBuilder pb = org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();

            File workingDirectory = FileUtil.toFile(project.getProjectDirectory());
            pb.setWorkingDirectory(workingDirectory.getAbsolutePath());
            pb.setRedirectErrorStream(false);
            pb.setExecutable("cargo");
            pb.setArguments(Arrays.asList(command.arguments));

            console.getOut().println(String.format("-- cargo %s --", String.join(",", command.arguments)));

            return pb.call();
        }
    }

    /**
     *
     */
    public static class SequentialCargoProcesses implements Callable<Integer> {

        private final Project project;
        private final CargoBuildCommand[] commands;

        SequentialCargoProcesses(Project project, CargoBuildCommand[] commands) {
            this.project = project;
            this.commands = commands;
        }

        @Override
        public Integer call() throws Exception {
            // Get a proper console for the input/output
            String projectName = ProjectUtils.getInformation(project).getName();
            String commandNames = Arrays.stream(commands).map(CargoBuildCommand::getDisplayName).collect(Collectors.joining(","));
            String ioName = String.format("%s (%s)", projectName, commandNames);
            InputOutput console = IOProvider.getDefault().getIO(ioName, false);

            ExecutionDescriptor ed = new ExecutionDescriptor()
                    .inputOutput(IOProvider.getDefault().getIO(ioName, false))
                    .inputVisible(true)
                    .frontWindow(false)
                    .frontWindowOnError(true)
                    .noReset(true)
                    .showProgress(false)
                    .controllable(true);

            int resultCode = 0;

            for (CargoBuildCommand command : commands) {
                CargoProcess process = new CargoProcess(project, command, console);
                ExecutionService service = ExecutionService.newService(process, ed, ioName);
                Future<Integer> resultCodeFuture = service.run();
                resultCode = resultCodeFuture.get();
                if (resultCode != 0) {
                    console.getErr().println(String.format("Command \"cargo %s\" failed with exit status %d",
                            String.join(" ", command.arguments),
                            resultCode));
                    break;
                }
            }
            return resultCode;
        }

    }

    private final RequestProcessor requestProcessor;

    public CargoBuildImpl() {
        this.requestProcessor = new RequestProcessor(CargoBuildImpl.class);
    }

    @Override
    public void build(Project project, CargoBuildCommand[] commands) throws IOException {
        CargoTOML cargotoml = project.getLookup().lookup(CargoTOML.class);
        if (cargotoml == null) {
            throw new IOException(String.format("Don't know how to run  project (%s) that has not a Cargo.toml on its lookup",
                    project.getProjectDirectory().getNameExt()));
        }
        if (commands.length == 0) {
            return;
        }
        requestProcessor.submit(new SequentialCargoProcesses(project, commands));
    }

}
