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

package org.netbeans.modules.j2ee.common;

import java.io.File;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Libor Kotouc
 */
public class J2eeModuleProviderImpl extends J2eeModuleProvider {
    
    private Set<Datasource> moduleDatasources;
    private Set<Datasource> serverDatasources;
    private boolean creationAllowed;
    
    public J2eeModuleProviderImpl(Set<Datasource> moduleDatasources, Set<Datasource> serverDatasources) {
        this(moduleDatasources, serverDatasources, true);
    }

    public J2eeModuleProviderImpl(Set<Datasource> moduleDatasources, Set<Datasource> serverDatasources, boolean creationAllowed) {
        this.moduleDatasources = moduleDatasources;
        this.serverDatasources = serverDatasources;
        this.creationAllowed = creationAllowed;
    }

    // J2eeModuleProvider abstract methods implementation
    
    public void setServerInstanceID(String severInstanceID) {
    }

    @Override
    public String getServerID() {
        return null;
    }

    @Override
    public String getServerInstanceID() {
        return null;
    }

    public File getDeploymentConfigurationFile(String name) {
        return null;
    }

    public FileObject findDeploymentConfigurationFile(String name) {
        return null;
    }

    public ModuleChangeReporter getModuleChangeReporter() {
        return null;
    }

    public J2eeModule getJ2eeModule() {
        return null;
    }

    // J2eeModuleProvider DS API methods
    
    public Set<Datasource> getModuleDatasources() {
        return moduleDatasources;
    }

    public Set<Datasource> getServerDatasources() {
        return serverDatasources;
    }
    
    public boolean isDatasourceCreationSupported() {
        return creationAllowed;
    }    
    
}
