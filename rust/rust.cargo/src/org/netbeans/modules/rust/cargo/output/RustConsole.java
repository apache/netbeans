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
package org.netbeans.modules.rust.cargo.output;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.cargo.impl.CargoCLIImpl;
import org.netbeans.modules.rust.options.api.CargoOptions;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColors;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Responsible for handling the Rust inputOutput.
 */
@NbBundle.Messages({
    "MSG_WORKING_DIRECTORY=Working directory:",
    "MSG_CARGO_PATH=Cargo: ",
    "MSG_RERUN=Re-run",
    "MSG_RERUN_SHORT=Re-runs these same commands again",
    "MSG_RUST_OPTIONS=Options",
    "MSG_RUST_OPTIONS_SHORT=Open Rust options panel",
})
public final class RustConsole {

    private static final class ReRunAction extends AbstractAction {

        @StaticResource
        private static final String RERUN_ICON = "org/netbeans/modules/rust/cargo/output/resources/rerun.png"; // NOI18N

        private final Runnable reRunCommand;

        private ReRunAction(Runnable reRunCommand) {
            super(Bundle.MSG_RERUN(), ImageUtilities.image2Icon(ImageUtilities.loadImage(RERUN_ICON)));
            this.reRunCommand = reRunCommand;
            putValue(Action.SHORT_DESCRIPTION, Bundle.MSG_RERUN_SHORT());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            reRunCommand.run();
        }

    }
    private static final class OptionsAction extends AbstractAction { // #59396

        @StaticResource
        private static final String OPTIONS_ICON = "org/netbeans/modules/rust/cargo/output/resources/options.png"; // NOI18N

        @Override
        public Object getValue(String key) {
            if (key.equals(Action.SMALL_ICON)) {
                return ImageUtilities.image2Icon(ImageUtilities.loadImage(OPTIONS_ICON));
            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
                return Bundle.MSG_RUST_OPTIONS_SHORT();
            } else {
                return super.getValue(key);
            }
        }

        public void actionPerformed(ActionEvent e) {
            CargoOptions.showRustCargoOptions();
        }

    }
    private final CargoTOML cargotoml;
    private final InputOutput inputOutput;
    private final String name;
    private final Runnable reRunAction;

    /**
     * Creates a new RustConsole that prints out Cargo command results.
     *
     * @param cargotoml The Cargo.toml for the project.
     * @param name The name of the console (that name of the tab)
     * @param reRunCommand an action to be invoked if the user wants to re-run
     * something, or null.
     * @throws IOException if an I/O error happens.
     */
    public RustConsole(CargoTOML cargotoml, String name, Runnable reRunCommand) throws IOException {
        this.cargotoml = cargotoml;
        this.name = name;
        this.reRunAction = reRunCommand;

        ArrayList<Action> actions = new ArrayList<>();

        if (reRunCommand != null) {
            actions.add(new ReRunAction(reRunCommand));
        }

        actions.add(new OptionsAction());

        inputOutput = IOProvider.getDefault().getIO(name, false, actions.toArray(new Action[0]), IOContainer.getDefault());
        inputOutput.select();
        inputOutput.getOut().reset();

        // Print working directory and cargo path
        File workingDirectory = FileUtil.toFile(cargotoml.getFileObject()).getParentFile();
        Path cargo = CargoOptions.getCargoLocation(false);

        String message = String.format("# %s %s", // NOI18N
NbBundle.getMessage(CargoCLIImpl.class, "MSG_WORKING_DIRECTORY"),
                workingDirectory.getAbsolutePath());
        printInformationMessage(message);
        message = String.format("# %s %s", // NOI18N
NbBundle.getMessage(CargoCLIImpl.class, "MSG_CARGO_PATH"),
                cargo);
        printInformationMessage(message);
    }

    /**
     * Returns the underlying InputOutput object used for printing out messages.
     *
     * @return The InputOutput object.
     */
    public InputOutput getInputOutput() {
        return inputOutput;
    }

    /**
     * The name of this console.
     *
     * @return As shown in the Output tab.
     */
    public String getName() {
        return name;
    }

    /**
     * Prints a (possibly colored) information message.
     *
     * @param message The message to print
     * @throws IOException On error
     */
    public void printInformationMessage(String message) throws IOException {
        if (IOColorLines.isSupported(inputOutput)) {
            IOColorLines.println(inputOutput, message, IOColors.getColor(inputOutput, IOColors.OutputType.LOG_DEBUG));
        } else {
            inputOutput.getOut().println(message);
        }
    }

    /**
     * Prints a (possibly colored) error message.
     *
     * @param message The message to print
     * @throws IOException On error
     */
    public void printErrorMessage(String message) throws IOException {
        inputOutput.getErr().println(message);
    }

}
