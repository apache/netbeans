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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.rust.cargo.api.CargoBuild;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author antonio
 */
@ServiceProvider(service = CargoBuild.class)
public class CargoBuildImpl implements CargoBuild {

    private static final Logger LOG = Logger.getLogger(CargoBuildImpl.class.getName());

    @Override
    public void build(Project project, CargoBuildMode[] modes) throws IOException {
        // TODO: Enhance this
        CargoTOML cargotoml = project.getLookup().lookup(CargoTOML.class);
        if (cargotoml == null) {
            throw new IOException(String.format("Don't know how to run  project (%s) that has not a Cargo.toml on its lookup", project.getProjectDirectory().getNameExt()));
        }
        for (CargoBuildMode mode : modes) {
            LOG.log(Level.INFO, String.format("Building project with cargo %s with command %s",
                    project.getProjectDirectory().getNameExt(),
                    mode.name()
            ));
        }
    }

}
