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
package org.netbeans.modules.rust.project;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.rust.cargo.api.CargoCommand;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import org.netbeans.modules.rust.cargo.api.Cargo;

/**
 * An ActionProvider for Rust projects.
 */
public final class RustProjectActionProvider implements ActionProvider {

    private static final Logger LOG = Logger.getLogger(RustProjectActionProvider.class.getName());

    private static final String[] SUPPORTED_ACTIONS = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_RUN,
        COMMAND_DEBUG,};

    private final RustProject project;

    public RustProjectActionProvider(RustProject project) {
        this.project = project;
    }

    @Override
    public String[] getSupportedActions() {
        return SUPPORTED_ACTIONS;
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        // TODO: Enhance this
        Cargo build = Lookup.getDefault().lookup(Cargo.class);
        if (build == null) {
            LOG.log(Level.INFO, String.format("No CargoBuild in this application."));
        } else {
            CargoCommand[] commands = {};
            CargoTOML cargotoml = project.getCargoTOML();
            switch (command) {
                case COMMAND_BUILD:
                    commands = new CargoCommand[]{CargoCommand.CARGO_BUILD};
                    break;
                case COMMAND_CLEAN:
                    commands = new CargoCommand[]{CargoCommand.CARGO_CLEAN};
                    break;
                case COMMAND_REBUILD:
                    commands = new CargoCommand[]{CargoCommand.CARGO_CLEAN, CargoCommand.CARGO_BUILD};
                    break;
                case COMMAND_RUN:
                    commands = new CargoCommand[]{CargoCommand.CARGO_RUN};
                default:
                    LOG.log(Level.WARNING, String.format("Invoked action %s but cannot find a CargoBuild mode for it", command));
            }
            try {
                build.cargo(project.getCargoTOML(), commands);
            } catch (IOException ioe) {
                throw new IllegalArgumentException(ioe.getMessage(), ioe);
            }
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return true;
    }

}
