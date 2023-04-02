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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.rust.cargo.api.CargoCommand;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.netbeans.modules.rust.cargo.api.Cargo;
import org.netbeans.modules.rust.cargo.api.RustPackage;
import org.netbeans.modules.rust.options.api.CargoOptions;
import org.openide.LifecycleManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * CargoBuildImpl is used to invoke a set of predefined "cargo" commands.
 *
 * @author antonio
 */
@ServiceProvider(service = Cargo.class)
public class CargoBuildImpl implements Cargo {

    private static final Logger LOG = Logger.getLogger(CargoBuildImpl.class.getName());

    /**
     * A Callable used to invoke a single Cargo command (such as "cargo cargo",
     * for instance.
     */
    private static class CargoProcess implements Callable<Process> {

        private final CargoTOML cargotoml;
        private final CargoCommand command;
        private final InputOutput console;
        private final String[] options;

        CargoProcess(CargoTOML cargotoml, CargoCommand command, String[] options, InputOutput console) {
            this.cargotoml = cargotoml;
            this.command = command;
            this.console = console;
            this.options = options;
        }

        @Override
        public Process call() throws Exception {
            org.netbeans.api.extexecution.base.ProcessBuilder pb = org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();

            ArrayList<String> arguments = new ArrayList<>();
            arguments.addAll(Arrays.asList(command.arguments));
            if (options != null) {
                arguments.addAll(Arrays.asList(options));
            }

            File workingDirectory = FileUtil.toFile(cargotoml.getFileObject()).getParentFile();
            pb.setWorkingDirectory(workingDirectory.getAbsolutePath());
            pb.setRedirectErrorStream(false);
            Path cargo = CargoOptions.getCargoLocation(false);
            pb.setExecutable(cargo.toString());
            pb.setArguments(arguments);

            console.getOut().println(String.format("%n$ cargo %s", String.join(" ", arguments))); // NOI18N

            return pb.call();
        }
    }

    /**
     * A Callable used to invoke an array of commands.
     */
    @NbBundle.Messages({
        "MSG_WORKING_DIRECTORY=Working directory:",
        "MSG_CARGO_PATH=Cargo: "
    })
    public static class SequentialCargoProcesses implements Callable<Integer> {

        private final CargoTOML cargotoml;
        private final CargoCommand[] commands;
        private final String[] options;

        SequentialCargoProcesses(CargoTOML cargotoml, CargoCommand[] commands, String[] options) {
            this.cargotoml = cargotoml;
            this.commands = commands;
            this.options = options;
        }

