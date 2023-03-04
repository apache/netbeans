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


package org.netbeans.tests.j2eeserver.plugin;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInitializationException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;

/**
 *
 * @author  nn136682
 */
public class ManagerWrapperFactory extends OptionalDeploymentManagerFactory {

    protected boolean initialized;

    /** Creates a new instance of ManagerWrapperFactory */
    public ManagerWrapperFactory() {
    }

    public FindJSPServlet getFindJSPServlet(javax.enterprise.deploy.spi.DeploymentManager dm) {
        return null;
    }

    public IncrementalDeployment getIncrementalDeployment(javax.enterprise.deploy.spi.DeploymentManager dm) {
        return new TestIncrementalDeployment(dm);
    }
    
    public StartServer getStartServer(javax.enterprise.deploy.spi.DeploymentManager dm) {
        return new TestStartServer(dm);
    }

    @Override
    public ServerInstanceDescriptor getServerInstanceDescriptor(DeploymentManager dm) {
        return new TestInstanceDescriptor(dm);
    }

    @Override
    public synchronized void finishServerInitialization() throws ServerInitializationException {
        if (initialized) {
            throw new IllegalStateException("Initialization called twice");
        }
        super.finishServerInitialization();
        initialized = true;
    }

    public synchronized boolean isInitialized() {
        return initialized;
    }
 
}
