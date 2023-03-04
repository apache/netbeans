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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.impl.TargetServer;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;

/**
 * Context describing everything necessary for a module deployment.
 *
 * @since org.netbeans.modules.j2eeserver/4 1.70
 */
public final class DeploymentContext {

    static {
        TargetServer.DeploymentContextAccessor.setDefault(new TargetServer.DeploymentContextAccessor() {

            @Override
            public DeploymentContext createDeploymentContext(J2eeModule module, File moduleArchive,
                    File deploymentPlan, File[] requiredLibraries, AppChangeDescriptor changes) {
                return new DeploymentContext(module, moduleArchive, deploymentPlan, requiredLibraries, changes);
            }
        });
    }

    private final J2eeModule module;

    private final File moduleFile;

    private final File deploymentPlan;

    private final File[] requiredLibraries;

    private AppChangeDescriptor changes;

    private DeploymentContext(J2eeModule module, File moduleFile, File deploymentPlan, File[] requiredLibraries, AppChangeDescriptor changes) {
        assert requiredLibraries != null;
        this.module = module;
        this.moduleFile = moduleFile;
        this.deploymentPlan = deploymentPlan;
        this.requiredLibraries = requiredLibraries.clone();
        this.changes = changes;
    }

    public J2eeModule getModule() {
        return module;
    }

    public File getDeploymentPlan() {
        return deploymentPlan;
    }

    public File getModuleFile() {
        return moduleFile;
    }

    /**
     * Array of jar files which this EE module depends on and which has to be
     * deployed with the module.
     *
     * @return array of files; never null; array can be empty
     */
    public File[] getRequiredLibraries() {
        return requiredLibraries;
    }

    public AppChangeDescriptor getChanges() {
        return changes;
    }

}