        @Override
        public Integer call() throws Exception {
            Path cargo = CargoOptions.getCargoLocation(true);
            if (cargo == null) {
                return -1;
            }
            // Get a proper console for the input/output
            String projectName = cargotoml.getPackageName();
            String commandNames = Arrays.stream(commands).map(CargoCommand::getDisplayName).collect(Collectors.joining(",")); // NOI18N
            String ioName = String.format("%s (%s)", projectName, commandNames); // NOI18N
            InputOutput console = IOProvider.getDefault().getIO(ioName, false);
            console.select();

            File workingDirectory = FileUtil.toFile(cargotoml.getFileObject()).getParentFile();
            console.getOut().format("# %s %s%n", // NOI18N
                    NbBundle.getMessage(CargoBuildImpl.class, "MSG_WORKING_DIRECTORY"), 
                    workingDirectory.getAbsolutePath()); 
            console.getOut().format("# %s %s%n",  // NOI18N
                    NbBundle.getMessage(CargoBuildImpl.class, "MSG_CARGO_PATH"), 
                    cargo); 

            ExecutionDescriptor ed = new ExecutionDescriptor()
                    .inputOutput(IOProvider.getDefault().getIO(ioName, false))
                    .inputVisible(true)
                    .frontWindow(false)
                    .frontWindowOnError(true)
                    .noReset(true)
                    .showProgress(false)
                    .controllable(true);

            int resultCode = 0;

            for (CargoCommand command : commands) {
                CargoProcess process = new CargoProcess(cargotoml, command, options, console);
                ExecutionService service = ExecutionService.newService(process, ed, ioName);
                Future<Integer> resultCodeFuture = service.run();
                try {
                    resultCode = resultCodeFuture.get();
                } catch (Exception e) {
                    console.getErr().println(String.format("Cargo execution failed: %s%n", e.getMessage()));
                    Exceptions.printStackTrace(e);
                }
                if (resultCode != 0) {
                    console.getErr().println(String.format("Command \"cargo %s\" failed with exit status %d", // NOI18N
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
    public void cargo(CargoTOML cargotoml, CargoCommand[] commands, String... options) throws IOException {
        if (cargotoml == null) {
            throw new NullPointerException("Missing Cargo.toml file"); // NOI18N
        }
        if (commands.length == 0) {
            return;
        }
        Path cargo = CargoOptions.getCargoLocation(true);
        if (cargo == null) {
            return;
        }
        // Let's save stuff just in case
        LifecycleManager.getDefault().saveAll();
        requestProcessor.submit(new SequentialCargoProcesses(cargotoml, commands, options));
    }

    /**
     * Runs `cargo search [text] --limit 15 --color never`
     */
    private static final class CargoSearch implements Callable<List<RustPackage>> {

        private final String text;
        private final CargoTOML cargotoml;

        public CargoSearch(CargoTOML cargotoml, String text) {
            this.cargotoml = cargotoml;
            this.text = text;
        }

        @Override
        public List<RustPackage> call() throws Exception {
            Path cargo = CargoOptions.getCargoLocation(true);
            if (cargo == null) {
                return Collections.emptyList();
            }
            org.netbeans.api.extexecution.base.ProcessBuilder pb = org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
            File workingDirectory = new File(System.getProperty("user.home")); // NOI18N
            pb.setWorkingDirectory(workingDirectory.getAbsolutePath());
            pb.setRedirectErrorStream(false);
            pb.setExecutable(cargo.toString());
            String[] arguments = {
                "search", // NOI18N
                text, // TODO: What happens with spaces?
                "--limit", // NOI18N
                "30", // NOI18N
                "--color", // NOI18N
                "never", // NOI18N
            };
            pb.setArguments(Arrays.asList(arguments));
            Process process = pb.call();
            ArrayList<String> lines = new ArrayList<>(20);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                do {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    lines.add(line);
                } while (true);
            }
            process.waitFor(5, TimeUnit.SECONDS);
            List<RustPackage> packages = filterLines(cargotoml, lines);
            process.destroy();
            return packages;
        }

    }

    static List<RustPackage> filterLines(CargoTOML cargotoml, List<String> lines) {
        ArrayList<RustPackage> packages = new ArrayList<>(lines.size());
        for (String line : lines) {
            String name = null;
            String version = null;
            String description = null;
            int i = line.indexOf(' ');
            if (i == -1) {
                break;
            }
            name = line.substring(0, i);
            line = line.substring(i + 1);

            i = line.indexOf('"');
            if (i == -1) {
                break;
            }
            line = line.substring(i + 1);
            i = line.indexOf('"');
            if (i == -1) {
                break;
            }
            version = line.substring(0, i);
            line = line.substring(i + 1);
            i = line.indexOf('#');
            if (i == -1) {
                break;
            }
            description = line.substring(i + 1);
            description = description.replace("\n", "");

            RustPackage rustPackage = RustPackage.withNameVersionAndDescription(cargotoml, name, version, description);
            packages.add(rustPackage);
        }
        return packages;
    }

    @Override
    public Future<List<RustPackage>> search(CargoTOML cargotoml, String text) throws IOException {
        return requestProcessor.submit(new CargoSearch(cargotoml, text));
    }

}
