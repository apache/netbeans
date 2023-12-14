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
package org.netbeans.modules.rust.cargo.impl.nodes.actions.dependencies;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.rust.cargo.api.CargoCLICommand;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.cargo.api.RustPackage;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.netbeans.modules.rust.cargo.impl.nodes.RustProjectDependenciesNode;
import org.netbeans.modules.rust.cargo.api.CargoCLI;

/**
 * Removes a given dependency from a rust project.
 *
 * @author antonio
 */
public class RustRemoveDependencyAction extends AbstractAction {

    private final CargoTOML cargotoml;
    private final RustPackage rustPackage;
    private final RustProjectDependenciesNode.DependencyType dependencyType;

    public RustRemoveDependencyAction(CargoTOML cargotoml, RustPackage rustPackage, RustProjectDependenciesNode.DependencyType dependencyType) {
        super(CargoCLICommand.CARGO_REMOVE.getDisplayName());
        putValue(Action.SHORT_DESCRIPTION, CargoCLICommand.CARGO_REMOVE.getDescription());
        this.cargotoml = cargotoml;
        this.dependencyType = dependencyType;
        this.rustPackage = rustPackage;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CargoCLI build = Lookup.getDefault().lookup(CargoCLI.class);
        if (build != null) {
            try {
                ArrayList<String> arguments = new ArrayList<>();
                switch (dependencyType) {
                    case BUILD_DEPENDENCY:
                        arguments.add("--build"); // NOI18N
                        break;
                    case DEV_DEPENDENCY:
                        arguments.add("--dev"); // NOI18N
                        break;
                }
                arguments.add(rustPackage.getName());
                build.cargo(cargotoml, new CargoCLICommand[]{CargoCLICommand.CARGO_REMOVE},
                        arguments.toArray(new String[0]));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
