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

package org.netbeans.modules.gradle.javaee;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;

/**
 *
 * @author Laszlo Kishalmi
 */
public abstract class BaseEEModuleProvider extends J2eeModuleProvider {

    protected final Project project;
    protected J2eeModule javaeemodule;

    public BaseEEModuleProvider(Project project) {
        this.project = project;
    }

    protected abstract J2eeModuleImplementation2 getModuleImpl();

    @Override
    public synchronized J2eeModule getJ2eeModule() {
        if (javaeemodule == null) {
            javaeemodule = J2eeModuleFactory.createJ2eeModule(getModuleImpl());
        }
        return javaeemodule;
    }

    @Override
    public ModuleChangeReporter getModuleChangeReporter() {
        return DummyModuleChangeReporter.DUMMY_REPORTER;
    }

    @Override
    public void setServerInstanceID(String serverInstanceID) {
        JavaEEProjectSettings.setServerInstanceID(project, serverInstanceID);
    }

    @Override
    public String getServerInstanceID() {
        return JavaEEProjectSettings.getServerInstanceID(project);
    }

    @Override
    public String getServerID() {
        String instanceID = getServerInstanceID();
        if (instanceID != null) {
            try {
                return Deployment.getDefault().getServerInstance(instanceID).getServerID();
            } catch (InstanceRemovedException ex) {
            }
        }
        return null;
    }

}
