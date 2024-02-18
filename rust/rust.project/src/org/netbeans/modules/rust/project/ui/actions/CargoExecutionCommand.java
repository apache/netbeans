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
package org.netbeans.modules.rust.project.ui.actions;

import java.io.IOException;
import java.util.Objects;
import org.netbeans.modules.rust.cargo.api.CargoCLI;
import org.netbeans.modules.rust.cargo.api.CargoCLICommand;
import org.netbeans.modules.rust.project.RustProject;
import org.openide.util.Lookup;

public class CargoExecutionCommand extends Command {

    private String commandId;
    private CargoCLICommand[] commands;

    public CargoExecutionCommand(RustProject project, String commandId, CargoCLICommand[] commands) {
        super(project);
        Objects.nonNull(commandId);
        Objects.nonNull(commands);
        if (commands.length == 0) {
            throw new IllegalArgumentException("At least one cargo command has to be configured");
        }
        this.commandId = commandId;
        this.commands = commands;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }

    @Override
    public boolean asyncCallRequired() {
        return false;
    }

    @Override
    public boolean isActionEnabledInternal(Lookup context) {
        return Lookup.getDefault().lookup(CargoCLI.class) != null;
    }

    @Override
    public void invokeActionInternal(Lookup context) {
        CargoCLI build = Lookup.getDefault().lookup(CargoCLI.class);
        try {
            build.cargo(getProject().getCargoTOML(), commands);
        } catch (IOException ioe) {
            throw new IllegalArgumentException(ioe.getMessage(), ioe);
        }
    }

}
